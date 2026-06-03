package dev.rm20.anglersalmanac.Models;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.Color;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Registration.HytaleAsset;
import dev.rm20.anglersalmanac.Utils.ColourUtils;
import dev.rm20.codecannotation.AutoCodecBuilder;
import dev.rm20.codecannotation.Annotations.CodecAnnotations;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@HytaleAsset(
        path = "AnglersAlmanacBook"
)
public class BookAssetData implements JsonAssetWithMap<String, DefaultAssetMap<String, BookAssetData>> {

    public static final BuilderCodec<ZoneInfo> ZONE_INFO_CODEC;
    public static final BuilderCodec<SpreadTemplate> SPREAD_CODEC;
    public static final BuilderCodec<habitatsInfo> HABITAT_INFO_CODEC;
    public static final AssetBuilderCodec<String, BookAssetData> CODEC;

    static {
        ZONE_INFO_CODEC = AutoCodecBuilder.create(ZoneInfo.class, ZoneInfo::new);
        AutoCodecBuilder.register(ZoneInfo.class, ZONE_INFO_CODEC);

        SPREAD_CODEC = AutoCodecBuilder.create(SpreadTemplate.class, SpreadTemplate::new);
        AutoCodecBuilder.register(SpreadTemplate.class, SPREAD_CODEC);

        HABITAT_INFO_CODEC = AutoCodecBuilder.create(habitatsInfo.class, habitatsInfo::new);
        AutoCodecBuilder.register(habitatsInfo.class, HABITAT_INFO_CODEC);

        try {
            CODEC = AutoCodecBuilder.createAsset(
                    BookAssetData.class,
                    BookAssetData::new,
                    BookAssetData.class.getDeclaredField("id"),
                    BookAssetData.class.getDeclaredField("data")
            );
        } catch (NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static class habitatsInfo {
        @CodecAnnotations.Field("ZoneName")
        public String ZoneName;
        @CodecAnnotations.Field("ZoneInfo")
        public ZoneInfo zoneInfo;
        @CodecAnnotations.Field("Spread")
        public SpreadTemplate[] pages;
    }

    public static class SpreadTemplate {
        @CodecAnnotations.Field("UiFile")
        public String uiFile;
        @CodecAnnotations.Field("IsDoublePage")
        public boolean isDoublePage;
        @CodecAnnotations.Field("LeftPage")
        public String LeftPage;
        @CodecAnnotations.Field("RightPage")
        public String RightPage;
    }

    public static class ZoneInfo {
        @CodecAnnotations.Field("DisplayName")
        public String displayName;
        @CodecAnnotations.Field("ZoneDescription")
        public String zoneDescription;
        @CodecAnnotations.Field("ZoneImage")
        @CodecAnnotations.CustomValidator(type = "png", value = "UI/Custom/Almanac/Fish/Assets")
        public String ZoneImage;
        @CodecAnnotations.Field("ProgressBarColour")
        public Color ProgressBarColour;
        @CodecAnnotations.Field("TabIcon")
        @CodecAnnotations.CustomValidator(type = "png", value = "UI/Custom/Almanac/Utils/Assets/Tabs")
        public String tabIcon;
        @CodecAnnotations.Field("TabColour")
        public Color tabColour;
    }

    private String id;
    private AssetExtraInfo.Data data;

    @CodecAnnotations.Field("Habitats")
    private habitatsInfo[] habitats;

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

        List<BookAssetData> allBooks = new ArrayList<>(getAssetStore().getAssetMap().getAssetMap().values());
        allBooks.sort((a, b) -> {
            if ("Vanilla".equals(a.id)) return -1;
            if ("Vanilla".equals(b.id)) return 1;
            return 0;
        });

        allBooks.forEach(book -> {
            if (book.getHabitats() == null) return;
            for (habitatsInfo habitat : book.getHabitats()) {
                String key = habitat.ZoneName.toLowerCase();
                if (!mergedMap.containsKey(key)) {
                    habitatsInfo copy = new habitatsInfo();
                    copy.ZoneName = habitat.ZoneName;
                    copy.zoneInfo = habitat.zoneInfo;
                    copy.pages = mergePages(new SpreadTemplate[0], habitat.pages);
                    mergedMap.put(key, copy);
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
