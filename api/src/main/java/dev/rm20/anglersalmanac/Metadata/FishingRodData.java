package dev.rm20.anglersalmanac.Metadata;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import dev.rm20.codecannotation.AutoCodecBuilder;
import dev.rm20.codecannotation.Annotations.CodecAnnotations;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class FishingRodData {
    public static final String KEY = "AnglersAlmanacBoundBobber";

    public static final BuilderCodec<FishingRodData> CODEC =
            AutoCodecBuilder.create(FishingRodData.class, FishingRodData::new);

    public static final KeyedCodec<FishingRodData> KEYED_CODEC = new KeyedCodec<>(KEY, CODEC);

    @Getter
    @Setter
    @CodecAnnotations.Field("BoundBobber")
    private UUID boundBobber = null;

    @Getter
    @Setter
    @CodecAnnotations.Field("BoundMinigame")
    private UUID boundMinigame = null;

    @Getter
    @Setter
    @CodecAnnotations.Field("Mode")
    private int Mode = 0;

    public FishingRodData() {
    }



    /*private RodStats rodStats;
    public RodStats getRodStats() {return this.rodStats;
    }
    public void setRodStats(RodStats stats) {
        this.rodStats = stats;
    }

    private List<RodStats> attachmentStats = new ArrayList<>();
    public List<RodStats> getAttachmentStats(){ return attachmentStats; }
    public void setAttachmentStats(List<RodStats> attachmentStats) {this.attachmentStats = attachmentStats;}

     */
}
