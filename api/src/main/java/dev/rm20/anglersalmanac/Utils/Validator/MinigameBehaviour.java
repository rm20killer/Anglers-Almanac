package dev.rm20.anglersalmanac.Utils.Validator;

public enum MinigameBehaviour {
    DARTING("darting"),
    FLOATER("floater"),
    SINKER("sinker"),
    HEAVY_SINKER("heavy_sinker"),
    AGGRESSIVE("aggressive"),
    ERRATIC("erratic"),
    STEADY("steady"),
    NONE("None");

    public static final MinigameBehaviour[] VALUES = values();
    private final String keyword;

    MinigameBehaviour(String keyword) {
        this.keyword = keyword;
    }

    public String getKeyword() {
        return keyword;
    }

}
