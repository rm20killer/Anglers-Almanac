package dev.rm20.anglersalmanac.Systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Components.MinigameWidgetComponent;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public class TensionBarJanitorSystem extends EntityTickingSystem<EntityStore> {

    private static final long SAFETY_CEILING_MS = 600_000L; // 10 Min?

    @Override
    public void tick(float deltaTime, int i, @NonNull ArchetypeChunk<EntityStore> archetypeChunk, @NonNull Store<EntityStore> store, @NonNull CommandBuffer<EntityStore> commandBuffer) {
        Ref<EntityStore> widgetRef = archetypeChunk.getReferenceTo(i);
        if (!widgetRef.isValid()) return;

        MinigameWidgetComponent widget = commandBuffer.getComponent(widgetRef, MinigameWidgetComponent.COMPONENT_TYPE);
        if (widget == null) return;

        World world = store.getExternalData().getWorld();
        long now = System.currentTimeMillis();

        boolean shouldDespawn = false;

        if (now - widget.spawnedAtMs() > SAFETY_CEILING_MS) {
            shouldDespawn = true;
        } else {
            Ref<EntityStore> ownerRef = world.getEntityRef(widget.ownerGameId());
            if (ownerRef == null || !ownerRef.isValid()) {
                shouldDespawn = true;
            }
        }

        if (shouldDespawn) {
            world.execute(() -> {
                if (widgetRef.isValid()) {
                    try {
                        store.removeEntity(widgetRef, RemoveReason.REMOVE);
                    } catch (Exception e) {
                        AnglersAlmanac.LOGGER.atWarning().withCause(e).log("Janitor failed to remove orphan widget entity: " + widgetRef);
                    }
                }
            });
        }
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return MinigameWidgetComponent.COMPONENT_TYPE;
    }
}