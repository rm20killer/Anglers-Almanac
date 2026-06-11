package dev.rm20.anglersalmanac.api;

import com.hypixel.hytale.server.core.util.Config;
import dev.rm20.anglersalmanac.Config.AnglersAlmanacConfig;
import dev.rm20.anglersalmanac.Config.MinigameConfig_TensionBar;

public class AnglersAlmanacAPI {
    private static IAlmanacProvider AlmanacInstance;
    private static ILootProvider lootProvider;
    private static Config<AnglersAlmanacConfig> anglersConfig;
    private static Config<MinigameConfig_TensionBar> minigame_TensionBar_Config;

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

}