package dev.rm20.anglersalmanac.Metadata;

import javax.annotation.Nonnull;

public record ZoneInfo(
        String zone,
        int tier,
        String descriptor) {
    @Override @Nonnull
    public String toString() {
        return String.format("Zone: %s | Tier: %d | Type: %s", zone, tier, descriptor);
    }
}
