package dev.rm20.anglersalmanac.Utils.Intergration;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.common.semver.SemverRange;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AnglersAlmanac;

import java.lang.reflect.Method;
import java.util.Optional;

public class MMOSkillTree {
    private static final String MOD_NAME = "MMOSkillTree";
    private static final String GROUP_NAME = "Ziggfreed";
    private Method getLevelMethod;
    private Method getSkillComponentMethod;
    private Method getLuckChanceMethod;
    public boolean enabled;
    public MMOSkillTree() {
        try {
            PluginManager pm = PluginManager.get();
            PluginIdentifier targetId = new PluginIdentifier(GROUP_NAME, MOD_NAME);
            if (!pm.hasPlugin(targetId, SemverRange.WILDCARD)) {
                AnglersAlmanac.LOGGER.atInfo().log("MMOSkillTree not detected.");
                return;
            }
            Optional<Object> pluginOptional = Optional.ofNullable(pm.getPlugin(targetId));
            if (pluginOptional.isEmpty()) return;
            Object pluginInstance = pluginOptional.get();
            ClassLoader targetLoader = pluginInstance.getClass().getClassLoader();
            Class<?> apiClass = Class.forName("com.ziggfreed.mmoskilltree.api.MMOSkillTreeAPI", true, targetLoader);
            Class<?> serviceClass = Class.forName("com.ziggfreed.mmoskilltree.service.SkillTreeService", true, targetLoader);
            Class<?> skillComponentClass = Class.forName("com.ziggfreed.mmoskilltree.data.SkillComponent", true, targetLoader);

            this.getLevelMethod = apiClass.getMethod("getLevel", Store.class, Ref.class, String.class);
            this.getSkillComponentMethod = apiClass.getMethod("getSkillComponent", Store.class, Ref.class);
            this.getLuckChanceMethod = serviceClass.getMethod("getLuckChanceByName", skillComponentClass, String.class);
            this.enabled = true;
            AnglersAlmanac.LOGGER.atInfo().log("Successfully hooked into MMOSkillTree API.");

        } catch (ClassNotFoundException e) {
            AnglersAlmanac.LOGGER.atWarning().log("MMOSkillTree found, but API class was missing.");
        } catch (NoSuchMethodException e) {
            AnglersAlmanac.LOGGER.atWarning().log("MMOSkillTree API found, but getLevel method signature is different.");
        } catch (Throwable t) {
            AnglersAlmanac.LOGGER.atWarning().log("General failure loading MMOSkillTree integration: " + t.getMessage());
        }
    }

    public int getFishingLevel(Store<EntityStore> store, Ref<EntityStore> ref) {
        if (!enabled || getLevelMethod == null) return 0;
        try {
            Object result = getLevelMethod.invoke(null, store, ref, "FISHING");
            return (result instanceof Integer) ? (int) result : 0;
        } catch (Exception e) {
            AnglersAlmanac.LOGGER.atSevere().log("Failed to invoke getLevel: " + e.getMessage());
            return 0;
        }
    }

    public double getFishingLuck(Store<EntityStore> store, Ref<EntityStore> ref) {
        if (!enabled || getSkillComponentMethod == null || getLuckChanceMethod == null) return 0.0;

        try {
            Object skillComponent = getSkillComponentMethod.invoke(null, store, ref);
            if (skillComponent == null) return 0.0;

            Object luckResult = getLuckChanceMethod.invoke(null, skillComponent, "FISHING");
            return (luckResult instanceof Double) ? (double) luckResult : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }
}
