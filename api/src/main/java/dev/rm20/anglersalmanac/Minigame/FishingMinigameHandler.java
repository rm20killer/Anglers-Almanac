package dev.rm20.anglersalmanac.Minigame;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.NonNull;

/**
 * The interface Fishing minigame handler.
 */
public interface FishingMinigameHandler {

    /**
     * Called when a player hooks a fish and the minigame is initialized.
     * @param bobberRef the bobber ref
     * @param player the player
     * @param commandBuffer the command buffer
     * @param depth the depth
     * @param fishingRod the fishing rod
     */
    void startGame(Ref<EntityStore> bobberRef, Player player, CommandBuffer<EntityStore> commandBuffer, int depth, ItemStack fishingRod);

    /**
     * Called when a minigame is forcibly ended or canceled.
     * @param commandBuffer the command buffer
     * @param minigameRef the minigame ref
     */
    void cancelGame(CommandBuffer<EntityStore> commandBuffer, Ref<EntityStore> minigameRef);

    /**
     * Called when the player performs an interaction input during the minigame.
     * @param commandBuffer the command buffer
     * @param minigameRef the minigame ref
     * @param interactionType the interaction type
     * @param context the context
     * @param cooldownHandler the cooldown handler
     * @return  true if interaction was handled successfully.
     */
    boolean handleInteraction(CommandBuffer<EntityStore> commandBuffer, Ref<EntityStore> minigameRef, @NonNull InteractionType interactionType, @NonNull InteractionContext context, @NonNull CooldownHandler cooldownHandler);
}