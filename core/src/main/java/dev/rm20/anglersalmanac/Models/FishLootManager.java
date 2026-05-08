package dev.rm20.anglersalmanac.Models;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Metadata.FishingContext;
import dev.rm20.anglersalmanac.Metadata.FishingModifier;
import dev.rm20.anglersalmanac.Registration.HytaleAsset;
import dev.rm20.anglersalmanac.Utils.Validator.GameIcon;
import dev.rm20.anglersalmanac.Utils.Validator.MinigameBehaviour;
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

    // Codecs
    public static final BuilderCodec<BookInfo> Book_CODEC = BuilderCodec.builder(BookInfo.class, BookInfo::new)
            .append(new KeyedCodec<>("Image_Path", Codec.STRING), (h, v) -> h.image_file = v, h -> h.image_file).add()
            .append(new KeyedCodec<>("Habitat_Info", Codec.STRING), (h, v) -> h.habitat_info = v, h -> h.habitat_info).add()
            .append(new KeyedCodec<>("Page_File_Ui", Codec.STRING), (h, v) -> h.PageFileUI = v, h -> h.PageFileUI).add()
            .build();

    public static final BuilderCodec<Quantity> Quantity_CODEC = BuilderCodec.builder(Quantity.class, Quantity::new)
            .documentation("Defines the range of items dropped. If Max is left at default or lower than Min, the amount will be exactly Min.")
            .append(new KeyedCodec<>("Min", Codec.INTEGER), (h, v) -> h.min_amount = v, h -> h.min_amount)
            .documentation("Setting min will set the least amount of items to give")
            .addValidator(Validators.min(1)).add()
            .append(new KeyedCodec<>("Max", Codec.INTEGER), (h, v) -> h.max_amount = v, h -> h.max_amount)
            .documentation("Enables random amount of items to give from min to max").add()
            .build();

    public static final BuilderCodec<Height> HEIGHT_CODEC = BuilderCodec.builder(Height.class, () -> new Height(0, -1))
            .documentation("Defines the vertical range (Y-level) where this fish can be caught.")
            .append(new KeyedCodec<>("Min_y", Codec.INTEGER), (h, v) -> h.min_y = v, h -> h.min_y)
            .documentation("The fish will not spawn below this height.")
            .addValidator(Validators.min(0)).add()
            .append(new KeyedCodec<>("Max_y", Codec.INTEGER), (h, v) -> h.max_y = v, h -> h.max_y)
            .documentation("The maximum Y-level requirement. Set to -1 to disable the upper limit (infinity).")
            .addValidator(Validators.min(-1)).add()
            .build();


//    public static final BuilderCodec<ExcludeHabitats> EXHABITATS_CODEC = BuilderCodec.builder(ExcludeHabitats.class, ExcludeHabitats::new)
//            .append(new KeyedCodec<>("Exclude_zones", new MapCodec<>(Codec.FLOAT, HashMap::new )),(h, map) -> h.exclude_zones = map, h -> h.exclude_zones).add()
//            .build();

    public static final BuilderCodec<Habitats> HABITATS_CODEC = BuilderCodec.builder(Habitats.class, Habitats::new)
            .documentation("Set what area and conditions to give the item.")
            .append(new KeyedCodec<>("Zones", new ArrayCodec<>(Codec.STRING, String[]::new)), (h, v) -> h.zones = v, h -> h.zones)
            .addValidator(Validators.uniqueInArray()).add()
            .append(new KeyedCodec<>("Tiers", new ArrayCodec<>(Codec.INTEGER, Integer[]::new)), (h, v) -> h.tier = v, h -> h.tier)
            .addValidator(Validators.uniqueInArray()).add()
            .append(new KeyedCodec<>("Regions", new ArrayCodec<>(Codec.STRING, String[]::new)), (h, v) -> h.regions = v, h -> h.regions)
            .addValidator(Validators.uniqueInArray()).add()
            .append(new KeyedCodec<>("Biomes", new ArrayCodec<>(Codec.STRING, String[]::new)), (h, v) -> h.biomes = v, h -> h.biomes)
            .addValidator(Validators.uniqueInArray()).add()
            .append(new KeyedCodec<>("Time_of_day", new ArrayCodec<>(new EnumCodec<>(TimePeriod.class), TimePeriod[]::new)), (h, v) -> h.time_of_day = v, h -> h.time_of_day)
            .addValidator(Validators.uniqueInArray()).add()
            .append(new KeyedCodec<>("Moon_phase", new ArrayCodec<>(Codec.INTEGER, Integer[]::new)), (h, v) -> h.moon_phase = v, h -> h.moon_phase)
            .documentation("If a value of -1 is selected then it be disabled")
            .addValidator(Validators.uniqueInArray()).add()
            .append(new KeyedCodec<>("Min_depth", Codec.INTEGER), (h, v) -> h.min_depth = v, h -> h.min_depth)
            .addValidator(Validators.min(0)).add()
            .append(new KeyedCodec<>("Height", HEIGHT_CODEC), (h, v) -> h.height = v, h -> h.height).add()
            .append(new KeyedCodec<>("Exclude_zones", new ArrayCodec<>(Codec.STRING, String[]::new)), (h, v) -> h.exclude_zones = v, h -> h.exclude_zones)
            .addValidator(Validators.uniqueInArray()).add()
            .append(new KeyedCodec<>("Exclude_tiers", new ArrayCodec<>(Codec.INTEGER, Integer[]::new)), (h, v) -> h.exclude_tiers = v, h -> h.exclude_tiers)
            .addValidator(Validators.uniqueInArray()).add()
            .append(new KeyedCodec<>("Exclude_biomes", new ArrayCodec<>(Codec.STRING, String[]::new)), (h, v) -> h.exclude_biomes = v, h -> h.exclude_biomes)
            .addValidator(Validators.uniqueInArray()).add()
            .append(new KeyedCodec<>("Exclude_regions", new ArrayCodec<>(Codec.STRING, String[]::new)), (h, v) -> h.exclude_regions = v, h -> h.exclude_regions)
            .addValidator(Validators.uniqueInArray()).add()
            .append(new KeyedCodec<>("Weight_multiplier", Codec.FLOAT), (h, v) -> h.weight_multiplier = v, h -> h.weight_multiplier)
            .add()
            .append(new KeyedCodec<>("Required_Bait", new ArrayCodec<>(Codec.STRING, String[]::new)), (h, v) -> h.required_bait = v, h -> h.required_bait)
            .documentation("List of Bait ID that can get the loot")
            .addValidator(Validators.uniqueInArray()).add()
            .append(new KeyedCodec<>("Required_Power", Codec.INTEGER), (s, v) -> s.required_power = v, s -> s.required_power)
            .addValidator(Validators.min(0)).add()
            //.append(new KeyedCodec<>("Exclude_Habitats", EXHABITATS_CODEC), (h, v) -> h.excludeHabitats = v, h -> h.excludeHabitats).add()
            .build();

    public static final BuilderCodec<MinigameStats> STATS_CODEC = BuilderCodec.builder(MinigameStats.class, MinigameStats::new)
            .append(new KeyedCodec<>("Difficulty", Codec.INTEGER), (s, v) -> s.difficulty = v, s -> s.difficulty)
            .addValidator(Validators.nonNull()).add()
            .append(new KeyedCodec<>("Behavior", new EnumCodec<>(MinigameBehaviour.class)), (s, v) -> s.behavior = v, s -> s.behavior)
            .addValidator(Validators.nonNull()).add()
            .append(new KeyedCodec<>("Stamina", Codec.INTEGER), (s, v) -> s.stamina = v, s -> s.stamina)
            .addValidator(Validators.nonNull()).add()
            .append(new KeyedCodec<>("GameIcon", new EnumCodec<>(GameIcon.class)), (s, v) -> s.gameIcon = v, s -> s.gameIcon).add()
            .build();


    public static final AssetBuilderCodec<String, FishLootManager> CODEC = AssetBuilderCodec.builder(
                    FishLootManager.class,
                    FishLootManager::new,
                    Codec.STRING,
                    (t, id) -> t.id = id,
                    t -> t.id,
                    (t, data) -> t.data = data,
                    t -> t.data
            )
            .appendInherited(new KeyedCodec<>("Category",  new ArrayCodec<>(Codec.STRING, String[]::new)), (t, v) -> t.category = v, t -> t.category, (t, p) -> t.category = p.category)
            .add()
            .appendInherited(new KeyedCodec<>("ItemId", Codec.STRING), (t, v) -> t.itemID = v, t -> t.itemID, (t, p) -> t.itemID = p.itemID)
            .add()
            .appendInherited(new KeyedCodec<>("EntityId", Codec.STRING), (t, v) -> t.entityID = v, t -> t.entityID, (t, p) -> t.entityID = p.entityID)
            .add()
            .appendInherited(new KeyedCodec<>("Name", Codec.STRING), (t, v) -> t.name = v, t -> t.name, (t, p) -> t.name = p.name)
            .addValidator(Validators.nonEmptyString()).add()
            .appendInherited(new KeyedCodec<>("Description", Codec.STRING), (t, v) -> t.description = v, t -> t.description, (t, p) -> t.description = p.description).add()
            .appendInherited(new KeyedCodec<>("FamilyId", Codec.STRING), (t, v) -> t.familyId = v, t -> t.familyId, (t, p) -> t.familyId = p.familyId).add()
            .appendInherited(new KeyedCodec<>("Rarity", Codec.STRING), (t, v) -> t.rarity = v, t -> t.rarity, (t, p) -> t.rarity = p.rarity)
            .addValidator(Validators.nonEmptyString()).add()
            .appendInherited(new KeyedCodec<>("Weight", Codec.INTEGER), (t, v) -> t.weight = v, t -> t.weight, (t, p) -> t.weight = p.weight)
            .addValidator(Validators.min(0)).add()
            .appendInherited(new KeyedCodec<>("Size", Codec.INTEGER), (t, v) -> t.size = v, t -> t.size, (t, p) -> t.size = p.size)
            .addValidator(Validators.min(0)).add()
            .appendInherited(new KeyedCodec<>("Quantity", Quantity_CODEC), (t, v) -> t.quantity = (FishLoot.Quantity) v, t -> t.quantity, (t, p) -> t.quantity = p.quantity).add()
            .appendInherited(new KeyedCodec<>("IsGlobal", Codec.BOOLEAN), (t, v) -> t.isGlobal = v, t -> t.isGlobal, (t, p) -> t.isGlobal = p.isGlobal).add()
            .appendInherited(new KeyedCodec<>("Habitats", HABITATS_CODEC), (t, v) -> t.habitats = v, t -> t.habitats, (t, p) -> t.habitats = p.habitats).add()
            .appendInherited(new KeyedCodec<>("Minigame_stats", STATS_CODEC), (t, v) -> t.minigameStats = v, t -> t.minigameStats, (t, p) -> t.minigameStats = p.minigameStats).add()
            .appendInherited(new KeyedCodec<>("Book_info", Book_CODEC), (t, v) -> t.bookInfo = v, t -> t.bookInfo, (t, p) -> t.bookInfo = p.bookInfo).add()
            .build();

    private static AssetStore<String, FishLootManager, DefaultAssetMap<String, FishLootManager>> ASSET_STORE;

    public static AssetStore<String, FishLootManager, DefaultAssetMap<String, FishLootManager>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(FishLootManager.class);
        }
        return ASSET_STORE;
    }

    // Fields

    private String id;
    private AssetExtraInfo.Data data;

    private int size;

    public FishLootManager() {
    }

    @Override
    public String getId() {
        return id;
    }


    // Classes used by BuilderCodec


    public static class ExcludeHabitats {
        Map<String, Float> exclude_zones = new HashMap<>();
    }


    // Reward logic
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
                        //AnglersAlmanac.LOGGER.atInfo().log(loot.getId() + ":" + weight);
                    }
                    if (loot.habitats != null && loot.habitats.required_power > fishingPower) {
                        weight = 0;
                    }
                    if (fishingPower > 1.0f) {
                        int rarityTier = getRarityWeight(loot.getId());
                        if(rarityTier == 99)
                        {
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

        return getAllLoot().stream().filter(loot -> loot.id.equalsIgnoreCase(id)).findFirst().orElse(null);
    }

    private static boolean isEligible(FishLootManager loot, GeoKey key) {
        if(!AnglersAlmanac.MOD_CONFIG.get().getShouldHabCheck()) return true;
        Habitats hab = loot.getHabitats();
        if (hab == null) {
            //AnglersAlmanac.LOGGER.atInfo().log(loot.getName() + " is has no habitat info");
            return true;
        }


        //Check exclude_biomes and checks if weight is 0 incase
        if (containsIgnoreCase(hab.exclude_biomes, key.biome()) && hab.weight_multiplier == 0) {
            return false;
        }

        if (containsIgnoreCase(hab.exclude_regions, key.region()) && hab.weight_multiplier == 0) {
            return false;
        }

        if (containsIgnoreCase(hab.exclude_zones, key.zone()) && hab.weight_multiplier == 0) {
            return false;
        }

        if (loot.isGlobal()) {
            //AnglersAlmanac.LOGGER.atInfo().log(loot.getName() + " is global fish");
            return true;
        }

        boolean hasRequirement = false;
        boolean matchedAny = false;
        if (hab.biomes != null && hab.biomes.length > 0) {
            hasRequirement = true;
            for (String b : hab.biomes) {
                if (b.equalsIgnoreCase(key.biome())) {
                    //AnglersAlmanac.LOGGER.atInfo().log(loot.getName() + "found at biome: "+ key.biome());
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