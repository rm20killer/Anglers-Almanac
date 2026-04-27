package dev.rm20.anglersalmanac.Utils.Validator;

public enum TimePeriod {
    DAWN("dawn"),
    DAY("day"),
    DUSK("dusk"),
    NIGHT("night"),
    ANY("any");

    public static final TimePeriod[] VALUES = values();
    private final String keyword;

    TimePeriod(String keyword) {
        this.keyword = keyword;
    }

    public String getKeyword() {
        return keyword;
    }

    public static TimePeriod fromKeyword(String keyword) {
        for (TimePeriod period : VALUES) {
            if (period.keyword.equalsIgnoreCase(keyword)) {
                return period;
            }
        }
        return ANY; // Default fallback
    }


    public static TimePeriod fromHour(int hour) {
        if (hour >= 5 && hour < 10) return DAWN;
        if (hour >= 10 && hour < 19) return DAY;
        if (hour >= 19 && hour < 21) return DUSK;
        return NIGHT;
    }
}
