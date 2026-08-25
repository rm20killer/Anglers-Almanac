package dev.rm20.anglersalmanac.MinigameManager.Handlers;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.Interactions.Rod.UseRodInteraction;
import dev.rm20.anglersalmanac.Minigame.FishingMinigameHandler;
import dev.rm20.anglersalmanac.Utils.CatchUtils;
import lombok.NonNull;

public class NoMinigameHandler implements FishingMinigameHandler {

    @Override
    public void startGame(Ref<EntityStore> bobberRef, Player player, CommandBuffer<EntityStore> commandBuffer, int depth, ItemStack fishingRod) {
        CatchUtils.DropLoot(CatchUtils.FirstRoll(bobberRef, player, commandBuffer, depth), player, commandBuffer, bobberRef, -1);
        UseRodInteraction.cancelFishing(commandBuffer, player, fishingRod);
    }

    @Override
    public void cancelGame(CommandBuffer<EntityStore> commandBuffer, Ref<EntityStore> minigameRef) {}

    @Override
    public boolean handleInteraction(CommandBuffer<EntityStore> commandBuffer, Ref<EntityStore> minigameRef, @NonNull InteractionType interactionType, @NonNull InteractionContext context, @NonNull CooldownHandler cooldownHandler) {
        return true;
    }
}
