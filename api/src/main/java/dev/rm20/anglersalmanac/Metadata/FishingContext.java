package dev.rm20.anglersalmanac.Metadata;

import dev.rm20.anglersalmanac.Utils.Validator.TimePeriod;

public record FishingContext(
        TimePeriod time,
        int moonPhase,
        String zone,
        int tier,
        String region,
        String biome,
        double yPos,
        String weather,
        int waterDepth
) { }