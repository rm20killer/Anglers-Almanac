package dev.rm20.anglersalmanac.Utils;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Rotation3fc;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.ItemUtils;
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
import com.hypixel.hytale.server.core.universe.world.WorldMapTracker;
import com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.util.InventoryHelper;
import dev.rm20.anglersalmanac.AlmanacBook.BookPageManager;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Components.BobberComponent;
import dev.rm20.anglersalmanac.IEvents.LootCaughtEvent;
import dev.rm20.anglersalmanac.Metadata.FishingContext;
import dev.rm20.anglersalmanac.Metadata.FishingModifier;
import dev.rm20.anglersalmanac.Metadata.MinigamePRating;
import dev.rm20.anglersalmanac.Metadata.ZoneInfo;
import dev.rm20.anglersalmanac.MinigameManager.Minigame;
import dev.rm20.anglersalmanac.Models.FishBaitData;
import dev.rm20.anglersalmanac.Models.FishLootManager;
import dev.rm20.anglersalmanac.Models.MinigameRodStats;
import it.unimi.dsi.fastutil.Pair;

import java.awt.*;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import static dev.rm20.anglersalmanac.Metadata.FishingModifier.mergeModifiers;

public class CatchUtils {
    public static FishLootManager FirstRoll(Ref<EntityStore> bobberRef, Player player, CommandBuffer<EntityStore> commandBuffer, int depth) {
        //AnglersAlmanac.LOGGER.atInfo().log("Doing first roll");
        //AnglersAlmanac plugin = AnglersAlmanac.getInstance();

        // Modifiers
        Store<EntityStore> store = bobberRef.getStore();
        BobberComponent bobberComp = store.getComponent(bobberRef, BobberComponent.getComponentType());
        FishingModifier.Modifiers baitMods = null;
        String baitID = null;
        if (bobberComp != null && bobberComp.getBaitName() != null) {
            FishBaitData baitAsset = BaitUtils.getBaitData(bobberComp.getBaitName());
            //AnglersAlmanac.LOGGER.atInfo().log(baitAsset.getId());
            if (baitAsset != null)
            {
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
        String timeKeyword = TimeUtils.getTimeKeyword(gameTime);
        int moonPhase = timeResource.getMoonPhase();

        // Habitats info
        WorldMapTracker worldMapTracker = player.getWorldMapTracker();
        WorldMapTracker.ZoneDiscoveryInfo currentZone = worldMapTracker.getCurrentZone();
        String Region = "Unknown";
        String Biome = "Unknown";
        String zone = "Unknown";
        int tier = 1;
        if (currentZone != null) {
            String RawZone = currentZone.regionName();
            Region = currentZone.zoneName();
            Biome = worldMapTracker.getCurrentBiomeName();
            ZoneInfo info = EnvironmentParser.parse(RawZone);
            zone = info.zone();
            tier = info.tier();
        } else {
            World world = player.getWorld();
            //AnglersAlmanac.LOGGER.atInfo().log(world.getName());
            String worldName = world.getName();
            if (worldName.contains("Portals_Taiga")) {
                zone = "3";
                Region = "Portals_Taiga";
            } else if (worldName.contains("Portals_Hedera")) {
                zone = "3";
                Region = "Portals_Hedera";
            } else if (worldName.contains("Portals_Oasis")) {
                zone = "2";
                Region = "Portals_Oasis";
            } else if (worldName.contains("Portals_Jungles")) {
                Region = "Portals_Jungles";
            } else if (worldName.contains("Portals_Henges")) {
                zone = "3";
                Region = "Portals_Henges";
            }
        }


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
                depth,
                baitID,
                1
        );
        // get fish

        FishLootManager lootEntry = FishLootManager.getRandomWeightedLoot(LocationInfo, masterModifier, FishingPowerUtils.getTotalFishingPower(store,player.getReference()));
        if (lootEntry == null) {
            return FishLootManager.getFishData("Stick");
        }
        String lootID = lootEntry.getItemID();
        return lootEntry;

    }

    public static void DropItem(ItemStack loot, Player player, CommandBuffer<EntityStore> commandBuffer, Ref<EntityStore> bobberRef) {


        assert player.getReference() != null;
        TransformComponent transform = player.getReference().getStore().getComponent(player.getReference(), TransformComponent.getComponentType());

        ItemUtils.interactivelyPickupItem(player.getReference(), loot, transform.getPosition(), commandBuffer);

        //TODO
        //Rework to make it look like the fish is coming from the bobber and fly to the player?

    }

    public static void DropLoot(FishLootManager loot, Player player, CommandBuffer<EntityStore> commandBuffer, Ref<EntityStore> bobberRef, int rating) {
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
                // No idea if the thing above works need testing
            }
        }
        DropItem(fishStack, player, commandBuffer, bobberRef);
        SaveLoot(player, loot, rating);

    }

    public static void SaveLoot(Player player, FishLootManager loot, int ratingScore) {
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
            return AnglersAlmanac.getInstance().database.saveCatch(uuid.getUuid().toString(), loot.getId(), isLegendary, rating);
        }).thenAccept(isNewDiscovery -> {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            if (duration > 100) {
                AnglersAlmanac.LOGGER.atWarning().log("Slow DB Write: " + duration + "ms for player " + uuid.getUuid());
            }

            //AnglersAlmanac.LOGGER.atInfo().log("DB Write: " +duration+"ms for player "+uuid.getUuid());


            if (playerRef1 == null) return;
            dispatchCaughtFishEvents(loot, isNewDiscovery, isLegendary, player, ratingScore);
            if (isNewDiscovery) {
                ItemStack itemStack = new ItemStack(loot.getItemID(),1);
                String fishDisplayName = Message.translation(itemStack.getItem().getTranslationKey()).getAnsiMessage();
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
            }
            BookPageManager.invalidateCache(String.valueOf(playerRef1.getUuid()));
        }).exceptionally(ex -> {
            AnglersAlmanac.LOGGER.atSevere().withCause(ex).log("Database error");
            return null;
        });
    }

    public static void dispatchCaughtFishEvents(FishLootManager item, boolean isNew, boolean isLegendary, Player player, int ratingScore) {
        var eventBus = HytaleServer.get().getEventBus();
        LootCaughtEvent mainEvent = new LootCaughtEvent(item, isNew,isLegendary, player, ratingScore);
        eventBus.dispatchFor(LootCaughtEvent.class).dispatch(mainEvent);
    }

    private static void showDiscoveryUI(PlayerRef ref, String fishName, String header, Color color) {
        Message fishDisplay = Message.raw(fishName).color(color);
        Message titleHeader = Message.translation(header);
        EventTitleUtil.showEventTitleToPlayer(ref, fishDisplay, titleHeader, false, null, 2, 0.5f, 0.5f);
    }
}
