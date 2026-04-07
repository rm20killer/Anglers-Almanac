package dev.rm20.anglersalmanac.Metadata;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;

public class FishingModifier {
    public String targetId;
    public float chanceMultiplier = 1.0f;

    public FishingModifier() {
    }

    public static final BuilderCodec<FishingModifier> CODEC = BuilderCodec.builder(FishingModifier.class, FishingModifier::new)
            .append(new KeyedCodec<>("TargetId", Codec.STRING), (t, v) -> t.targetId = v, t -> t.targetId).add()
            .append(new KeyedCodec<>("Multiplier", Codec.FLOAT), (t, v) -> t.chanceMultiplier = v, t -> t.chanceMultiplier).add()
            .build();

    public static class Modifiers {
        public FishingModifier[] biomeModifiers;
        public FishingModifier[] zoneModifiers;
        public FishingModifier[] regionModifiers;
        public FishingModifier[] familyModifiers;
        public FishingModifier[] itemModifiers;
        public float defaultMultiplier = 1.0f;

        public Modifiers() {
        }

        public static final BuilderCodec<Modifiers> CODEC = BuilderCodec.builder(Modifiers.class, Modifiers::new)
                .append(new KeyedCodec<>("Biomes", new ArrayCodec<>(FishingModifier.CODEC, FishingModifier[]::new)), (t, v) -> t.biomeModifiers = v, t -> t.biomeModifiers).add()
                .append(new KeyedCodec<>("Zones", new ArrayCodec<>(FishingModifier.CODEC, FishingModifier[]::new)), (t, v) -> t.zoneModifiers = v, t -> t.zoneModifiers).add()
                .append(new KeyedCodec<>("Regions", new ArrayCodec<>(FishingModifier.CODEC, FishingModifier[]::new)), (t, v) -> t.regionModifiers = v, t -> t.regionModifiers).add()
                .append(new KeyedCodec<>("Families", new ArrayCodec<>(FishingModifier.CODEC, FishingModifier[]::new)), (t, v) -> t.familyModifiers = v, t -> t.familyModifiers).add()
                .append(new KeyedCodec<>("Items", new ArrayCodec<>(FishingModifier.CODEC, FishingModifier[]::new)), (t, v) -> t.itemModifiers = v, t -> t.itemModifiers).add()
                .append(new KeyedCodec<>("DefaultMultiplier", Codec.FLOAT), (t, v) -> t.defaultMultiplier = v, t -> t.defaultMultiplier).add()
                .build();
    }


    public static Modifiers mergeModifiers(FishingModifier.Modifiers... modifiers) {
        FishingModifier.Modifiers master = new FishingModifier.Modifiers();

        java.util.List<FishingModifier> biomes = new java.util.ArrayList<>();
        java.util.List<FishingModifier> zones = new java.util.ArrayList<>();
        java.util.List<FishingModifier> regions = new java.util.ArrayList<>();
        java.util.List<FishingModifier> families = new java.util.ArrayList<>();
        java.util.List<FishingModifier> items = new java.util.ArrayList<>();
        float combinedDefault = 1.0f;

        for (FishingModifier.Modifiers mod : modifiers) {
            if (mod == null) continue;
            if (mod.biomeModifiers != null) java.util.Collections.addAll(biomes, mod.biomeModifiers);
            if (mod.zoneModifiers != null) java.util.Collections.addAll(zones, mod.zoneModifiers);
            if (mod.regionModifiers != null) java.util.Collections.addAll(regions, mod.regionModifiers);
            if (mod.familyModifiers != null) java.util.Collections.addAll(families, mod.familyModifiers);
            if (mod.itemModifiers != null) java.util.Collections.addAll(items, mod.itemModifiers);
            combinedDefault *= mod.defaultMultiplier;
        }

        master.biomeModifiers = biomes.toArray(new FishingModifier[0]);
        master.zoneModifiers = zones.toArray(new FishingModifier[0]);
        master.regionModifiers = regions.toArray(new FishingModifier[0]);
        master.familyModifiers = families.toArray(new FishingModifier[0]);
        master.itemModifiers = items.toArray(new FishingModifier[0]);
        master.defaultMultiplier = combinedDefault;

        return master;
    }
}