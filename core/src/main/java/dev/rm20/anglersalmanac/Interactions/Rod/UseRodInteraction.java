package dev.rm20.anglersalmanac.Interactions.Rod;


import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.modules.physics.component.PhysicsValues;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.MinigameManager.MinigameManager;
import dev.rm20.anglersalmanac.Components.BobberComponent;
import dev.rm20.anglersalmanac.Components.PhysicsComponent;
import dev.rm20.anglersalmanac.Metadata.FishingRodData;
import dev.rm20.anglersalmanac.Utils.BaitUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;


public class UseRodInteraction extends SimpleInstantInteraction {

    public static final BuilderCodec<UseRodInteraction> CODEC = BuilderCodec.builder(
            UseRodInteraction.class, UseRodInteraction::new, SimpleInstantInteraction.CODEC
    ).build();

    @Override
    protected void firstRun(@Nonnull InteractionType interactionType, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
        Ref<EntityStore> playerRef = context.getOwningEntity();
        ItemStack heldItem = context.getHeldItem();
        if (commandBuffer == null || playerRef == null || heldItem == null) return;

        Player player = commandBuffer.getComponent(playerRef, Player.getComponentType());
        if (player == null) return;
        FishingRodData meta = heldItem.getFromMetadataOrNull(FishingRodData.KEY, FishingRodData.CODEC);


        if(!checkSaneMetadata(heldItem, commandBuffer)){
            AnglersAlmanac.LOGGER.atInfo().log("Fixing busted metadata within UseRodInteraction for: "+player.getDisplayName());
            cancelFishing(commandBuffer, player, heldItem);
            context.getState().state = InteractionState.Failed;
        }

        if(shouldCast(heldItem, commandBuffer)){
            // CastBobberInteraction set as Next in Interaction asset for the rods.
            context.getState().state = InteractionState.Finished;
        }else{
            // ReelBobberInteraction must be set as Failed interaction in rods Interaction asset.
            context.getState().state = InteractionState.Failed;
        }


        //AnglersAlmanac.LOGGER.atInfo().log("Doing cast / reel interaction!");



    }

    public static boolean shouldCast(ItemStack heldItem, @Nonnull ComponentAccessor<EntityStore> accessor) {
        FishingRodData meta = heldItem.getFromMetadataOrNull(FishingRodData.KEY, FishingRodData.CODEC);

        if(!checkSaneMetadata(meta, accessor)){
            return false;
        }
        // Choose reel or cast depending on rod state.
        if (meta != null && meta.getBoundBobber() != null) {
            if (meta.getMode() == 0) {
                return false;
            }
        }

        return true;


    }


    public static boolean checkSaneMetadata(ItemStack heldItem, @Nonnull ComponentAccessor<EntityStore> accessor) {
        // Catch for if the rod mode got messed up. (e.g. Disconnecting from server while minigame active).
        // Checks if the metadata is in a state which should not exist.
        FishingRodData meta = heldItem.getFromMetadataOrNull(FishingRodData.KEY, FishingRodData.CODEC);
        if (meta != null) {
            boolean shouldReset = false;
            if (meta.getBoundBobber() != null) {
                if (accessor.getExternalData().getRefFromUUID(meta.getBoundBobber()) == null) {
                    shouldReset = true;
                }
            }
            if (!shouldReset && meta.getMode() != 0 && meta.getBoundMinigame() != null) {
                if (accessor.getExternalData().getRefFromUUID(meta.getBoundMinigame()) == null) {
                    shouldReset = true;
                }
            }

            if (shouldReset) {
                //meta = heldItem.getFromMetadataOrNull(FishingRodData.KEY, FishingRodData.CODEC);
                return false; // Something has broken.
            }
            return true; // All seems okay.
        }
        return false; // No metadata found.
    }

    public static boolean checkSaneMetadata(FishingRodData meta, @Nonnull ComponentAccessor<EntityStore> accessor) {
        // Catch for if the rod mode got messed up. (e.g. Disconnecting from server while minigame active).
        // Checks if the metadata is in a state which should not exist.
        if (meta != null) {
            boolean shouldReset = false;
            if (meta.getBoundBobber() != null) {
                if (accessor.getExternalData().getRefFromUUID(meta.getBoundBobber()) == null) {
                    shouldReset = true;
                }
            }
            if (!shouldReset && meta.getMode() != 0 && meta.getBoundMinigame() != null) {
                if (accessor.getExternalData().getRefFromUUID(meta.getBoundMinigame()) == null) {
                    shouldReset = true;
                }
            }

            if (shouldReset) {
                return false; // Something has broken.
            }
            return true; // All seems okay.
        }
        return false; // No metadata found.
    }



    public static void cancelFishing(CommandBuffer<EntityStore> commandBuffer, Player player, ItemStack heldItem) {
        //AnglersAlmanac.LOGGER.atInfo().log("Cancelling fishing.");
        if(heldItem==null)
        {
            return;
        }

        World world = commandBuffer.getExternalData().getWorld();
        FishingRodData meta = heldItem.getFromMetadataOrNull(FishingRodData.KEYED_CODEC);
        if(meta==null)
        {
            AnglersAlmanac.LOGGER.atWarning().log("Failed to clear" + heldItem);
            return;
        }
        // Attempt to remove bobber and minigame if not already.
        RemoveFishingEntities(commandBuffer, meta, world);
        Ref<EntityStore> playerRef = player.getReference();
        if(playerRef != null) {
            InventoryComponent.Hotbar inv = playerRef.getStore().getComponent(player.getReference(), InventoryComponent.Hotbar.getComponentType());
            if (inv != null) {
                updateMetadata(inv, inv.getActiveSlot(), heldItem, null, null, 0);
            }
        }
        else
        {
            AnglersAlmanac.LOGGER.atWarning().log("Failed to clear" + heldItem.getItem() +" on "+ player.getDisplayName());
        }


    }

    public static void cancelFishing(CommandBuffer<EntityStore> commandBuffer, Player player, ItemStack heldItem, byte slot) {
        //AnglersAlmanac.LOGGER.atInfo().log("Cancelling fishing.");
        if(heldItem==null)
        {
            return;
        }
        World world = commandBuffer.getExternalData().getWorld();
        FishingRodData meta = heldItem.getFromMetadataOrNull(FishingRodData.KEYED_CODEC);
        // Attempt to remove bobber and minigame if not already.
        RemoveFishingEntities(commandBuffer, meta, world);
        Ref<EntityStore> playerRef = player.getReference();
        if(playerRef != null) {
            InventoryComponent.Hotbar inv = playerRef.getStore().getComponent(player.getReference(), InventoryComponent.Hotbar.getComponentType());
            updateMetadata(inv, slot, heldItem, null, null, 0);
        }
        else
        {
            AnglersAlmanac.LOGGER.atWarning().log("Failed to clear" + heldItem.getItem() +" on "+ player.getDisplayName());
        }

    }

    private static void RemoveFishingEntities(CommandBuffer<EntityStore> commandBuffer, FishingRodData meta, World world) {
        if(meta != null) {
            if(meta.getBoundBobber() != null) {
                UUID bobberUuid = meta.getBoundBobber();
                meta.setBoundBobber(null);

                final FishingRodData finalMeta = meta;
                final Ref<EntityStore> bobberRef = world.getEntityStore().getRefFromUUID(bobberUuid);
                if (bobberRef == null) {
                    meta.setBoundBobber(null);
                }
                else
                {
                    try {
                        commandBuffer.getExternalData().getWorld().execute(() -> {
                            if (!bobberRef.isValid()) {
                                finalMeta.setBoundBobber(null);
                                return;
                            }
                            try {
                                world.getEntityStore().getStore().removeEntity(bobberRef, RemoveReason.REMOVE);
                                finalMeta.setBoundBobber(null);
                            } catch (Exception e) {
                                AnglersAlmanac.LOGGER.atFine().withCause(e).log(
                                        "Bobber remove race lost — already removed by another path");
                                finalMeta.setBoundBobber(null);
                            }
                        });
                    } catch (Exception e) {
                        AnglersAlmanac.LOGGER.atWarning().withCause(e).log("Failed to enqueue bobber remove");
                    }
                }
            }


            if(meta.getBoundMinigame() != null) {
                UUID minigameUUID = meta.getBoundMinigame();
                meta.setBoundMinigame(null);
                Ref<EntityStore> minigameRef = world.getEntityStore().getRefFromUUID(minigameUUID);
                if (minigameRef != null && minigameRef.isValid()) {
                    MinigameManager.CancelGame(commandBuffer, minigameRef);
                }
            }
        }
    }


    public static void updateMetadata(InventoryComponent.Hotbar inventory, byte slot, ItemStack stack, @Nullable UUID bobberId, @Nullable UUID minigameId, int rodMode) {
        ItemStack newRod;

        FishingRodData fishingMetaData = stack.getFromMetadataOrNull(FishingRodData.KEY, FishingRodData.CODEC);
        if (fishingMetaData == null) {
            fishingMetaData = new FishingRodData();
        }
        fishingMetaData.setBoundBobber(bobberId);
        fishingMetaData.setBoundMinigame(minigameId);
        fishingMetaData.setMode(rodMode);
        newRod = stack.withMetadata(FishingRodData.KEYED_CODEC, fishingMetaData);

        inventory.getInventory().replaceItemStackInSlot(slot, stack, newRod);
        //AnglersAlmanac.LOGGER.atInfo().log("Updated metadata: %s, %s, %s", fishingMetaData.getBoundBobber(), fishingMetaData.getBoundMinigame(), fishingMetaData.getMode());
    }




}