package dev.rm20.anglersalmanac.Models;

import lombok.Getter;

@Getter
public enum FishSize {
    XS("xs", "XS", 0.50f, -2.0f, 0.70f),
    S("s", "S", 0.75f, -1.0f, 0.85f),
    M("m", "M", 1.00f, 0.0f, 1.00f),
    L("l", "L", 1.25f, 1.0f, 1.25f),
    XL("xl", "XL", 1.50f, 2.0f, 1.50f);

    private final String code;
    private final String displayName;
    private final float valueMultiplier;
    private final float curvePosition;
    private final float scale;

    FishSize(String code, String displayName, float valueMultiplier, float curvePosition, float scale) {
        this.code = code;
        this.displayName = displayName;
        this.valueMultiplier = valueMultiplier;
        this.curvePosition = curvePosition;
        this.scale = scale;
    }

    public static FishSize fromString(String str) {
        if (str == null || str.isBlank()) {
            return M;
        }
        String trimmed = str.trim();
        for (FishSize size : values()) {
            if (size.code.equalsIgnoreCase(trimmed) || size.displayName.equalsIgnoreCase(trimmed)) {
                return size;
            }
        }
        return M;
    }
}
