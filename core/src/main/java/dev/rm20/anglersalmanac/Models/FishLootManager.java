package dev.rm20.anglersalmanac.Models;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Metadata.FishingContext;
import dev.rm20.anglersalmanac.Metadata.FishingModifier;
import dev.rm20.anglersalmanac.Registration.HytaleAsset;
import dev.rm20.anglersalmanac.Utils.AutoCodecBuilder;
import dev.rm20.anglersalmanac.Utils.Annotations.CodecAnnotations;
import dev.rm20.anglersalmanac.Utils.Validator.TimePeriod;
import dev.rm20.anglersalmanac.api.ILootProvider;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@HytaleAsset(
        path = "AnglersAlmanac"
)
public class FishLootManager extends FishLoot implements JsonAssetWithMap<String, DefaultAssetMap<String, FishLootManager>>, ILootProvider {

    public static final BuilderCodec<BookInfo> Book_CODEC;
    public static final BuilderCodec<Quantity> Quantity_CODEC;
    public static final BuilderCodec<Height> HEIGHT_CODEC;
    public static final BuilderCodec<MinigameStats> STATS_CODEC;
    public static final BuilderCodec<Habitats> HABITATS_CODEC;
    public static final AssetBuilderCodec<String, FishLootManager> CODEC;

    static {
        Book_CODEC = AutoCodecBuilder.create(BookInfo.class, BookInfo::new);
        AutoCodecBuilder.register(BookInfo.class, Book_CODEC);

        Quantity_CODEC = AutoCodecBuilder.create(Quantity.class, Quantity::new);
        AutoCodecBuilder.register(Quantity.class, Quantity_CODEC);

        HEIGHT_CODEC = AutoCodecBuilder.create(Height.class, () -> new Height(0, -1));
        AutoCodecBuilder.register(Height.class, HEIGHT_CODEC);

        STATS_CODEC = AutoCodecBuilder.create(MinigameStats.class, MinigameStats::new);
        AutoCodecBuilder.register(MinigameStats.class, STATS_CODEC);

        HABITATS_CODEC = AutoCodecBuilder.create(Habitats.class, Habitats::new);
        AutoCodecBuilder.register(Habitats.class, HABITATS_CODEC);

        try {
            CODEC = AutoCodecBuilder.createAsset(
                    FishLootManager.class,
                    FishLootManager::new,
                    FishLootManager.class.getDeclaredField("id"),
                    FishLootManager.class.getDeclaredField("data")
            );
        } catch (NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static AssetStore<String, FishLootManager, DefaultAssetMap<String, FishLootManager>> ASSET_STORE;

    public static AssetStore<String, FishLootManager, DefaultAssetMap<String, FishLootManager>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(FishLootManager.class);
        }
        return ASSET_STORE;
    }

    private String id;
    private AssetExtraInfo.Data data;

    @CodecAnnotations.Field("Size")
    private int size;

    public FishLootManager() {
    }

    @Override
    public String getId() {
        return id;
    }

    public static class ExcludeHabitats {
        Map<String, Float> exclude_zones = new HashMap<>();
    }


    public static Collection<FishLootManager> getAllLoot() {
        return getAssetStore().getAssetMap().getAssetMap().values();
    }


    //Cache system
    public record GeoKey(String biome, String region, String zone, int tier) {
    }

    private static final LoadingCache<GeoKey, List<FishLootManager>> geoLootCache = Caffeine.newBuilder()
            .expireAfterAccess(15, TimeUnit.MINUTES)
            .softValues()
            .build(key -> getAllLoot().stream().filter(loot -> isEligible(loot, key)).toList());

    public static void invalidateCache() {
        geoLootCache.invalidateAll();
    }

    @Override
    public FishLoot getRandomFish(FishingContext ctx, @Nullable Object modifiers) {
        return getRandomWeightedLoot(ctx, (FishingModifier.Modifiers) modifiers , 1);
    }

    public FishLoot getRandomFish(FishingContext ctx, @Nullable Object modifiers,float fishingPower) {
        return getRandomWeightedLoot(ctx, (FishingModifier.Modifiers) modifiers , fishingPower);
    }

    public static FishLootManager getRandomWeightedLoot(FishingContext ctx, @Nullable FishingModifier.Modifiers modifiers) {
        return getRandomWeightedLoot(ctx,modifiers,1);
    }

    public static FishLootManager getRandomWeightedLoot(FishingContext ctx, @Nullable FishingModifier.Modifiers modifiers, float fishingPower) {
        GeoKey key = new GeoKey(ctx.biome(), ctx.region(), ctx.zone(), ctx.tier());
        List<FishLootManager> geoPossible = geoLootCache.get(key);

        List<FishLootManager> possibleLoot = new ArrayList<>();
        Map<FishLootManager, Float> calculatedWeights = new HashMap<>();
        float totalWeight = 0f;
        if (modifiers != null) {
            fishingPower = fishingPower + modifiers.fishingPower;
        }
        for (FishLootManager loot : Objects.requireNonNull(geoPossible)) {
            if (checkEnvironment(loot, ctx)) {
                float weight = (float) loot.getExclusionWeight(loot, ctx);
                if (weight > 0) {
                    if (modifiers != null) {
                        weight *= calculateFinalMultiplier(loot, ctx, modifiers);
                    }
                    if (loot.getHabitats() != null && loot.getHabitats().required_power > fishingPower) {
                        weight = 0;
                    }
                    if (fishingPower > 1.0f) {
                        int rarityTier = getRarityWeight(loot.getId());
                        if(rarityTier == 99) {
                            rarityTier = 0;
                        }
                        float powerBonus = (fishingPower - 1.0f) * 0.5f * rarityTier;
                        weight += powerBonus;
                    }

                    if (weight > 0) {
                        possibleLoot.add(loot);
                        calculatedWeights.put(loot, weight);
                        totalWeight += weight;
                    }
                }
            }
        }

        if (possibleLoot.isEmpty()) return null;
        float randomIndex = ThreadLocalRandom.current().nextFloat() * totalWeight;
        float currentSum = 0f;
        for (FishLootManager loot : possibleLoot) {
            currentSum += calculatedWeights.get(loot);
            if (randomIndex < currentSum) return loot;
        }
        return possibleLoot.getFirst();
    }

    public static List<FishLootManager> getFishInArea(FishingContext ctx) {
        GeoKey key = new GeoKey(ctx.biome(), ctx.region(), ctx.zone(), ctx.tier());
        return geoLootCache.get(key);
    }

    public static FishLootManager getFishData(String id) {
        if (id == null) return null;
        return getAllLoot().stream().filter(loot -> loot.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
    }

    private static boolean isEligible(FishLootManager loot, GeoKey key) {
        if(!AnglersAlmanac.MOD_CONFIG.get().getShouldHabCheck()) return true;
        Habitats hab = loot.getHabitats();
        if (hab == null) return true;

        if (containsIgnoreCase(hab.exclude_biomes, key.biome()) && hab.weight_multiplier == 0) return false;
        if (containsIgnoreCase(hab.exclude_regions, key.region()) && hab.weight_multiplier == 0) return false;
        if (containsIgnoreCase(hab.exclude_zones, key.zone()) && hab.weight_multiplier == 0) return false;

        if (loot.isGlobal()) return true;

        boolean hasRequirement = false;
        boolean matchedAny = false;
        if (hab.biomes != null && hab.biomes.length > 0) {
            hasRequirement = true;
            for (String b : hab.biomes) {
                if (b.equalsIgnoreCase(key.biome())) {
                    matchedAny = true;
                    break;
                }
            }
        }

        if (!matchedAny && hab.regions != null && hab.regions.length > 0) {
            hasRequirement = true;
            for (String r : hab.regions) {
                if (r.equalsIgnoreCase(key.region())) {
                    //AnglersAlmanac.LOGGER.atInfo().log(loot.getName() + "found at region: "+ key.region());
                    matchedAny = true;
                    break;
                }
            }
        }

        if (!matchedAny && hab.zones != null && hab.zones.length > 0) {
            hasRequirement = true;
            for (String z : hab.zones) {
                if (z.equalsIgnoreCase(key.zone())) {
                    // If zone matches, still respect the tier requirement if it exists
                    if (hab.tier == null || hab.tier.length == 0 || Arrays.stream(hab.tier).anyMatch(t -> t == key.tier())) {
                        //AnglersAlmanac.LOGGER.atInfo().log(loot.getName() + "found at zone and tier: "+ key.zone() + " : "+ key.tier());
                        matchedAny = true;
                    }
                    break;
                }
            }
        }
        return !hasRequirement || matchedAny;
    }

    private static boolean checkEnvironment(FishLootManager loot, FishingContext ctx) {
        if(AnglersAlmanac.MOD_CONFIG.get().getShouldEnvironmentCheck()) return true;
        Habitats hab = loot.getHabitats();
        if (hab == null) return true;


        // Time of day
        if (hab.time_of_day != null && hab.time_of_day.length > 0) {
            boolean match = Arrays.stream(hab.time_of_day).anyMatch(t -> t != null && (t.equals(TimePeriod.ANY) || t.equals(ctx.time())));
            if (!match) return false;
        }
        // Weather TODO: READD To CODEC
        if (hab.required_weather != null && hab.required_weather.length > 0) {
            boolean match = Arrays.stream(hab.required_weather).anyMatch(w -> w != null && (w.equalsIgnoreCase("any") || w.equalsIgnoreCase(ctx.weather())));
            if (!match) return false;
        }

        // Moon phase
        if (hab.moon_phase != null && hab.moon_phase.length > 0) {
            boolean match = Arrays.stream(hab.moon_phase)
                    .anyMatch(p -> p == -1 || p == ctx.moonPhase());

            if (!match) return false;
        }
        // Water
        if (ctx.waterDepth() < hab.min_depth) return false;
        // Y level
        if (hab.height != null) {
            if (ctx.yPos() < hab.height.min_y) return false;
            return hab.height.max_y == -1 || !(ctx.yPos() > hab.height.max_y);
        }

        return true;
    }

    public int getExclusionWeight(FishLootManager loot, FishingContext ctx) {
        if (loot.habitats == null) return this.weight;

        boolean isExcluded = containsIgnoreCase(loot.habitats.exclude_biomes, ctx.biome()) ||
                containsIgnoreCase(loot.habitats.exclude_regions, ctx.region()) ||
                containsIgnoreCase(loot.habitats.exclude_zones, ctx.zone()) ||
                Arrays.asList(loot.habitats.exclude_tiers).contains(ctx.tier()) ||
                containsIgnoreCase(loot.habitats.required_bait, ctx.baitAsset());

        if (isExcluded) {
            return Math.round(this.weight * loot.habitats.weight_multiplier);
        }
        return this.weight;
    }

    private static boolean containsIgnoreCase(String[] array, String value) {
        if (array == null || value == null) return false;
        for (String s : array) {
            if (value.equalsIgnoreCase(s)) return true;
        }
        return false;
    }

    private static float calculateFinalMultiplier(FishLootManager loot, FishingContext ctx, FishingModifier.Modifiers m) {
        float val;
        if ((val = getVal(m.itemModifiers, loot.getItemID())) != -1f) return val;
        if ((val = getVal(m.familyModifiers, loot.getFamilyId())) != -1f) return val;
        if ((val = getVal(m.biomeModifiers, ctx.biome())) != -1f) return val;
        if ((val = getVal(m.zoneModifiers, ctx.zone())) != -1f) return val;

        return m.defaultMultiplier;
    }

    private static float getVal(FishingModifier[] array, String id) {
        if (array == null || id == null) return -1f;
        for (FishingModifier mod : array) {
            if (id.equalsIgnoreCase(mod.targetId)) return mod.chanceMultiplier;
        }
        return -1f;
    }

    public static int getRarityWeight(String fishId) {
        var data = FishLootManager.getFishData(fishId);
        if (data == null) return 99;

        return switch (data.getRarity().toLowerCase()) {
            case "junk" -> -1;
            case "common" -> 0;
            case "uncommon" -> 1;
            case "rare" -> 2;
            case "epic" -> 3;
            case "legendary" -> 4;
            default -> 99; // Default weight for unknown rarities
        };
    }
}