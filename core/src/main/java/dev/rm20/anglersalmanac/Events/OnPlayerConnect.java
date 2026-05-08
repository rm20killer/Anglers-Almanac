package dev.rm20.anglersalmanac.Events;


import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AlmanacBook.AlmanacBook;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Registration.EventInfo;


@EventInfo(PlayerConnectEvent.class)
public class OnPlayerConnect {
    public static void handle(PlayerConnectEvent event) {
        //AnglersAlmanac.LOGGER.atInfo().log("Player joined");
        PlayerRef playerRef1 = event.getPlayerRef();
        AlmanacBook.sendTranslations(playerRef1);
        //event.getPlayer().getInventory().markChanged();
        //AlmanacBook.syncOwnBookOnJoin(playerRef1);
    }
}
