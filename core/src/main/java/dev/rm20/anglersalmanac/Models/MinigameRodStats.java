package dev.rm20.anglersalmanac.Models;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import dev.rm20.anglersalmanac.Metadata.FishingModifier;
import dev.rm20.anglersalmanac.Metadata.RodStats;
import dev.rm20.anglersalmanac.Registration.HytaleAsset;
import dev.rm20.anglersalmanac.Utils.AutoCodecBuilder;
import dev.rm20.anglersalmanac.Utils.Annotations.CodecAnnotations;

@HytaleAsset(
        path = "AnglersAlmanacRodStats"
)
public class MinigameRodStats implements JsonAssetWithMap<String, DefaultAssetMap<String, MinigameRodStats>> {

    public static final String KEY = "AA_MinigameRodStats";

    public static final AssetBuilderCodec<String, MinigameRodStats> CODEC;
    public static final KeyedCodec<MinigameRodStats> KEYED_CODEC;

    static {
        if (FishingModifier.CODEC == null) {
            FishingModifier.CODEC = AutoCodecBuilder.create(FishingModifier.class, FishingModifier::new);
            AutoCodecBuilder.register(FishingModifier.class, FishingModifier.CODEC);
        }
        if (FishingModifier.Modifiers.CODEC == null) {
            FishingModifier.Modifiers.CODEC = AutoCodecBuilder.create(FishingModifier.Modifiers.class, FishingModifier.Modifiers::new);
            AutoCodecBuilder.register(FishingModifier.Modifiers.class, FishingModifier.Modifiers.CODEC);
        }

        AutoCodecBuilder.register(RodStats.class, RodStats.CODEC);

        try {
            CODEC = AutoCodecBuilder.createAsset(
                    MinigameRodStats.class,
                    MinigameRodStats::new,
                    MinigameRodStats.class.getDeclaredField("id"),
                    MinigameRodStats.class.getDeclaredField("data")
            );
            KEYED_CODEC = new KeyedCodec<>(KEY, CODEC);
        } catch (NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public String id;
    public AssetExtraInfo.Data data;

    @CodecAnnotations.Field("Name") public String name;
    @CodecAnnotations.Field("Description") public String description;
    @CodecAnnotations.Field("Stats") public RodStats stats;
    @CodecAnnotations.Field("Modifiers") public FishingModifier.Modifiers modifiers = new FishingModifier.Modifiers();

    private static AssetStore<String, MinigameRodStats, DefaultAssetMap<String, MinigameRodStats>> ASSET_STORE;

    public static AssetStore<String, MinigameRodStats, DefaultAssetMap<String, MinigameRodStats>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(MinigameRodStats.class);
        }
        return ASSET_STORE;
    }

    @Override
    public String getId() {
        return id;
    }

    public static RodStats getRodStatsFromRodId(String rodId){
        RodStats stats;
        stats = getAssetStore().getAssetMap().getAsset(rodId).stats;
        assert stats != null;
        return stats;
    }

    public static FishingModifier.Modifiers getModifiersFromRodId(String rodId){
        FishingModifier.Modifiers modifiers;
        modifiers = getAssetStore().getAssetMap().getAsset(rodId).modifiers;
        if(modifiers ==null)
        {
            modifiers = new FishingModifier.Modifiers();
        }
        return modifiers;
    }
}