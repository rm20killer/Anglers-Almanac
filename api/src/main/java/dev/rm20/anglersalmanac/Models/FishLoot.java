package dev.rm20.anglersalmanac.Models;

import dev.rm20.anglersalmanac.Utils.Annotations.CodecAnnotations;
import dev.rm20.anglersalmanac.Utils.Validator.GameIcon;
import dev.rm20.anglersalmanac.Utils.Validator.MinigameBehaviour;
import dev.rm20.anglersalmanac.Utils.Validator.TimePeriod;

public class FishLoot {
    @CodecAnnotations.Field("ItemId")
    protected String itemID;

    @CodecAnnotations.Field("EntityId")
    protected String entityID;

    @CodecAnnotations.Field("Name")
    @CodecAnnotations.NonEmpty
    protected String name;

    @CodecAnnotations.Field("Description")
    protected String description;

    @CodecAnnotations.Field("Category")
    protected String[] category;

    @CodecAnnotations.Field("FamilyId")
    protected String familyId;

    @CodecAnnotations.Field("Rarity")
    @CodecAnnotations.NonEmpty
    protected String rarity;

    @CodecAnnotations.Field("Weight")
    @CodecAnnotations.Min(0)
    protected int weight;

    @CodecAnnotations.Field("IsGlobal")
    protected boolean isGlobal;

    @CodecAnnotations.Field("Habitats")
    protected Habitats habitats;

    @CodecAnnotations.Field("Quantity")
    protected Quantity quantity;

    @CodecAnnotations.Field("Minigame_stats")
    protected MinigameStats minigameStats;

    @CodecAnnotations.Field("Book_info")
    protected BookInfo bookInfo;

    public FishLoot() {
    }

    // Getters
    public String getItemID() {
        if (itemID == null) {
            return "entity:" + entityID;
        }
        return itemID;
    }

    public String getEntityID() {
        return entityID;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String[] getCategory() {
        return category;
    }

    public String getFamilyId() {
        return familyId;
    }

    public String getRarity() {
        return rarity;
    }

    public int getWeight() {
        return weight;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public Habitats getHabitats() {
        return habitats;
    }

    public BookInfo getBookInfo() {
        return bookInfo;
    }

    public static class BookInfo {
        @CodecAnnotations.Field("Image_Path")
        public String image_file;

        @CodecAnnotations.Field("Habitat_Info")
        public String habitat_info;

        @CodecAnnotations.Field("Page_File_Ui")
        public String PageFileUI;
    }


    public static class Habitats {
        @CodecAnnotations.Field(value = "Zones", doc = "Set what area and conditions to give the item.")
        @CodecAnnotations.UniqueArray
        public String[] zones = new String[0];

        @CodecAnnotations.Field("Tiers")
        @CodecAnnotations.UniqueArray
        public Integer[] tier = new Integer[0];

        @CodecAnnotations.Field("Regions")
        @CodecAnnotations.UniqueArray
        public String[] regions = new String[0];

        @CodecAnnotations.Field("Biomes")
        @CodecAnnotations.UniqueArray
        public String[] biomes = new String[0];

        @CodecAnnotations.Field("Time_of_day")
        @CodecAnnotations.UniqueArray
        public TimePeriod[] time_of_day = new TimePeriod[0];

        public String[] required_weather = new String[0]; // NOT IN USE

        @CodecAnnotations.Field(value = "Moon_phase", doc = "If a value of -1 is selected then it be disabled")
        @CodecAnnotations.UniqueArray
        public Integer[] moon_phase = new Integer[0];

        @CodecAnnotations.Field("Min_depth")
        @CodecAnnotations.Min(0)
        public int min_depth = 0;

        @CodecAnnotations.Field("Height")
        public Height height = new Height(0, -1);

        @CodecAnnotations.Field("Exclude_zones")
        @CodecAnnotations.UniqueArray
        public String[] exclude_zones = new String[0];

        @CodecAnnotations.Field("Exclude_tiers")
        @CodecAnnotations.UniqueArray
        public Integer[] exclude_tiers = new Integer[0];

        @CodecAnnotations.Field("Exclude_biomes")
        @CodecAnnotations.UniqueArray
        public String[] exclude_biomes = new String[0];

        @CodecAnnotations.Field("Exclude_regions")
        @CodecAnnotations.UniqueArray
        public String[] exclude_regions = new String[0];

        @CodecAnnotations.Field("Weight_multiplier")
        public float weight_multiplier = 0.0f;

        @CodecAnnotations.Field(value = "Required_Bait", doc = "List of Bait ID that can get the loot")
        @CodecAnnotations.UniqueArray
        public String[] required_bait = new String[0];

        @CodecAnnotations.Field("Required_Power")
        @CodecAnnotations.Min(0)
        public int required_power = 0;

        public Habitats() {
        }
    }

    public static class Height {
        @CodecAnnotations.Field(value = "Min_y", doc = "The fish will not spawn below this height.")
        @CodecAnnotations.Min(0)
        public int min_y;

        @CodecAnnotations.Field(value = "Max_y", doc = "The maximum Y-level requirement. Set to -1 to disable the upper limit (infinity).")
        @CodecAnnotations.Min(-1)
        public int max_y;

        public Height() {
        }

        public Height(int min, int max) {
            this.min_y = min;
            this.max_y = max;
        }
    }

    public static class MinigameStats {
        @CodecAnnotations.Field("Difficulty")
        @CodecAnnotations.NonNull
        public int difficulty = 1;

        @CodecAnnotations.Field("Behavior")
        @CodecAnnotations.NonNull
        public MinigameBehaviour behavior = MinigameBehaviour.NONE;

        @CodecAnnotations.Field("Stamina")
        @CodecAnnotations.NonNull
        public int stamina = 1;

        @CodecAnnotations.Field("GameIcon")
        public GameIcon gameIcon = GameIcon.FISH;
    }

    public static class Quantity {
        @CodecAnnotations.Field(value = "Min", doc = "Setting min will set the least amount of items to give")
        @CodecAnnotations.Min(1)
        public int min_amount = 1;

        @CodecAnnotations.Field(value = "Max", doc = "Enables random amount of items to give from min to max")
        public int max_amount = 1;
    }

    public MinigameStats getMinigameStats() {
        if (minigameStats == null) {
            MinigameStats stats = new MinigameStats();
            stats.stamina = 0;
            stats.difficulty = 0;
            stats.behavior = MinigameBehaviour.NONE;
        }
        return minigameStats;
    }

    public static String getRarityColour(String Rarity) {
        if (Rarity == null) return "#5A5B5B";

        return switch (Rarity.toLowerCase()) {
            case "common" -> "#5A5B5B";
            case "uncommon" -> "#005205";
            case "rare" -> "#5481EB";
            case "epic" -> "#5C337B";
            case "legendary" -> "#FFDB4B";
            default -> "#5A5B5B"; // Default weight for unknown rarities
        };
    }

    public static int getRarityWeightFromRarity(String Rarity) {
        return switch (Rarity.toLowerCase()) {
            case "common" -> 0;
            case "uncommon" -> 1;
            case "rare" -> 2;
            case "epic" -> 3;
            case "legendary" -> 4;
            default -> -1; // Default weight for unknown rarities
        };
    }

}