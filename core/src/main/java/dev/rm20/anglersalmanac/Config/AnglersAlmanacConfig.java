package dev.rm20.anglersalmanac.Config;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import dev.rm20.codecannotation.AutoCodecBuilder;
import dev.rm20.codecannotation.Annotations.CodecAnnotations;

public class AnglersAlmanacConfig {
    public static final String KEY = "Config";

    public static final BuilderCodec<AnglersAlmanacConfig> CODEC =
            AutoCodecBuilder.create(AnglersAlmanacConfig.class, AnglersAlmanacConfig::new);

    public static final KeyedCodec<AnglersAlmanacConfig> KEYED_CODEC = new KeyedCodec<>(KEY, CODEC);

    @CodecAnnotations.Field(value = "MinigameToUse", doc = "The name of the minigame logic to use for fishing.")
    private String minigameToUse = "TensionBar";

    @CodecAnnotations.Field(value = "UseBait", doc = "If fishing should use bait when casting")
    private Boolean ShouldUseBait = false;

    @CodecAnnotations.Field(value = "ShouldHabCheck", doc = "If the loot table should check habitat info")
    private Boolean ShouldHabCheck = true;

    @CodecAnnotations.Field(value = "ShouldEnvironmentCheck", doc = "If the loot table should check Environment info like y level, depth, time of day etc")
    private Boolean ShouldEnvironmentCheck = true;

    @CodecAnnotations.Field( value = "ShouldHookEntities", doc = "If entities can be hooked (including players)")
    private Boolean HookEntities = true;
    public AnglersAlmanacConfig() {
    }

    // Getter
    public String getMinigameToUse() {
        return minigameToUse;
    }

    // Setter
    public void setMinigameToUse(String minigameToUse) {
        this.minigameToUse = minigameToUse;
    }

    public Boolean getShouldUseBait() {
        return ShouldUseBait;
    }

    public void setShouldUseBait(Boolean shouldUseBait) {
        ShouldUseBait = shouldUseBait;
    }

    public Boolean getShouldHabCheck() {
        return ShouldHabCheck;
    }

    public void setShouldHabCheck(Boolean shouldHabCheck) {
        ShouldHabCheck = shouldHabCheck;
    }

    public Boolean getShouldEnvironmentCheck() {
        return ShouldEnvironmentCheck;
    }

    public void setShouldEnvironmentCheck(Boolean shouldEnvironmentCheck) {
        ShouldEnvironmentCheck = shouldEnvironmentCheck;
    }

    public Boolean getHookEntities() {
        return HookEntities;
    }

    public void setHookEntities(Boolean hookEntities) {
        HookEntities = hookEntities;
    }
}