package dev.rm20.anglersalmanac.api;

@FunctionalInterface
public interface PageCacheInvalidator {
    void invalidate(String playerUuid);
}
