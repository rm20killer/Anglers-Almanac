package dev.rm20.anglersalmanac.Minigame;

import com.hypixel.hytale.logger.HytaleLogger;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class MinigameRegistry {
    private static final Map<String, FishingMinigameHandler> HANDLERS = new ConcurrentHashMap<>();

    /**
     * Registers a new minigame handler with a unique identifier.
     */
    public static void register(String id, FishingMinigameHandler handler) {
        if (HANDLERS.containsKey(id)) {
            HytaleLogger.forEnclosingClass().atWarning().log("Overwriting minigame handler for key: " + id);
        }
        HANDLERS.put(id, handler);
        HytaleLogger.forEnclosingClass().atInfo().log("Registered minigame: " + id);
    }

    public static Optional<FishingMinigameHandler> get(String id) {
        return Optional.ofNullable(HANDLERS.get(id));
    }

    public static boolean contains(String id) {
        return HANDLERS.containsKey(id);
    }
}