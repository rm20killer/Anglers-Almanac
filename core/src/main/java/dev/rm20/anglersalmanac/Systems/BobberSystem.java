package dev.rm20.anglersalmanac.Systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Components.BobberComponent;
import dev.rm20.anglersalmanac.Interactions.Rod.UseRodInteraction;
import dev.rm20.anglersalmanac.Metadata.FishingRodData;
import dev.rm20.anglersalmanac.Utils.WaterUtils;
import dev.rm20.anglersalmanac.api.AnglersAlmanacAPI;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;
import java.util.UUID;

public class BobberSystem extends EntityTickingSystem<EntityStore> {
    private final Random random = new Random();
    private static final float APPROACH_TIME = 2.5f;

    private static final float DespawnRange = 64.0f * 64.0f; // 64 blocks


    @Override
    public void tick(float v, int i, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        BobberComponent component = archetypeChunk.getComponent(i, BobberComponent.getComponentType());
        TransformComponent transform = archetypeChunk.getComponent(i, TransformComponent.getComponentType());
        Player player;
        TransformComponent playerTransform = null;
        if (component != null) {
            player = component.getPlayer();
            if (player == null) {
                UUIDComponent uuidComponent = archetypeChunk.getComponent(i, UUIDComponent.getComponentType());
                if (uuidComponent != null) {
                    AnglersAlmanac.LOGGER.atInfo().log("No player found for BobberSystem: " + uuidComponent.getUuid());
                }
                try {
                    commandBuffer.getExternalData().getWorld().execute(() -> {
                        if (archetypeChunk.getReferenceTo(i).isValid()) {
                            store.removeEntity(archetypeChunk.getReferenceTo(i), RemoveReason.REMOVE);
                        } else {
                            AnglersAlmanac.LOGGER.atWarning().log("Something went wrong with the bobber");
                        }
                    });
                } catch (RuntimeException e) {
                    AnglersAlmanac.LOGGER.atWarning().withCause(e).log("Failed to remove bobber");
                }
                return;
            } else {
                if (player.getReference() == null) {
                    AnglersAlmanac.LOGGER.atInfo().log("BobberSystem was attached to player that no longer is there, removing");
                    try {
                        commandBuffer.getExternalData().getWorld().execute(() -> {
                            if (archetypeChunk.getReferenceTo(i).isValid()) {
                                store.removeEntity(archetypeChunk.getReferenceTo(i), RemoveReason.REMOVE);
                            } else {
                                AnglersAlmanac.LOGGER.atWarning().log("Something went wrong with the bobber");
                            }
                        });
                    } catch (RuntimeException e) {
                        AnglersAlmanac.LOGGER.atWarning().withCause(e).log("Failed to remove bobber");
                    }
                }
                if (player.getReference() == null) {
                    try {
                        commandBuffer.getExternalData().getWorld().execute(() -> {
                            if (archetypeChunk.getReferenceTo(i).isValid()) {
                                store.removeEntity(archetypeChunk.getReferenceTo(i), RemoveReason.REMOVE);
                            } else {
                                AnglersAlmanac.LOGGER.atWarning().log("Something went wrong with the bobber (no player ref)");
                            }
                        });
                    } catch (RuntimeException e) {
                        AnglersAlmanac.LOGGER.atWarning().withCause(e).log("Failed to remove bobber");
                    }
                    return;
                }
                playerTransform = commandBuffer.getComponent(player.getReference(), TransformComponent.getComponentType());
                Vector3d playerPos = playerTransform != null ? playerTransform.getPosition() : new Vector3d(0, -64, 0);
                double distSq = getDistanceSquared(playerPos, transform != null ? transform.getPosition() : new Vector3d(0, -129, 0));
                if (distSq > DespawnRange) {
                    Ref<EntityStore> playerref = player.getReference();
                    if (playerref != null) {
                        UseRodInteraction.cancelFishing(commandBuffer, player, component.fishingRod, component.slot);
                    }
                    PlayerRef playerRef1 = playerref.getStore().getComponent(playerref, PlayerRef.getComponentType());
                    if (playerRef1 == null) {
                        UseRodInteraction.cancelFishing(commandBuffer, player, component.fishingRod, component.slot);
                    }
                    AnglersAlmanac.LOGGER.atInfo().log(playerRef1.getUsername() + " To far away from bobber, Despawn");
                    UseRodInteraction.cancelFishing(commandBuffer, player, component.fishingRod, component.slot);
                    return;
                }
            }

            InventoryComponent.Hotbar inv = player.getReference().getStore().getComponent(player.getReference(), InventoryComponent.Hotbar.getComponentType());
            ItemStack heldItem = inv != null ? inv.getActiveItem() : null;
            FishingRodData meta = (heldItem != null) ? heldItem.getFromMetadataOrNull(FishingRodData.KEY, FishingRodData.CODEC) : null;
            UUIDComponent uuidComp = archetypeChunk.getComponent(i, UUIDComponent.getComponentType());
            UUID bobberUuid = (uuidComp != null) ? uuidComp.getUuid() : null;

            boolean isLinked = meta != null && bobberUuid != null && bobberUuid.equals(meta.getBoundBobber());

            if (!isLinked) {
                if (component.fishingRod == null) {
                    try {
                        commandBuffer.getExternalData().getWorld().execute(() -> {
                            store.removeEntity(archetypeChunk.getReferenceTo(i), RemoveReason.REMOVE);
                        });
                    } catch (RuntimeException e) {
                        AnglersAlmanac.LOGGER.atWarning().withCause(e).log("Failed to remove bobber");
                    }
                } else {
                    UseRodInteraction.cancelFishing(commandBuffer, player, component.fishingRod, component.slot);
                }
                return;
            }
            component.slot = inv.getActiveSlot();
            component.fishingRod = heldItem;
        } else {
            player = null;
        }
        if (component == null) {
            AnglersAlmanac.LOGGER.atWarning().log("no bobber component");
            return;
        }
        if (!component.InWater()) return;
        //AnglersAlmanac.LOGGER.atInfo().log(String.valueOf(component.getBobberAge()));
        float newAge = component.getBobberAge() + v;
        component.setBobberAge(newAge);

        if (newAge < 2.0f) return;

        if (component.isCanCatch()) {
            float catchTime = component.getCatchTimer() - v;
            if (catchTime <= 0) {
                // Fish escaped
                component.setCanCatch(false);
                component.setBaitName(null);
                component.resetFishApproach();
                resetWaitTimer(component);
            } else {
                component.setCatchTimer(catchTime);
            }
        } else {
            float timeUntilCatch = component.getTimeUntilCatch();
            if (timeUntilCatch < 0) {
                resetWaitTimer(component);
                return;
            }

            float updatedTime = timeUntilCatch - v;
            if (updatedTime <= 0) {
                boolean requiresBait = AnglersAlmanacAPI.getConfig().get().getShouldUseBait();
                String baitName = component.getBaitName();
                if (requiresBait && (baitName == null || baitName.isEmpty())) {
                    //SendBaitNotification(player);
                    //AnglersAlmanac.LOGGER.atInfo().log("No bait on rod");
                    component.resetFishApproach();
                    resetWaitTimer(component);
                    return;
                }
                //AnglersAlmanac.LOGGER.atInfo().log(baitName);
                // Fish bite logic
                component.setCanCatch(true);
                component.setTimeUntilCatch(0);
                component.resetFishApproach();
                ParticleUtil.spawnParticleEffect("Fish_Alert", new Vector3d(transform.getPosition()).add(0, 0.5, 0), store);
                ParticleUtil.spawnParticleEffect("Water_Splash", new Vector3d(transform.getPosition()).add(0, 0.1, 0), store);
                //Audio
                int audio = SoundEvent.getAssetMap().getIndex("AA_Fishing_Bubble");
                World world = store.getExternalData().getWorld();
                EntityStore store2 = world.getEntityStore();
                world.execute(() -> {
                    SoundUtil.playSoundEvent3dToPlayer(player.getReference(), audio, SoundCategory.SFX, transform.getPosition(), store2.getStore());
                });
                // Reaction window: How long the player has to click.
                float reactionWindow = 1.0f + random.nextFloat() * 2.0f;
                component.setCatchTimer(reactionWindow);
            } else {
                component.setTimeUntilCatch(updatedTime);

                boolean requiresBait = AnglersAlmanacAPI.getConfig().get().getShouldUseBait();
                String baitName = component.getBaitName();
                boolean hasBaitIfRequired = !requiresBait || (baitName != null && !baitName.isEmpty());

                if (hasBaitIfRequired && updatedTime <= APPROACH_TIME) {
                    tickFishApproach(component, transform, store, v);
                }
            }
        }


    }

    private void initFishApproach(BobberComponent component, TransformComponent transform, World world) {
        Vector3d bobberPos = transform.getPosition();
        double bestStartX = bobberPos.x;
        double bestStartZ = bobberPos.z;
        boolean foundWater = false;

        // Try to find a direction with clear water surface
        double baseAngle = random.nextDouble() * 2.0 * Math.PI;
        double[] testDistances = new double[]{3.5, 2.5, 1.8, 1.2};

        distanceLoop:
        for (double dist : testDistances) {
            for (int i = 0; i < 16; i++) {
                double angle = baseAngle + i * (2.0 * Math.PI / 16.0);
                double candidateStartX = bobberPos.x + Math.cos(angle) * dist;
                double candidateStartZ = bobberPos.z + Math.sin(angle) * dist;
                double midX = (bobberPos.x + candidateStartX) * 0.5;
                double midZ = (bobberPos.z + candidateStartZ) * 0.5;

                // Make sure start and midpoint are on water and not other blocks
                if (WaterUtils.getWaterSurfaceAt(world, candidateStartX, candidateStartZ, bobberPos.y) != null &&
                        WaterUtils.getWaterSurfaceAt(world, midX, midZ, bobberPos.y) != null) {
                    bestStartX = candidateStartX;
                    bestStartZ = candidateStartZ;
                    foundWater = true;
                    break distanceLoop;
                }
            }
        }

        if (!foundWater) {
            bestStartX = bobberPos.x + (random.nextDouble() - 0.5) * 1.0;
            bestStartZ = bobberPos.z + (random.nextDouble() - 0.5) * 1.0;
        }

        component.setFishApproaching(true);
        component.setFishApproachStartX(bestStartX);
        component.setFishApproachStartZ(bestStartZ);
        component.setFishApproachDuration(component.getTimeUntilCatch());
        component.setFishParticleTimer(0f);
    }

    private void tickFishApproach(BobberComponent component, TransformComponent transform, Store<EntityStore> store, float dt) {
        World world = store.getExternalData().getWorld();
        Vector3d bobberPos = transform.getPosition();

        if (!component.isFishApproaching()) {
            initFishApproach(component, transform, world);
        }

        float duration = component.getFishApproachDuration();
        if (duration <= 0.001f) {
            duration = APPROACH_TIME;
        }

        float remaining = component.getTimeUntilCatch();
        float progress = 1.0f - (remaining / duration);
        if (progress < 0f) progress = 0f;
        if (progress > 1f) progress = 1f;

        double startX = component.getFishApproachStartX();
        double startZ = component.getFishApproachStartZ();

        // Interpolate position from start towards bobber
        double curX = startX + (bobberPos.x - startX) * progress;
        double curZ = startZ + (bobberPos.z - startZ) * progress;

        // Subtle side-to-side swimming oscillation
        double dirX = bobberPos.x - startX;
        double dirZ = bobberPos.z - startZ;
        double len = Math.sqrt(dirX * dirX + dirZ * dirZ);
        if (len > 0.01) {
            double perpX = -dirZ / len;
            double perpZ = dirX / len;
            double wave = Math.sin(progress * Math.PI * 6.0) * 0.25 * (1.0 - progress);
            curX += perpX * wave;
            curZ += perpZ * wave;
        }

        // Particle spawn timer (every ~0.15s)
        float timer = component.getFishParticleTimer() + dt;
        if (timer >= 0.15f) {
            component.setFishParticleTimer(0f);

            // Must spawn on water and NOT on other blocks
            Double waterSurfaceY = WaterUtils.getWaterSurfaceAt(world, curX, curZ, bobberPos.y);
            if (waterSurfaceY != null) {
                Vector3d particlePos = new Vector3d(curX, waterSurfaceY, curZ);
                ParticleUtil.spawnParticleEffect("Fish_Approach", particlePos, store);
            }
        } else {
            component.setFishParticleTimer(timer);
        }
    }

    /**
     * Resets the timer with a much wider, longer range for a "realistic" feel.
     */
    private void resetWaitTimer(BobberComponent component) {
        // This makes fishing feel like a secondary activity rather than a rapid-fire minigame.
        component.resetFishApproach();
        float minWait = 5.0f;
        float maxWait = 20.0f;
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