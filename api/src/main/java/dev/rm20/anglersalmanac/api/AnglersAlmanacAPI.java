package dev.rm20.anglersalmanac.api;

public class AnglersAlmanacAPI {
    private static IAlmanacProvider AlmanacInstance;
    private static ILootProvider lootProvider;

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
}