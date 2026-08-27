package dev.rm20.anglersalmanac.Registration;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Components.*;

public class ComponentManager {
    public static void registerComponent(AnglersAlmanac plugin) {
        ComponentType<EntityStore, BobberComponent> registeredType = plugin.getEntityStoreRegistry().registerComponent(BobberComponent.class, BobberComponent::new);
        BobberComponent.setComponentType(registeredType);


        MinigameComponent_TensionBar.COMPONENT_TYPE = plugin.getEntityStoreRegistry().registerComponent(MinigameComponent_TensionBar.class, MinigameComponent_TensionBar::new);
        AudioPlayerComponent.COMPONENT_TYPE = plugin.getEntityStoreRegistry().registerComponent(AudioPlayerComponent.class, AudioPlayerComponent::new);
        MinigameWidgetComponent.COMPONENT_TYPE = plugin.getEntityStoreRegistry().registerComponent(MinigameWidgetComponent.class, MinigameWidgetComponent::new);

        ComponentType<EntityStore, PhysicsComponent> type = plugin.getEntityStoreRegistry().registerComponent(PhysicsComponent.class, PhysicsComponent::new);
        PhysicsComponent.setComponentType(type);
    }
}
