package dev.rm20.anglersalmanac.IEvents;

import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import dev.rm20.anglersalmanac.Models.FishLoot;

/**
 * Dispatched by the Minigame Manager when a player successfully completes
 * the fishing minigame.
 * <p>
 * This event contains data regarding the specific catch, the player's
 * performance score, and whether the catch represents a new discovery.
 * </p>
 */
public record LootCaughtEvent(FishLoot loot, boolean newDiscovery, boolean isLegendary, Player player,
                              float performance) implements IEvent<Void> {


    /**
     * @return The full loot manager object containing comprehensive catch details.
     */
    @Override
    public FishLoot loot() {
        return loot;
    }

    /**
     * A shortcut to get the unique identifier of the item caught.
     *
     * @return The String ID of the caught item.
     */
    public String getLootID() {
        return loot.getItemID();
    }

    /**
     * @return The Player who performed the catch.
     */
    @Override
    public Player player() {
        return player;
    }

    /**
     * @return True if this is the first time the player has caught this specific loot.
     */
    @Override
    public boolean newDiscovery() {
        return newDiscovery;
    }

    /**
     * @return True if the caught item has the Legendary flag enabled.
     */
    @Override
    public boolean isLegendary() {
        return isLegendary;
    }

    /**
     * @return The raw precision score (0-100) of the minigame completion.
     */
    @Override
    public float performance() {
        return performance;
    }

    /**
     * Returns a human-readable string rating based on the performance score.
     * <p>
     * <b>Thresholds:</b>
     * <ul>
     *   <li><b>95+:</b> "perfect"</li>
     *   <li><b>80 - 94:</b> "great"</li>
     *   <li><b>40 - 79:</b> "good"</li>
     *   <li><b>-1:</b> "nil"</li>
     *   <li><b>Other:</b> "fail"</li>
     * </ul>
     * </p>
     *
     * @return A string rating: "perfect", "great", "good", "nil", or "fail".
     */
    public String getPRating() {
        if (performance >= 95) {
            return "perfect";
        }

        if (performance >= 80) {
            return "great";
        }

        if (performance >= 40) {
            return "good";
        }

        if (performance == -1) {
            return "nil";
        }

        return "fail";
    }
}


