package dev.rm20.anglersalmanac.MinigameManager.Handlers;

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
import dev.rm20.anglersalmanac.Metadata.FishingRodData;
import dev.rm20.anglersalmanac.Minigame.FishingMinigameHandler;
import org.jspecify.annotations.NonNull;

public class TensionBarMinigameHandler implements FishingMinigameHandler {

    @Override
    public void startGame(Ref<EntityStore> bobberRef, Player player, CommandBuffer<EntityStore> commandBuffer, int depth, ItemStack fishingRod) {
        InventoryComponent.Hotbar hotbarComp = player.getReference().getStore().getComponent(player.getReference(), InventoryComponent.Hotbar.getComponentType());
        if (hotbarComp == null) return;

        FishingRodData meta = fishingRod.getFromMetadataOrNull(FishingRodData.KEYED_CODEC);
        if (meta == null) {
            UseRodInteraction.cancelFishing(commandBuffer, player, fishingRod);
            return;
        }

        MinigameComponent_TensionBar minigame = MinigameComponent_TensionBar.spawnMinigame(
                commandBuffer, player.getReference(), bobberRef, fishingRod.getItemId()
        );
        UseRodInteraction.updateMetadata(hotbarComp, hotbarComp.getActiveSlot(), hotbarComp.getActiveItem(), meta.getBoundBobber(), minigame.selfUUID, 1);
    }

    @Override
    public void cancelGame(CommandBuffer<EntityStore> commandBuffer, Ref<EntityStore> minigameRef) {
        MinigameComponent_TensionBar minigame = commandBuffer.getComponent(minigameRef, MinigameComponent_TensionBar.COMPONENT_TYPE);
        if (minigame == null) {
            AnglersAlmanac.LOGGER.atWarning().log("Missing ref for TensionBar minigame on cancel");
            return;
        }
        minigame.despawnSelf(commandBuffer.getExternalData().getWorld());
    }

    @Override
    public boolean handleInteraction(CommandBuffer<EntityStore> commandBuffer, Ref<EntityStore> minigameRef, @NonNull InteractionType interactionType, @NonNull InteractionContext context, @NonNull CooldownHandler cooldownHandler) {
        MinigameComponent_TensionBar minigame = commandBuffer.getComponent(minigameRef, MinigameComponent_TensionBar.COMPONENT_TYPE);
        if (minigame == null) {
            cancelGame(commandBuffer, minigameRef);
            AnglersAlmanac.LOGGER.atWarning().log("Missing ref for TensionBar minigame on interaction");
            return false;
        }
        minigame.DoInteraction(interactionType, context, cooldownHandler);
        return true;
    }
}