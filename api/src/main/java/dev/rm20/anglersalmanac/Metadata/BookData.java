package dev.rm20.anglersalmanac.Metadata;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import dev.rm20.codecannotation.Annotations.CodecAnnotations;
import dev.rm20.codecannotation.AutoCodecBuilder;

public class BookData {
    public static final String KEY = "AnglersAlmanacBookOwner";

    public static final BuilderCodec<BookData> CODEC =
            AutoCodecBuilder.create(BookData.class, BookData::new);

    public static final KeyedCodec<BookData> KEYED_CODEC = new KeyedCodec<>(KEY, CODEC);

    @CodecAnnotations.Field("PlayerUUID")
    private String playerUUID = "";

    @CodecAnnotations.Field("PlayerName")
    private String playerName = "";

    @CodecAnnotations.Field("PageNumber")
    private int pageNumber = 0;

    public BookData() {
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getPlayerUUID() {
        return this.playerUUID;
    }

    public void setPlayerUUID(String playerUUID) {
        this.playerUUID = playerUUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

}