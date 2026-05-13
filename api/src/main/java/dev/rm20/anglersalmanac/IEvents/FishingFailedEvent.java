package dev.rm20.anglersalmanac.IEvents;

import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import dev.rm20.anglersalmanac.Models.FishLoot;

/**
 * Dispatched by the Minigame System (e.g., MinigameSystem_TensionBar) when a player
 * fails a fishing minigame.
 * <p>
 * This event provides access to the {@link FishLoot} that was lost, allowing for
 * custom failure logic, sympathetic messaging, or statistical tracking of missed catches.
 * </p>
 */
public class FishingFailedEvent implements IEvent<Void> {
    private final FishLoot lootMissed;
    private final Player player;

    /**
     * Constructs a new FishingFailedEvent.
     *
     * @param lootMissed The {@link FishLoot} object the player failed to reel in.
     * @param player     The {@link Player} who was participating in the minigame.
     */
    public FishingFailedEvent(FishLoot lootMissed, Player player) {
        this.lootMissed = lootMissed;
        this.player = player;
    }

    /**
     * Retrieves the full loot object for the item that was missed.
     *
     * @return The {@link FishLoot} the player failed to catch.
     */
    public FishLoot getMissedLoot() {
        return lootMissed;
    }

    /**
     * A shortcut to get the unique identifier of the item caught.
     *
     * @return The String ID of the caught item.
     */
    public String getLootID() {
        return lootMissed.getItemID();
    }

    /**
     * @return The Player who performed the catch.
     */
    public Player getPlayer() {
        return player;
    }
}