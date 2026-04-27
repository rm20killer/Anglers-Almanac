package dev.rm20.anglersalmanac.Metadata;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class BookData {
    public static final String KEY = "AnglersAlmanacBookOwner";
    public static final BuilderCodec<BookData> CODEC = BuilderCodec.builder(BookData.class, BookData::new)
            .append(new KeyedCodec<>("PlayerUUID", Codec.STRING), (metaData, value) -> metaData.playerUUID = value, (config) -> config.playerUUID).add()
            .append(new KeyedCodec<>("PlayerName", Codec.STRING), (metaData, value) -> metaData.playerName = value, (config) -> config.playerName).add()
            .append(new KeyedCodec<>("PageNumber", Codec.INTEGER), (metaData, value) -> metaData.pageNumber = value, (config) -> config.pageNumber).add()
            .build();

    public static final KeyedCodec<BookData> KEYED_CODEC = new KeyedCodec<>(KEY, CODEC);

    private String playerUUID = "";
    private String playerName = "";

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    private int pageNumber = 0;

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