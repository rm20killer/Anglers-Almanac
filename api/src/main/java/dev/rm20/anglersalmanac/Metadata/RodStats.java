package dev.rm20.anglersalmanac.Metadata;


import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;


public class RodStats{

    public float control;
    public float difficulty;
    public float forgiveness;
    public float stamina;
    public float fishWeightMul;
    public float rarityMul;


    public RodStats(){
    }
    public RodStats(float control, float difficulty, float forgiveness, float stamina, float fishWeightMul, float rarityMul){
        this.control = control;
        this.difficulty = difficulty;
        this.forgiveness = forgiveness;
        this.stamina = stamina;
        this.fishWeightMul = fishWeightMul;
        this.rarityMul = rarityMul;
    }

    public static final String KEY = "AA_RodStats";
    public static final BuilderCodec<RodStats> CODEC = BuilderCodec.builder(RodStats.class, RodStats::new)  // s = setter, g = getter, v = value to set.
            .append(new KeyedCodec<>("Control",     Codec.FLOAT), (s, v) -> s.control     = v, (g) -> g.control).add()
            .append(new KeyedCodec<>("Difficulty",  Codec.FLOAT), (s, v) -> s.difficulty  = v, (g) -> g.difficulty).add()
            .append(new KeyedCodec<>("Forgiveness", Codec.FLOAT), (s, v) -> s.forgiveness = v, (g) -> g.forgiveness).add()
            .append(new KeyedCodec<>("Stamina",     Codec.FLOAT), (s, v) -> s.stamina     = v, (g) -> g.stamina).add()
            .append(new KeyedCodec<>("FishWeightMul",  Codec.FLOAT), (s, v) -> s.fishWeightMul  = v, (g) -> g.fishWeightMul).add()
            .append(new KeyedCodec<>("RarityMul",   Codec.FLOAT), (s, v) -> s.rarityMul   = v, (g) -> g.rarityMul).add()
            .build();
    public static final KeyedCodec<RodStats> KEYED_CODEC = new KeyedCodec<>(KEY, CODEC);


}
