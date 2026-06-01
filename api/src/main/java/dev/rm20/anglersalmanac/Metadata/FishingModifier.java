package dev.rm20.anglersalmanac.Metadata;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import dev.rm20.anglersalmanac.Utils.AutoCodecBuilder;
import dev.rm20.anglersalmanac.Utils.Annotations.CodecAnnotations;

public class FishingModifier {

    public static BuilderCodec<FishingModifier> CODEC;

    @CodecAnnotations.Field("TargetId")
    public String targetId;

    @CodecAnnotations.Field("Multiplier")
    public float chanceMultiplier = 1.0f;

    public FishingModifier() {
    }

    public static class Modifiers {

        public static BuilderCodec<Modifiers> CODEC;

        static {
            CODEC = AutoCodecBuilder.create(Modifiers.class, Modifiers::new);
            AutoCodecBuilder.register(Modifiers.class, CODEC);
        }

        @CodecAnnotations.Field("Fishing_Power")
        public float fishingPower = 0f;

        @CodecAnnotations.Field("Biomes")
        public FishingModifier[] biomeModifiers;

        @CodecAnnotations.Field("Zones")
        public FishingModifier[] zoneModifiers;

        @CodecAnnotations.Field("Regions")
        public FishingModifier[] regionModifiers;

        @CodecAnnotations.Field("Families")
        public FishingModifier[] familyModifiers;

        @CodecAnnotations.Field("Items")
        public FishingModifier[] itemModifiers;

        @CodecAnnotations.Field("DefaultMultiplier")
        public float defaultMultiplier = 1.0f;

        public Modifiers() {
        }
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