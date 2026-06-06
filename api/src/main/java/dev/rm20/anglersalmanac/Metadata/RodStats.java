package dev.rm20.anglersalmanac.Metadata;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import dev.rm20.codecannotation.Annotations.CodecAnnotations;
import dev.rm20.codecannotation.AutoCodecBuilder;


public class RodStats {

    public static final String KEY = "AA_RodStats";

    public static final BuilderCodec<RodStats> CODEC =
            AutoCodecBuilder.create(RodStats.class, RodStats::new);

    public static final KeyedCodec<RodStats> KEYED_CODEC = new KeyedCodec<>(KEY, CODEC);

    @CodecAnnotations.Field("Control")
    public float control;

    @CodecAnnotations.Field("Difficulty")
    public float difficulty;

    @CodecAnnotations.Field("Forgiveness")
    public float forgiveness;

    @CodecAnnotations.Field("Stamina")
    public float stamina;

    @CodecAnnotations.Field("FishWeightMul")
    public float fishWeightMul;

    @CodecAnnotations.Field("RarityMul")
    public float rarityMul;

    public RodStats() {
    }

    public RodStats(float control, float difficulty, float forgiveness, float stamina, float fishWeightMul, float rarityMul) {
        this.control = control;
        this.difficulty = difficulty;
        this.forgiveness = forgiveness;
        this.stamina = stamina;
        this.fishWeightMul = fishWeightMul;
        this.rarityMul = rarityMul;
    }
}