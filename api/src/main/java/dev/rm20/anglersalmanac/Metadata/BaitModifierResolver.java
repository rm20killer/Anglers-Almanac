package dev.rm20.anglersalmanac.Metadata;

@FunctionalInterface
public interface BaitModifierResolver {
    FishingModifier.Modifiers getModifiers(String baitName);
}
