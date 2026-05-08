package dev.rm20.anglersalmanac.api;

import dev.rm20.anglersalmanac.Metadata.MinigamePRating;

import java.util.Map;

public interface IAlmanacProvider {

    PlayerStatsData getPlayerStats(String uuid);

    boolean hasPlayerCaught(String playerUUID, String fishId);

    Map<String, Integer> getAllFishCounts(String playerUUID);

    record FishEntry(String name, int count) {}

    class PlayerStatsData {
        public int totalCatches = 0;
        public int legendaryCount = 0;
        public java.util.List<FishEntry> topFish = new java.util.ArrayList<>();
        public java.util.Map<String, Integer> ratingsMap = new java.util.HashMap<>();
        public java.util.Map<String, Integer> catchMap = new java.util.HashMap<>();

        public boolean hasCaught(String fishId) { return catchMap.containsKey(fishId); }
        public int getRatingCount(MinigamePRating.PerformanceRating rating) { return ratingsMap.getOrDefault(rating.name(), 0); }
        public int getFishCount(String fishId) { return catchMap.getOrDefault(fishId, 0); }
    }
}
