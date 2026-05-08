package dev.rm20.anglersalmanac.Metadata;

public record ZoneInfo(
        String zone,
        int tier,
        String descriptor) {
    @Override
    public String toString() {
        return String.format("Zone: %s | Tier: %d | Type: %s", zone, tier, descriptor);
    }
}
