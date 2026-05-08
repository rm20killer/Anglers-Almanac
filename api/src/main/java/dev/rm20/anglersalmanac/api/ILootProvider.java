package dev.rm20.anglersalmanac.api;

import dev.rm20.anglersalmanac.Metadata.FishingContext;
import dev.rm20.anglersalmanac.Models.FishLoot;

import javax.annotation.Nullable;
import java.util.Collection;

public interface ILootProvider {
    static FishLoot getFishData(String id) {
        return null;
    }

    static Collection<? extends FishLoot> getAllLoot() {
        return null;
    }

    FishLoot getRandomFish(FishingContext ctx, @Nullable Object modifiers);
}