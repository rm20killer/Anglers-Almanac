package dev.rm20.anglersalmanac.Registration;

import com.google.common.reflect.ClassPath;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.JsonAsset;
import com.hypixel.hytale.assetstore.codec.AssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import dev.rm20.anglersalmanac.AnglersAlmanac;

import java.lang.reflect.Field;

public class AssetRegisterManager {

    private static final String ASSET_PACKAGE = "dev.rm20.anglersalmanac.Models";

    public static void registerAll(AnglersAlmanac plugin) {
        try {
            ClassPath classPath = ClassPath.from(plugin.getClass().getClassLoader());
            int count = 0;
            for (ClassPath.ClassInfo classInfo : classPath.getTopLevelClassesRecursive(ASSET_PACKAGE)) {
                Class<?> clazz = classInfo.load();
                if (clazz.isAnnotationPresent(HytaleAsset.class)) {
                    HytaleAsset meta = clazz.getAnnotation(HytaleAsset.class);

                    if (JsonAsset.class.isAssignableFrom(clazz)) {
                        registerSingleStore((Class) clazz, meta.path());
                        count++;
                    }
                }
            }
            plugin.getLogger().atInfo().log("Auto-registered " + count + " Hytale Asset Stores.");
        } catch (Exception e) {
            plugin.getLogger().atSevere().log("Failed to scan assets", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends JsonAssetWithMap<String, DefaultAssetMap<String, T>> & JsonAsset<String>> void registerSingleStore(Class<T> clazz, String path) {
        try {
            Field codecField = clazz.getDeclaredField("CODEC");
            Codec<T> codec = (Codec<T>) codecField.get(null);
            AssetRegistry.register(
                    HytaleAssetStore.builder(clazz, new DefaultAssetMap<String, T>())
                            .setPath(path)
                            .setCodec((AssetCodec<String, T>) codec)
                            .setKeyFunction(T::getId)
                            .build()
            );

        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("Could not register " + clazz.getSimpleName() + ": Missing static CODEC field.");
        }
    }
}