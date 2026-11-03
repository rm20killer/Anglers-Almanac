package dev.rm20.anglersalmanac.Utils;

import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Models.FishSize;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;

public class FishStateManager {

    private static Field assetMapField;
    private static Field assetMapLockField;
    private static Field itemIdField;
    private static Field itemScaleField;
    private static Field itemVariantField;
    private static Field itemStateToBlockField;
    private static Field itemBlockToStateField;

    static {
        try {
            assetMapField = DefaultAssetMap.class.getDeclaredField("assetMap");
            assetMapField.setAccessible(true);
        } catch (Exception e) {
            AnglersAlmanac.LOGGER.atWarning().withCause(e).log("Failed to reflect DefaultAssetMap.assetMap");
        }

        try {
            assetMapLockField = DefaultAssetMap.class.getDeclaredField("assetMapLock");
            assetMapLockField.setAccessible(true);
        } catch (Exception e) {
            AnglersAlmanac.LOGGER.atWarning().withCause(e).log("Failed to reflect DefaultAssetMap.assetMapLock");
        }

        try {
            itemIdField = Item.class.getDeclaredField("id");
            itemIdField.setAccessible(true);
            itemScaleField = Item.class.getDeclaredField("scale");
            itemScaleField.setAccessible(true);
            itemVariantField = Item.class.getDeclaredField("variant");
            itemVariantField.setAccessible(true);
            itemStateToBlockField = Item.class.getDeclaredField("stateToBlock");
            itemStateToBlockField.setAccessible(true);
            itemBlockToStateField = Item.class.getDeclaredField("blockToState");
            itemBlockToStateField.setAccessible(true);
        } catch (Exception e) {
            AnglersAlmanac.LOGGER.atWarning().withCause(e).log("Failed to reflect Item fields");
        }
    }

    public static void registerAllFishStates() {
        try {
            AssetStore<String, Item, DefaultAssetMap<String, Item>> store = Item.getAssetStore();
            if (store == null) return;
            DefaultAssetMap<String, Item> assetMapObj = store.getAssetMap();
            if (assetMapObj == null) return;

            Map<String, Item> internalMap = getInternalAssetMap(assetMapObj);
            if (internalMap == null) return;

            for (String itemId : new ArrayList<>(internalMap.keySet())) {
                if (itemId != null && itemId.startsWith("Fish_") && !itemId.startsWith("*")) {
                    Item item = internalMap.get(itemId);
                    if (item != null) {
                        ensureFishStates(item, assetMapObj, internalMap);
                    }
                }
            }
        } catch (Exception e) {
            AnglersAlmanac.LOGGER.atWarning().withCause(e).log("Error registering all fish states");
        }
    }

    public static void ensureFishStates(@Nonnull Item parentItem) {
        try {
            AssetStore<String, Item, DefaultAssetMap<String, Item>> store = Item.getAssetStore();
            if (store == null) return;
            DefaultAssetMap<String, Item> assetMapObj = store.getAssetMap();
            if (assetMapObj == null) return;

            Map<String, Item> internalMap = getInternalAssetMap(assetMapObj);
            if (internalMap == null) return;

            ensureFishStates(parentItem, assetMapObj, internalMap);
        } catch (Exception e) {
            AnglersAlmanac.LOGGER.atWarning().withCause(e).log("Error ensuring fish states for item " + parentItem.getId());
        }
    }

    public static void ensureFishStates(@Nonnull Item parentItem,
                                        @Nonnull DefaultAssetMap<String, Item> assetMapObj,
                                        @Nonnull Map<String, Item> internalMap) {
        String parentId = parentItem.getId();
        if (parentId == null || !parentId.startsWith("Fish_") || parentId.startsWith("*")) {
            return;
        }

        Map<String, String> stateToBlock = getOrCreateStateToBlock(parentItem);
        Map<String, String> blockToState = getOrCreateBlockToState(parentItem);
        if (stateToBlock == null || blockToState == null) return;

        StampedLock lock = getAssetMapLock(assetMapObj);

        for (FishSize size : FishSize.values()) {
            String stateName = size.name();
            if (stateToBlock.containsKey(stateName)) {
                continue;
            }

            String childItemId = "*" + parentId + "_State_" + stateName;

            Item childItem = new Item(parentItem);
            try {
                if (itemIdField != null) itemIdField.set(childItem, childItemId);
                if (itemVariantField != null) itemVariantField.set(childItem, true);
                if (itemScaleField != null) itemScaleField.set(childItem, parentItem.getScale() * size.getScale());
                childItem.invalidatePacketCache();
            } catch (Exception ex) {
                AnglersAlmanac.LOGGER.atWarning().withCause(ex).log("Failed to configure child item: " + childItemId);
                continue;
            }

            if (lock != null) {
                long stamp = lock.writeLock();
                try {
                    internalMap.put(childItemId, childItem);
                } finally {
                    lock.unlockWrite(stamp);
                }
            } else {
                internalMap.put(childItemId, childItem);
            }

            stateToBlock.put(stateName, childItemId);
            blockToState.put(childItemId, stateName);
        }
    }

    @Nonnull
    public static String getBaseItemId(@Nonnull String itemId) {
        if (itemId == null || itemId.isEmpty()) return "";
        if (!itemId.startsWith("*")) {
            return itemId;
        }
        String stripped = itemId.substring(1);
        int stateIdx = stripped.indexOf("_State_");
        if (stateIdx != -1) {
            return stripped.substring(0, stateIdx);
        }
        for (FishSize size : FishSize.values()) {
            String suffix = "_" + size.name();
            if (stripped.endsWith(suffix)) {
                return stripped.substring(0, stripped.length() - suffix.length());
            }
        }
        return stripped;
    }

    @Nonnull
    public static ItemStack applyFishSizeState(@Nonnull ItemStack stack, @Nullable FishSize size) {
        if (stack.getItem() == null) return stack;
        String itemId = stack.getItemId();
        if (itemId == null || !itemId.startsWith("Fish_")) {
            return stack;
        }

        FishSize targetSize = (size != null) ? size : FishSize.M;
        String stateName = targetSize.name();

        ensureFishStates(stack.getItem());

        if (stack.getItem().getItemIdForState(stateName) != null) {
            try {
                return stack.withState(stateName);
            } catch (Exception e) {
                AnglersAlmanac.LOGGER.atWarning().withCause(e).log("Failed to apply state " + stateName + " to " + itemId);
            }
        }
        return stack;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static Map<String, Item> getInternalAssetMap(@Nonnull DefaultAssetMap<String, Item> assetMapObj) {
        if (assetMapField == null) return null;
        try {
            return (Map<String, Item>) assetMapField.get(assetMapObj);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private static StampedLock getAssetMapLock(@Nonnull DefaultAssetMap<String, Item> assetMapObj) {
        if (assetMapLockField == null) return null;
        try {
            return (StampedLock) assetMapLockField.get(assetMapObj);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static Map<String, String> getOrCreateStateToBlock(@Nonnull Item item) {
        if (itemStateToBlockField == null) return null;
        try {
            Map<String, String> map = (Map<String, String>) itemStateToBlockField.get(item);
            if (map == null) {
                map = new HashMap<>();
                itemStateToBlockField.set(item, map);
            } else if (!(map instanceof HashMap)) {
                map = new HashMap<>(map);
                itemStateToBlockField.set(item, map);
            }
            return map;
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static Map<String, String> getOrCreateBlockToState(@Nonnull Item item) {
        if (itemBlockToStateField == null) return null;
        try {
            Map<String, String> map = (Map<String, String>) itemBlockToStateField.get(item);
            if (map == null) {
                map = new HashMap<>();
                itemBlockToStateField.set(item, map);
            } else if (!(map instanceof HashMap)) {
                map = new HashMap<>(map);
                itemBlockToStateField.set(item, map);
            }
            return map;
        } catch (Exception e) {
            return null;
        }
    }
}
