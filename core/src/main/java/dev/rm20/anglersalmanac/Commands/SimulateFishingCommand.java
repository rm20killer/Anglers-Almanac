package dev.rm20.anglersalmanac.Commands;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Metadata.FishingContext;
import dev.rm20.anglersalmanac.Metadata.FishingModifier;
import dev.rm20.anglersalmanac.Models.FishLootManager;
import dev.rm20.anglersalmanac.Models.FishSize;
import dev.rm20.anglersalmanac.Models.MinigameRodStats;
import dev.rm20.anglersalmanac.Registration.CommandInfo;
import dev.rm20.anglersalmanac.Utils.FishSizeCalculator;
import dev.rm20.anglersalmanac.Utils.TimeUtils;
import dev.rm20.anglersalmanac.Utils.TriggerVolumeUtils;
import dev.rm20.anglersalmanac.Utils.Validator.TimePeriod;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@CommandInfo(
        name = "sim",
        description = "Simulates fishing rolls at current location and displays roll statistics",
        aliases = {"simfish", "simulate"},
        parent = "almanac",
        permission = "AnglersAlmanac.admin"
)
public class SimulateFishingCommand extends AbstractPlayerCommand {

    public SimulateFishingCommand(String name, String description) {
        super(name, description);
        requirePermission("AnglersAlmanac.admin");
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        if (!playerRef.hasPermission("AnglersAlmanac.admin")) {
            commandContext.sendMessage(Message.translation("anglersalmanac.cmd.error.noPerms"));
            return;
        }
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) {
            commandContext.sendMessage(Message.translation("anglersalmanac.cmd.error.notPlayer"));
            return;
        }

        var transform = store.getComponent(ref, TransformComponent.getComponentType());
        double y = (transform != null) ? transform.getPosition().y : 0;

        WorldTimeResource timeResource = store.getResource(WorldTimeResource.getResourceType());
        TimePeriod timeKeyword = TimeUtils.getTimePeriod(timeResource.getGameTime().toString());

        TriggerVolumeManager manager = store.getResource(TriggerVolumesPlugin.get().getManagerResourceType());
        String triggerKey = TriggerVolumeUtils.getFishingZone(manager, transform.getPosition());

        String region = "Unknown";
        int tier = 0;
        String biome = "Unknown";
        String zone = "Unknown";

        FishingContext locationInfo = new FishingContext(
                timeKeyword,
                timeResource.getMoonPhase(),
                zone,
                tier,
                region,
                biome,
                y,
                "clear",
                20,
                null,
                1,
                triggerKey
        );

        InventoryComponent.Hotbar hotbar = store.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
        ItemStack heldItem = hotbar != null ? hotbar.getActiveItem() : null;
        FishingModifier.Modifiers rodMods = null;
        if (heldItem != null) {
            rodMods = MinigameRodStats.getModifiersFromRodId(heldItem.getItemId());
        }
        FishingModifier.Modifiers modifiers = FishingModifier.mergeModifiers(rodMods);

        Map<String, Integer> results = new HashMap<>();
        Map<FishSize, Integer> sizeCounts = new EnumMap<>(FishSize.class);
        for (FishSize s : FishSize.values()) {
            sizeCounts.put(s, 0);
        }

        int totalRolls = 50;
        int failures = 0;
        float totalFishValue = 0.0f;

        for (int i = 0; i < totalRolls; i++) {
            FishLootManager lootEntry = FishLootManager.getRandomWeightedLoot(locationInfo, modifiers);
            if (lootEntry != null && lootEntry.getItemID() != null) {
                results.merge(lootEntry.getItemID(), 1, Integer::sum);

                if (lootEntry.getItemID().startsWith("Fish_")) {
                    FishSize size = FishSizeCalculator.rollSize(locationInfo, modifiers, lootEntry);
                    sizeCounts.merge(size, 1, Integer::sum);

                    float value = lootEntry.getBaseSellPrice() * size.getValueMultiplier();
                    totalFishValue += value;
                } else {
                    totalFishValue += lootEntry.getBaseSellPrice();
                }
            } else {
                failures++;
            }
        }

        float catchSpeed = modifiers.catchSpeed > 0.001f ? modifiers.catchSpeed : 1.0f;
        float settleTime = (catchSpeed > 1.0f) ? Math.max(1.0f, 4.0f / catchSpeed) : 4.0f;
        float avgWaitTime = Math.max(1.0f, 15.0f / catchSpeed);
        float avgWaitPerBite = settleTime + avgWaitTime;
        float totalPlayerWaitSeconds = totalRolls * avgWaitPerBite;
        int successes = totalRolls - failures;

        logSimulation(locationInfo, modifiers, results, sizeCounts, totalRolls, failures, totalFishValue, avgWaitPerBite, totalPlayerWaitSeconds);

        commandContext.sendMessage(Message.translation("anglersalmanac.cmd.sim.done"));
        commandContext.sendMessage(Message.raw(String.format(
                "Total Value: %.2f coins | Avg: %.2f/catch",
                totalFishValue,
                successes > 0 ? (totalFishValue / successes) : 0.0f
        )).color(Color.YELLOW));
        commandContext.sendMessage(Message.raw(String.format(
                "Est. Wait Time: %s (Avg %.1fs/bite, Speed: %.2fx)",
                formatDuration(totalPlayerWaitSeconds),
                avgWaitPerBite,
                catchSpeed
        )).color(Color.CYAN));
    }

    private void logSimulation(FishingContext ctx, FishingModifier.Modifiers modifiers,
                               Map<String, Integer> results,
                               Map<FishSize, Integer> sizeCounts,
                               int total, int fails,
                               float totalValue, float avgWaitPerBite, float totalWaitSeconds) {
        int successes = total - fails;
        float catchSpeed = (modifiers != null && modifiers.catchSpeed > 0.001f) ? modifiers.catchSpeed : 1.0f;
        float sizeSkew = (modifiers != null) ? modifiers.sizeSkew : 0.0f;

        StringBuilder sb = new StringBuilder();
        sb.append("\n === FISHING SIMULATION (n=").append(total).append(") ===");
        sb.append("\n Location: ").append(ctx.biome()).append(" | Tier: ").append(ctx.tier());
        sb.append("\n Depth: 20 | Time: ").append(ctx.time().getKeyword());
        sb.append("\n Trigger Volume: ").append(ctx.triggerKey() != null ? ctx.triggerKey() : "None");
        sb.append(String.format("\n Active Modifiers: Catch Speed: %.2fx | Size Skew: %+.2f", catchSpeed, sizeSkew));
        sb.append("\n-------------------------------------------");
        sb.append("\n Catches:");
        results.forEach((id, count) -> {
            sb.append(String.format("\n- %-25s : %d%% (%d)", id, (count * 100 / total), count));
        });
        if (fails > 0) {
            sb.append(String.format("\n- %-25s : %d%% (%d)", "EMPTY_ROLL (Fail)", (fails * 100 / total), fails));
        }
        sb.append("\n-------------------------------------------");
        sb.append("\n Fish Size Distribution:");
        for (FishSize size : FishSize.values()) {
            int count = sizeCounts.getOrDefault(size, 0);
            int pct = (count * 100) / total;
            sb.append(String.format("\n- %-4s (x%.2f) : %2d%% (%d)", size.name(), size.getValueMultiplier(), pct, count));
        }
        sb.append("\n-------------------------------------------");
        sb.append("\n Economy & Value:");
        sb.append(String.format("\n- Total Fish Value       : %.2f coins", totalValue));
        sb.append(String.format("\n- Avg Value Per Catch    : %.2f coins", successes > 0 ? (totalValue / successes) : 0.0f));
        sb.append(String.format("\n- Avg Value Per Roll     : %.2f coins", (totalValue / total)));
        sb.append("\n-------------------------------------------");
        sb.append("\n Estimated Player Wait Time:");
        sb.append(String.format("\n- Avg Wait Per Bite      : %.1fs (Speed: %.2fx)", avgWaitPerBite, catchSpeed));
        sb.append(String.format("\n- Avg Wait Per Fish      : %.1fs", successes > 0 ? (totalWaitSeconds / successes) : totalWaitSeconds));
        sb.append(String.format("\n- Total Estimated Wait   : %s (%.0fs)", formatDuration(totalWaitSeconds), totalWaitSeconds));
        sb.append("\n===========================================");

        AnglersAlmanac.LOGGER.atInfo().log(sb.toString());
    }

    private String formatDuration(float totalSeconds) {
        long sec = (long) totalSeconds;
        long hours = sec / 3600;
        long minutes = (sec % 3600) / 60;
        long seconds = sec % 60;
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
}
