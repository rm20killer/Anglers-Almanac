package dev.rm20.anglersalmanac.Interactions;


import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
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


public class LaunchBobberInteraction extends SimpleInstantInteraction {

    public static final BuilderCodec<LaunchBobberInteraction> CODEC = BuilderCodec.builder(
            LaunchBobberInteraction.class, LaunchBobberInteraction::new, SimpleInstantInteraction.CODEC
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


        //AnglersAlmanac.LOGGER.atInfo().log("Testing if can cast / reel interaction");

        // Catch for if the rod mode got messed up. (e.g. Disconnecting from server while minigame active).
        // Checks if the metadata is in a state which should not exist.
        if (meta != null) {
            boolean shouldReset = false;
            if (meta.getBoundBobber() != null) {
                if (commandBuffer.getExternalData().getRefFromUUID(meta.getBoundBobber()) == null) {
                    shouldReset = true;
                }
            }
            if (!shouldReset && meta.getMode() != 0 && meta.getBoundMinigame() != null) {
                if (commandBuffer.getExternalData().getRefFromUUID(meta.getBoundMinigame()) == null) {
                    shouldReset = true;
                }
            }

            if (shouldReset) {
                AnglersAlmanac.LOGGER.atInfo().log("Fixing busted metadata for: "+player.getDisplayName());
                cancelFishing(commandBuffer, player, heldItem);
                meta = heldItem.getFromMetadataOrNull(FishingRodData.KEY, FishingRodData.CODEC);
            }
        }

        /*
        // Check rod mode, moving to minigame interaction if in minigame mode.
        if(meta != null && meta.getMode() != 0){
            context.getState().state = InteractionState.Finished;
            return;
        }

         */

        //AnglersAlmanac.LOGGER.atInfo().log("Doing cast / reel interaction!");

        // Choose reel or cast depending on rod state.
        if (meta != null && meta.getBoundBobber() != null) {
            if (meta.getMode() == 0) {
                reelIn(commandBuffer, player, heldItem, meta.getBoundBobber(), meta, playerRef);
            }
        } else {
            castOut(interactionType, context, player);
        }
    }

    private void castOut(@Nonnull InteractionType interactionType, @Nonnull InteractionContext interactionContext, Player player) {
        //AnglersAlmanac.LOGGER.atInfo().log("Casting out");
        CommandBuffer<EntityStore> commandBuffer = interactionContext.getCommandBuffer();
        Ref<EntityStore> playerRef = interactionContext.getOwningEntity();
        TransformComponent transform = playerRef.getStore().getComponent(playerRef, TransformComponent.getComponentType());
        Transform lookTransform = TargetUtil.getLook(playerRef, commandBuffer);
        Vector3d spawnPos = transform.getPosition().clone();
        spawnPos.add(0, 1.5, 0);
        Vector3d lookDir = lookTransform.getDirection();
        Vector3d launchVelocity = new Vector3d(lookDir.x * 15, (lookDir.y * 15) + 2, lookDir.z * 15);
        Holder<EntityStore> bobberHolder = EntityStore.REGISTRY.newHolder();
        Vector3f rotation = new Vector3f();
        HeadRotation playerHead = commandBuffer.getComponent(playerRef, HeadRotation.getComponentType());
        if (playerHead != null) {
            rotation.setYaw(playerHead.getRotation().getYaw() + (float) (Math.PI / 180.0) * 180.0F);
        }

        bobberHolder.addComponent(HeadRotation.getComponentType(), new HeadRotation(rotation));
        bobberHolder.addComponent(TransformComponent.getComponentType(), new TransformComponent(spawnPos, rotation));
        bobberHolder.addComponent(Velocity.getComponentType(), new Velocity(launchVelocity));
        bobberHolder.ensureComponent(PhysicsValues.getComponentType());
        bobberHolder.addComponent(PhysicsComponent.getComponentType(), new PhysicsComponent());
        BobberComponent bobberComp = new BobberComponent();
        bobberComp.setPlayer(player);
        ItemStack bait = BaitUtils.findBait(player.getReference().getStore(), player.getReference());

        if (bait != null) {
            BaitUtils.removeBait(player,bait.getItemId());
            bobberComp.setBaitName(bait.getItemId());
        }
        else
        {
            BaitUtils.SendBaitNotification(player);
        }
        bobberHolder.addComponent(BobberComponent.getComponentType(), bobberComp);
        ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset("Bobber");
        if (modelAsset == null) modelAsset = ModelAsset.DEBUG;

        Model model = Model.createScaledModel(modelAsset, 2f);
        bobberHolder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
        bobberHolder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
        if (model.getBoundingBox() != null) {
            bobberHolder.addComponent(BoundingBox.getComponentType(), new BoundingBox(model.getBoundingBox()));
        }
        bobberHolder.putComponent(NetworkId.getComponentType(), new NetworkId(playerRef.getStore().getExternalData().takeNextNetworkId()));

        UUID bobberId = UUID.randomUUID();
        bobberHolder.addComponent(UUIDComponent.getComponentType(), new UUIDComponent(bobberId));

        commandBuffer.getExternalData().getWorld().execute(() -> {
            commandBuffer.addEntity(bobberHolder, AddReason.SPAWN);
        });

        ItemStack heldItem = interactionContext.getHeldItem();
//        FishingRodData meta = heldItem.getFromMetadataOrNull(FishingRodData.KEY, FishingRodData.CODEC);
        InventoryComponent.Hotbar inv = player.getReference().getStore().getComponent(player.getReference(), InventoryComponent.Hotbar.getComponentType());
        updateMetadata(inv, interactionContext.getHeldItemSlot(), heldItem, bobberId, null, 0);
        //play sound here
        int audio = SoundEvent.getAssetMap().getIndex("AA_Fishing_Reel");
        SoundUtil.playSoundEvent3d(audio, SoundCategory.SFX, transform.getPosition(), playerRef.getStore());
        //AnglersAlmanac.LOGGER.atInfo().log("Rod metadata %s, %s", heldItem.getFromMetadataOrNull(FishingRodData.KEYED_CODEC), heldItem.getFromMetadataOrNull(ItemModeData.KEYED_CODEC));
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
                    cancelFishing(commandBuffer, player, heldItem);
                }
            }
            else
            {
                AnglersAlmanac.LOGGER.atWarning().log("Failed to reel in" + bobberId);
                AnglersAlmanac.LOGGER.atInfo().log("Fixing busted metadata");
                cancelFishing(commandBuffer, player, heldItem);
            }

        } else {
            AnglersAlmanac.LOGGER.atWarning().log("Failed to reel in" + bobberId);
            AnglersAlmanac.LOGGER.atInfo().log("Fixing busted metadata");
            cancelFishing(commandBuffer, player, heldItem);
        }


    }

    public static void cancelFishing(CommandBuffer<EntityStore> commandBuffer, Player player, ItemStack heldItem) {
        //AnglersAlmanac.LOGGER.atInfo().log("Cancelling fishing.");
        World world = commandBuffer.getExternalData().getWorld();
        if(heldItem==null)
        {
            return;
        }
        FishingRodData meta = heldItem.getFromMetadataOrNull(FishingRodData.KEYED_CODEC);
        InventoryComponent.Hotbar inv = player.getReference().getStore().getComponent(player.getReference(), InventoryComponent.Hotbar.getComponentType());

        // Attempt to remove bobber and minigame if not already.
        if(meta != null) {
            if(meta.getBoundBobber() != null) {
                Ref<EntityStore> bobberRef = world.getEntityStore().getRefFromUUID(meta.getBoundBobber());
                if (bobberRef != null && bobberRef.isValid()) {
                    try {
                        commandBuffer.removeEntity(bobberRef, RemoveReason.REMOVE);
                    } catch (RuntimeException e) {
                        AnglersAlmanac.LOGGER.atWarning().withCause(e).log("Failed to remove bobber");
                    }
                }
                meta.setBoundBobber(null);
            }
            if(meta.getBoundMinigame() != null) {
                Ref<EntityStore> minigameRef = world.getEntityStore().getRefFromUUID(meta.getBoundMinigame());
                if (minigameRef != null && minigameRef.isValid()) {
                    MinigameManager.CancelGame(commandBuffer, minigameRef);
                }
                meta.setBoundMinigame(null);
            }
        }

        updateMetadata(inv, inv.getActiveSlot(), heldItem, null, null, 0);

    }

    public static void cancelFishing(CommandBuffer<EntityStore> commandBuffer, Player player, ItemStack heldItem, byte slot) {
        //AnglersAlmanac.LOGGER.atInfo().log("Cancelling fishing.");
        World world = commandBuffer.getExternalData().getWorld();
        if(heldItem==null)
        {
            return;
        }
        FishingRodData meta = heldItem.getFromMetadataOrNull(FishingRodData.KEYED_CODEC);
        InventoryComponent.Hotbar inv = player.getReference().getStore().getComponent(player.getReference(), InventoryComponent.Hotbar.getComponentType());

        // Attempt to remove bobber and minigame if not already.
        if(meta != null) {
            if(meta.getBoundBobber() != null) {
                Ref<EntityStore> bobberRef = world.getEntityStore().getRefFromUUID(meta.getBoundBobber());
                if (bobberRef != null && bobberRef.isValid()) {
                    try {
                        commandBuffer.removeEntity(bobberRef, RemoveReason.REMOVE);
                    } catch (RuntimeException e) {
                        AnglersAlmanac.LOGGER.atWarning().withCause(e).log("Failed to remove bobber");
                    }
                } else if (bobberRef == null) {
                    updateMetadata(inv, inv.getActiveSlot(), heldItem, null, null, 0);
                }
            }
            if(meta.getBoundMinigame() != null) {
                Ref<EntityStore> minigameRef = world.getEntityStore().getRefFromUUID(meta.getBoundMinigame());
                if (minigameRef != null && minigameRef.isValid()) {
                    MinigameManager.CancelGame(commandBuffer, minigameRef);
                }
                else if (minigameRef != null && !minigameRef.isValid()) {
                    updateMetadata(inv, inv.getActiveSlot(), heldItem, null, null, 0);
                }
            }
        }

        updateMetadata(inv, inv.getActiveSlot(), heldItem, null, null, 0);

    }


//    public static void updateMetadata(Inventory inventory, byte slot, ItemStack stack, @Nullable UUID bobberId, @Nullable UUID minigameId, int rodMode) {
//        ItemStack newRod;
//
//        FishingRodData fishingMetaData = stack.getFromMetadataOrNull(FishingRodData.KEY, FishingRodData.CODEC);
//        if (fishingMetaData == null) {
//            fishingMetaData = new FishingRodData();
//        }
//        fishingMetaData.setBoundBobber(bobberId);
//        fishingMetaData.setBoundMinigame(minigameId);
//        fishingMetaData.setMode(rodMode);
//        newRod = stack.withMetadata(FishingRodData.KEYED_CODEC, fishingMetaData);
//
//        inventory.getHotbar().replaceItemStackInSlot(slot, stack, newRod);
//        //AnglersAlmanac.LOGGER.atInfo().log("Updated metadata: %s, %s, %s", fishingMetaData.getBoundBobber(), fishingMetaData.getBoundMinigame(), fishingMetaData.getMode());
//    }

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