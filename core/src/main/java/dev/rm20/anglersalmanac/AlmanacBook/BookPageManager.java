package dev.rm20.anglersalmanac.AlmanacBook;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.PageManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AlmanacBook.Pages.FishDataUiPage;
import dev.rm20.anglersalmanac.AlmanacBook.Pages.FishZoneUiPage;
import dev.rm20.anglersalmanac.AlmanacBook.Pages.GlossaryPage;
import dev.rm20.anglersalmanac.AlmanacBook.Pages.StatUiPage;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Models.BookAssetData;
import dev.rm20.anglersalmanac.Models.FishLootManager;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class BookPageManager {

    private static final Cache<String, AlmanacDatabase.PlayerStatsData> statsCache = Caffeine.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .maximumSize(128)
            .build();

    public static AlmanacDatabase.PlayerStatsData getStats(String playerUUID)
    {
        AlmanacDatabase.PlayerStatsData cachedStats = statsCache.getIfPresent(playerUUID);
        if(cachedStats !=null)
        {
            return cachedStats;
        }
        else{
            AlmanacDatabase.PlayerStatsData stats =  AnglersAlmanac.getInstance().database.getPlayerStats(playerUUID);
            statsCache.put(playerUUID, stats);
            return stats;
        }
    }
    public static void invalidateCache(String playerUUID)
    {
        statsCache.invalidate(playerUUID);
    }
    public static void invalidateCache()
    {
        statsCache.invalidateAll();
    }

    public static void OpenPage(Player player, int page, String playerUUID, String playerName) {
        AlmanacDatabase.PlayerStatsData cachedStats = statsCache.getIfPresent(playerUUID);
        if(cachedStats !=null)
        {
            renderPage(player, page, playerUUID, playerName, cachedStats);
        }
        else
        {
            AlmanacDatabase.PlayerStatsData stats = AnglersAlmanac.getInstance().database.getPlayerStats(playerUUID);
            statsCache.put(playerUUID, stats);
            renderPage(player, page, playerUUID, playerName, stats);

            // Move out of main thread
//            CompletableFuture.supplyAsync(() -> {
//                return AnglersAlmanac.getInstance().database.getPlayerStats(playerUUID);
//            }).thenAccept(stats -> {
//                statsCache.put(playerUUID, stats);
//                renderPage(player, page, playerUUID, playerName, stats);
//            }).exceptionally(ex -> {
//                ex.printStackTrace();
//                return null;
//            });
        }
        AlmanacDatabase db = AnglersAlmanac.getInstance().database;
        AlmanacDatabase.PlayerStatsData stats = db.getPlayerStats(playerUUID);
    }

    private static void renderPage(Player player, int page, String playerUUID, String playerName, AlmanacDatabase.PlayerStatsData stats)
    {
        Ref<EntityStore> playerRef = player.getReference();
        PlayerRef playerRef1 = playerRef.getStore().getComponent(playerRef, PlayerRef.getComponentType());
        BookAssetData bookAsset = BookAssetData.getMasterMergedBook();

        if (bookAsset == null) return;
        PageManager pageManager = player.getPageManager();
        List<BookAssetData.SpreadTemplate> pages = bookAsset.getFlattenedPages();
        BookAssetData.SpreadTemplate currentSpread = pages.get(page);
        String UiFile = currentSpread.uiFile;
        //AnglersAlmanac.LOGGER.atInfo().log(pages.get(page).uiFile);
        if (UiFile.equalsIgnoreCase("Almanac/AlmanacStats.ui")) {
            StatUiPage statUiPage = new StatUiPage(playerRef1, playerUUID, playerName, stats);
            pageManager.openCustomPage(playerRef, playerRef.getStore(), statUiPage);
        } else if (currentSpread.isDoublePage) {
            //TODO: 1 context 2 pages
        } else if (UiFile.equalsIgnoreCase("Almanac/Fish/AlmanacFishZone.ui")) {
            FishLootManager FishDataRight = null;
            if (currentSpread.RightPage != null && !currentSpread.RightPage.isEmpty()) {
                FishDataRight = FishLootManager.getFishData(currentSpread.RightPage);
            }
            FishZoneUiPage fishZoneUiPage = new FishZoneUiPage(playerRef1, playerUUID, playerName, stats, currentSpread.LeftPage, FishDataRight, page,getCurrentZoneInfo(bookAsset.getHabitats(),page));
            pageManager.openCustomPage(playerRef, playerRef.getStore(), fishZoneUiPage);
        } else if (UiFile.startsWith("Almanac/Fish/AlmanacFish")) {
            FishLootManager FishDataLeft = null;
            if (currentSpread.LeftPage != null && !currentSpread.LeftPage.isEmpty()) {
                FishDataLeft = FishLootManager.getFishData(currentSpread.LeftPage);
            }

            FishLootManager FishDataRight = null;
            if (currentSpread.RightPage != null && !currentSpread.RightPage.isEmpty()) {
                FishDataRight = FishLootManager.getFishData(currentSpread.RightPage);
            }

            String uiFile = pages.get(page).uiFile;
            FishDataUiPage fishDataUiPage = new FishDataUiPage(playerRef1, playerUUID, playerName, stats, FishDataLeft, FishDataRight, page, uiFile);
            pageManager.openCustomPage(playerRef, playerRef.getStore(), fishDataUiPage);
        } else if (UiFile.startsWith("Almanac/Fish/AlamanacGlossary"))
        {
            GlossaryPage glossaryPage = new GlossaryPage(playerRef1, playerUUID, playerName, page);
            pageManager.openCustomPage(playerRef, playerRef.getStore(), glossaryPage);
        } else {
            AnglersAlmanac.LOGGER.atSevere().log("Error getting UI with page: "+page+" @ "+UiFile);
        }
    }

    public static int getNextPage(int currentPage) {
        var assetMap = BookAssetData.getAssetStore().getAssetMap().getAssetMap();
        if (assetMap.isEmpty()) return currentPage;
        BookAssetData bookAsset = BookAssetData.getMasterMergedBook();
        List<BookAssetData.SpreadTemplate> pages = bookAsset.getFlattenedPages();
        if (currentPage + 1 < pages.size()) {
            return currentPage + 1;
        }
        return currentPage;
    }


    public static BookAssetData.ZoneInfo getCurrentZoneInfo(BookAssetData.habitatsInfo[] allHabitats, int page) {
        int TotalPageIndex = 0;

        for (BookAssetData.habitatsInfo habitat : allHabitats) {
            int habitatPageCount = habitat.pages.length;
            if (page >= TotalPageIndex && page < (TotalPageIndex + habitatPageCount)) {
                return habitat.zoneInfo;
            }
            TotalPageIndex += habitatPageCount;
        }

        return null;
    }

    public static int getPageIndexForZone(String zoneName) {
        BookAssetData bookAsset = BookAssetData.getMasterMergedBook();
        int pageIndex = 0;
        for (BookAssetData.habitatsInfo habitat : bookAsset.getHabitats()) {
            if (habitat.ZoneName.equalsIgnoreCase(zoneName)) {
                return pageIndex;
            }
            pageIndex += habitat.pages.length;
        }
        return 0;
    }

    public static int getPageIndexForFish(String fishId) {
        BookAssetData bookAsset = BookAssetData.getMasterMergedBook();
        List<BookAssetData.SpreadTemplate> pages = bookAsset.getFlattenedPages();
        for (int i = 0; i < pages.size(); i++) {
            BookAssetData.SpreadTemplate spread = pages.get(i);
            if (fishId.equals(spread.LeftPage) || fishId.equals(spread.RightPage)) {
                return i;
            }
        }
        return -1;
    }



}
