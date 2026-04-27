package dev.rm20.anglersalmanac.Utils;

import com.hypixel.hytale.protocol.Color;

public class ColourUtils {


    public static String toHex(Color color) {
        char[] hexChars = new char[7];
        hexChars[0] = '#';
        String chars = "0123456789ABCDEF";

        int r = color.red & 0xFF;
        int g = color.green & 0xFF;
        int b = color.blue & 0xFF;

        hexChars[1] = chars.charAt(r >>> 4);
        hexChars[2] = chars.charAt(r & 0x0F);
        hexChars[3] = chars.charAt(g >>> 4);
        hexChars[4] = chars.charAt(g & 0x0F);
        hexChars[5] = chars.charAt(b >>> 4);
        hexChars[6] = chars.charAt(b & 0x0F);

        return new String(hexChars);
    }
}
