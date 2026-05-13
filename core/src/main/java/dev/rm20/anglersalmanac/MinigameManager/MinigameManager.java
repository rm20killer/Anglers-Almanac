package dev.rm20.anglersalmanac.MinigameManager;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Components.MinigameComponent_TensionBar;
import dev.rm20.anglersalmanac.Interactions.Rod.UseRodInteraction;
import dev.rm20.anglersalmanac.Metadata.*;
import dev.rm20.anglersalmanac.Utils.*;
import org.jspecify.annotations.NonNull;

public class MinigameManager {
    public static void StartGame(Ref<EntityStore> bobberRef, Player player, CommandBuffer<EntityStore> commandBuffer, int depth) {

        InventoryComponent.Hotbar hotbarComp = player.getReference().getStore().getComponent(player.getReference(), InventoryComponent.Hotbar.getComponentType());
        if (hotbarComp == null) {
            return;
        }

        ItemStack fishingRod = hotbarComp.getActiveItem();
        if (fishingRod == null) {
            return;
        }


        // Select which minigame to use from the config and set it up.
        switch (AnglersAlmanac.MOD_CONFIG.get().getMinigameToUse()) {
            case "TensionBar":
                FishingRodData meta = fishingRod.getFromMetadataOrNull(FishingRodData.KEYED_CODEC);
                if (meta == null) {
                    UseRodInteraction.cancelFishing(commandBuffer, player, fishingRod);
                    break;
                }
                MinigameComponent_TensionBar minigame = MinigameComponent_TensionBar.spawnMinigame(commandBuffer, player.getReference(), bobberRef, fishingRod.getItemId());
                UseRodInteraction.updateMetadata(hotbarComp, hotbarComp.getActiveSlot(), hotbarComp.getActiveItem(), meta.getBoundBobber(), minigame.selfUUID, 1);
                break;
            case "NoMinigame":
                CatchUtils.DropLoot(CatchUtils.FirstRoll(bobberRef, player, commandBuffer, depth), player, commandBuffer, bobberRef, -1);
                UseRodInteraction.cancelFishing(commandBuffer, player, fishingRod);
                break;
            default: // No Minigame, just reel fish.
                CatchUtils.DropLoot(CatchUtils.FirstRoll(bobberRef, player, commandBuffer, depth), player, commandBuffer, bobberRef, -1);
                UseRodInteraction.cancelFishing(commandBuffer, player, fishingRod);
                break;
        }


    }

    public static void CancelGame(CommandBuffer<EntityStore> commandBuffer, Ref<EntityStore> minigameRef) {

        // Select which minigame to use from the config and cancel it.
        switch (AnglersAlmanac.MOD_CONFIG.get().getMinigameToUse()) {
            case "TensionBar":
                //AnglersAlmanac.LOGGER.atInfo().log("Canceling TensionBar Minigame");
                MinigameComponent_TensionBar minigame = commandBuffer.getComponent(minigameRef, MinigameComponent_TensionBar.COMPONENT_TYPE);
                if (minigame == null) {
                    AnglersAlmanac.LOGGER.atWarning().log("Missing ref for minigame");
                    return;
                } else {
                    minigame.despawnSelf(commandBuffer.getExternalData().getWorld());
                }
                break;
            case "NoMinigame":
                break;
            default:
                break;
        }

    }


    public static boolean DoMinigameInteraction(CommandBuffer<EntityStore> commandBuffer, Ref<EntityStore> minigameRef, @NonNull InteractionType interactionType, @NonNull InteractionContext context, @NonNull CooldownHandler cooldownHandler) {
        switch (AnglersAlmanac.MOD_CONFIG.get().getMinigameToUse()) {
            case "TensionBar":
                MinigameComponent_TensionBar minigame = commandBuffer.getComponent(minigameRef, MinigameComponent_TensionBar.COMPONENT_TYPE);
                if (minigame == null) {
                    CancelGame(commandBuffer, minigameRef);
                    AnglersAlmanac.LOGGER.atWarning().log("Missing ref for minigame");
                    return false;
                } else {
                    minigame.DoInteraction(interactionType, context, cooldownHandler);
                }
                break;
            case "NoMinigame":
                break;
            default:
                break;
        }
        return true;
    }


}
