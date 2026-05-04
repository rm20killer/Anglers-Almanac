package dev.rm20.anglersalmanac.Systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Components.BobberComponent;
import dev.rm20.anglersalmanac.Interactions.LaunchBobberInteraction;
import dev.rm20.anglersalmanac.Metadata.FishingRodData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;
import java.util.UUID;

import static dev.rm20.anglersalmanac.Utils.BaitUtils.SendBaitNotification;

public class BobberSystem extends EntityTickingSystem<EntityStore> {
    private final Random random = new Random();
    ItemStack fishingRod = null;
    byte slot = 0;


    private static final float DespawnRange = 64.0f * 64.0f; // 64 blocks


    @Override
    public void tick(float v, int i, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        BobberComponent component = archetypeChunk.getComponent(i, BobberComponent.getComponentType());
        TransformComponent transform = archetypeChunk.getComponent(i, TransformComponent.getComponentType());
        Player player;
        if(component !=null)
        {
            player = component.getPlayer();
            if (player == null) {
                UUIDComponent uuidComponent = archetypeChunk.getComponent(i, UUIDComponent.getComponentType());
                if(uuidComponent != null)
                {
                    AnglersAlmanac.LOGGER.atInfo().log("No player found for BobberSystem" + uuidComponent.getUuid());
                }
                try {
                    commandBuffer.getExternalData().getWorld().execute(() -> {
                        store.removeEntity(archetypeChunk.getReferenceTo(i), RemoveReason.REMOVE);
                    });
                } catch (RuntimeException e) {
                    AnglersAlmanac.LOGGER.atWarning().withCause(e).log("Failed to remove bobber");
                }
                return;
            }
            else
            {
                if(player.getReference() == null)
                {
                    AnglersAlmanac.LOGGER.atInfo().log("BobberSystem was attached to player that no longer is there, removing");
                    try {
                        commandBuffer.getExternalData().getWorld().execute(() -> {
                            store.removeEntity(archetypeChunk.getReferenceTo(i), RemoveReason.REMOVE);
                        });
                    } catch (RuntimeException e) {
                        AnglersAlmanac.LOGGER.atWarning().withCause(e).log("Failed to remove bobber");
                    }
                }
                TransformComponent playerTransform = commandBuffer.getComponent(player.getReference(), TransformComponent.getComponentType());
                Vector3d playerPos = playerTransform != null ? playerTransform.getPosition() : new Vector3d(0,-64,0);
                double distSq = getDistanceSquared(playerPos, transform != null ? transform.getPosition() : new Vector3d(0,-129,0));
                if (distSq > DespawnRange) {
                    AnglersAlmanac.LOGGER.atInfo().log(player.getDisplayName()+" To far away from bobber, Despawn");
                    LaunchBobberInteraction.cancelFishing(commandBuffer, player, fishingRod, slot);
                    return;
                }
            }

            ItemStack heldItem = player.getInventory().getItemInHand();
            FishingRodData meta = (heldItem != null) ? heldItem.getFromMetadataOrNull(FishingRodData.KEY, FishingRodData.CODEC) : null;
            UUIDComponent uuidComp = archetypeChunk.getComponent(i, UUIDComponent.getComponentType());
            UUID bobberUuid = (uuidComp != null) ? uuidComp.getUuid() : null;

            boolean isLinked = meta != null && bobberUuid != null && bobberUuid.equals(meta.getBoundBobber());

            if (!isLinked) {
                if (fishingRod == null) {
                    try {
                        commandBuffer.getExternalData().getWorld().execute(() -> {
                            store.removeEntity(archetypeChunk.getReferenceTo(i), RemoveReason.REMOVE);
                        });
                    } catch (RuntimeException e) {
                        AnglersAlmanac.LOGGER.atWarning().withCause(e).log("Failed to remove bobber");
                    }
                } else {
                    //AnglersAlmanac.LOGGER.atInfo().log(player.getDisplayName() + " fishing rod data changed while fishing.");
                    LaunchBobberInteraction.cancelFishing(commandBuffer, player, fishingRod, slot);
                }
                return;
            }
            slot = player.getInventory().getActiveHotbarSlot();
            fishingRod = heldItem;
        } else {
            player = null;
        }

        if (component == null || !component.InWater()) return;

        float newAge = component.getBobberAge() + v;
        component.setBobberAge(newAge);

        if (newAge < 4.0f) return;

        if (component.isCanCatch()) {
            float catchTime = component.getCatchTimer() - v;
            if (catchTime <= 0) {
                // Fish escaped
                component.setCanCatch(false);
                component.setBaitName(null);
                resetWaitTimer(component);
            } else {
                component.setCatchTimer(catchTime);
            }
        } else {
            float timeUntilCatch = component.getTimeUntilCatch();
            if (timeUntilCatch <= 0) {
                boolean requiresBait = AnglersAlmanac.MOD_CONFIG.get().getShouldUseBait();
                String baitName = component.getBaitName();
                if (requiresBait && (baitName == null || baitName.isEmpty())) {
                    SendBaitNotification(player);
                    //AnglersAlmanac.LOGGER.atInfo().log("No bait on rod");
                    resetWaitTimer(component);
                    return;
                }
                //AnglersAlmanac.LOGGER.atInfo().log(baitName);
                // Fish bite logic
                component.setCanCatch(true);
                ParticleUtil.spawnParticleEffect("Fish_Alert", transform.getPosition().clone().add(0, 0.5, 0), store);
                //Audio
                int audio = SoundEvent.getAssetMap().getIndex("AA_Fishing_Bubble");
                World world = store.getExternalData().getWorld();
                EntityStore store2 = world.getEntityStore();
                world.execute(() -> {
                    SoundUtil.playSoundEvent3dToPlayer(player.getReference(), audio, SoundCategory.SFX, transform.getPosition(), store2.getStore());
                });
                // Reaction window: How long the player has to click.
                // Making this slightly more random (e.g., 0.8s to 3.0s)
                float reactionWindow = 0.8f + random.nextFloat() * 2.2f;
                component.setCatchTimer(reactionWindow);
            } else {
                component.setTimeUntilCatch(timeUntilCatch - v);
            }
        }
    }

    /**
     * Resets the timer with a much wider, longer range for a "realistic" feel.
     */
    private void resetWaitTimer(BobberComponent component) {
        // This makes fishing feel like a secondary activity rather than a rapid-fire minigame.
        float minWait = 5.0f;
        float maxWait = 35.0f;
        float randomWait = minWait + random.nextFloat() * (maxWait - minWait);

        component.setTimeUntilCatch(randomWait);
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(BobberComponent.getComponentType());
    }

    private double getDistanceSquared(Vector3d pos1, Vector3d pos2) {
        double dx = pos1.x - pos2.x;
        double dy = pos1.y - pos2.y;
        double dz = pos1.z - pos2.z;
        return dx * dx + dy * dy + dz * dz;
    }
}
