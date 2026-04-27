package dev.rm20.anglersalmanac.Metadata;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import java.util.UUID;

public class FishingRodData {
    public static final String KEY = "AnglersAlmanacBoundBobber";
    public static final BuilderCodec<FishingRodData> CODEC = BuilderCodec.builder(FishingRodData.class, FishingRodData::new)
            .append(new KeyedCodec<>("BoundBobber", Codec.UUID_BINARY), (metaData, value) -> metaData.boundBobber = value, (config) -> config.boundBobber).add()
            .append(new KeyedCodec<>("BoundMinigame", Codec.UUID_BINARY), (metaData, value) -> metaData.boundMinigame = value, (config) -> config.boundMinigame).add()
            .append(new KeyedCodec<>("Mode", Codec.INTEGER), (metaData, value) -> metaData.mode = value, (config) -> config.mode).add()
            //.append(RodStats.KEYED_CODEC, (s, v) -> s.rodStats = v, (g) -> g.rodStats).add()
            .build();
    public static final KeyedCodec<FishingRodData> KEYED_CODEC = new KeyedCodec<>(KEY, CODEC);

    private UUID boundBobber = null;
    public UUID getBoundBobber() {
        return this.boundBobber;
    }
    public void setBoundBobber(UUID uuid) {
        this.boundBobber = uuid;
    }

    private UUID boundMinigame = null;
    public UUID getBoundMinigame() {
        return this.boundMinigame;
    }
    public void setBoundMinigame(UUID uuid) {
        this.boundMinigame = uuid;
    }

    private int mode = 0;
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
