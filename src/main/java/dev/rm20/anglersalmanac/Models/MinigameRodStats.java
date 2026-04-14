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

@HytaleAsset(
        path = "AnglersAlmanacRodStats"
)
public class MinigameRodStats implements JsonAssetWithMap<String, DefaultAssetMap<String, MinigameRodStats>> {

    public String id;
    public AssetExtraInfo.Data data;
    public String name;
    public String description;
    public RodStats stats;
    public FishingModifier.Modifiers modifiers = new FishingModifier.Modifiers();

    public static final String KEY = "AA_MinigameRodStats";

    public static final AssetBuilderCodec<String, MinigameRodStats> CODEC = AssetBuilderCodec.builder(
            MinigameRodStats.class,
            MinigameRodStats::new,
            Codec.STRING,
            (t, id) -> t.id = id,
            t -> t.id,
            (t, data) -> t.data = data,
            t -> t.data
    )
        .appendInherited(new KeyedCodec<>("Name", Codec.STRING), (t, v) -> t.name = v, t -> t.name, (t, p) -> t.name = p.name).add()
        .appendInherited(new KeyedCodec<>("Description", Codec.STRING), (t, v) -> t.description = v, t -> t.description, (t, p) -> t.description = p.description).add()
        .appendInherited(new KeyedCodec<>("Stats", RodStats.CODEC), (t, v) -> t.stats = v, t -> t.stats, (t, p) -> t.stats = p.stats).add()
        .appendInherited(new KeyedCodec<>("Modifiers", FishingModifier.Modifiers.CODEC), (t, v) -> t.modifiers = v, t -> t.modifiers, (t, p) -> t.modifiers = p.modifiers).add()
    .build();

    public static final KeyedCodec<MinigameRodStats> KEYED_CODEC = new KeyedCodec<>(KEY, CODEC);

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
