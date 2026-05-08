package dev.rm20.anglersalmanac.Utils;

import java.util.HashMap;
import java.util.Map;

public class StampUtil {
    private static final Map<String, String[]> STAMP_REGISTRY = new HashMap<>();

    static {
        STAMP_REGISTRY.put("common", new String[]{"Common", "Common1", "Common2"});
        STAMP_REGISTRY.put("uncommon", new String[]{"Uncommon", "Uncommon1", "Uncommon2"});
        STAMP_REGISTRY.put("rare", new String[]{"Rare", "Rare1", "Rare2"});
        STAMP_REGISTRY.put("epic", new String[]{"Epic", "Epic1", "Epic2"});
        STAMP_REGISTRY.put("legendary", new String[]{"Legendary", "Legendary1", "Legendary2"});
    }

    public static String getStamp(String input, String rarity) {
        String[] fileList = STAMP_REGISTRY.get(rarity.toLowerCase());

        if (fileList == null || fileList.length == 0) {
            return null;
        }
        int hash = input.hashCode();
        hash = ((hash >>> 16) ^ hash) * 0x45d9f3b;
        int index = Math.abs(hash) % fileList.length;
        return fileList[index];
    }
}
