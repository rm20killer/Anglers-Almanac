package dev.rm20.anglersalmanac.Utils.Validator;

public enum GameIcon {
    FISH("fish"),
    TREASURE("treasure"),
    BEAST("beast");

    public static final GameIcon[] VALUES = values();
    private final String keyword;
    GameIcon(String keyword) {
        this.keyword = keyword;
    }

    public String getKeyword() {
        return keyword;
    }

    public static GameIcon fromKeyword(String keyword) {
        for (GameIcon icon : VALUES) {
            if (icon.keyword.equalsIgnoreCase(keyword)) {
                return icon;
            }
        }
        return FISH;
    }

    public static String getModelId(GameIcon gameIcon) {
        if(gameIcon == FISH) return "SSF_FishIcon";
        if(gameIcon == TREASURE) return "AA_Minigame_Loot_Icon";
        if(gameIcon == BEAST) return "SSF_SwordIcon";
        return "SSF_FishIcon";
    }
}
