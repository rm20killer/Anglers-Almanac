package dev.rm20.anglersalmanac.Events;

import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.universe.Universe;
import dev.rm20.anglersalmanac.Models.BookAssetData;
import dev.rm20.anglersalmanac.Registration.EventInfo;
import dev.rm20.anglersalmanac.Models.FishLootManager;

@EventInfo(PlayerDisconnectEvent.class)
public class OnPlayerDisconnectEvent {
    public static void handle(PlayerDisconnectEvent event) {
        if(Universe.get().getPlayerCount()<=1)
        {
            //AnglersAlmanac.LOGGER.atInfo().log("Clearing book Cache");
            BookAssetData.invalidateCache();
            FishLootManager.invalidateCache();
        }

    }
}
