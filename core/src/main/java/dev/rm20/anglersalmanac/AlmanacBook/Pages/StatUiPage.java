package dev.rm20.anglersalmanac.AlmanacBook.Pages;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AlmanacBook.AlmanacDatabase;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Metadata.MinigamePRating;
import dev.rm20.anglersalmanac.MinigameManager.Minigame;
import dev.rm20.anglersalmanac.Models.BookAssetData;
import dev.rm20.anglersalmanac.Models.FishLootManager;
import dev.rm20.anglersalmanac.Utils.pageUtils;

import javax.annotation.Nonnull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static dev.rm20.anglersalmanac.AlmanacBook.BookPageManager.OpenPage;
import static dev.rm20.anglersalmanac.AlmanacBook.BookPageManager.getPageIndexForZone;
import static dev.rm20.anglersalmanac.Models.BookAssetData.getZoneRank;


public class StatUiPage extends InteractiveCustomUIPage<pageUtils.AlmanacGuiData> {

    private final String PlayerUUID;
    private final String PlayerName;
    private final AlmanacDatabase.PlayerStatsData stats;

    public StatUiPage(PlayerRef playerRef, String playerUUID, String playerName, AlmanacDatabase.PlayerStatsData stats) {
        super(playerRef, CustomPageLifetime.CanDismiss, pageUtils.AlmanacGuiData.CODEC);
        PlayerUUID = playerUUID;
        PlayerName = playerName;
        this.stats = stats;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder,
                      @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {


        uiCommandBuilder.append("Almanac/AlmanacStats.ui");

        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#GlossaryButton",
                EventData.of(pageUtils.AlmanacGuiData.KEY_BUTTON, "Glossary"),
                false
        );

        //AlmanacDatabase db = AnglersAlmanac.getInstance().database;
        //AlmanacDatabase.PlayerStatsData stats = db.getPlayerStats(this.PlayerUUID);
        pageUtils.buildTabs(uiCommandBuilder, uiEventBuilder, 0);
        pageUtils.addDynamicNav(uiCommandBuilder, uiEventBuilder, 0);

        uiCommandBuilder.set("#TotalFish.TextSpans", Message.translation("anglersalmanac.almanac.stats.total").param("count", stats.totalCatches));
        uiCommandBuilder.set("#LegendaryCount.TextSpans", Message.translation("anglersalmanac.almanac.stats.legendary").param("count", stats.legendaryCount));
        int perfects = stats.getRatingCount(MinigamePRating.PerformanceRating.PERFECT);
        uiCommandBuilder.set("#PerfectCount.TextSpans", Message.translation("anglersalmanac.almanac.stats.perfect").param("count", perfects));
        int great = stats.getRatingCount(MinigamePRating.PerformanceRating.GREAT);
        uiCommandBuilder.set("#GreatCount.TextSpans", Message.translation("anglersalmanac.almanac.stats.great").param("count", great));
        uiCommandBuilder.set("#Header.TextSpans", Message.translation("anglersalmanac.almanac.stats.player").param("name", (this.PlayerName != null ? this.PlayerName : Message.translation("anglersalmanac.almanac.unknown").toString())));

        String topFish = "None yet!";

        if (stats.topFish != null && !stats.topFish.isEmpty()) {
            String fishId = stats.topFish.get(0).name();
            var fishData = FishLootManager.getFishData(fishId);

            if (fishData != null) {
                topFish = fishData.getName();
            } else {
                topFish = fishId;
            }
        }
        uiCommandBuilder.set("#MostFound.TextSpans", Message.raw("Most found: " + topFish));

        List<Map.Entry<String, Integer>> sortedFish = stats.catchMap.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(15)
                .toList();
        for (int i = 0; i < 10; i++) {
            String labelId = "#Fish" + (i + 1);

            if (i < sortedFish.size()) {
                Map.Entry<String, Integer> entry = sortedFish.get(i);
                FishLootManager loot = FishLootManager.getFishData(entry.getKey());
                if(loot == null)
                {
                    uiCommandBuilder.set(labelId + ".TextSpans", Message.raw("- " + entry.getKey() + " : " + entry.getValue()));
                }
                else
                {
                    uiCommandBuilder.set(labelId + ".TextSpans", Message.raw("- " + loot.getName() + " : " + entry.getValue()));

                    //todo: FIX this
                    ItemStack item = new ItemStack(loot.getItemID());
                    if(item.isValid())
                    {
                        String itemName =  Message.translation(item.getItem().getTranslationKey()).getAnsiMessage();
                        if(itemName.isEmpty()||itemName.equals("server.items.Unknown.name"))
                        {
                            uiCommandBuilder.set(labelId + ".TextSpans", Message.raw("- " + loot.getName() + " : " + entry.getValue()));
                        }
                        else
                        {
                            uiCommandBuilder.set(labelId + ".TextSpans", Message.raw("- " + itemName + " : " + entry.getValue()));
                        }
                    }
                    else
                    {
                        uiCommandBuilder.set(labelId + ".TextSpans", Message.raw("- " + loot.getName() + " : " + entry.getValue()));
                    }

                }
            } else {
                uiCommandBuilder.set(labelId + ".TextSpans", Message.raw("- ??? : 0"));
            }
        }


        //Right page
        BookAssetData bookAsset = BookAssetData.getMasterMergedBook();
        Map<String, BookAssetData.HabitatProgress> progress = bookAsset.getAllHabitatProgress(PlayerUUID);
        int globalCaught = progress.values().stream().mapToInt(p -> p.caught()).sum();
        int globalTotal = progress.values().stream().mapToInt(p -> p.total()).sum();
        float globalPercent = (globalTotal > 0) ? (float) globalCaught / globalTotal : 0;
        uiCommandBuilder.set("#FishProgress.Value", globalPercent);
        CreateList(uiCommandBuilder, uiEventBuilder, bookAsset);



//        uiEventBuilder.addEventBinding(
//                CustomUIEventBindingType.Activating,
//                "#StatsTabIcon",
//                EventData.of(pageUtils.AlmanacGuiData.KEY_BUTTON, "OpenZone:almanacstats"),
//                false
//        );

        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#GlossaryTabIcon",
                EventData.of(pageUtils.AlmanacGuiData.KEY_BUTTON, "OpenZone:alamanacglossary"),
                false
        );
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull pageUtils.AlmanacGuiData data) {
        super.handleDataEvent(ref, store, data);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null || data.getButton() == null) return;
        if (data.getButton().equals("NextPage")) {
            OpenPage(player, 1, PlayerUUID, PlayerName);
        }
        if(data.getButton().equals("Glossary"))
        {
            OpenPage(player, getPageIndexForZone("alamanacglossary"), PlayerUUID, PlayerName);
        }
        // Zone click
        if (data.getButton().startsWith("OpenZone:")) {
            String zoneName = data.getButton().split(":")[1];
            OpenPage(player, getPageIndexForZone(zoneName), PlayerUUID, PlayerName);
        }
    }


    public Message createProgressBar(int caught, int total) {
        int barWidth = 15;
        int filledCount = (total > 0) ? (int) ((float) caught / total * barWidth) : 0;

        Message bar = Message.raw("[").color("#494950");
        Message filledPart = Message.raw("=".repeat(filledCount)).color("#3498DB");
        Message emptyPart = Message.raw("_".repeat(barWidth - filledCount)).color("#494950");
        Message closingBracket = Message.raw("]").color("#494950");

        return bar.insertAll(filledPart, emptyPart, closingBracket).monospace(true);
    }

    public void CreateList(UICommandBuilder uiCommandBuilder, UIEventBuilder uiEventBuilder, BookAssetData bookAsset) {
        uiCommandBuilder.clear("#ZonessInfo");
        Map<String, BookAssetData.HabitatProgress> allProgress = bookAsset.getAllHabitatProgress(PlayerUUID);
        List<String> sortedZones = allProgress.keySet().stream()
                .sorted((a, b) -> {
                    int rankA = getZoneRank(a);
                    int rankB = getZoneRank(b);
                    if (rankA != rankB) {
                        return Integer.compare(rankA, rankB);
                    }
                    return a.compareToIgnoreCase(b);
                })
                .toList();
        int index = 0;
        for (String zoneName : sortedZones) {
            if (Objects.equals(zoneName, "almanacstats") || Objects.equals(zoneName, "alamanacglossary") ) {
                continue;
            }
            BookAssetData.habitatsInfo habitatData = Arrays.stream(bookAsset.getHabitats())
                    .filter(h -> h.ZoneName.equalsIgnoreCase(zoneName))
                    .findFirst()
                    .orElse(null);

            BookAssetData.HabitatProgress habitat = allProgress.get(zoneName);
            uiCommandBuilder.append("#ZonessInfo", "Almanac/Utils/HabitatEntry.ui");
            String basePath = "#ZonessInfo[" + index + "]";

            // Set name + bar
            String name = zoneName;
            if(habitatData !=null)
            {
                if (habitatData.zoneInfo != null && habitatData.zoneInfo.displayName != null && !habitatData.zoneInfo.displayName.isEmpty()) {
                    name = habitatData.zoneInfo.displayName;
                }
            }

            uiCommandBuilder.set(basePath + " #HabitatHeader.Text", name + ":");

            Message progressBar = createProgressBar(habitat.caught(), habitat.total());
            uiCommandBuilder.set(basePath + " #HabitatProgressText.TextSpans", progressBar);
            // Add Tooltip
            uiCommandBuilder.set(basePath + " #HabitatButton.TooltipText",
                    Message.translation("anglersalmanac.almanac.stats.habitat_tooltip")
                            .param("caught", habitat.caught())
                            .param("total", habitat.total()));
            // Make button work
            uiEventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    basePath + " #HabitatButton",
                    EventData.of(pageUtils.AlmanacGuiData.KEY_BUTTON, "OpenZone:" + zoneName),
                    false
            );
            index++;
        }
    }
}
