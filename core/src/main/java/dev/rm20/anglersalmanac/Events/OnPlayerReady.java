package dev.rm20.anglersalmanac.Events;


import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AlmanacBook.AlmanacBook;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Registration.EventInfo;


@EventInfo(PlayerReadyEvent.class)
public class OnPlayerReady {
    public static void handle(PlayerReadyEvent event) {
//        AnglersAlmanac.LOGGER.atInfo().log("Player joined");
//        Ref<EntityStore> playerRef =  event.getPlayerRef();
//        PlayerRef playerRef1 = playerRef.getStore().getComponent(playerRef, PlayerRef.getComponentType());
//        AlmanacBook.sendTranslations(playerRef1);
//        AlmanacBook.syncOwnBookOnJoin(playerRef1);
    }
}
