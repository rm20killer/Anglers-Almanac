package dev.rm20.anglersalmanac.Config;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import dev.rm20.codecannotation.AutoCodecBuilder;
import dev.rm20.codecannotation.Annotations.CodecAnnotations;
import lombok.Getter;
import lombok.Setter;

public class AnglersAlmanacConfig {
    public static final String KEY = "Config";

    public static final BuilderCodec<AnglersAlmanacConfig> CODEC =
            AutoCodecBuilder.create(AnglersAlmanacConfig.class, AnglersAlmanacConfig::new);

    public static final KeyedCodec<AnglersAlmanacConfig> KEYED_CODEC = new KeyedCodec<>(KEY, CODEC);

    @Getter
    @Setter
    @CodecAnnotations.Field(value = "MinigameToUse", doc = "The name of the minigame logic to use for fishing.")
    private String MinigameToUse = "TensionBar";

    @Getter
    @Setter
    @CodecAnnotations.Field(value = "UseBait", doc = "If fishing should use bait when casting")
    private Boolean ShouldUseBait = false;

    @Getter
    @Setter
    @CodecAnnotations.Field(value = "ShouldHabCheck", doc = "If the loot table should check habitat info")
    private Boolean ShouldHabCheck = true;

    @Getter
    @Setter
    @CodecAnnotations.Field(value = "ShouldEnvironmentCheck", doc = "If the loot table should check Environment info like y level, depth, time of day etc")
    private Boolean ShouldEnvironmentCheck = true;

    @Getter
    @Setter
    @CodecAnnotations.Field( value = "ShouldHookEntities", doc = "If entities can be hooked (including players)")
    private Boolean HookEntities = true;

    @Getter
    @Setter
    @CodecAnnotations.Field(value = "EntityPullBaseForce", doc = "The base horizontal pull force applied to a hooked entity.")
    private Double EntityPullBaseForce = 60.0;

    @Getter
    @Setter
    @CodecAnnotations.Field(value = "EntityPullDistanceMultiplier", doc = "How much stronger the horizontal pull gets the further away the entity is.")
    private Double EntityPullDistanceMultiplier = 5.0;

    @Getter
    @Setter
    @CodecAnnotations.Field(value = "EntityYMultiplier", doc = "How much higher the entity is launched upward based on their distance.")
    private Double EntityYMultiplier = 0.2;

    public AnglersAlmanacConfig() {
    }

}