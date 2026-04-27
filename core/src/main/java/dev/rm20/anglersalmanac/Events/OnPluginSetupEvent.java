package dev.rm20.anglersalmanac.Events;

import com.hypixel.hytale.server.core.event.events.BootEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.plugin.event.PluginSetupEvent;
import com.hypixel.hytale.server.core.universe.Universe;
import dev.rm20.anglersalmanac.AlmanacBook.AlmanacBook;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Models.BookAssetData;
import dev.rm20.anglersalmanac.Models.FishLootManager;
import dev.rm20.anglersalmanac.Registration.EventInfo;

@EventInfo(BootEvent.class)
public class OnPluginSetupEvent {

    public static void handle(BootEvent event) {
        AlmanacBook.reloadAllItem();

    }
}
