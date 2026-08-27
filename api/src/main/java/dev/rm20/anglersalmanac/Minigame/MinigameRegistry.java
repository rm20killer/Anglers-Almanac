package dev.rm20.anglersalmanac.Minigame;

import com.hypixel.hytale.logger.HytaleLogger;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MinigameRegistry {
    private static final Map<String, FishingMinigameHandler> HANDLERS = new ConcurrentHashMap<>();

    /**
     * Registers a new minigame handler with a unique identifier.
     * @param id the id
     * @param handler the handler
     */
    public static void register(String id, FishingMinigameHandler handler) {
        if (HANDLERS.containsKey(id)) {
            HytaleLogger.forEnclosingClass().atWarning().log("Overwriting minigame handler for key: " + id);
        }
        HANDLERS.put(id, handler);
        HytaleLogger.forEnclosingClass().atInfo().log("Registered minigame: " + id);
    }

    /**
     * Returns the fishing minigame based on id.
     *
     * @param id the id
     * @return the fishing minigame.
     */
    public static Optional<FishingMinigameHandler> get(String id) {
        return Optional.ofNullable(HANDLERS.get(id));
    }

    /**
     * Checks if an id has a minigame hanlder attached to it
     *
     * @param id the id
     * @return boolean
     */
    public static boolean contains(String id) {
        return HANDLERS.containsKey(id);
    }

    /**
     * Returns an unmodifiable set of all registered minigame IDs.
     *
     * @return set of registered IDs
     */
    public static Set<String> getIds() {
        return Collections.unmodifiableSet(HANDLERS.keySet());
    }

}