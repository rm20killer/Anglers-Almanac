package dev.rm20.anglersalmanac.Models;

import dev.rm20.anglersalmanac.Utils.Validator.GameIcon;
import dev.rm20.anglersalmanac.Utils.Validator.MinigameBehaviour;
import dev.rm20.anglersalmanac.Utils.Validator.TimePeriod;

public class FishLoot {
    protected String itemID;
    protected String entityID;
    protected String name;
    protected String description;
    protected String[] category;
    protected String familyId;
    protected String rarity;
    protected int weight;
    protected boolean isGlobal;
    protected Habitats habitats;
    protected Quantity quantity;
    protected MinigameStats minigameStats;
    protected BookInfo bookInfo;
    public FishLoot() {}

    // Getters
    public String getItemID() {
        if(itemID == null) {
            return "entity:"+entityID;
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
    public String[] getCategory() { return category; }
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
    public BookInfo getBookInfo() {return bookInfo;}

    public static class BookInfo {
        public String image_file;
        public String habitat_info;
        public String PageFileUI;
    }




    public static class Habitats {
        public String[] zones = new String[0];
        public Integer[] tier = new Integer[0];
        public String[] regions = new String[0];
        public String[] biomes = new String[0];
        public TimePeriod[] time_of_day = new TimePeriod[0];
        public String[] required_weather = new String[0];
        public Integer[] moon_phase = new Integer[0];
        public int min_depth = 0;
        public Height height = new Height(0, -1);
        public String[] exclude_zones = new String[0];
        public Integer[] exclude_tiers = new Integer[0];
        public String[] exclude_biomes = new String[0];
        public String[] exclude_regions = new String[0];
        //public ExcludeHabitats excludeHabitats;
        public float weight_multiplier = 0.0f;

        public String[] required_bait = new String[0];
        public int required_power = 0;
    }

    public static class Height {
        public int min_y;
        public int max_y;

        public Height(int min, int max) {
            this.min_y = min;
            this.max_y = max;
        }
    }

    public static class MinigameStats {
        public int difficulty = 1;
        public MinigameBehaviour behavior = MinigameBehaviour.NONE;
        public int stamina = 1;
        public GameIcon gameIcon = GameIcon.FISH;
    }

    public static class Quantity {
        public int min_amount = 1;
        public int max_amount = 1;
    }

    public MinigameStats getMinigameStats() {
        if(minigameStats == null)
        {
            MinigameStats stats = new MinigameStats();
            stats.stamina=0;
            stats.difficulty=0;
            stats.behavior=MinigameBehaviour.NONE;
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