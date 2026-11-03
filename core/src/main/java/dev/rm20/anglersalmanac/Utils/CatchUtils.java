package dev.rm20.anglersalmanac.Utils;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.util.InventoryHelper;
import dev.rm20.anglersalmanac.AlmanacBook.BookPageManager;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Components.BobberComponent;
import com.hypixel.hytale.server.core.universe.world.WorldMapTracker;
import dev.rm20.anglersalmanac.Metadata.ZoneInfo;
import dev.rm20.anglersalmanac.Utils.EnvironmentParser;
import dev.rm20.anglersalmanac.IEvents.LootCaughtEvent;
import dev.rm20.anglersalmanac.IEvents.PreFishRollEvent;
import dev.rm20.anglersalmanac.Inventory.FishBagComponent;
import dev.rm20.anglersalmanac.Metadata.FishItemData;
import dev.rm20.anglersalmanac.Metadata.FishingContext;
import dev.rm20.anglersalmanac.Metadata.FishingModifier;
import dev.rm20.anglersalmanac.Metadata.MinigamePRating;
import dev.rm20.anglersalmanac.Minigame.Minigame;
import dev.rm20.anglersalmanac.Models.FishBaitData;
import dev.rm20.anglersalmanac.Models.FishLoot;
import dev.rm20.anglersalmanac.Models.FishLootManager;
import dev.rm20.anglersalmanac.Models.FishSize;
import dev.rm20.anglersalmanac.Models.MinigameRodStats;
import dev.rm20.anglersalmanac.api.ICatchManager;
import it.unimi.dsi.fastutil.Pair;
import org.joml.Quaternionf;
import org.joml.Vector3d;

import java.awt.*;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import static dev.rm20.anglersalmanac.Metadata.FishingModifier.mergeModifiers;

public class CatchUtils implements ICatchManager {
    private static final CatchUtils INSTANCE = new CatchUtils();
    public static CatchUtils getInstance() { return INSTANCE; }

    @Override
    public FishLoot firstRoll(Ref<EntityStore> bobberRef, Player player, CommandBuffer<EntityStore> commandBuffer, int depth) {
        return FirstRoll(bobberRef, player, commandBuffer, depth);
    }

    @Override
    public void dropItem(ItemStack loot, Player player, CommandBuffer<EntityStore> commandBuffer, Ref<EntityStore> bobberRef) {
        DropItem(loot, player, commandBuffer, bobberRef);
    }

    @Override
    public void dropLoot(FishLoot loot, Player player, CommandBuffer<EntityStore> commandBuffer, Ref<EntityStore> bobberRef, int rating) {
        DropLoot(loot, player, commandBuffer, bobberRef, rating);
    }

    public static FishLoot FirstRoll(Ref<EntityStore> bobberRef, Player player, CommandBuffer<EntityStore> commandBuffer, int depth) {
        //AnglersAlmanac.LOGGER.atInfo().log("Doing first roll");
        //AnglersAlmanac plugin = AnglersAlmanac.getInstance();

        // Modifiers
        Store<EntityStore> store = bobberRef.getStore();
        BobberComponent bobberComp = store.getComponent(bobberRef, BobberComponent.getComponentType());
        FishingModifier.Modifiers baitMods = null;
        String baitID = null;
        if (bobberComp != null && bobberComp.getBaitName() != null) {
            FishBaitData baitAsset = BaitUtils.getBaitData(bobberComp.getBaitName());
            if (baitAsset != null) {
                baitMods = baitAsset.modifiers;
                baitID = baitAsset.getId();
            }
        }
        FishingModifier.Modifiers rodMods = null;
        InventoryComponent.Hotbar hotbarComp = player.getReference().getStore().getComponent(player.getReference(), InventoryComponent.Hotbar.getComponentType());
        ItemStack fishingRod = hotbarComp != null ? hotbarComp.getActiveItem() : null;
        if (fishingRod != null) {
            rodMods = MinigameRodStats.getModifiersFromRodId(fishingRod.getItemId());
        }

        FishingModifier.Modifiers masterModifier = mergeModifiers(baitMods, rodMods);

        // Location
        var transform = store.getComponent(bobberRef, TransformComponent.getComponentType());
        if (transform == null) return null;

//        double x = transform.getPosition().getX();
        double y = transform.getPosition().y;
//        double z = transform.getPosition().getZ();

        // time info
        WorldTimeResource timeResource = store.getResource(WorldTimeResource.getResourceType());
        var gameTime = timeResource.getGameTime().toString();
//        String timeKeyword = TimeUtils.getTimeKeyword(gameTime);
        int moonPhase = timeResource.getMoonPhase();

        // Habitats info
//        WorldMapTracker worldMapTracker = player.getWorldMapTracker();
//        WorldMapTracker.ZoneDiscoveryInfo currentZone = worldMapTracker.getCurrentZone();
        String Region = "Unknown";
        String Biome = "Unknown";
        String zone = "Unknown";
        int tier = 1;

        if (player != null) {
            try {
                WorldMapTracker worldMapTracker = player.getWorldMapTracker();
                if (worldMapTracker != null) {
                    WorldMapTracker.ZoneDiscoveryInfo currentZone = worldMapTracker.getCurrentZone();
                    if (currentZone != null) {
                        String rawZone = currentZone.regionName();
                        Region = currentZone.zoneName();
                        ZoneInfo info = EnvironmentParser.parse(rawZone);
                        if (info != null) {
                            zone = info.zone();
                            tier = info.tier();
                        }
                    }
                    String currentBiome = worldMapTracker.getCurrentBiomeName();
                    if (currentBiome != null) {
                        Biome = currentBiome;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        TriggerVolumeManager manager = store.getResource(TriggerVolumesPlugin.get().getManagerResourceType());

        String triggerKey = TriggerVolumeUtils.getFishingZone(manager, transform.getPosition());

        int effectiveDepth = Math.max(1, depth);

        // Combine
        FishingContext LocationInfo = new FishingContext(
                TimeUtils.getTimePeriod(gameTime),
                moonPhase,
                zone,
                tier,
                Region,
                Biome,
                y,
                "clear",
                effectiveDepth,
                baitID,
                FishingPowerUtils.getTotalFishingPower(store, player.getReference()),
                triggerKey
        );
        // get fish


        var eventBus = HytaleServer.get().getEventBus();
        PreFishRollEvent event = new PreFishRollEvent(bobberRef, player, LocationInfo);

        eventBus.dispatchFor(PreFishRollEvent.class).dispatch(event);

        FishLoot lootEntry = null;
        if (event.isCancelled()) {
            String itemId = event.getOverriddenLootId();
            String modOverriding = event.getOverridingModName();
            if (itemId == null) {
                itemId = "Stick";
            }
            lootEntry = FishLootManager.getInternalFishData(itemId);
            if (lootEntry == null) {
                AnglersAlmanac.LOGGER.atWarning().log(modOverriding + " tried to override the item to: " + itemId + " which does not exist, fall back to normal system");
                lootEntry = FishLootManager.getRandomWeightedLoot(LocationInfo, masterModifier);
            }
        } else {
            lootEntry = FishLootManager.getRandomWeightedLoot(LocationInfo, masterModifier);
        }

        if (lootEntry == null) {
            lootEntry = FishLootManager.getInternalFishData("Stick");
        }

        if (lootEntry != null) {
            FishLoot rolledFish = lootEntry.copy();
            if (rolledFish.getItemID() != null && rolledFish.getItemID().startsWith("Fish_")) {
                FishSize rolledSize = FishSizeCalculator.rollSize(LocationInfo, masterModifier, rolledFish);
                rolledFish.setFishSize(rolledSize);
            } else {
                rolledFish.setFishSize(null);
            }
            return rolledFish;
        }

        return null;
    }

    //MODIFIED
    public static final ComponentType<EntityStore, ? extends InventoryComponent>[] FISH_ROUTING = new ComponentType[]{
            FishBagComponent.getComponentType()
    };

    public static boolean DropItem(ItemStack loot, Player player, CommandBuffer<EntityStore> commandBuffer, Ref<EntityStore> bobberRef) {
        if (player.getReference() != null) {
            var accessor = player.getReference().getStore();

            if (accessor.getComponent(player.getReference(), FishBagComponent.getComponentType()) == null) {
                commandBuffer.getExternalData().getWorld().execute(() -> {
                    AnglersAlmanac.LOGGER.atInfo().log("Attaching new FishBagComponent to player.");
                    accessor.putComponent(
                            player.getReference(),
                            FishBagComponent.getComponentType(),
                            new FishBagComponent(FishBagComponent.DEFAULT_CAPACITY)
                    );
                    DropItem(loot, player, commandBuffer, bobberRef);
                });
                return true;
            }



            var combined = InventoryComponent.getCombined(accessor, player.getReference(), FISH_ROUTING);
//            AnglersAlmanac.LOGGER.atInfo().log(String.valueOf(combined.getContainersSize()));
            var remainder = combined.addItemStack(loot);
            if (remainder.getRemainder() != null && remainder.getRemainder().getQuantity() > 0) {
                //AnglersAlmanac.LOGGER.atInfo().log("Fish bag is full!");

                Ref<EntityStore> playerRef = player.getReference();
                if(playerRef == null) return false;
                PlayerRef playerRef1 = playerRef.getStore().getComponent(playerRef, PlayerRef.getComponentType());
                if(playerRef1 == null) return false;
                Message titleMessage = Message.join(Message.raw("Fish bag full"));
                titleMessage.color(Color.RED);
                Message subtitleMessage = Message.translation("sell your fish at the Fisher");
                try {
                    var packetHandler = playerRef1.getPacketHandler();
                    NotificationUtil.sendNotification(
                            packetHandler,
                            titleMessage,
                            subtitleMessage,
                            NotificationStyle.Danger);
                } catch (Exception e) {
                    AnglersAlmanac.LOGGER.atWarning().log("Failed to send notification to " + playerRef1.getUsername() + ": " + e.getMessage());
                }
                return false;
            }
            return true;
        }
        return false;
    }

    public static void DropLoot(FishLoot loot, Player player, CommandBuffer<EntityStore> commandBuffer, Ref<EntityStore> bobberRef, int rating) {
        //AnglersAlmanac.LOGGER.atInfo().log(loot.getItemID());
        if (loot == null) return;
        if (loot.getEntityID() !=null)
        {
            AnglersAlmanac.LOGGER.atInfo().log("Spawning entity");
            //spawn entity
            World world = player.getWorld();
            Store<EntityStore> store = world.getEntityStore().getStore();
            Vector3d position = bobberRef.getStore().getComponent(bobberRef, TransformComponent.getComponentType()).getPosition();
            position.y = position.y + 4;
            TransformComponent transform = player.getReference().getStore().getComponent(player.getReference(), TransformComponent.getComponentType());
            Vector3d PlayerPos = transform.getPosition();
            Vector3d direction = new Vector3d(PlayerPos).sub(position).normalize();
            Quaternionf destQuat = new org.joml.Quaternionf().lookAlong((float)direction.x, (float)direction.y, (float)direction.z, 0, 1, 0);
            Rotation3f lookat = new Rotation3f(0, destQuat.y, 0);

            world.execute(() -> {
                Pair<Ref<EntityStore>, INonPlayerCharacter> result = NPCPlugin.get().spawnNPC(store, loot.getEntityID(), null, position, lookat);

                if (result != null) {
                    Ref<EntityStore> npcRef = result.first();
                    INonPlayerCharacter npc = result.second();
                    Vector3d launchVelocity = new Vector3d(direction.x * 15, 1, direction.z * 15).mul(30);
                    Velocity velocity = npcRef.getStore().getComponent(npcRef, Velocity.getComponentType());
                    if (velocity != null) {
                        //AnglersAlmanac.LOGGER.atInfo().log("applied velocity");
                        //AnglersAlmanac.LOGGER.atInfo().log(launchVelocity.toString());
                        velocity.addInstruction(launchVelocity, null, ChangeVelocityType.Add);
                    }
                    else
                    {
                        //AnglersAlmanac.LOGGER.atInfo().log("applied velocity with new compoent");
                        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
                        holder.addComponent(Velocity.getComponentType(), new Velocity(launchVelocity));
                    }
                }
            });

            SaveLoot(player, loot, rating);
            return;
        }
        //loot item
        if (loot.getItemID() == null) return;
        ItemStack fishStack;
        fishStack = InventoryHelper.createItem(loot.getItemID());

        if (fishStack == null) {
            return;
        }
        if (loot.getQuantity() != null) {
            int min = loot.getQuantity().min_amount;
            int max = loot.getQuantity().max_amount;

            if (max <= min) {
                fishStack.withQuantity(min);
            } else {
                int quantity = new Random().nextInt(min, max + 1);
                fishStack.withQuantity(quantity);
            }
        }

        if (loot.getItemID().startsWith("Fish_")) {
            FishSize size = loot.getFishSize() != null ? loot.getFishSize() : FishSize.M;
            fishStack = FishStateManager.applyFishSizeState(fishStack, size);
            fishStack = fishStack.withMetadata(FishItemData.KEYED_CODEC, new FishItemData(size, loot.getSellPrice()));
        }

        float timeTaken = 0.0f;
        if (bobberRef != null && bobberRef.isValid()) {
            try {
                BobberComponent bobberComp = bobberRef.getStore().getComponent(bobberRef, BobberComponent.getComponentType());
                if (bobberComp != null) {
                    timeTaken = bobberComp.getBobberAge();
                }
            } catch (Exception ignored) {
            }
        }

        if (DropItem(fishStack, player, commandBuffer, bobberRef)) {
            sendCatchNotification(player, fishStack, loot, timeTaken);
            SaveLoot(player, loot, rating);
        }
    }

    public static void sendCatchNotification(Player player, ItemStack fishStack, FishLoot loot, float timeTaken) {
        Ref<EntityStore> playerRef = player.getReference();
        if (playerRef == null) return;
        PlayerRef playerRef1 = playerRef.getStore().getComponent(playerRef, PlayerRef.getComponentType());
        if (playerRef1 == null) return;

        ItemWithAllMetadata icon = fishStack.toPacket();

        Message titleMessage = Message.join(Message.translation(fishStack.getItem().getTranslationKey()));
        if (loot.getFishSize() != null) {
            titleMessage = Message.join(titleMessage, Message.raw(" (" + loot.getFishSize().getDisplayName() + ")"));
        }

        Color titleColor = getColor(loot);
        titleMessage.color(titleColor);

        String timeStr;
        if (timeTaken >= 60.0f) {
            int minutes = (int) (timeTaken / 60);
            float seconds = timeTaken % 60;
            timeStr = String.format("%dm %.1fs", minutes, seconds);
        } else if (timeTaken > 0.0f) {
            timeStr = String.format("%.1fs", timeTaken);
        } else {
            timeStr = "<1s";
        }

        String subtitleText = String.format("Time: %s | Value: %.0f coins", timeStr, loot.getSellPrice());
        Message subtitleMessage = Message.raw(subtitleText);
        subtitleMessage.color(Color.YELLOW);

        try {
            var packetHandler = playerRef1.getPacketHandler();
            NotificationUtil.sendNotification(
                    packetHandler,
                    titleMessage,
                    subtitleMessage,
                    icon,
                    NotificationStyle.Success
            );
        } catch (Exception e) {
            AnglersAlmanac.LOGGER.atWarning().log("Failed to send catch notification to " + playerRef1.getUsername() + ": " + e.getMessage());
        }
    }

    private static Color getColor(FishLoot loot) {
        Color titleColor = Color.GREEN;
        if (loot.getRarity() != null) {
            switch (loot.getRarity().toLowerCase()) {
                case "legendary" -> titleColor = Color.YELLOW;
                case "epic" -> Color.MAGENTA.getClass(); // keep magenta
                case "rare" -> Color.CYAN.getClass();
                case "uncommon" -> Color.GREEN.getClass();
                default -> Color.WHITE.getClass();
            }
            switch (loot.getRarity().toLowerCase()) {
                case "legendary" -> titleColor = Color.YELLOW;
                case "epic" -> titleColor = Color.MAGENTA;
                case "rare" -> titleColor = Color.CYAN;
                case "uncommon" -> titleColor = Color.GREEN;
                default -> titleColor = Color.WHITE;
            }
        }
        return titleColor;
    }

    public static void SaveLoot(Player player, FishLoot loot, int ratingScore) {
        long startTime = System.currentTimeMillis();
        //save to database
        var playerRef = player.getReference();
        assert playerRef != null;
        UUIDComponent uuid = playerRef.getStore().getComponent(playerRef, UUIDComponent.getComponentType());
        PlayerRef playerRef1 = playerRef.getStore().getComponent(playerRef, PlayerRef.getComponentType());
        assert uuid != null;
        boolean isLegendary = loot.getRarity().equalsIgnoreCase("Legendary");
        MinigamePRating.PerformanceRating rating = Minigame.getPerformanceRating(ratingScore);
        CompletableFuture.supplyAsync(() -> {
            String lootid = FishLootManager.getIdFromItemID(loot.getItemID());
            if (lootid == null) {
                lootid = loot.getItemID();
            }
            return AnglersAlmanac.getInstance().database.saveCatch(uuid.getUuid().toString(), lootid, isLegendary, rating);
        }).thenAccept(isNewDiscovery -> {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            if (duration > 100) {
                AnglersAlmanac.LOGGER.atWarning().log("Slow DB Write: " + duration + "ms for player " + uuid.getUuid());
            }

            //AnglersAlmanac.LOGGER.atInfo().log("DB Write: " +duration+"ms for player "+uuid.getUuid());


            if (playerRef1 == null) return;
            dispatchCaughtFishEvents(loot, isNewDiscovery, isLegendary, player, ratingScore);
            ItemStack itemStack = new ItemStack(loot.getItemID(),1);
            String fishDisplayName = Message.translation(itemStack.getItem().getTranslationKey()).getAnsiMessage();
            if (loot.getItemID() != null && loot.getItemID().startsWith("Fish_") && loot.getFishSize() != null) {
                fishDisplayName = fishDisplayName + " (" + loot.getFishSize().getDisplayName() + ")";
            }

            if (isNewDiscovery) {
                if (isLegendary) {
                    showDiscoveryUI(playerRef1, fishDisplayName, "anglersalmanac.fishing.caught.legDiscovered", Color.YELLOW);
                    int audio = SoundEvent.getAssetMap().getIndex("AA_Fishing_Book_New_Fish_2");
                    assert player.getWorld() != null;
                    player.getWorld().execute(() -> {
                        SoundUtil.playSoundEvent2dToPlayer(playerRef1, audio, SoundCategory.UI);
                    });
                } else {
                    showDiscoveryUI(playerRef1, fishDisplayName, "anglersalmanac.fishing.caught.newFish", Color.GREEN);
                    int audio = SoundEvent.getAssetMap().getIndex("AA_Fishing_Book_New_Fish_1");
                    assert player.getWorld() != null;
                    player.getWorld().execute(() -> {
                        SoundUtil.playSoundEvent2dToPlayer(playerRef1, audio, SoundCategory.UI);
                    });
                }
            } else {
                showDiscoveryUI(playerRef1, fishDisplayName, "anglersalmanac.fishing.caught.nonFish", Color.GREEN);
                int audio = SoundEvent.getAssetMap().getIndex("AA_Fishing_Book_New_Fish_1");
                assert player.getWorld() != null;
                player.getWorld().execute(() -> {
                    SoundUtil.playSoundEvent2dToPlayer(playerRef1, audio, SoundCategory.UI);
                });
            }
            BookPageManager.invalidateCache(String.valueOf(playerRef1.getUuid()));
        }).exceptionally(ex -> {
            AnglersAlmanac.LOGGER.atSevere().withCause(ex).log("Database error");
            return null;
        });
    }

    public static void dispatchCaughtFishEvents(FishLoot item, boolean isNew, boolean isLegendary, Player player, int ratingScore) {
        var eventBus = HytaleServer.get().getEventBus();
        LootCaughtEvent mainEvent = new LootCaughtEvent(item, isNew, isLegendary, player, ratingScore);
        eventBus.dispatchFor(LootCaughtEvent.class).dispatch(mainEvent);
    }

    private static void showDiscoveryUI(PlayerRef ref, String fishName, String header, Color color) {
        Message fishDisplay = Message.raw(fishName).color(color);
        Message titleHeader = Message.translation(header);
        EventTitleUtil.showEventTitleToPlayer(ref, fishDisplay, titleHeader, false, null, 2, 0.5f, 0.5f);
    }
}
