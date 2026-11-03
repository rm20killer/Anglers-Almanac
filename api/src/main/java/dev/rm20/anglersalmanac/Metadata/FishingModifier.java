package dev.rm20.anglersalmanac.Metadata;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import dev.rm20.anglersalmanac.Models.FishSize;
import dev.rm20.codecannotation.AutoCodecBuilder;
import dev.rm20.codecannotation.Annotations.CodecAnnotations;

import java.util.HashMap;
import java.util.Map;

public class FishingModifier {

    public static BuilderCodec<FishingModifier> CODEC;

    static {
        if (CODEC == null) {
            CODEC = AutoCodecBuilder.create(FishingModifier.class, FishingModifier::new);
            AutoCodecBuilder.register(FishingModifier.class, CODEC);
        }
    }

    @CodecAnnotations.Field("TargetId")
    public String targetId;

    @CodecAnnotations.Field("Multiplier")
    public float chanceMultiplier = 1.0f;

    @CodecAnnotations.Field("Size")
    public String size;

    @CodecAnnotations.Field("SizeSkew")
    public float sizeSkew = 0.0f;

    public FishingModifier() {
    }

    public FishingModifier(String targetId, float chanceMultiplier) {
        this.targetId = targetId;
        this.chanceMultiplier = chanceMultiplier;
    }

    public FishingModifier(String targetId, String size, float chanceMultiplier) {
        this.targetId = targetId;
        this.size = size;
        this.chanceMultiplier = chanceMultiplier;
    }

    public static class Modifiers {

        public static BuilderCodec<Modifiers> CODEC;

        static {
            if (FishingModifier.CODEC == null) {
                FishingModifier.CODEC = AutoCodecBuilder.create(FishingModifier.class, FishingModifier::new);
                AutoCodecBuilder.register(FishingModifier.class, FishingModifier.CODEC);
            }
            CODEC = AutoCodecBuilder.create(Modifiers.class, Modifiers::new);
            AutoCodecBuilder.register(Modifiers.class, CODEC);
        }

        @CodecAnnotations.Field("Fishing_Power")
        public float fishingPower = 0f;

        @CodecAnnotations.Field("CatchSpeed")
        public float catchSpeed = 1.0f;

        @CodecAnnotations.Field("SizeSkew")
        public float sizeSkew = 0.0f;

        @CodecAnnotations.Field("Sizes")
        public FishingModifier[] sizeModifiers;

        @CodecAnnotations.Field("TriggerVolumes")
        public FishingModifier[] triggerVolumeModifiers;

        @CodecAnnotations.Field("FishSizeModifiers")
        public FishingModifier[] fishSizeModifiers;

        @CodecAnnotations.Field("TriggerVolumeSizeModifiers")
        public FishingModifier[] triggerVolumeSizeModifiers;

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

        public float getSizeMultiplier(FishSize targetSize) {
            if (targetSize == null || sizeModifiers == null) return 1.0f;
            float mult = 1.0f;
            for (FishingModifier mod : sizeModifiers) {
                if (mod == null || mod.targetId == null) continue;
                if (mod.targetId.equalsIgnoreCase(targetSize.name()) ||
                        mod.targetId.equalsIgnoreCase(targetSize.getCode()) ||
                        mod.targetId.equalsIgnoreCase(targetSize.getDisplayName())) {
                    mult *= mod.chanceMultiplier;
                }
            }
            return mult;
        }

        public float getFishSizeMultiplier(String itemId, String fishId, FishSize targetSize) {
            if (targetSize == null) return 1.0f;
            float mult = 1.0f;
            mult *= findSizeMultiplier(this.fishSizeModifiers, itemId, fishId, targetSize);
            mult *= findSizeMultiplier(this.itemModifiers, itemId, fishId, targetSize);
            return mult;
        }

        public float getTriggerVolumeSizeMultiplier(String triggerKey, FishSize targetSize) {
            if (triggerKey == null || targetSize == null) return 1.0f;
            float mult = 1.0f;
            mult *= findSizeMultiplier(this.triggerVolumeSizeModifiers, triggerKey, triggerKey, targetSize);
            mult *= findSizeMultiplier(this.triggerVolumeModifiers, triggerKey, triggerKey, targetSize);
            return mult;
        }

        public float getTotalSizeSkew(String itemId, String fishId, String triggerKey) {
            float total = this.sizeSkew;
            total += findSkew(this.fishSizeModifiers, itemId, fishId);
            total += findSkew(this.itemModifiers, itemId, fishId);
            if (triggerKey != null) {
                total += findSkew(this.triggerVolumeSizeModifiers, triggerKey, triggerKey);
                total += findSkew(this.triggerVolumeModifiers, triggerKey, triggerKey);
            }
            return total;
        }

        private float findSizeMultiplier(FishingModifier[] array, String id1, String id2, FishSize size) {
            if (array == null) return 1.0f;
            float mult = 1.0f;
            for (FishingModifier mod : array) {
                if (mod == null || mod.targetId == null) continue;

                boolean idMatch = (id1 != null && mod.targetId.equalsIgnoreCase(id1)) ||
                        (id2 != null && mod.targetId.equalsIgnoreCase(id2));

                if (idMatch) {
                    if (mod.size != null && !mod.size.isBlank()) {
                        if (mod.size.equalsIgnoreCase(size.name()) ||
                                mod.size.equalsIgnoreCase(size.getCode()) ||
                                mod.size.equalsIgnoreCase(size.getDisplayName())) {
                            mult *= mod.chanceMultiplier;
                        }
                    }
                } else {
                    String expectedSuffix1 = ":" + size.name();
                    String expectedSuffix2 = ":" + size.getCode();
                    if ((id1 != null && (mod.targetId.equalsIgnoreCase(id1 + expectedSuffix1) || mod.targetId.equalsIgnoreCase(id1 + expectedSuffix2))) ||
                            (id2 != null && (mod.targetId.equalsIgnoreCase(id2 + expectedSuffix1) || mod.targetId.equalsIgnoreCase(id2 + expectedSuffix2)))) {
                        mult *= mod.chanceMultiplier;
                    }
                }
            }
            return mult;
        }

        private float findSkew(FishingModifier[] array, String id1, String id2) {
            if (array == null) return 0.0f;
            float skew = 0.0f;
            for (FishingModifier mod : array) {
                if (mod == null || mod.targetId == null) continue;
                if ((id1 != null && mod.targetId.equalsIgnoreCase(id1)) ||
                        (id2 != null && mod.targetId.equalsIgnoreCase(id2))) {
                    skew += mod.sizeSkew;
                }
            }
            return skew;
        }
    }


    public static Modifiers mergeModifiers(FishingModifier.Modifiers... modifiers) {
        FishingModifier.Modifiers master = new FishingModifier.Modifiers();
        Map<String, Float> biomeMap = new HashMap<>();
        Map<String, Float> zoneMap = new HashMap<>();
        Map<String, Float> regionMap = new HashMap<>();
        Map<String, Float> familyMap = new HashMap<>();
        Map<String, Float> itemMap = new HashMap<>();
        Map<String, Float> triggerVolumeMap = new HashMap<>();
        Map<String, Float> sizeMap = new HashMap<>();
        Map<String, Float> fishSizeMap = new HashMap<>();
        Map<String, Float> triggerVolumeSizeMap = new HashMap<>();

        float combinedDefault = 1.0f;
        float combinedPower = 1.0f;
        float combinedCatchSpeed = 1.0f;
        float combinedSizeSkew = 0.0f;

        for (FishingModifier.Modifiers mod : modifiers) {
            if (mod == null) continue;
            combinedPower += mod.fishingPower;
            if (mod.catchSpeed > 0.001f) {
                combinedCatchSpeed *= mod.catchSpeed;
            }
            combinedSizeSkew += mod.sizeSkew;

            mergeIntoMap(biomeMap, mod.biomeModifiers);
            mergeIntoMap(zoneMap, mod.zoneModifiers);
            mergeIntoMap(regionMap, mod.regionModifiers);
            mergeIntoMap(familyMap, mod.familyModifiers);
            mergeIntoMap(itemMap, mod.itemModifiers);
            mergeIntoMap(triggerVolumeMap, mod.triggerVolumeModifiers);
            mergeIntoMap(sizeMap, mod.sizeModifiers);
            mergeIntoMap(fishSizeMap, mod.fishSizeModifiers);
            mergeIntoMap(triggerVolumeSizeMap, mod.triggerVolumeSizeModifiers);

            combinedDefault *= mod.defaultMultiplier;
        }

        master.fishingPower = combinedPower;
        master.catchSpeed = combinedCatchSpeed;
        master.sizeSkew = combinedSizeSkew;
        master.biomeModifiers = mapToArray(biomeMap);
        master.zoneModifiers = mapToArray(zoneMap);
        master.regionModifiers = mapToArray(regionMap);
        master.familyModifiers = mapToArray(familyMap);
        master.itemModifiers = mapToArray(itemMap);
        master.triggerVolumeModifiers = mapToArray(triggerVolumeMap);
        master.sizeModifiers = mapToArray(sizeMap);
        master.fishSizeModifiers = mapToArray(fishSizeMap);
        master.triggerVolumeSizeModifiers = mapToArray(triggerVolumeSizeMap);
        master.defaultMultiplier = combinedDefault;

        return master;
    }

    private static void mergeIntoMap(Map<String, Float> map, FishingModifier[] mods) {
        if (mods == null) return;
        for (FishingModifier mod : mods) {
            if (mod == null || mod.targetId == null) continue;
            String key = mod.targetId;
            if (mod.size != null && !mod.size.isBlank()) {
                key = mod.targetId + ":" + mod.size.toUpperCase();
            }
            map.merge(key, mod.chanceMultiplier, (oldVal, newVal) -> oldVal * newVal);
        }
    }

    private static FishingModifier[] mapToArray(Map<String, Float> map) {
        return map.entrySet().stream().map(entry -> {
            FishingModifier fm = new FishingModifier();
            String key = entry.getKey();
            if (key.contains(":")) {
                String[] parts = key.split(":", 2);
                fm.targetId = parts[0];
                fm.size = parts[1];
            } else {
                fm.targetId = key;
            }
            fm.chanceMultiplier = entry.getValue();
            return fm;
        }).toArray(FishingModifier[]::new);
    }
}
