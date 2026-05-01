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
        public float fishingPower = 0f;
        public FishingModifier[] biomeModifiers;
        public FishingModifier[] zoneModifiers;
        public FishingModifier[] regionModifiers;
        public FishingModifier[] familyModifiers;
        public FishingModifier[] itemModifiers;
        public float defaultMultiplier = 1.0f;

        public Modifiers() {
        }

        public static final BuilderCodec<Modifiers> CODEC = BuilderCodec.builder(Modifiers.class, Modifiers::new)
                .append(new KeyedCodec<>("Fishing_Power", Codec.FLOAT), (t, v) -> t.fishingPower = v, t -> t.fishingPower).add()
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
        java.util.Map<String, Float> biomeMap = new java.util.HashMap<>();
        java.util.Map<String, Float> zoneMap = new java.util.HashMap<>();
        java.util.Map<String, Float> regionMap = new java.util.HashMap<>();
        java.util.Map<String, Float> familyMap = new java.util.HashMap<>();
        java.util.Map<String, Float> itemMap = new java.util.HashMap<>();
        float combinedDefault = 1.0f;
        float combinedPower = 1.0f;
        for (FishingModifier.Modifiers mod : modifiers) {
            if (mod == null) continue;
            combinedPower+=mod.fishingPower;
            mergeIntoMap(biomeMap, mod.biomeModifiers);
            mergeIntoMap(zoneMap, mod.zoneModifiers);
            mergeIntoMap(regionMap, mod.regionModifiers);
            mergeIntoMap(familyMap, mod.familyModifiers);
            mergeIntoMap(itemMap, mod.itemModifiers);

            combinedDefault *= mod.defaultMultiplier;
        }
        master.fishingPower = combinedPower;
        master.biomeModifiers = mapToArray(biomeMap);
        master.zoneModifiers = mapToArray(zoneMap);
        master.regionModifiers = mapToArray(regionMap);
        master.familyModifiers = mapToArray(familyMap);
        master.itemModifiers = mapToArray(itemMap);
        master.defaultMultiplier = combinedDefault;

        return master;
    }

    private static void mergeIntoMap(java.util.Map<String, Float> map, FishingModifier[] mods) {
        if (mods == null) return;
        for (FishingModifier mod : mods) {
            if (mod == null || mod.targetId == null) continue;
            //if traget id matches then multiples the vaules
            map.merge(mod.targetId, mod.chanceMultiplier, (oldVal, newVal) -> oldVal * newVal);
        }
    }

    private static FishingModifier[] mapToArray(java.util.Map<String, Float> map) {
        return map.entrySet().stream().map(entry -> {
            FishingModifier fm = new FishingModifier();
            fm.targetId = entry.getKey();
            fm.chanceMultiplier = entry.getValue();
            return fm;
        }).toArray(FishingModifier[]::new);
    }
}