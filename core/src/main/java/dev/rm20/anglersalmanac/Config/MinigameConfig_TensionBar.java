package dev.rm20.anglersalmanac.Config;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import dev.rm20.anglersalmanac.Utils.AutoCodecBuilder;
import dev.rm20.anglersalmanac.Utils.Annotations.CodecAnnotations;

import javax.annotation.Nullable;

public class MinigameConfig_TensionBar {
    public static final String KEY = "MinigameConfig_TensionBar";

    // Automatically processes all fields annotated with @CodecAnnotations.Field below
    @SuppressWarnings("unchecked")
    public static final BuilderCodec<MinigameConfig_TensionBar> CODEC =
            AutoCodecBuilder.create(MinigameConfig_TensionBar.class, MinigameConfig_TensionBar::new);

    public static final KeyedCodec<MinigameConfig_TensionBar> KEYED_CODEC = new KeyedCodec<>(KEY, CODEC);

    // Config fields:

    @CodecAnnotations.Field(value = "MaxHookTime", doc = "The longest that it can take to hook a fish in seconds.")
    public float maxHookTime = 6.0f;

    @CodecAnnotations.Field(value = "FishEscapeRate", doc = "The progress lost per-second that the fish is not in the catch bar.")
    public float fishEscapeRate = 0.08f;

    @CodecAnnotations.Field(value = "FishReelRate", doc = "The progress gain per second when the fish is inside catch bar.")
    public float fishReelRate = 0.2f;

    @CodecAnnotations.Field(value = "BarRadius", doc = "The size of half the bar, used to check if bar is over the fish.")
    public float barRadius = 0.1f;

    @CodecAnnotations.Field(value = "FishMaxVeocity", doc = "The maximum speed of the fish.")
    public float fishMaxVeocity = 0.45f;

    @CodecAnnotations.Field(value = "FishMinSpeed", doc = "Range = 0.0f to 1.0f. The slowest fraction of fishMaxVelocity that fish is allowed to be.")
    public float fishMinSpeed = 0.0f;

    @CodecAnnotations.Field(value = "MinigameModelVerticalOffset", doc = "The height above bobber to display the minigame elements.")
    public double minigameModelVerticalOffset = 1.0;

    @CodecAnnotations.Field(value = "MinigameScaleMin", doc = "Minigame display minimum size.")
    public float minigameScaleMin = 1.5f;

    @CodecAnnotations.Field(value = "MinigameScaleMax", doc = "Minigame display maximum size.")
    public float minigameScaleMax = 14.0f;

    @CodecAnnotations.Field(value = "MinigameScaleMultiplier", doc = "minigameScale = distance from player * minigame scale multiplier.")
    public float minigameScaleMultiplier = 1.0f;

    @CodecAnnotations.Field(value = "CastCooldown", doc = "Seconds before rod can be cast or reeled.")
    public float castCooldown = 0.5f;

    @CodecAnnotations.Field(value = "BarGravity", doc = "How fast the bar falls when not being risen. Should be close to fish max velocity.")
    public float barGravity = 0.65f;

    @CodecAnnotations.Field(value = "BarSpeed", doc = "How fast the bar rises when right click is held. Should be faster than fish max velocity.")
    public float barSpeed = 0.6f;

    @CodecAnnotations.Field(value = "BarAcceleration", doc = "The rate at which the bar accelerates multiplied by the speed of its direction.")
    public float barAcceleration = 0.27f;

    @CodecAnnotations.Field(value = "FishChangeDirectionMaxInterval", doc = "The longest amount of time between fish changing direction.")
    public float fishChangeDirectionMaxInterval = 0.45f;

    @CodecAnnotations.Field(value = "FishAcceleration", doc = "The rate at which the fish reaches target velocity")
    public float fishAcceleration = 0.15f;

    @CodecAnnotations.Field(value = "FishBouyancy", doc = "The fishes bias towards floating (positive value) or sinking (negative value).")
    public float fishBouyancy = 0.0f;

    public MinigameConfig_TensionBar() {
    }

    @Nullable
    @Override
    public MinigameConfig_TensionBar clone() {
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
        r.fishAcceleration = this.fishAcceleration;
        r.fishBouyancy = this.fishBouyancy;
        return r;
    }
}