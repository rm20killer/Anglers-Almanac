package dev.rm20.anglersalmanac.Systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Components.AudioPlayerComponent;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class AudioPlayerSystem extends EntityTickingSystem<EntityStore> {
    @Override
    public void tick(float deltaTime, int index, @NonNull ArchetypeChunk<EntityStore> archetypeChunk, @NonNull Store<EntityStore> store, @NonNull CommandBuffer<EntityStore> commandBuffer) {

        AudioPlayerComponent apc = store.getComponent(archetypeChunk.getReferenceTo(index), AudioPlayerComponent.COMPONENT_TYPE);
        if (apc == null) {
            store.getExternalData().getWorld().execute(() -> {
                Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
                if (ref.isValid()) {
                    try {
                        store.removeEntity(ref, RemoveReason.REMOVE);
                    } catch (RuntimeException e) {
                        AnglersAlmanac.LOGGER.atWarning().withCause(e).log("Failed to remove Audio Player System: "+ref.toString());
                    }
                }
            });
            return;
        }
        // Run audio components set to auto-play.
        if(apc.autoplayAsRandom){
            apc.doLoopAll(true, store);
        }else if(apc.autoplay){
            apc.doLoopAll(false, store);
        }

    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return AudioPlayerComponent.COMPONENT_TYPE;
    }

}
