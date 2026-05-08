package dev.rm20.anglersalmanac.Utils;

public class TextUtils {
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
