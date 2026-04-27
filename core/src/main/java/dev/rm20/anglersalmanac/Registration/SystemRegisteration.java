package dev.rm20.anglersalmanac.Registration;

import com.google.common.reflect.ClassPath;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AnglersAlmanac;

public class SystemRegisteration {
    private static final String PACKAGE_NAME = "dev.rm20.anglersalmanac.Systems";

    public static void registerSystem(AnglersAlmanac plugin) {
        try {
            ClassPath classPath = ClassPath.from(plugin.getClass().getClassLoader());
            int count = 0;

            for (ClassPath.ClassInfo classInfo : classPath.getTopLevelClassesRecursive(PACKAGE_NAME)) {
                Class<?> loadedClass = classInfo.load();
                if (EntityTickingSystem.class.isAssignableFrom(loadedClass)) {

                    try {
                        EntityTickingSystem<EntityStore> systemInstance = (EntityTickingSystem<EntityStore>) loadedClass.getDeclaredConstructor().newInstance();
                        plugin.getEntityStoreRegistry().registerSystem(systemInstance);
                        plugin.getLogger().atInfo().log("Successfully registered system: " + loadedClass.getSimpleName());
                        count++;
                    } catch (Exception e) {
                        plugin.getLogger().atInfo().log("Could not register command class: " + loadedClass.getSimpleName());
                        e.printStackTrace();
                    }
                }
            }
            plugin.getLogger().atInfo().log("Registered " + count + " system.");
        } catch (Exception e) {
            plugin.getLogger().atInfo().log("Failed to load command classes.");
            e.printStackTrace();
        }
    }
}
