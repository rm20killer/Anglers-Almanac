package dev.rm20.anglersalmanac.AlmanacBook;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.hypixel.hytale.assetstore.AssetMap;
import com.hypixel.hytale.protocol.ItemBase;
import com.hypixel.hytale.protocol.ItemTranslationProperties;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateItems;
import com.hypixel.hytale.protocol.packets.assets.UpdateTranslations;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.rm20.anglersalmanac.AnglersAlmanac;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;

//TODO: what ever I was doing here.
public class AlmanacBook {
    public static Item syncCustomBookDisplay(PlayerRef playerRef, String playerUuid, String playerName) {
        Item baseItem = Item.getAssetMap().getAsset("Almanac_Book");
        String customId = "almanac.book." + playerUuid;
        Item customItem = CloneItem(customId, baseItem);
        registerItemOnServer(customId, baseItem);

        ItemBase definition = customItem.toPacket().clone();

        definition.id = customId;
        definition.translationProperties = new ItemTranslationProperties(
                customId+".name",
                customId+".description"
        );

        Map<String, String> translations = new HashMap<>();
        AlmanacRepository.saveBookId(playerUuid, customId, playerName);

        translations.put(customId+".name", playerName + "'s Angler's Almanac");
        translations.put(customId+".description", "<color is=\"#AAAAAA\">Bound to ID:</color>\n<i>" + playerUuid + "</i>");

        UpdateTranslations packet = new UpdateTranslations();
        packet.type = UpdateType.AddOrUpdate;
        packet.translations = translations;
        playerRef.getPacketHandler().writeNoCache(packet);

        UpdateItems itemPacket = new UpdateItems();
        itemPacket.type = UpdateType.AddOrUpdate;
        itemPacket.items = new HashMap<>();
        itemPacket.items.put(customId, definition);
        playerRef.getPacketHandler().writeNoCache(itemPacket);
        return customItem;
    }

    public static Item CloneItem(String newId, Item original) {
        Item newItem = new Item(original);

        try {
            java.lang.reflect.Field idField = Item.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(newItem, newId);
        } catch (Exception e) {
            AnglersAlmanac.LOGGER.atSevere().withCause(e).log("Failed to clone item for "+newId);

            return null;
        }

        return newItem;
    }

    public static void registerItemOnServer(String customId, Item baseItem) {
        try {
            AssetMap<String, Item> assetMap = Item.getAssetMap();
            Field mapField = assetMap.getClass().getDeclaredField("assetMap");
            Field lockField = assetMap.getClass().getDeclaredField("assetMapLock");

            mapField.setAccessible(true);
            lockField.setAccessible(true);
            Map<String, Item> internalMap = (Map<String, Item>) mapField.get(assetMap);
            StampedLock lock = (StampedLock) lockField.get(assetMap);
            if (internalMap.containsKey(customId)) return;

            // Thread-safe injection
            long stamp = lock.writeLock();
            try {
                internalMap.put(customId, baseItem);
            } finally {
                lock.unlockWrite(stamp);
            }
        } catch (Exception e) {
            AnglersAlmanac.LOGGER.atSevere().withCause(e).log("Failed to register "+customId+ " on to the server");
        }
    }


    public static void reloadAllItem() {
        Item baseItem = Item.getAssetMap().getAsset("Almanac_Book");
        if (baseItem == null) {
            AnglersAlmanac.LOGGER.atSevere().log("Could not find base Almanac_Book asset during reload!");
            return;
        }

        Map<String, AlmanacRepository.BookEntry> savedBooks = AlmanacRepository.getAllSavedBooks();

        for (Map.Entry<String, AlmanacRepository.BookEntry> entry : savedBooks.entrySet()) {
            String customId = entry.getValue().customId;
            Item customItem = CloneItem(customId, baseItem);
            registerItemOnServer(customId, customItem);
        }

        AnglersAlmanac.LOGGER.atInfo().log("Re-registered " + savedBooks.size() + " custom books from database.");
    }


    @Deprecated
    // Not well optimised
    public static void sendTranslations(PlayerRef playerRef) {
        Item baseItem = Item.getAssetMap().getAsset("Almanac_Book");
        if (baseItem == null) return;

        Map<String, AlmanacRepository.BookEntry> allBooks = AlmanacRepository.getAllSavedBooks();

        if (allBooks.isEmpty()) return;

        Map<String, String> allTranslations = new HashMap<>();
        Map<String, ItemBase> allItemDefinitions = new HashMap<>();

        for (Map.Entry<String, AlmanacRepository.BookEntry> entry : allBooks.entrySet()) {
            String playerUuid = entry.getKey();
            String customId = entry.getValue().customId;
            String playerName = entry.getValue().playerName;

            ItemBase definition = baseItem.toPacket().clone();
            definition.id = customId;
            definition.translationProperties = new ItemTranslationProperties(
                    customId + ".name",
                    customId + ".description"
            );

            allItemDefinitions.put(customId, definition);
            allTranslations.put(customId + ".name", playerName + "'s Angler's Almanac");
            allTranslations.put(customId + ".description", "<color is=\"#AAAAAA\">Bound to ID:</color>\n<i>" + playerUuid + "</i>");
        }

        UpdateTranslations transPacket = new UpdateTranslations();
        transPacket.type = UpdateType.AddOrUpdate;
        transPacket.translations = allTranslations;
        playerRef.getPacketHandler().writeNoCache(transPacket);

        UpdateItems itemPacket = new UpdateItems();
        itemPacket.type = UpdateType.AddOrUpdate;
        itemPacket.items = allItemDefinitions;
        playerRef.getPacketHandler().writeNoCache(itemPacket);

        AnglersAlmanac.LOGGER.atInfo().log("Synced " + allBooks.size() + " almanacs to " + playerRef.getUsername());
    }


    private static final Cache<String, ItemBase> DEFINITION_CACHE = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build();

    public static void invalidateCache()
    {
        DEFINITION_CACHE.invalidateAll();
    }

    private static void sendSingleBookSync(PlayerRef recipient, String targetUuid, String targetName, String customId) {
        ItemBase definition = DEFINITION_CACHE.get(customId, k -> {
            Item baseItem = Item.getAssetMap().getAsset("Almanac_Book");
            Item customItem = CloneItem(customId, baseItem);
            ItemBase def = customItem.toPacket().clone();
            def.id = customId;
            def.translationProperties = new ItemTranslationProperties(customId + ".name", customId + ".description");
            return def;
        });

        UpdateTranslations transPacket = new UpdateTranslations();
        transPacket.type = UpdateType.AddOrUpdate;
        transPacket.translations = Map.of(
                customId + ".name", targetName + "'s Angler's Almanac",
                customId + ".description", "<color is=\"#AAAAAA\">Bound to ID:</color>\n<i>" + targetUuid + "</i>"
        );
        recipient.getPacketHandler().writeNoCache(transPacket);

        UpdateItems itemPacket = new UpdateItems();
        itemPacket.type = UpdateType.AddOrUpdate;
        itemPacket.items = Map.of(customId, definition);
        recipient.getPacketHandler().writeNoCache(itemPacket);
    }

    public static void syncOwnBookOnJoin(PlayerRef player) {
        String uuid = String.valueOf(player.getUuid());
        AlmanacRepository.BookEntry entry = AlmanacRepository.getBookData(uuid);

        if (entry != null) {
            sendSingleBookSync(player, uuid, entry.playerName, entry.customId);
            AnglersAlmanac.LOGGER.atInfo().log("Synced personal almanac to " + player.getUsername());
        }
    }

    public static void syncEncounteredBook(PlayerRef viewer, String bookCustomId) {
        if (!bookCustomId.startsWith("almanac.book.")) return;
        String targetUuid = bookCustomId.replace("almanac.book.", "");
        AlmanacRepository.BookEntry data = AlmanacRepository.getBookData(targetUuid);
        if (data != null) {
            sendSingleBookSync(viewer, targetUuid, data.playerName, bookCustomId);
        }
    }

}
