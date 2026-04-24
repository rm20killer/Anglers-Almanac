package dev.rm20.anglersalmanac.IEvents;

import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import dev.rm20.anglersalmanac.Models.FishLootManager;

public class LootCaughtEvent implements IEvent<Void> {
    private final FishLootManager loot;
    private final Player player;
    private final boolean newDiscovery;
    private final boolean isLegendary;
    private final float performance;

    public LootCaughtEvent(FishLootManager lootId, boolean newDiscovery, boolean isLegendary, Player player, int performance) {
        this.loot = lootId;
        this.player = player;
        this.newDiscovery = newDiscovery;
        this.isLegendary = isLegendary;
        this.performance = performance;
    }

    public FishLootManager getLoot() {
        return loot;
    }

    public String getLootID() {
        return loot.getItemID();
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isNewDiscovery() {
        return newDiscovery;
    }

    public boolean isLegendary() {
        return isLegendary;
    }

    public float getPerformance() {
        return performance;
    }

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


