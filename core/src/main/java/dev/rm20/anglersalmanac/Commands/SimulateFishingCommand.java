package dev.rm20.anglersalmanac.Commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldMapTracker;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Metadata.FishingContext;
import dev.rm20.anglersalmanac.Metadata.ZoneInfo;
import dev.rm20.anglersalmanac.Models.FishLootManager;
import dev.rm20.anglersalmanac.Registration.CommandInfo;
import dev.rm20.anglersalmanac.Utils.EnvironmentParser;
import dev.rm20.anglersalmanac.Utils.TimeUtils;
import dev.rm20.anglersalmanac.Utils.Validator.TimePeriod;

import javax.annotation.Nonnull;

@CommandInfo(
        name = "simfish",
        description = "Simulates 100 fishing rolls at current location with depth 3"
)
public class SimulateFishingCommand extends AbstractPlayerCommand {

    public SimulateFishingCommand(String name, String description) {
        super(name, description);
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        if(!playerRef.hasPermission("AnglersAlmanac.admin"))
        {
            commandContext.sendMessage(Message.translation("anglersalmanac.cmd.error.noPerms"));
            return;
        }
        Player player = playerRef.getComponent(Player.getComponentType());
        if(player == null)
        {
            commandContext.sendMessage(Message.translation("anglersalmanac.cmd.error.notPlayer"));
            return;
        }

        var transform = store.getComponent(ref, TransformComponent.getComponentType());
        double y = (transform != null) ? transform.getPosition().y : 0;

        WorldTimeResource timeResource = store.getResource(WorldTimeResource.getResourceType());
        TimePeriod timeKeyword = TimeUtils.getTimePeriod(timeResource.getGameTime().toString());

        WorldMapTracker worldMapTracker = player.getWorldMapTracker();
        WorldMapTracker.ZoneDiscoveryInfo currentZone = worldMapTracker.getCurrentZone();
        ZoneInfo info = EnvironmentParser.parse(currentZone.regionName());

        FishingContext locationInfo = new FishingContext(
                timeKeyword,
                timeResource.getMoonPhase(),
                info.zone(),
                info.tier(),
                currentZone.zoneName(),
                worldMapTracker.getCurrentBiomeName(),
                y,
                "clear",
                20,
                null,
                1
        );

        java.util.Map<String, Integer> results = new java.util.HashMap<>();
        int totalRolls = 1000;
        int failures = 0;

        for (int i = 0; i < totalRolls; i++) {
            FishLootManager lootEntry = FishLootManager.getRandomWeightedLoot(locationInfo, null);
            if (lootEntry != null && lootEntry.getItemID() != null) {
                results.merge(lootEntry.getItemID(), 1, Integer::sum);
            } else {
                failures++;
            }
        }

        logSimulation(locationInfo, results, totalRolls, failures);
        commandContext.sendMessage(Message.translation("anglersalmanac.cmd.sim.done"));
    }


    private void logSimulation(FishingContext ctx, java.util.Map<String, Integer> results, int total, int fails) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n === FISHING SIMULATION (n=").append(total).append(") ===");
        sb.append("\n Location: ").append(ctx.biome()).append(" | Tier: ").append(ctx.tier());
        sb.append("\n Depth: 20 | Time: ").append(ctx.time().getKeyword());
        sb.append("\n-------------------------------------------");
        results.forEach((id, count) -> {
            sb.append(String.format("\n- %-25s : %d%% (%d)", id, (count * 100 / total), count));
        });
        if (fails > 0) {
            sb.append(String.format("\n- %-25s : %d%%", "EMPTY_ROLL (Fail)", (fails * 100 / total)));
        }
        sb.append("\n===========================================");

        AnglersAlmanac.LOGGER.atInfo().log(sb.toString());
    }
}