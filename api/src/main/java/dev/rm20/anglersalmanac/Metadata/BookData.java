package dev.rm20.anglersalmanac.Metadata;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import dev.rm20.codecannotation.Annotations.CodecAnnotations;
import dev.rm20.codecannotation.AutoCodecBuilder;
import lombok.Getter;
import lombok.Setter;

public class BookData {
    public static final String KEY = "AnglersAlmanacBookOwner";

    public static final BuilderCodec<BookData> CODEC =
            AutoCodecBuilder.create(BookData.class, BookData::new);

    public static final KeyedCodec<BookData> KEYED_CODEC = new KeyedCodec<>(KEY, CODEC);

    @Getter
    @Setter
    @CodecAnnotations.Field("PlayerUUID")
    private String PlayerUUID = "";

    @Getter
    @Setter
    @CodecAnnotations.Field("PlayerName")
    private String PlayerName = "";

    @Getter
    @Setter
    @CodecAnnotations.Field("PageNumber")
    private int PageNumber = 0;

    public BookData() {
    }

}