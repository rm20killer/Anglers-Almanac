package dev.rm20.anglersalmanac.Metadata;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import dev.rm20.anglersalmanac.Utils.AutoCodecBuilder;
import dev.rm20.anglersalmanac.Utils.Annotations.CodecAnnotations;

import java.util.UUID;

public class FishingRodData {
    public static final String KEY = "AnglersAlmanacBoundBobber";

    public static final BuilderCodec<FishingRodData> CODEC =
            AutoCodecBuilder.create(FishingRodData.class, FishingRodData::new);

    public static final KeyedCodec<FishingRodData> KEYED_CODEC = new KeyedCodec<>(KEY, CODEC);

    @CodecAnnotations.Field("BoundBobber")
    private UUID boundBobber = null;

    @CodecAnnotations.Field("BoundMinigame")
    private UUID boundMinigame = null;

    @CodecAnnotations.Field("Mode")
    private int mode = 0;

    public FishingRodData() {
    }

    public UUID getBoundBobber() {
        return this.boundBobber;
    }
    public void setBoundBobber(UUID uuid) {
        this.boundBobber = uuid;
    }

    public UUID getBoundMinigame() {
        return this.boundMinigame;
    }
    public void setBoundMinigame(UUID uuid) {
        this.boundMinigame = uuid;
    }

    public int getMode() {
        return this.mode;
    }
    public void setMode(int i) {
        this.mode = i;
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
