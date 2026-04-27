package dev.rm20.anglersalmanac.Config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class AnglersAlmanacConfig {
    public static final String KEY = "MinigameConfig";

    public static final BuilderCodec<AnglersAlmanacConfig> CODEC = BuilderCodec.builder(AnglersAlmanacConfig.class, AnglersAlmanacConfig::new)
            .append(new KeyedCodec<>("MinigameToUse", Codec.STRING),
                    (config, value) -> config.minigameToUse = value,
                    (config) -> config.minigameToUse)
            .documentation("The name of the minigame logic to use for fishing.").add()
            .append(new KeyedCodec<>("UseBait", Codec.BOOLEAN),
                    (config, value) -> config.ShouldUseBait = value,
                    (config) -> config.ShouldUseBait)
            .documentation("If fishing should use bait when casting").add()
            .build();

    public static final KeyedCodec<AnglersAlmanacConfig> KEYED_CODEC = new KeyedCodec<>(KEY, CODEC);

    private String minigameToUse = "TensionBar";
    private Boolean ShouldUseBait = false;
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
}