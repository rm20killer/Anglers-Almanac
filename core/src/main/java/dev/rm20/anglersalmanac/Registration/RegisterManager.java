package dev.rm20.anglersalmanac.Registration;

import com.google.common.reflect.ClassPath;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;

public class RegisterManager {

    public static void registerCommands(AnglersAlmanac plugin) {
        final String PACKAGE_NAME = "dev.rm20.anglersalmanac.Commands";
        try {
            ClassPath classPath = ClassPath.from(plugin.getClass().getClassLoader());
            int count = 0;

            for (ClassPath.ClassInfo classInfo : classPath.getTopLevelClassesRecursive(PACKAGE_NAME)) {
                Class<?> loadedClass = classInfo.load();
                if (AbstractCommand.class.isAssignableFrom(loadedClass) && loadedClass.isAnnotationPresent(CommandInfo.class)) {
                    CommandInfo info = loadedClass.getAnnotation(CommandInfo.class);
                    try {
                        AbstractCommand command = (AbstractCommand) loadedClass
                                .getConstructor(String.class, String.class)
                                .newInstance(info.name(), info.description());

                        // Register
                        plugin.getCommandRegistry().registerCommand(command);

                        plugin.getLogger().atInfo().log("Successfully registered command: " + info.name());
                        count++;
                    } catch (Exception e) {
                        plugin.getLogger().atInfo().log("Could not register command class: " + loadedClass.getSimpleName());
                        e.printStackTrace();
                    }
                }
            }
            plugin.getLogger().atInfo().log("Registered " + count + " commands automatically.");
        } catch (Exception e) {
            plugin.getLogger().atInfo().log("Failed to load command classes.");
            e.printStackTrace();
        }
    }

    public static void registerEvents(AnglersAlmanac plugin) {
        final String PACKAGE_NAME = "dev.rm20.anglersalmanac.Events";
        try {
            ClassPath classPath = ClassPath.from(plugin.getClass().getClassLoader());
            int count = 0;

            for (ClassPath.ClassInfo classInfo : classPath.getTopLevelClassesRecursive(PACKAGE_NAME)) {
                Class<?> loadedClass = classInfo.load();

                if (loadedClass.isAnnotationPresent(EventInfo.class)) {
                    EventInfo info = loadedClass.getAnnotation(EventInfo.class);
                    Class eventType = info.value(); // Use raw Class here for easier interop

                    try {
                        // Use a raw cast to register the event without generic conflicts
                        plugin.getEventRegistry().registerGlobal(eventType, (event) -> {
                            try {
                                loadedClass.getMethod("handle", eventType).invoke(null, event);
                            } catch (Exception e) {
                                plugin.getLogger().atSevere()
                                        .withCause(e)
                                        .log("Failed to execute event handler in: " + loadedClass.getSimpleName());
                            }
                        });

                        plugin.getLogger().atInfo().log("Successfully registered event: " + loadedClass.getSimpleName());
                        count++;
                    } catch (Exception e) {
                        plugin.getLogger().atSevere().log("Could not find valid 'handle' method in: " + loadedClass.getSimpleName());
                        e.printStackTrace();
                    }
                }
            }
            plugin.getLogger().atInfo().log("Registered " + count + " events automatically.");
        } catch (Exception e) {
            plugin.getLogger().atSevere().log("Failed to load event classes.");
            e.printStackTrace();
        }
    }

}