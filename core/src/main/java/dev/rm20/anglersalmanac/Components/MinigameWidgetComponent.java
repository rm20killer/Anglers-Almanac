package dev.rm20.anglersalmanac.Components;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public record MinigameWidgetComponent(UUID ownerGameId, long spawnedAtMs) implements Component<EntityStore> {
    public static ComponentType<EntityStore, MinigameWidgetComponent> COMPONENT_TYPE;

    public MinigameWidgetComponent(UUID ownerGameId) {
        this(ownerGameId, System.currentTimeMillis());
    }

    public MinigameWidgetComponent() {
        this(null, System.currentTimeMillis());
    }

    public static ComponentType<EntityStore, MinigameWidgetComponent> getComponentType() {
        return COMPONENT_TYPE;
    }

    @Override
    public @Nullable Component<EntityStore> clone() {
        return new MinigameWidgetComponent(this.ownerGameId, this.spawnedAtMs);
    }
}