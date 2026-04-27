package dev.rm20.anglersalmanac.MinigameManager.SkillCheck;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class DialEventData {
    public static final BuilderCodec<DialEventData> CODEC =
            BuilderCodec.builder(DialEventData.class, DialEventData::new)
                    .append(new KeyedCodec<>("Action", Codec.STRING),
                            (d, v) -> d.action = v, d -> d.action).add()
                    // Change Float to Integer to match the Anchor class
                    .append(new KeyedCodec<>("@Position", Codec.INTEGER),
                            (d, v) -> d.position = v, d -> d.position).add()
                    .build();

    public String action;
    public Integer position;
    public float rotation;
}
