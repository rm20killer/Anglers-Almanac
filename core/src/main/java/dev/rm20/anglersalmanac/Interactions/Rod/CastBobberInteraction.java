package dev.rm20.anglersalmanac.Interactions.Rod;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleAttractor;
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSpawner;
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSpawnerGroup;
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSystem;
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
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Components.BobberComponent;
import dev.rm20.anglersalmanac.Components.PhysicsComponent;
import dev.rm20.anglersalmanac.Metadata.FishingRodData;
import dev.rm20.anglersalmanac.Utils.BaitUtils;

import javax.annotation.Nonnull;
import java.util.UUID;

public class CastBobberInteraction extends SimpleInstantInteraction {
    public static final BuilderCodec<CastBobberInteraction> CODEC = BuilderCodec.builder(
            CastBobberInteraction.class, CastBobberInteraction::new, SimpleInstantInteraction.CODEC
    ).build();

    @Override
    protected void firstRun(@Nonnull InteractionType interactionType, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
        Ref<EntityStore> playerRef = context.getOwningEntity();
        ItemStack heldItem = context.getHeldItem();
        if (commandBuffer == null || playerRef == null || heldItem == null) return;

        Player player = commandBuffer.getComponent(playerRef, Player.getComponentType());
        FishingRodData meta = heldItem.getFromMetadataOrNull(FishingRodData.KEY, FishingRodData.CODEC);
        //AnglersAlmanac.LOGGER.atInfo().log("Casting out");
        castOut(interactionType, context, player);
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
        UseRodInteraction.updateMetadata(inv, interactionContext.getHeldItemSlot(), heldItem, bobberId, null, 0);
        //play sound here
        int audio = SoundEvent.getAssetMap().getIndex("AA_Fishing_Reel");
        SoundUtil.playSoundEvent3d(audio, SoundCategory.SFX, transform.getPosition(), playerRef.getStore());

        // Trigger cast animation
        //AnimationAction attackAnim = new AnimationAction("attack");
        //AnimationUtils.playAnimation(player.getReference(), AnimationSlot.Action, "AA_Rod", "AA_Rod_Cast", true, interactionContext.getCommandBuffer().getStore());
        //attackAnim.execute(ref, role, sensorInfo, dt, store);

        //AnglersAlmanac.LOGGER.atInfo().log("Rod metadata %s, %s", heldItem.getFromMetadataOrNull(FishingRodData.KEYED_CODEC), heldItem.getFromMetadataOrNull(ItemModeData.KEYED_CODEC));
    }
}
