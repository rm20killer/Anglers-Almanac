package dev.rm20.anglersalmanac.Systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.modules.collision.BlockCollisionData;
import com.hypixel.hytale.server.core.modules.collision.CollisionModule;
import com.hypixel.hytale.server.core.modules.collision.CollisionResult;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Components.PhysicsComponent;
import dev.rm20.anglersalmanac.Components.BobberComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CustomPhysicsSystem extends EntityTickingSystem<EntityStore> {
    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                     @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        BobberComponent bobberComp = archetypeChunk.getComponent(index, BobberComponent.getComponentType());
        TransformComponent transform = archetypeChunk.getComponent(index, TransformComponent.getComponentType());
        Velocity velocityComp = archetypeChunk.getComponent(index, Velocity.getComponentType());
        BoundingBox boundingBoxComponent = archetypeChunk.getComponent(index, BoundingBox.getComponentType());

        if (transform == null || velocityComp == null || boundingBoxComponent == null) return;

        World world = store.getExternalData().getWorld();
        Vector3d position = new Vector3d(transform.getPosition());
        Vector3d velocity = new Vector3d();
        velocityComp.assignVelocityTo(velocity);

        // 1. Precise Fluid Check
        // We check the fluid at the current position
        int fluidId = world.getFluidId((int)position.x, (int)Math.floor(position.y), (int)position.z);
        boolean inWater = (fluidId == 7||fluidId == 8||fluidId == 12);
//        playedWaterSFX =false;
        // 2. Apply Forces
        if (inWater) {
            if(!bobberComp.InWater())
            {
                bobberComp.setInWater(true);
                //Audio
                int audio = SoundEvent.getAssetMap().getIndex("AA_Fishing_Water");
                EntityStore store2 = world.getEntityStore();
                world.execute(() -> {
                    SoundUtil.playSoundEvent3d(audio,SoundCategory.SFX, position, store2.getStore());
                });
            }
            applyWaterForces(dt, position, velocity, bobberComp, world);
        } else {
            if(bobberComp.InWater())
            {
                bobberComp.setInWater(false);
            }
            // Apply Air Gravity
            velocity.y -= 25.0 * dt;
            velocity.x *= 0.99;
            velocity.z *= 0.99;
        }

        // 3. Collision
        Vector3d scaledVel = new Vector3d(velocity).scale(dt);
        CollisionResult result = new CollisionResult();
        Box box = boundingBoxComponent.getBoundingBox();

        if (CollisionModule.isBelowMovementThreshold(scaledVel)) {
            CollisionModule.findBlockCollisionsShortDistance(world, box, position, scaledVel, result);
        } else {
            CollisionModule.findBlockCollisionsIterative(world, box, position, scaledVel, true, result);
        }

        if (result.getFirstBlockCollision() != null) {
            BlockCollisionData data = result.getFirstBlockCollision();
            position.assign(data.collisionPoint);

            // Bounce Logic
            // Formula: v_new = v - 2 * (v . n) * n
            double dotProduct = velocity.dot(data.collisionNormal);

            // Ground Collision
            if (data.collisionNormal.y > 0.7) {
                // Only bounce if fast enough (e.g., > 3 units/s)
                if (Math.abs(velocity.y) > 3.0 && !inWater) {
                    // 40% lose bounciness
                    velocity.y = -velocity.y * 0.4;
                    // Friction on bounce
                    velocity.x *= 0.8;
                    velocity.z *= 0.8;
                } else if (!inWater) {
                    velocity.scale(0);
                } else {
                    velocity.y = Math.max(0, velocity.y);
                }
            } else {
                // Wall/Side Collision
                // 60% energy retention on wall hits
                double elasticity = 0.6;
                velocity.x -= (1.0 + elasticity) * dotProduct * data.collisionNormal.x;
                velocity.y -= (1.0 + elasticity) * dotProduct * data.collisionNormal.y;
                velocity.z -= (1.0 + elasticity) * dotProduct * data.collisionNormal.z;
            }
        } else {
            position.add(scaledVel);
        }

        // Water Bobbing
        if (inWater && bobberComp != null) {
            long time = System.currentTimeMillis();
            double freq = bobberComp.isCanCatch() ? 0.02 : 0.005;
            double amp = bobberComp.isCanCatch() ? 0.04 : 0.01;
            position.y += Math.sin(time * freq) * amp;
        }

        // Sync
        velocityComp.set(velocity);
        transform.setPosition(position);

        // Remove if under the maps
        if (position.getY() < -16.0) {
            try {
                commandBuffer.getExternalData().getWorld().execute(() -> {
                    store.removeEntity(archetypeChunk.getReferenceTo(index), RemoveReason.REMOVE);
                });

            } catch (RuntimeException e) {
                AnglersAlmanac.LOGGER.atWarning().withCause(e).log("Failed to remove bobber");
            }
        }
    }

    private void applyWaterForces(float dt, Vector3d position, Vector3d velocity, BobberComponent bobber, World world) {
        double waterTop = getWaterSurfaceLevel(world,position);
        bobber.setWaterDepth(calculateWaterDepth(world,position,waterTop));
//        AnglersAlmanac.LOGGER.atInfo().log(String.valueOf(bobber.getWaterDepth()));
        double targetY = waterTop - 0.25;

        double depthError = targetY - position.y;
        if (depthError > 0) {
            velocity.y += (depthError * 35.0) * dt;
        } else {
            velocity.y -= 15.0 * dt;
        }
        velocity.x *= 0.85;
        velocity.z *= 0.85;
        velocity.y *= 0.80;
    }

    private double getWaterSurfaceLevel(World world, Vector3d position) {
        int startY = (int) Math.floor(position.y);
        int x = (int) position.x;
        int z = (int) position.z;

        // Scan upwards from current block to find air
        // Limit to 10 blocks to prevent lag
        for (int y = startY; y < startY + 10; y++) {
            if (world.getFluidId(x, y + 1, z) != 7) {
                return (double) y + 1.0;
            }
        }
        return startY + 1.0;
    }

    private int calculateWaterDepth(World world, Vector3d position, double surfaceY) {
        int depth = 0;
        int x = (int) Math.floor(position.x);
        int z = (int) Math.floor(position.z);
        int startY = (int) Math.floor(surfaceY - 0.1);
        //limit to 32
        for (int i = 0; i < 32; i++) {
            int currentY = startY - i;
            int fluidId = world.getFluidId(x, currentY, z);
            if (fluidId == 7 || fluidId == 8 || fluidId == 12) {
                depth++;
            } else {
                break;
            }
        }
        return depth;
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(PhysicsComponent.getComponentType());
    }

    private void playWaterSFX()
    {

    }
}