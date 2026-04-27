package dev.rm20.anglersalmanac.Components;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nullable;

public class PhysicsComponent implements Component<EntityStore> {
    private static ComponentType<EntityStore, PhysicsComponent> TYPE;

    public static ComponentType<EntityStore, PhysicsComponent> getComponentType() {
        return TYPE;
    }

    public static void setComponentType(ComponentType<EntityStore, PhysicsComponent> type) {
        TYPE = type;
    }

    public PhysicsComponent() {}

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        return new PhysicsComponent();
    }
}