package dev.rm20.anglersalmanac.IEvents;

import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import dev.rm20.anglersalmanac.Models.FishLoot;

public class FishingFailedEvent implements IEvent<Void> {
    private final FishLoot lootMissed;
    private final Player player;

    public FishingFailedEvent(FishLoot lootMissed, Player player) {
        this.lootMissed = lootMissed;
        this.player = player;
    }

    public FishLoot getMissedLoot() {
        return lootMissed;
    }

    public String getLootID() {
        return lootMissed.getItemID();
    }

    public Player getPlayer() {
        return player;
    }
}