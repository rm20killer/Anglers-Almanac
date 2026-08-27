package dev.rm20.anglersalmanac.Metadata;

@FunctionalInterface
public interface RodModifierResolver {
    FishingModifier.Modifiers getModifiers(String rodItemId);
}


