package dev.rm20.anglersalmanac.Utils;


/**
 * Utility class providing static methods for text manipulation and formatting.
 * <p>
 * This class includes functionality for scrambling text for visual effects
 * and formatting internal strings (like enum names) into human-readable display names.
 * </p>
 */
public class TextUtils {

    /**
     * Scrambles the non-whitespace characters of a string using a random sequence.
     * <p>
     * Each call to this method will likely produce a different result. Whitespace
     * is preserved to maintain the original structure of the input string.
     * </p>
     *
     * @param input The string to be scrambled.
     * @return A scrambled version of the input string; returns an empty string if input is null.
     */
    public static String scrambleText(String input) {
        if (input == null) return "";
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder scrambled = new StringBuilder();
        java.util.Random random = new java.util.Random();

        for (char c : input.toCharArray()) {
            if (Character.isWhitespace(c)) {
                scrambled.append(c);
            } else {
                scrambled.append(characters.charAt(random.nextInt(characters.length())));
            }
        }
        return scrambled.toString();
    }

    /**
     * Scrambles the non-whitespace characters of a string using the input's hash code as a seed.
     * <p>
     * Unlike {@link #scrambleText(String)}, this method is deterministic. Providing the same
     * input string will always result in the same scrambled output.
     * </p>
     *
     * @param input The string to be scrambled.
     * @return A deterministically scrambled version of the input; returns an empty string if input is null.
     */
    public static String seededScrambleText(String input) {
        if (input == null) return "";
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder scrambled = new StringBuilder();
        long seed = input.hashCode();
        java.util.Random random = new java.util.Random(seed);

        for (char c : input.toCharArray()) {
            if (Character.isWhitespace(c)) {
                scrambled.append(c);
            } else {
                scrambled.append(characters.charAt(random.nextInt(characters.length())));
            }
        }
        return scrambled.toString();
    }


    /**
     * Converts an underscored string into a
     * Title Case format
     * <p>
     * This is particularly useful for converting Enum names or constant identifiers
     * into user-friendly display labels.
     * </p>
     *
     * @param input The underscored string to format.
     * @return The formatted title-case string; returns an empty string if input is null or empty.
     */
    public static String formatDisplayName(String input) {
        if (input == null || input.isEmpty()) return "";

        // Split by underscore
        String[] words = input.split("_");
        StringBuilder formatted = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                // Capitalize first letter, lowercase the rest, add a space
                formatted.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        return formatted.toString().trim();
    }
}
