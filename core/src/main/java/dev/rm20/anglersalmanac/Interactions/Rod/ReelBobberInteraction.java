package dev.rm20.anglersalmanac.Interactions.Rod;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Components.BobberComponent;
import dev.rm20.anglersalmanac.Config.AnglersAlmanacConfig;
import dev.rm20.anglersalmanac.IEvents.FishingRodEntityPullEvent;
import dev.rm20.anglersalmanac.Metadata.FishingRodData;
import dev.rm20.anglersalmanac.Metadata.RodStats;
import dev.rm20.anglersalmanac.MinigameManager.MinigameManager;
import dev.rm20.anglersalmanac.Models.MinigameRodStats;
import dev.rm20.anglersalmanac.Utils.BaitUtils;
import org.joml.Vector3d;
import org.jspecify.annotations.NonNull;

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
                if (bobberComp.isHookedToEntity()) {
                    hookEntity(commandBuffer, playerRef, bobberComp, fishingRodData);
                    UseRodInteraction.cancelFishing(commandBuffer, player, heldItem);
                    return;
                }

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

    private static void hookEntity(CommandBuffer<EntityStore> commandBuffer, Ref<EntityStore> playerRef, BobberComponent bobberComp, FishingRodData fishingRodData) {
        Ref<EntityStore> targetRef = bobberComp.getHookedEntity();

        if (targetRef != null && targetRef.isValid()) {
            TransformComponent pTransform = commandBuffer.getComponent(playerRef, TransformComponent.getComponentType());
            TransformComponent tTransform = targetRef.getStore().getComponent(targetRef, TransformComponent.getComponentType());
            Velocity tVelocity = targetRef.getStore().getComponent(targetRef, Velocity.getComponentType());

            if (pTransform != null && tTransform != null) {
                Vector3d pPos = pTransform.getPosition();
                Vector3d tPos = tTransform.getPosition();

                Vector3d direction = new Vector3d(pPos).sub(tPos);
                double distance = direction.length();

                if (distance > 0.1) {
                    direction.normalize();
                    RodStats rodStats = MinigameRodStats.getRodStatsFromRodId(bobberComp.fishingRod.getItemId());
                    float rodMult =1f;
                    if (rodStats != null) {
                        rodMult = rodStats.fishWeightMul;
                    }
                    Vector3d launchVelocity = getVector3d(distance, direction,rodMult);
                    if(targetRef.getStore().getComponent(targetRef, Player.getComponentType()) == null){
                        launchVelocity.mul(2);
                    }
                    var eventBus = HytaleServer.get().getEventBus();
                    FishingRodEntityPullEvent event = new FishingRodEntityPullEvent(playerRef, targetRef, launchVelocity);

                    eventBus.dispatchFor(FishingRodEntityPullEvent.class).dispatch(event);
                    if (event.isCancelled()) {
                        return;
                    }

                    if (tVelocity != null) {
                        tVelocity.addInstruction(event.getLaunchVelocity(), null, ChangeVelocityType.Add);
                    } else {
                        Holder<EntityStore> holder = targetRef.getStore().getRegistry().newHolder();
                        holder.addComponent(Velocity.getComponentType(), new Velocity(event.getLaunchVelocity()));
                    }
                }
            }
        }
    }

    private static @NonNull Vector3d getVector3d(double distance, Vector3d direction, float rodMult) {
        AnglersAlmanacConfig config = AnglersAlmanac.MOD_CONFIG.get();
        double baseMultiplier = config.getEntityPullBaseForce();
        double distanceScale = config.getEntityPullDistanceMultiplier();
        double horizontalPower = baseMultiplier + (distance * distanceScale);

        return new Vector3d(
                direction.x * horizontalPower * rodMult,
                1 + (distance * config.getEntityYMultiplier()) * rodMult,
                direction.z * horizontalPower * rodMult
        ).mul(5);
    }
}
