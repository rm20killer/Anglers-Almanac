package dev.rm20.anglersalmanac.Config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nullable;

public class MinigameConfig_TensionBar {
    public static final String KEY = "MinigameConfig_TensionBar";
    // Config:
    public float maxHookTime = 6.0f; // The longest that it can take to hook a fish in seconds.
    public float fishEscapeRate = 0.08f; // The progress lost per-second that the fish is not in the catch bar.
    public float fishReelRate = 0.2f; // The progress gain per second when the fish is inside catch bar.
    public float barRadius = 0.1f; // The size of half the bar, used to check if bar is over the fish.
    public float fishMaxVeocity = 0.45f; // The maximum speed of the fish.
    public float fishMinSpeed = 0.0f; // Range = 0.0f to 1.0f. The slowest fraction of fishMaxVelocity that fish is allowed to be.
    public double minigameModelVerticalOffset = 1.0; // The height above bobber to display the minigame elements.
    public float minigameScaleMin = 1.5f; // Minigame display minimum size.
    public float minigameScaleMax = 14.0f; // Minigame display maximum size.
    public float minigameScaleMultiplier = 1.0f; // minigameScale = distance from player * minigame scale multiplier.
    public float castCooldown = 0.5f; // Seconds before rod can be cast or reeled.
    public float barGravity = 0.65f; // How fast the bar falls when not being risen. Should be close to fish max velocity.
    public float barSpeed = 0.6f; // How fast the bar rises when right click is held. Should be faster than fish max velocity.
    public float barAcceleration = 0.27f; // The rate at which the bar accelerates multiplied by the speed of its direction.
    public float fishChangeDirectionMaxInterval = 0.45f; // The longest amount of time between fish changing direction.
    public float fishAcceleration = 0.15f; // The rate at which the fish comes up to full speed.
    public float fishBouyancy = 0.0f; // The fishes bias towards floating (positive value) or sinking (negative value).
    // Builds the codec for plugin configuration.
    public static final BuilderCodec CODEC = BuilderCodec.builder(MinigameConfig_TensionBar.class, MinigameConfig_TensionBar::new)
        // Add a new key and value to the config.
        .append(new KeyedCodec<Float>("MaxHookTime", Codec.FLOAT)
                ,(config, map) -> config.maxHookTime = map
                ,(config) -> config.maxHookTime)
                .documentation("The longest that it can take to hook a fish in seconds.")
                .add()

        .append(new KeyedCodec<Float>("FishEscapeRate", Codec.FLOAT),
                (config, value) -> config.fishEscapeRate = value,
                (config) -> config.fishEscapeRate)
                .documentation("The progress lost per-second that the fish is not in the catch bar.")
                .add()

        .append(new KeyedCodec<Float>("FishReelRate", Codec.FLOAT)
                ,(config, value) -> config.fishReelRate = value
                ,(config) -> config.fishReelRate)
                .documentation("The progress gain per second when the fish is inside catch bar.")
                .add()

        .append(new KeyedCodec<Float>("BarRadius", Codec.FLOAT)
                ,(config, value) -> config.barRadius = value
                ,(config) -> config.barRadius)
                .documentation("The size of half the bar, used to check if bar is over the fish.")
                .add()
        .append(new KeyedCodec<Float>("FishMaxVeocity", Codec.FLOAT)
                ,(config, value) -> config.fishMaxVeocity = value
                ,(config) -> config.fishMaxVeocity)
                .documentation("The maximum speed of the fish.")
                .add()
        .append(new KeyedCodec<Float>("FishMinSpeed", Codec.FLOAT)
                ,(config, value) -> config.fishMinSpeed = value
                ,(config) -> config.fishMinSpeed)
                .documentation("Range = 0.0f to 1.0f. The slowest fraction of fishMaxVelocity that fish is allowed to be.")
                .add()
        .append(new KeyedCodec<Double>("MinigameModelVerticalOffset", Codec.DOUBLE)
                ,(config, value) -> config.minigameModelVerticalOffset = value
                ,(config) -> config.minigameModelVerticalOffset)
                .documentation("The height above bobber to display the minigame elements.")
                .add()
        .append(new KeyedCodec<Float>("MinigameScaleMin", Codec.FLOAT)
                ,(config, value) -> config.minigameScaleMin = value
                ,(config) -> config.minigameScaleMin)
                .documentation("Minigame display minimum size.")
                .add()
        .append(new KeyedCodec<Float>("MinigameScaleMax", Codec.FLOAT)
                ,(config, value) -> config.minigameScaleMax = value
                ,(config) -> config.minigameScaleMax)
                .documentation("Minigame display maximum size.")
                .add()
        .append(new KeyedCodec<Float>("MinigameScaleMultiplier", Codec.FLOAT)
                ,(config, value) -> config.minigameScaleMultiplier = value
                ,(config) -> config.minigameScaleMultiplier)
                .documentation("minigameScale = distance from player * minigame scale multiplier.")
                .add()
        .append(new KeyedCodec<Float>("CastCooldown", Codec.FLOAT)
                ,(config, value) -> config.castCooldown = value
                ,(config) -> config.castCooldown)
                .documentation("Seconds before rod can be cast or reeled.")
                .add()
        .append(new KeyedCodec<Float>("BarGravity", Codec.FLOAT)
                ,(config, value) -> config.barGravity = value
                ,(config) -> config.barGravity)
                .documentation("How fast the bar falls when not being risen. Should be close to fish max velocity.")
                .add()
        .append(new KeyedCodec<Float>("BarSpeed", Codec.FLOAT)
                ,(config, value) -> config.barSpeed = value
                ,(config) -> config.barSpeed)
                .documentation("How fast the bar rises when right click is held. Should be faster than fish max velocity.")
                .add()
        .append(new KeyedCodec<Float>("BarAcceleration", Codec.FLOAT)
                ,(config, value) -> config.barAcceleration = value
                ,(config) -> config.barAcceleration)
                .documentation("The rate at which the bar accelerates multiplied by the speed of its direction.")
                .add()
        .append(new KeyedCodec<Float>("FishChangeDirectionMaxInterval", Codec.FLOAT)
                ,(config, value) -> config.fishChangeDirectionMaxInterval = value
                ,(config) -> config.fishChangeDirectionMaxInterval)
                .documentation("The longest amount of time between fish changing direction.")
                .add()
        .append(new KeyedCodec<Float>("FishAcceleration", Codec.FLOAT)
                ,(config, value) -> config.fishAcceleration = value
                ,(config) -> config.fishAcceleration)
                .documentation("The rate at which the fish reaches target velocity")
                .add()
        .append(new KeyedCodec("FishBouyancy", Codec.FLOAT)
                ,(config, value) -> config.fishBouyancy = value
                ,(config) -> config.fishBouyancy)
                .documentation("The fishes bias towards floating (positive value) or sinking (negative value).")
                .add()
        .build();

    public static final KeyedCodec<MinigameConfig_TensionBar> KEYED_CODEC = new KeyedCodec<>(KEY, CODEC);

    @Nullable
    @Override
    public MinigameConfig_TensionBar clone(){
        MinigameConfig_TensionBar r = new MinigameConfig_TensionBar();
        r.maxHookTime = this.maxHookTime;
        r.fishEscapeRate = this.fishEscapeRate;
        r.fishReelRate = this.fishReelRate;
        r.barRadius = this.barRadius;
        r.fishMaxVeocity = this.fishMaxVeocity;
        r.fishMinSpeed = this.fishMinSpeed;
        r.minigameModelVerticalOffset = this.minigameModelVerticalOffset;
        r.minigameScaleMin = this.minigameScaleMin;
        r.minigameScaleMax = this.minigameScaleMax;
        r.minigameScaleMultiplier = this.minigameScaleMultiplier;
        r.castCooldown = this.castCooldown;
        r.barGravity = this.barGravity;
        r.barSpeed = this.barSpeed;
        r.barAcceleration = this.barAcceleration;
        r.fishChangeDirectionMaxInterval = this.fishChangeDirectionMaxInterval;
        r.fishBouyancy = this.fishBouyancy;
        return r;
    }
}
