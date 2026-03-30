package dev.rm20.anglersalmanac.Models;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import dev.rm20.anglersalmanac.Registration.HytaleAsset;

@HytaleAsset(path = "AnglersAlmanacQuests")
public class QuestData implements JsonAssetWithMap<String, DefaultAssetMap<String, QuestData>> {

    private String id;
    private AssetExtraInfo.Data data;
    public String display_name;

    public int QuestPointsRequired;
    public String prerequisite_id;
    public Priority priority = Priority.MEDIUM;

    public int QuestPointsReward;
    public Reward[] rewards;

    public TargetCondition condition;

    public enum Priority {
        VERY_LOW,
        LOW,
        MEDIUM,
        HIGH,
        VERY_HIGH,
        HIGHEST;
    }

    public enum QuestType {
        COLLECTION,
        GEOGRAPHIC,
        COMPLETIONIST,
        REGIONAL_COLLECTION,
        ZONE_COMPLETIONIST;

        public boolean isZoneSpecific() {
            return this == GEOGRAPHIC ||
                    this == REGIONAL_COLLECTION ||
                    this == ZONE_COMPLETIONIST;
        }
    }

    public static class Reward {
        public String item;
        public int quantity;
    }

    public static class TargetCondition {
        public QuestType type;
        public int amount;
        public String zoneId;
        public String fishId;
        public Integer minY;
        public Integer maxY;
    }

    public static final String KEY = "AA_QuestData";

    public static final BuilderCodec<Reward> REWARD_CODEC = BuilderCodec.builder(Reward.class, Reward::new)
            .append(new KeyedCodec<>("Item", Codec.STRING), (t, v) -> t.item = v, t -> t.item).add()
            .append(new KeyedCodec<>("Quantity", Codec.INTEGER), (t, v) -> t.quantity = v, t -> t.quantity).add()
            .build();


    public static final BuilderCodec<TargetCondition> CONDITION_CODEC = BuilderCodec.builder(TargetCondition.class, TargetCondition::new)
            .append(new KeyedCodec<>("Type", new EnumCodec<>(QuestType.class)), (t, v) -> t.type = v, t -> t.type).add()
            .append(new KeyedCodec<>("Amount", Codec.INTEGER), (t, v) -> t.amount = v, t -> t.amount).add()
            .append(new KeyedCodec<>("ZoneId", Codec.STRING), (t, v) -> t.zoneId = v, t -> t.zoneId).add()
            .append(new KeyedCodec<>("FishId", Codec.STRING), (t, v) -> t.fishId = v, t -> t.fishId).add()
            .append(new KeyedCodec<>("MinY", Codec.INTEGER), (t, v) -> t.minY = v, t -> t.minY).add()
            .append(new KeyedCodec<>("MaxY", Codec.INTEGER), (t, v) -> t.maxY = v, t -> t.maxY).add()
            .build();


    public static final AssetBuilderCodec<String, QuestData> CODEC = AssetBuilderCodec.builder(
                    QuestData.class,
                    QuestData::new,
                    Codec.STRING,
                    (t, id) -> t.id = id,
                    t -> t.id,
                    (t, data) -> t.data = data,
                    t -> t.data
            )
            .appendInherited(new KeyedCodec<>("DisplayName", Codec.STRING), (t, v) -> t.display_name = v, t -> t.display_name, (t, p) -> t.display_name = p.display_name).add()
            .appendInherited(new KeyedCodec<>("Priority", new EnumCodec<>(Priority.class)), (t, v) -> t.priority = v, t -> t.priority, (t, p) -> t.priority = p.priority).add()
            .appendInherited(new KeyedCodec<>("QuestPointsRequired", Codec.INTEGER), (t, v) -> t.QuestPointsRequired = v, t -> t.QuestPointsRequired, (t, p) -> t.QuestPointsRequired = p.QuestPointsRequired).add()
            .appendInherited(new KeyedCodec<>("QuestPointsReward", Codec.INTEGER), (t, v) -> t.QuestPointsReward = v, t -> t.QuestPointsReward, (t, p) -> t.QuestPointsReward = p.QuestPointsReward).add()
            .appendInherited(new KeyedCodec<>("PrerequisiteId", Codec.STRING), (t, v) -> t.prerequisite_id = v, t -> t.prerequisite_id, (t, p) -> t.prerequisite_id = p.prerequisite_id).add()
            .appendInherited(new KeyedCodec<>("Rewards", new ArrayCodec<>(REWARD_CODEC, Reward[]::new)), (t, v) -> t.rewards = v, t -> t.rewards, (t,p) -> t.rewards = p.rewards).add()
            .appendInherited(new KeyedCodec<>("Condition", CONDITION_CODEC), (t,v) ->t.condition = v, t-> t.condition, (t,p) -> t.condition = p.condition).add()
            .build();


    @Override
    public String getId() {
        return id;
    }
}
