package dev.rm20.anglersalmanac.Models;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.KeyedCodec;
import dev.rm20.anglersalmanac.Metadata.FishingModifier;
import dev.rm20.anglersalmanac.Registration.HytaleAsset;
import dev.rm20.anglersalmanac.Utils.AutoCodecBuilder;
import dev.rm20.anglersalmanac.Utils.Annotations.CodecAnnotations;

@HytaleAsset(
        path = "AnglersAlmanacBaitData"
)
public class FishBaitData implements JsonAssetWithMap<String, DefaultAssetMap<String, FishBaitData>> {

    public static final String KEY = "AA_FishBaitData";
    public static final AssetBuilderCodec<String, FishBaitData> CODEC;
    public static final KeyedCodec<FishBaitData> KEYED_CODEC;

    static {
        if (FishingModifier.CODEC == null) {
            FishingModifier.CODEC = AutoCodecBuilder.create(FishingModifier.class, FishingModifier::new);
            AutoCodecBuilder.register(FishingModifier.class, FishingModifier.CODEC);
        }
        if (FishingModifier.Modifiers.CODEC == null) {
            FishingModifier.Modifiers.CODEC = AutoCodecBuilder.create(FishingModifier.Modifiers.class, FishingModifier.Modifiers::new);
            AutoCodecBuilder.register(FishingModifier.Modifiers.class, FishingModifier.Modifiers.CODEC);
        }
        try {
            CODEC = AutoCodecBuilder.createAsset(
                    FishBaitData.class,
                    FishBaitData::new,
                    FishBaitData.class.getDeclaredField("id"),
                    FishBaitData.class.getDeclaredField("data")
            );
            KEYED_CODEC = new KeyedCodec<>(KEY, CODEC);
        } catch (NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public String id;
    public AssetExtraInfo.Data data;

    @CodecAnnotations.Field("ItemId")
    public String itemId;

    @CodecAnnotations.Field("Modifiers")
    public FishingModifier.Modifiers modifiers = new FishingModifier.Modifiers();

    private static AssetStore<String, FishBaitData, DefaultAssetMap<String, FishBaitData>> ASSET_STORE;

    public static AssetStore<String, FishBaitData, DefaultAssetMap<String, FishBaitData>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(FishBaitData.class);
        }
        return ASSET_STORE;
    }


    @Override
    public String getId() {
        return id;
    }

    public float getMultiplier(FishingModifier[] array, String targetId) {
        if (array == null || targetId == null) return -1f;
        for (FishingModifier mod : array) {
            if (targetId.equalsIgnoreCase(mod.targetId)) {
                return mod.chanceMultiplier;
            }
        }
        return -1f;
    }
}