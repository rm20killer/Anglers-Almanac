package dev.rm20.anglersalmanac.IEvents;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.system.CancellableEcsEvent;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import javax.annotation.Nonnull;


/**
 * called by Reel interaction if an entity is attached to the bobber and about to pull the entity
 * <p>
 *  Allows the ability to cancel the event or modify the launch velocity.
 * </p>
 */
public class FishingRodEntityPullEvent extends CancellableEcsEvent implements IEvent<Void> {
    private final Ref<EntityStore> playerRef;
    private final Ref<EntityStore> targetRef;
    private Vector3d launchVelocity;

    public FishingRodEntityPullEvent(@Nonnull Ref<EntityStore> playerRef, Ref<EntityStore> targetRef, Vector3d launchVelocity) {
        this.playerRef = playerRef;
        this.targetRef = targetRef;
        this.launchVelocity = launchVelocity;
    }


    public Ref<EntityStore> getPlayer() { return playerRef; }
    public Ref<EntityStore> getTarget() { return targetRef; }
    public Vector3d getLaunchVelocity() { return launchVelocity; }
    public void setLaunchVelocity(Vector3d lVelocity) {
        launchVelocity = lVelocity;
    }

}
