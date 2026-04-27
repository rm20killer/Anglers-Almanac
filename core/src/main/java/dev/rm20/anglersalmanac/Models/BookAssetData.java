package dev.rm20.anglersalmanac.Models;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Registration.HytaleAsset;
import dev.rm20.anglersalmanac.Utils.ColourUtils;
import dev.rm20.anglersalmanac.Utils.Validator.CustomAssetValidator;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@HytaleAsset(
        path = "AnglersAlmanacBook"
)
public class BookAssetData implements JsonAssetWithMap<String, DefaultAssetMap<String, BookAssetData>> {

    public static final BuilderCodec<ZoneInfo> ZONE_INFO_CODEC = BuilderCodec.builder(ZoneInfo.class, ZoneInfo::new)
            .append(new KeyedCodec<>("DisplayName", Codec.STRING), (z, v) -> z.displayName = v, z -> z.displayName).add()
            .append(new KeyedCodec<>("ZoneDescription", Codec.STRING), (z, v) -> z.zoneDescription = v, z -> z.zoneDescription).add()
            .append(new KeyedCodec<>("ZoneImage", Codec.STRING), (z, v) -> z.ZoneImage = v, z -> z.ZoneImage)
            .addValidator(CustomAssetValidator.UI_ZONE_VALIDATOR).add()
            .append(new KeyedCodec<>("ProgressBarColour", ProtocolCodecs.COLOR), (z, v) -> z.ProgressBarColour = v, z -> z.ProgressBarColour).add()
            .append(new KeyedCodec<>("TabIcon", Codec.STRING), (z, v) -> z.tabIcon = v, z -> z.tabIcon)
            .addValidator(CustomAssetValidator.UI_TAB_VALIDATOR).add()
            .append(new KeyedCodec<>("TabColour", ProtocolCodecs.COLOR), (z, v) -> z.tabColour = v, z -> z.tabColour).add()
            .build();

    public static final BuilderCodec<SpreadTemplate> SPREAD_CODEC = BuilderCodec.builder(SpreadTemplate.class, SpreadTemplate::new)
            .append(new KeyedCodec<>("UiFile", Codec.STRING), (s, v) -> s.uiFile = v, s -> s.uiFile).add()
            .append(new KeyedCodec<>("IsDoublePage", Codec.BOOLEAN), (s, v) -> s.isDoublePage = v, s -> s.isDoublePage).add()
            .append(new KeyedCodec<>("LeftPage", Codec.STRING), (s, v) -> s.LeftPage = v, s -> s.LeftPage).add()
            .append(new KeyedCodec<>("RightPage", Codec.STRING), (s, v) -> s.RightPage = v, s -> s.RightPage).add()
            .build();

    public static final BuilderCodec<habitatsInfo> HABITAT_INFO_CODEC = BuilderCodec.builder(habitatsInfo.class, habitatsInfo::new)
            .append(new KeyedCodec<>("ZoneName", Codec.STRING), (h, v) -> h.ZoneName = v, h -> h.ZoneName).add()
            .append(new KeyedCodec<>("ZoneInfo", ZONE_INFO_CODEC), (c, v) -> c.zoneInfo = v, c -> c.zoneInfo).add()
            .append(new KeyedCodec<>("Spread", new ArrayCodec<>(SPREAD_CODEC, SpreadTemplate[]::new)), (h, v) -> h.pages = v, h -> h.pages).add()
            .build();


    public static final AssetBuilderCodec<String, BookAssetData> CODEC = AssetBuilderCodec.builder(
                    BookAssetData.class,
                    BookAssetData::new,
                    Codec.STRING,
                    (t, id) -> t.id = id,
                    t -> t.id,
                    (t, data) -> t.data = data,
                    t -> t.data
            )
            .appendInherited(new KeyedCodec<>("Habitats", new ArrayCodec<>(HABITAT_INFO_CODEC, habitatsInfo[]::new)),
                    (t, v) -> t.habitats = v,
                    t -> t.habitats,
                    (t, p) -> t.habitats = p.habitats).add()
            .build();

    // Asset Store

    private static AssetStore<String, BookAssetData, DefaultAssetMap<String, BookAssetData>> ASSET_STORE;

    public static AssetStore<String, BookAssetData, DefaultAssetMap<String, BookAssetData>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(BookAssetData.class);
        }
        return ASSET_STORE;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Fields
    private String id;
    private AssetExtraInfo.Data data;
    private habitatsInfo[] habitats;

    public static class habitatsInfo {
        public String ZoneName;
        public ZoneInfo zoneInfo;
        public SpreadTemplate[] pages;
    }

    public static class SpreadTemplate {
        public String uiFile;
        public boolean isDoublePage;
        public String LeftPage;
        public String RightPage;
    }

    public static class ZoneInfo {
        public String displayName;
        public String zoneDescription;
        public String ZoneImage;
        public Color ProgressBarColour;
        public String tabIcon;
        public Color tabColour;
    }

    public BookAssetData() {
    }

    @Override
    public String getId() {
        return id;
    }

    public habitatsInfo[] getHabitats() {
        return habitats;
    }

    public List<SpreadTemplate> getFlattenedPages() {
        if (habitats == null) return List.of();

        return Arrays.stream(habitats)
                .filter(habitat -> habitat.pages != null)
                .flatMap(habitat -> Arrays.stream(habitat.pages))
                .toList();
    }


    public record FishEntry(String id, boolean isItem) {
    }

    //HabitatCache
    private static final Cache<String, Map<String, List<FishEntry>>> habitatCache = Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();

    public List<FishEntry> getFishByHabitat(String habitatName) {
        Map<String, List<FishEntry>> cache = habitatCache.get("all_habitats", k -> buildCache());
        if (cache == null) return List.of();
        return cache.getOrDefault(habitatName.toLowerCase(), List.of());
    }

    public List<FishEntry> getAllFish() {
        Map<String, List<FishEntry>> cache = habitatCache.get("all_habitats", k -> buildCache());
        if (cache == null) return List.of();
        return cache.values().stream()
                .flatMap(List::stream)
                .toList();
    }

    private Map<String, List<FishEntry>> buildCache() {
        if (habitats == null) return Collections.emptyMap();
        Map<String, List<FishEntry>> newCache = new LinkedHashMap<>();
        Set<String> fishUiFiles = Set.of(
                "Almanac/Fish/AlmanacFish.ui",
                "Almanac/Fish/AlmanacFishZone.ui"
        );
        for (habitatsInfo habitat : habitats) {
            if (habitat == null || habitat.ZoneName == null) continue;

            List<FishEntry> fishList = Arrays.stream(habitat.pages != null ? habitat.pages : new SpreadTemplate[0])
                    .filter(Objects::nonNull)
                    .filter(spread -> fishUiFiles.contains(spread.uiFile))
                    .flatMap(spread -> Stream.of(spread.LeftPage, spread.RightPage))
                    .filter(id -> id != null && !id.isEmpty())
                    .distinct()
                    .map(id -> new FishEntry(id, FishLootManager.getFishData(id) != null))
                    .collect(Collectors.toList());

            newCache.put(habitat.ZoneName.toLowerCase(), Collections.unmodifiableList(fishList));
        }
        return Collections.unmodifiableMap(newCache);
    }


    public record HabitatProgress(int caught, int total) {
        public float getPercentage() {
            return total == 0 ? 0 : (float) caught / total;
        }
    }

    public Map<String, HabitatProgress> getAllHabitatProgress(String playerUUID) {
        Map<String, List<FishEntry>> cache = habitatCache.get("all_habitats", k -> buildCache());
        if (cache == null) return Collections.emptyMap();

        Map<String, HabitatProgress> globalProgress = new HashMap<>();
        var database = AnglersAlmanac.getInstance().database;

        cache.forEach((zoneName, fishList) -> {
            List<String> validItemIds = fishList.stream()
                    .filter(FishEntry::isItem)
                    .map(FishEntry::id)
                    .toList();

            if (validItemIds.isEmpty()) {
                globalProgress.put(zoneName, new HabitatProgress(0, 0));
                return;
            }
            long caughtCount = validItemIds.stream()
                    .filter(id -> database.hasPlayerCaught(playerUUID, id))
                    .count();

            globalProgress.put(zoneName, new HabitatProgress((int) caughtCount, validItemIds.size()));
        });
        return globalProgress;
    }

    private static final Cache<String, BookAssetData> MasterMergeCache = Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();
    private static final String MASTER_KEY = "master_almanac_merged";

    public static BookAssetData getMasterMergedBook() {
        return MasterMergeCache.get(MASTER_KEY, k -> buildMasterMergedBook());
    }

    public static void invalidateCache() {
        MasterMergeCache.invalidateAll();
        habitatCache.invalidateAll();
    }


    private static BookAssetData buildMasterMergedBook() {
        BookAssetData master = new BookAssetData();
        master.id = "master_almanac_merged";
        Map<String, habitatsInfo> mergedMap = new LinkedHashMap<>();

        getAssetStore().getAssetMap().getAssetMap().values().forEach(book -> {
            if (book.getHabitats() == null) return;
            for (habitatsInfo habitat : book.getHabitats()) {
                String key = habitat.ZoneName.toLowerCase();
                if (!mergedMap.containsKey(key)) {
                    habitat.pages = mergePages(new SpreadTemplate[0], habitat.pages);
                    mergedMap.put(key, habitat);
                } else {
                    habitatsInfo existing = mergedMap.get(key);
                    existing.pages = mergePages(existing.pages, habitat.pages);
                }
            }
        });

        master.habitats = mergedMap.values().stream()
                .sorted(Comparator.comparingInt(h -> getZoneRank(h.ZoneName)))
                .toArray(habitatsInfo[]::new);

        master.buildCache();
        //AnglersAlmanac.LOGGER.atInfo().log("Built book with "+ master.habitats.length+ " Habitats");
        return master;
    }

    private static SpreadTemplate[] mergePages(SpreadTemplate[] existing, SpreadTemplate[] incoming) {
        SpreadTemplate[] Existing = (existing == null) ? new SpreadTemplate[0] : existing;
        SpreadTemplate[] Incoming = (incoming == null) ? new SpreadTemplate[0] : incoming;
        Set<String> uniqueFishIds = new LinkedHashSet<>();
        List<SpreadTemplate> specialSpreads = new ArrayList<>();
        String standardFishUi = "Almanac/Fish/AlmanacFish.ui";
        String statsUi = "Almanac/AlmanacStats.ui";

        Stream.concat(Arrays.stream(Existing), Arrays.stream(Incoming)).forEach(spread -> {
            if (spread == null) return;
            if (!standardFishUi.equals(spread.uiFile)) {
                boolean alreadyExists = specialSpreads.stream().anyMatch(s -> s.uiFile.equals(spread.uiFile));
                if (!alreadyExists) {
                    if (statsUi.equals(spread.uiFile)) specialSpreads.add(0, spread);
                    else specialSpreads.add(spread);
                }
                return;
            }
            if (spread.LeftPage != null && !spread.LeftPage.isEmpty()) uniqueFishIds.add(spread.LeftPage);
            if (spread.RightPage != null && !spread.RightPage.isEmpty()) uniqueFishIds.add(spread.RightPage);
        });

        List<String> sortedFish = uniqueFishIds.stream()
                .sorted(Comparator.comparingInt(FishLootManager::getRarityWeight))
                .toList();

        List<SpreadTemplate> result = new ArrayList<>(specialSpreads);
        for (int i = 0; i < sortedFish.size(); i += 2) {
            SpreadTemplate s = new SpreadTemplate();
            s.uiFile = standardFishUi;
            s.isDoublePage = false;
            s.LeftPage = sortedFish.get(i);
            s.RightPage = (i + 1 < sortedFish.size()) ? sortedFish.get(i + 1) : "";
            result.add(s);
        }

        return result.toArray(new SpreadTemplate[0]);
    }


    public record BookTab(
            String zoneName,
            String icon,
            String colour,
            int startPage,
            boolean isToTheLeft,
            boolean isActive
    ) {
    }

    public List<BookTab> getTabsForCurrentPage(int currentPageIndex) {
        List<BookTab> tabs = new ArrayList<>();
        int pageCounter = 0;

        for (habitatsInfo habitat : habitats) {
            if (habitat.pages == null || habitat.pages.length == 0) continue;
            int habitatStartPage = pageCounter;
            boolean isActive = (currentPageIndex == habitatStartPage);
            boolean isToTheLeft = currentPageIndex > habitatStartPage;
            tabs.add(new BookTab(
                    habitat.ZoneName,
                    habitat.zoneInfo != null && habitat.zoneInfo.tabIcon != null ? habitat.zoneInfo.tabIcon : "",
                    habitat.zoneInfo != null && habitat.zoneInfo.tabColour != null ? ColourUtils.toHex(habitat.zoneInfo.tabColour) : "#ffffff",
                    habitatStartPage,
                    isToTheLeft,
                    isActive
            ));
            pageCounter += habitat.pages.length;
        }
        return tabs;
    }

    public static int getZoneRank(String name) {
        return switch (name.toLowerCase()) {
            case "almanacstats" -> 0;
            case "global" -> 1;
            case "ocean" -> 2;
            case "alamanacglossary" -> 99;
            default -> 98;
        };
    }


}
