package dev.rm20.anglersalmanac.Events;

import com.hypixel.hytale.assetstore.event.AssetStoreMonitorEvent;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Models.BookAssetData;
import dev.rm20.anglersalmanac.Registration.EventInfo;
import dev.rm20.anglersalmanac.Models.FishLootManager;


@EventInfo(AssetStoreMonitorEvent.class)
public class OnAssetMonitorEvent {
    public static void handle(AssetStoreMonitorEvent event) {
        AnglersAlmanac.LOGGER.atInfo().log("Clearing Cache");
        BookAssetData.invalidateCache();
        FishLootManager.invalidateCache();

    }
}
