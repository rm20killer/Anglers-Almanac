package dev.rm20.anglersalmanac.AlmanacBook.Pages;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AlmanacBook.AlmanacDatabase;
import dev.rm20.anglersalmanac.AlmanacBook.BookPageManager;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Models.BookAssetData;
import dev.rm20.anglersalmanac.Models.FishLootManager;
import dev.rm20.anglersalmanac.Utils.pageUtils;

import javax.annotation.Nonnull;
import java.util.List;

import static dev.rm20.anglersalmanac.AlmanacBook.BookPageManager.OpenPage;
import static dev.rm20.anglersalmanac.AlmanacBook.BookPageManager.getPageIndexForZone;

public class GlossaryPage extends InteractiveCustomUIPage<pageUtils.AlmanacGuiData> {
    private final String PlayerUUID;
    private final String PlayerName;
    private final int Page;

    public GlossaryPage(PlayerRef playerRef, String playerUUID, String playerName, int page) {
        super(playerRef, CustomPageLifetime.CanDismiss, pageUtils.AlmanacGuiData.CODEC);
        PlayerUUID = playerUUID;
        PlayerName = playerName;
        Page = page;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("Almanac/Fish/AlamanacGlossary.ui");
        pageUtils.addDynamicNav(uiCommandBuilder, uiEventBuilder, Page);
        pageUtils.buildTabs(uiCommandBuilder, uiEventBuilder, Page);

        BookAssetData bookAsset = BookAssetData.getMasterMergedBook();
        List<BookAssetData.FishEntry> Fish = bookAsset.getAllFish();
        List<BookAssetData.FishEntry> validFishItems = Fish.stream()
                .filter(BookAssetData.FishEntry::isItem)
                .toList();
        buildDynamicFishGrid(validFishItems, PlayerUUID, uiCommandBuilder, uiEventBuilder);


        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#StatsTabIcon",
                EventData.of(pageUtils.AlmanacGuiData.KEY_BUTTON, "OpenZone:almanacstats"),
                false
        );

//        uiEventBuilder.addEventBinding(
//                CustomUIEventBindingType.Activating,
//                "#GlossaryTabIcon",
//                EventData.of(pageUtils.AlmanacGuiData.KEY_BUTTON, "OpenZone:alamanacglossary"),
//                false
//        );
    }

    private void buildDynamicFishGrid(List<BookAssetData.FishEntry> validFishItems, String playerUUID, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder) {
        uiCommandBuilder.clear("#LeftFishGridContainer");
        AlmanacDatabase.PlayerStatsData stats = BookPageManager.getStats(playerUUID);
        int itemsPerRow = 5;
        int maxSlots = 50;
        int limit = Math.min(validFishItems.size(), maxSlots);
        for (int i = 0; i < limit; i++) {
            int rowIndex = i / itemsPerRow;
            int colIndex = i % itemsPerRow;
            String rowId = "#FishRow" + rowIndex;
            if (colIndex == 0) {
                uiCommandBuilder.appendInline("#LeftFishGridContainer",
                        "Group " + rowId + " { LayoutMode: Left; Anchor: (Top: 10, Left: 10); }");
            }
            uiCommandBuilder.append(rowId, "Almanac/Utils/FishItemSlot.ui");
            String slotPath = rowId + "[" + colIndex + "]";

            BookAssetData.FishEntry currentFish = validFishItems.get(i);
            FishLootManager actualItem = FishLootManager.getFishData(currentFish.id());

            uiEventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    slotPath + " #FishButton",
                    EventData.of(pageUtils.AlmanacGuiData.KEY_BUTTON, "OpenFish:" + currentFish.id()),
                    false
            );

            boolean hasCaught = stats.hasCaught(currentFish.id());

            if (hasCaught && actualItem != null) {
                uiCommandBuilder.set(slotPath + " #ItemIcon.ItemId", actualItem.getItemID());
                uiCommandBuilder.set(slotPath + " #FishButton.TooltipText", actualItem.getName());
                uiCommandBuilder.set(slotPath + " #ItemIconMissing.Visible", false);
                uiCommandBuilder.set(slotPath + " #ItemGradient.Background", FishLootManager.getRarityColour(actualItem.getRarity()));
            } else {
                uiCommandBuilder.set(slotPath + " #ItemIcon.ItemId", "");
                uiCommandBuilder.set(slotPath + " #ItemIconMissing.Visible", true);
                uiCommandBuilder.set(slotPath + " #FishButton.TooltipText", "Unknown Fish");
                uiCommandBuilder.set(slotPath + " #ItemGradient.Visible", false);

            }
        }
    }


    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull pageUtils.AlmanacGuiData data) {
        super.handleDataEvent(ref, store, data);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null || data.getButton() == null) return;
        if (data.getButton().equals("PrevPage")) {
            OpenPage(player, (Page - 1), PlayerUUID, PlayerName);
        }
        if (data.getButton().equals("NextPage")) {
            int newPage = BookPageManager.getNextPage(Page);
            if (newPage == Page) {
                return;
            }
            OpenPage(player, newPage, PlayerUUID, PlayerName);
        }

        if (data.getButton().startsWith("OpenFish:")) {
            String fishId = data.getButton().split(":")[1];
            int targetPage = BookPageManager.getPageIndexForFish(fishId);
            if (targetPage != -1) {
                BookPageManager.OpenPage(player, targetPage, PlayerUUID, PlayerName);
            }
        }


        // Zone click
        if (data.getButton().startsWith("OpenZone:")) {
            String zoneName = data.getButton().split(":")[1];
            OpenPage(player, getPageIndexForZone(zoneName), PlayerUUID, PlayerName);
        }

    }

}
