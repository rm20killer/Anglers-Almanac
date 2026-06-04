package dev.rm20.anglersalmanac.IEvents;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.system.CancellableEcsEvent;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;


/**
 * Dispatched when players cast a rod
 * <p>
 * This event contains interaction data and who casted the rod.
 * Marking the event as canceled will make it so the cast will not happen.
 * </p>
 */
public class FishingRodCastEvent extends CancellableEcsEvent implements IEvent<Void> {
    private final InteractionType interactionType;
    private final InteractionContext context;
    private final Ref<EntityStore> playerRef;

    public FishingRodCastEvent(@Nonnull InteractionType interactionType, @Nonnull InteractionContext context, @Nonnull Ref<EntityStore> playerRef) {
        this.interactionType = interactionType;
        this.context = context;
        this.playerRef = playerRef;
    }

    public InteractionType getInteractionType() { return interactionType; }
    public InteractionContext getContext() { return context; }
    public Ref<EntityStore> getPlayer() { return playerRef; }

}
