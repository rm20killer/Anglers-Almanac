package dev.rm20.anglersalmanac.Utils;

import dev.rm20.anglersalmanac.Metadata.FishingContext;
import dev.rm20.anglersalmanac.Metadata.FishingModifier;
import dev.rm20.anglersalmanac.Models.FishLoot;
import dev.rm20.anglersalmanac.Models.FishLootManager;
import dev.rm20.anglersalmanac.Models.FishSize;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class FishSizeCalculator {

    private static final float SIGMA = 1.0f;

    public static Map<FishSize, Float> calculateWeights(FishingContext ctx, FishingModifier.Modifiers modifiers, FishLoot fish) {
        Map<FishSize, Float> weights = new EnumMap<>(FishSize.class);

        String itemId = fish != null ? fish.getItemID() : null;
        String fishId = null;
        if (fish instanceof FishLootManager flm) {
            fishId = flm.getId();
        } else if (itemId != null) {
            try {
                fishId = FishLootManager.getIdFromItemID(itemId);
            } catch (Throwable ignored) {
            }
        }
        String triggerKey = ctx != null ? ctx.triggerKey() : null;

        float centerSkew = 0.0f;
        if (modifiers != null) {
            centerSkew = modifiers.getTotalSizeSkew(itemId, fishId, triggerKey);
        }

        for (FishSize size : FishSize.values()) {
            float x = size.getCurvePosition();

            float diff = x - centerSkew;
            float bellWeight = (float) Math.exp(-(diff * diff) / (2.0f * SIGMA * SIGMA));

            float multiplier = 1.0f;
            if (modifiers != null) {
                multiplier *= modifiers.getSizeMultiplier(size);
                multiplier *= modifiers.getFishSizeMultiplier(itemId, fishId, size);
                multiplier *= modifiers.getTriggerVolumeSizeMultiplier(triggerKey, size);
            }

            float finalWeight = Math.max(0.0001f, bellWeight * Math.max(0.0f, multiplier));
            weights.put(size, finalWeight);
        }

        return weights;
    }


    public static Map<FishSize, Float> calculateProbabilities(FishingContext ctx, FishingModifier.Modifiers modifiers, FishLoot fish) {
        Map<FishSize, Float> weights = calculateWeights(ctx, modifiers, fish);
        float totalWeight = 0.0f;
        for (float w : weights.values()) {
            totalWeight += w;
        }

        Map<FishSize, Float> probabilities = new EnumMap<>(FishSize.class);
        if (totalWeight <= 0) {
            for (FishSize size : FishSize.values()) {
                probabilities.put(size, size == FishSize.M ? 1.0f : 0.0f);
            }
            return probabilities;
        }

        for (Map.Entry<FishSize, Float> entry : weights.entrySet()) {
            probabilities.put(entry.getKey(), entry.getValue() / totalWeight);
        }
        return probabilities;
    }

    /**
     * Rolls a random fish size
     */
    public static FishSize rollSize(FishingContext ctx, FishingModifier.Modifiers modifiers, FishLoot fish) {
        Map<FishSize, Float> weights = calculateWeights(ctx, modifiers, fish);
        if (fish.getItemID().toLowerCase().startsWith("fish_"))
        {
            float totalWeight = 0.0f;
            for (float w : weights.values()) {
                totalWeight += w;
            }

            if (totalWeight <= 0) {
                return FishSize.M;
            }

            float randomVal = ThreadLocalRandom.current().nextFloat() * totalWeight;
            float currentSum = 0.0f;

            for (FishSize size : FishSize.values()) {
                float w = weights.getOrDefault(size, 0.0f);
                currentSum += w;
                if (randomVal <= currentSum) {
                    return size;
                }
            }

            return FishSize.M;
        }
        return FishSize.M;

    }
}
