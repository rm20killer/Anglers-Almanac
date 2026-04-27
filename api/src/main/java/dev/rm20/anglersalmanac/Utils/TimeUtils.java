package dev.rm20.anglersalmanac.Utils;

import dev.rm20.anglersalmanac.Utils.Validator.TimePeriod;

import java.time.ZonedDateTime;

public class TimeUtils {

    public static TimePeriod getTimePeriod(String hytaleTimestamp)
    {
        try {
            ZonedDateTime time = ZonedDateTime.parse(hytaleTimestamp);
            int hour = time.getHour();
            return TimePeriod.fromHour(hour);
        }
        catch (Exception e) {
            //fall back to day
            return TimePeriod.ANY;
        }
    }
    /**
     * @param hytaleTimestamp worldTimeResource.getGameTime().toString();
     * @return dawn, day, dusk, night
     */
    public static String getTimeKeyword(String hytaleTimestamp) {
        try {
            ZonedDateTime time = ZonedDateTime.parse(hytaleTimestamp);
            int hour = time.getHour();

            // Fishing Time Windows
            if (hour >= 5 && hour < 10) {
                return "dawn"; // 5 to 9.59
            } else if (hour >= 10 && hour < 19) {
                return "day"; // 10 to 18.59
            } else if (hour >= 19 && hour < 21) {
                return "dusk"; // 19 to 20.59
            } else {
                return "night"; // 21 to 4.59
            }
        } catch (Exception e) {
            //fall back to day
            return "day";
        }
    }
}
