package dev.rm20.anglersalmanac.Interactions.Rod;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Components.BobberComponent;
import dev.rm20.anglersalmanac.Metadata.FishingRodData;
import dev.rm20.anglersalmanac.MinigameManager.MinigameManager;
import dev.rm20.anglersalmanac.Utils.BaitUtils;

import javax.annotation.Nonnull;
import java.util.UUID;

public class ReelBobberInteraction extends SimpleInstantInteraction {
    public static final BuilderCodec<ReelBobberInteraction> CODEC = BuilderCodec.builder(
            ReelBobberInteraction.class, ReelBobberInteraction::new, SimpleInstantInteraction.CODEC
    ).build();

    @Override
    protected void firstRun(@Nonnull InteractionType interactionType, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
        Ref<EntityStore> playerRef = context.getOwningEntity();
        ItemStack heldItem = context.getHeldItem();
        if (commandBuffer == null || playerRef == null || heldItem == null) return;

        Player player = commandBuffer.getComponent(playerRef, Player.getComponentType());
        FishingRodData meta = heldItem.getFromMetadataOrNull(FishingRodData.KEY, FishingRodData.CODEC);

        if(!UseRodInteraction.shouldCast(heldItem, commandBuffer)) {
            reelIn(commandBuffer, player, heldItem, meta.getBoundBobber(), meta, playerRef);
            context.getState().state = InteractionState.Finished;
        }
        else{
            context.getState().state = InteractionState.Failed;
        }
    }
    private void reelIn(CommandBuffer<EntityStore> commandBuffer, Player player, ItemStack heldItem, UUID bobberId, FishingRodData fishingRodData, Ref<EntityStore> playerRef) {
        //AnglersAlmanac.LOGGER.atInfo().log("Reeling in");
        //play sound here

        World world = commandBuffer.getExternalData().getWorld();
        Ref<EntityStore> bobberRef = world.getEntityStore().getRefFromUUID(bobberId);
        if (bobberRef != null) {
            BobberComponent bobberComp = bobberRef.getStore().getComponent(bobberRef, BobberComponent.getComponentType());
            if (bobberComp != null) {
                if (bobberComp.isCanCatch()) {
                    //HytaleLogger.getLogger().atInfo().log("Fished a fish");
                    int depth = bobberComp.getWaterDepth();

                    MinigameManager.StartGame(bobberRef, player, commandBuffer, depth);
                    //launchFishAtPlayer(bobberRef,player,commandBuffer,depth);
                } else {
                    // Didn't hook fish, just stop fishing.
                    BaitUtils.giveBait(player,bobberComp.getBaitName(),commandBuffer);
                    UseRodInteraction.cancelFishing(commandBuffer, player, heldItem);
                }
            }
            else
            {
                AnglersAlmanac.LOGGER.atWarning().log("Failed to reel in" + bobberId);
                AnglersAlmanac.LOGGER.atInfo().log("Fixing busted metadata");
                UseRodInteraction.cancelFishing(commandBuffer, player, heldItem);
            }

        } else {
            AnglersAlmanac.LOGGER.atWarning().log("Failed to reel in" + bobberId);
            AnglersAlmanac.LOGGER.atInfo().log("Fixing busted metadata");
            UseRodInteraction.cancelFishing(commandBuffer, player, heldItem);
        }


    }
}
