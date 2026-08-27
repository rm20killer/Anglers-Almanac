package dev.rm20.anglersalmanac.api;

import com.hypixel.hytale.server.core.util.Config;
import dev.rm20.anglersalmanac.Config.AnglersAlmanacConfig;
import dev.rm20.anglersalmanac.Config.MinigameConfig_TensionBar;
import dev.rm20.anglersalmanac.Metadata.BaitModifierResolver;
import dev.rm20.anglersalmanac.Metadata.RodModifierResolver;

public class AnglersAlmanacAPI {
    private static IAlmanacProvider AlmanacInstance;
    private static ILootProvider lootProvider;
    private static Config<AnglersAlmanacConfig> anglersConfig;
    private static Config<MinigameConfig_TensionBar> minigame_TensionBar_Config;
    private static BaitModifierResolver baitResolver = (name) -> null;
    private static RodModifierResolver rodResolver = (id) -> null;
    private static PageCacheInvalidator cacheInvalidator = uuid -> {};
    private static Runnable globalCacheInvalidator = () -> {};
    private static ICatchManager catchManager;
    public static IAlmanacProvider getAlmanac() {
        return AlmanacInstance;
    }

    public static void setImplementation(IAlmanacProvider provider) {
        AlmanacInstance = provider;
    }


    public static void setLootProvider(ILootProvider provider) {
        lootProvider = provider;
    }

    public static ILootProvider getLoot() {
        return lootProvider;
    }

    public static Config<AnglersAlmanacConfig> getConfig() {
        return anglersConfig;
    }
    public static void setConfig(Config<AnglersAlmanacConfig> config) {
        anglersConfig = config;
    }

    public static Config<MinigameConfig_TensionBar> getMinigameConfig() {
        return minigame_TensionBar_Config;
    }
    public static void setMinigameConfig(Config<MinigameConfig_TensionBar> minigameConfig) {
        minigame_TensionBar_Config = minigameConfig;
    }



    public static void setBaitResolver(BaitModifierResolver resolver) {
        baitResolver = resolver != null ? resolver : (name) -> null;
    }

    public static void setRodResolver(RodModifierResolver resolver) {
        rodResolver = resolver != null ? resolver : (id) -> null;
    }

    public static BaitModifierResolver getBaitResolver() {
        return baitResolver;
    }

    public static RodModifierResolver getRodResolver() {
        return rodResolver;
    }


    public static void setCacheInvalidator(PageCacheInvalidator invalidator, Runnable globalInvalidator) {
        cacheInvalidator = invalidator != null ? invalidator : uuid -> {};
        globalCacheInvalidator = globalInvalidator != null ? globalInvalidator : () -> {};
    }

    public static void invalidatePlayerCache(String playerUuid) {
        cacheInvalidator.invalidate(playerUuid);
    }

    public static void invalidateAllCaches() {
        globalCacheInvalidator.run();
    }

    public static ICatchManager getCatchManager() {
        if (catchManager == null) {
            throw new IllegalStateException("AnglersAlmanac API has not been initialized by core yet.");
        }
        return catchManager;
    }

    // Called internally by core during startup
    public static void setCatchManager(ICatchManager manager) {
        catchManager = manager;
    }

}