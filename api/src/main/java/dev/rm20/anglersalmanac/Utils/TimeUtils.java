package dev.rm20.anglersalmanac.Utils;

import dev.rm20.anglersalmanac.Utils.Validator.TimePeriod;

import java.time.ZonedDateTime;

/**
 * Utility class for parsing and categorizing game time from Hytale timestamps.
 * <p>
 * This class translates ZonedDateTime strings into specific fishing time windows
 * and {@link TimePeriod} enums used throughout the almanac.
 * </p>
 */
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
     * Categorizes a Hytale timestamp into a specific fishing-related keyword.
     * <p>
     * The time windows are defined as follows:
     * <ul>
     *   <li><b>dawn:</b> 05:00 - 09:59</li>
     *   <li><b>day:</b> 10:00 - 18:59</li>
     *   <li><b>dusk:</b> 19:00 - 20:59</li>
     *   <li><b>night:</b> 21:00 - 04:59</li>
     * </ul>
     * </p>
     *
     * @param hytaleTimestamp worldTimeResource.getGameTime().toString()
     * @return A string keyword: "dawn", "day", "dusk", or "night". Defaults to "day" on error.
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
