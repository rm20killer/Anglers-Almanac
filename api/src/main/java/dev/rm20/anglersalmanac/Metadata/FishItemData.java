package dev.rm20.anglersalmanac.Metadata;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import dev.rm20.anglersalmanac.Models.FishSize;
import dev.rm20.codecannotation.Annotations.CodecAnnotations;
import dev.rm20.codecannotation.AutoCodecBuilder;
import lombok.Getter;
import lombok.Setter;

public class FishItemData {
    public static final String KEY = "AnglersAlmanacFishData";

    public static final BuilderCodec<FishItemData> CODEC =
            AutoCodecBuilder.create(FishItemData.class, FishItemData::new);

    public static final KeyedCodec<FishItemData> KEYED_CODEC = new KeyedCodec<>(KEY, CODEC);

    @Getter
    @Setter
    @CodecAnnotations.Field("Size")
    private String size = "M";

    @Getter
    @Setter
    @CodecAnnotations.Field("ValueMultiplier")
    private float valueMultiplier = 1.0f;

    @Getter
    @Setter
    @CodecAnnotations.Field("SellPrice")
    private float sellPrice = 0.0f;

    public FishItemData() {
    }

    public FishItemData(FishSize fishSize, float sellPrice) {
        if (fishSize != null) {
            this.size = fishSize.getCode();
            this.valueMultiplier = fishSize.getValueMultiplier();
        } else {
            this.size = "m";
            this.valueMultiplier = 1.0f;
        }
        this.sellPrice = sellPrice;
    }

    public FishSize getFishSize() {
        return FishSize.fromString(size);
    }
}
