package dev.rm20.anglersalmanac.Components;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.UUIDUtil;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Config.MinigameConfig_TensionBar;
import dev.rm20.anglersalmanac.Metadata.RodStats;
import dev.rm20.anglersalmanac.MinigameManager.Minigame;
import dev.rm20.anglersalmanac.MinigameManager.MinigameManager;
import dev.rm20.anglersalmanac.Models.FishLootManager;
import dev.rm20.anglersalmanac.Models.MinigameRodStats;
import dev.rm20.anglersalmanac.Utils.TransformUtils;
import dev.rm20.anglersalmanac.Utils.Validator.GameIcon;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class MinigameComponent_TensionBar extends Minigame implements Component<EntityStore> {
    public static ComponentType<EntityStore, MinigameComponent_TensionBar> COMPONENT_TYPE;


    public MinigameComponent_TensionBar() {

    }

    private boolean DEBUG = true;

    // Internal use:
    public float fightProgress = 0.25f; // The progress to successful catch. Success when progress is at 1f.
    public float fishPos = 0f; // The position of the fish in the bar as a scale from 0 - 1.
    public float barPos = 0f; // The position of the catch bar.
    public float barVelocity = 0f; // The current direction and speed of the fishing bar.
    public float nextFishMoveTime = 0.5f; // The time until the next fish movement.
    public float fishMoveTimer = 0f; // Counts up until next fish move.
    public float fishVelocity = 0f; // The movement of the fish.
    public float fishTargetVelocity = 0f; // The desired movement of the fish.
    public Ref<EntityStore> ownerRef;
    public Ref<EntityStore> bobberRef;
    public UUID selfUUID;

    public enum Trigger {NOTRIGGER, FISHMOVE, SUCCESS, FAIL, DONE}

    public Trigger stateTrigger = Trigger.NOTRIGGER;
    public UUID minigameFishModelId;
    //public UUID minigameBarModelId;
    public HashMap<String, UUID> gameModels = new HashMap<>();
    public UUID audioPlayerId;
    public float minigameScale = 2f; // The visual size of the minigame display, adjusted based on distance from bobber.

    public String[] reelInSounds = {"AA_Fishing_Reel_Slow0", "AA_Fishing_Reel_Slow1", "AA_Fishing_Reel_Slow2", "AA_Fishing_Reel_Slow3"};
    public String[] escapeSounds = {"AA_Fishing_Line_Tension0", "AA_Fishing_Line_Tension1", "AA_Fishing_Line_Tension2", "AA_Fishing_Line_Tension3"};
    public String[] barMidSectionModels = {"AA_TensionBar_Mid0", "AA_TensionBar_Mid1", "AA_TensionBar_Mid2"};
    public List<UUID> barModelEntityIds = new ArrayList<>();

    public MinigameConfig_TensionBar gameConfig;
    public FishLootManager.MinigameStats fishStats;

    // Used for calculating performance:
    public int ticksReeling = 0;
    public int ticksEscaping = 0;


    public MinigameComponent_TensionBar(Ref<EntityStore> ownerPlayerRef, Ref<EntityStore> bobberRef, UUID selfUUID) {
        this.ownerRef = ownerPlayerRef;
        this.bobberRef = bobberRef;
        this.selfUUID = selfUUID;
    }

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        MinigameComponent_TensionBar component = new MinigameComponent_TensionBar(this.ownerRef, this.bobberRef, this.selfUUID);
        component.setTimePlayed(getTimePlayed());
        component.setPoints(getPoints());
        component.setPerfectScore(getPerfectScore());
        component.stateTrigger = this.stateTrigger;
        return component;
    }

    public static ComponentType<EntityStore, MinigameComponent_TensionBar> getComponentType() {
        return COMPONENT_TYPE;
    }


    public static MinigameComponent_TensionBar spawnMinigame(CommandBuffer<EntityStore> commandBuffer, Ref<EntityStore> playerRef, Ref<EntityStore> bobberRef, String rodAssetId) {
        Vector3d bobberPos = commandBuffer.getComponent(bobberRef, TransformComponent.getComponentType()).getPosition().clone();

        // Set up and spawn minigame entity:
        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();

        UUID id = UUIDUtil.generateVersion3UUID();
        holder.addComponent(UUIDComponent.getComponentType(), new UUIDComponent(id));

        Vector3d spawnPos = commandBuffer.getComponent(bobberRef, TransformComponent.getComponentType()).getPosition().clone();
        holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(spawnPos.add(0, AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().minigameModelVerticalOffset, 0), Vector3f.ZERO));


        //  --- Minigame --------
        MinigameComponent_TensionBar game = new MinigameComponent_TensionBar(playerRef, bobberRef, id);
        // Set minigame config as defaults.
        game.gameConfig = AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().clone();
        // Assign fish and apply modifiers.
        game.fishHooked = MinigameManager.FirstRoll(bobberRef, commandBuffer.getComponent(playerRef, Player.getComponentType()), commandBuffer, commandBuffer.getComponent(bobberRef, BobberComponent.getComponentType()).getWaterDepth());
        assert game.fishHooked != null;
        //AnglersAlmanac.LOGGER.atInfo().log("Loading modifiers for fish: %s", game.fishHooked.getName());
        game.applyFishModifiers(game.fishHooked.getMinigameStats());
        // Apply rod modifiers.
        //AnglersAlmanac.LOGGER.atInfo().log("RodAssetId String = %s", rodAssetId);
        RodStats rodStats = MinigameRodStats.getRodStatsFromRodId(rodAssetId);
        game.applyRodModifiers(rodStats);
        holder.addComponent(MinigameComponent_TensionBar.COMPONENT_TYPE, game);

        //-----------------------

        // DEBUG
        //AnglersAlmanac.LOGGER.atInfo().log("Fish diffiucly = %s, behaviour = %s, stamina = %s", game.fishHooked.getMinigameStats().difficulty, game.fishHooked.getMinigameStats().behavior, game.fishHooked.getMinigameStats().stamina);


        // Adjust minigame initialisation variables:
        double distanceFromPlayer = bobberPos.distanceTo(commandBuffer.getComponent(playerRef, TransformComponent.getComponentType()).getPosition());
        game.minigameScale = Math.clamp((float) distanceFromPlayer * AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().minigameScaleMultiplier, AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().minigameScaleMin, AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().minigameScaleMax);

        commandBuffer.getExternalData().getWorld().execute(() -> {
            commandBuffer.addEntity(holder, AddReason.SPAWN);
        });
        String fishIcon = "SSF_FishIcon";
        if(game.fishHooked.getMinigameStats() !=null)
        {
            if(game.fishHooked.getMinigameStats().gameIcon != null)
            {
                fishIcon = GameIcon.getModelId(game.fishHooked.getMinigameStats().gameIcon);
            }
        }
        else
        {
            AnglersAlmanac.LOGGER.atWarning().log("Missing fish icon on" + game.fishHooked.getId());
        }
        game.spawnMinigameAdditionals(commandBuffer, spawnPos.clone(),fishIcon);

        return game;
    }

    public void despawnSelf(World world) {

        //AnglersAlmanac.LOGGER.atInfo().log("Fishing Minigame is despawning itself.");

        Store<EntityStore> store = world.getEntityStore().getStore();

        // Attempt to despawn additional models.
        List<Ref<EntityStore>> toRemove = new ArrayList<>();
        for (UUID id : gameModels.values()) {
            Ref<EntityStore> ref = world.getEntityRef(id);
            if (ref != null && ref.isValid()) toRemove.add(ref);
        }

        world.execute(() -> {
            for (Ref<EntityStore> ref : toRemove) {
                if (ref.isValid()) {
                    try {
                        store.removeEntity(ref, RemoveReason.REMOVE);
                    } catch (RuntimeException e) {
                        AnglersAlmanac.LOGGER.atWarning().withCause(e).log("Failed to remove gameModels");
                    }
                }
            }
            gameModels.clear();
        });
        barModelEntityIds.clear();


        // Despawn audio
        if (audioPlayerId != null) {
            Ref<EntityStore> audioPlayerRef = world.getEntityRef(audioPlayerId);
            if (audioPlayerRef != null) {
                world.execute(() -> {
                    if (audioPlayerRef.isValid()) {
                        try {
                            store.removeEntity(audioPlayerRef, RemoveReason.REMOVE);
                        } catch (RuntimeException e) {
                            AnglersAlmanac.LOGGER.atWarning().withCause(e).log("Failed to remove audioPlayer");
                        }
                    }
                });
            }
        }


        // Despawn self.
        Ref<EntityStore> selfRef = store.getExternalData().getRefFromUUID(selfUUID);
        if (selfRef != null) {
            world.execute(() -> {
                if (selfRef.isValid()) {
                    try {
                        store.removeEntity(selfRef, RemoveReason.REMOVE);
                    } catch (RuntimeException e) {
                        AnglersAlmanac.LOGGER.atWarning().withCause(e).log("Failed to remove self");
                    }
                }
            });
        }
    }

    public void spawnMinigameAdditionals(CommandBuffer<EntityStore> commandBuffer, Vector3d gamePos, String fishIcon) {

        Vector3d bobberPos = commandBuffer.getComponent(bobberRef, TransformComponent.getComponentType()).getPosition().clone();

        Vector3d playerPos = commandBuffer.getComponent(ownerRef, TransformComponent.getComponentType()).getPosition().clone();
        float camOffset = commandBuffer.getComponent(ownerRef, ModelComponent.getComponentType()).getModel().getEyeHeight();
        Vector3d playerHeadPos = playerPos.clone().add(new Vector3d(0, camOffset, 0));

        // Spawn audio player
        AudioPlayerComponent apc = AudioPlayerComponent.spawnNewAudioPlayerEntity(bobberPos, commandBuffer, ownerRef);
        apc.addSounds(reelInSounds);
        apc.allowedOverlap = 30000000;
        audioPlayerId = apc.selfUUID;


        // ------- FISH MODEL -------------------------------------------------------------------
        Holder<EntityStore> fishModelEntity = EntityStore.REGISTRY.newHolder();
        UUID fishModelId = UUIDUtil.generateVersion3UUID();
        fishModelEntity.addComponent(UUIDComponent.getComponentType(), new UUIDComponent(fishModelId));
        gameModels.put("fish", fishModelId);


        // Assign transform to minigame and move it above the bobber.
        fishModelEntity.addComponent(TransformComponent.getComponentType(), new TransformComponent());
        Vector3d newPos = gamePos.clone();
        newPos = newPos.add(new Vector3d(0, (fishPos * minigameScale), 0));
        fishModelEntity.getComponent(TransformComponent.getComponentType()).setPosition(newPos.clone());
        TransformUtils.applyBillboard(commandBuffer.getExternalData().getRefFromUUID(gameModels.get("fish")), ownerRef, new Vector3f(90, 0, 0), commandBuffer);

        // Add model.
        ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(fishIcon);
        if (modelAsset == null) modelAsset = ModelAsset.DEBUG;
        Model model = Model.createScaledModel(modelAsset, 0.5f * minigameScale);
        fishModelEntity.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
        fishModelEntity.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
        fishModelEntity.addComponent(BoundingBox.getComponentType(), new BoundingBox(model.getBoundingBox()));


        // Attach network component.
        fishModelEntity.addComponent(NetworkId.getComponentType(), new NetworkId(commandBuffer.getExternalData().takeNextNetworkId()));

        // Spawn the model in the world.
        World world = commandBuffer.getExternalData().getWorld();
        world.execute(() -> {
            commandBuffer.addEntity(fishModelEntity, AddReason.SPAWN);
            //AnglersAlmanac.LOGGER.atInfo().log("Spawned fish model at: %s", fishModelEntity.getComponent(TransformComponent.getComponentType()).getPosition());
        });


        //---------- BAR MODEL -----------------------------------------------------

        float barSizeMinusEnds = (gameConfig.barRadius * 2f) - 0.03125f; // 0.03125 = (1/64) * 2  --- 0-1 range to block texel size * 2.
        if (barSizeMinusEnds < 0) barSizeMinusEnds = 0;
        int numSections = Math.round(barSizeMinusEnds * 64.0f) + 2;

        //AnglersAlmanac.LOGGER.atInfo().log("barRadius: %s, numSections %s", gameConfig.barRadius, numSections);

        for (int i = 0; i < numSections; i++) {
            // For amount needed.
            Holder<EntityStore> barModelEntity = EntityStore.REGISTRY.newHolder();
            UUID barModelEntityId = UUID.randomUUID();
            barModelEntity.addComponent(UUIDComponent.getComponentType(), new UUIDComponent(barModelEntityId));
            gameModels.put("bar_" + i, barModelEntityId);
            barModelEntityIds.add(barModelEntityId);

            // Assign transform to minigame and move it above the bobber.
            barModelEntity.addComponent(TransformComponent.getComponentType(), new TransformComponent());
            Vector3d newBarPos = gamePos.clone();
            newBarPos.add(new Vector3d(0, (barPos * minigameScale), 0));
            Vector3d layering = newBarPos.clone().add(TransformUtils.moveAwayFrom(newBarPos.clone(), playerPos.clone(), 0.2));
            newBarPos = new Vector3d(layering.x, newBarPos.y, layering.z);
            barModelEntity.getComponent(TransformComponent.getComponentType()).setPosition(newBarPos.clone());


            // Add model.
            String modelName = barMidSectionModels[new Random().nextInt(barMidSectionModels.length)];
            if (i == 0 || i == numSections - 1) {
                modelName = "AA_TensionBar_End";
            }

            ModelAsset barModelAsset = ModelAsset.getAssetMap().getAsset(modelName);
            if (barModelAsset == null) barModelAsset = ModelAsset.DEBUG;
            Model barModel = Model.createScaledModel(barModelAsset, minigameScale);

            ModelComponent barModelComponent = new ModelComponent(barModel);
            barModelEntity.addComponent(PersistentModel.getComponentType(), new PersistentModel(barModel.toReference()));
            barModelEntity.addComponent(ModelComponent.getComponentType(), barModelComponent);
            barModelEntity.addComponent(BoundingBox.getComponentType(), new BoundingBox(barModel.getBoundingBox()));

            barModelEntity.addComponent(NetworkId.getComponentType(), new NetworkId(commandBuffer.getExternalData().takeNextNetworkId()));

            world.execute(() -> {
                commandBuffer.addEntity(barModelEntity, AddReason.SPAWN);
            });
        }


        // DEBUG BAR
        /*
        Holder<EntityStore> barModelEntity = EntityStore.REGISTRY.newHolder();
        UUID barModelEntityId = UUID.randomUUID();
        barModelEntity.addComponent(UUIDComponent.getComponentType(), new UUIDComponent(barModelEntityId));
        gameModels.put("bar_DEBUG", barModelEntityId);

        // Assign transform to minigame and move it above the bobber.
        barModelEntity.addComponent(TransformComponent.getComponentType(), new TransformComponent());
        Vector3d newBarPos = gamePos.clone();
        newBarPos.add(new Vector3d(0, (barPos * minigameScale), 0));
        Vector3d barLayering = newBarPos.clone().add(TransformUtils.moveAwayFrom(newBarPos.clone(), playerPos.clone(), 0.2));
        newBarPos = new Vector3d(barLayering.x, newBarPos.y, barLayering.z);
        barModelEntity.getComponent(TransformComponent.getComponentType()).setPosition(newBarPos.clone().add(new Vector3d(1.2, 0, 0)));

        ModelAsset barModelAsset = ModelAsset.getAssetMap().getAsset("SSF_FishingBar");
        if (barModelAsset == null) barModelAsset = ModelAsset.DEBUG;
        Model barModel = Model.createScaledModel(barModelAsset, (gameConfig.barRadius* 2f) * minigameScale);

        ModelComponent barModelComponent = new ModelComponent(barModel);
        barModelEntity.addComponent(PersistentModel.getComponentType(), new PersistentModel(barModel.toReference()));
        barModelEntity.addComponent(ModelComponent.getComponentType(), barModelComponent);
        barModelEntity.addComponent(BoundingBox.getComponentType(), new BoundingBox(barModel.getBoundingBox()));

        barModelEntity.addComponent(NetworkId.getComponentType(), new NetworkId(commandBuffer.getExternalData().takeNextNetworkId()));

        world.execute(() -> {commandBuffer.addEntity(barModelEntity, AddReason.SPAWN);});

         */


        //---------- Frame MODEL -----------------------------------------------------
        Holder<EntityStore> frameUpperModelEntity = EntityStore.REGISTRY.newHolder();
        UUID frameUpperModelEntityId = UUID.randomUUID();
        frameUpperModelEntity.addComponent(UUIDComponent.getComponentType(), new UUIDComponent(frameUpperModelEntityId));
        gameModels.put("frameUpper", frameUpperModelEntityId);

        frameUpperModelEntity.addComponent(TransformComponent.getComponentType(), new TransformComponent());
        Vector3d upperFramePos = gamePos.clone();
        Vector3d layering = upperFramePos.clone().add(TransformUtils.moveAwayFrom(upperFramePos.clone(), playerPos.clone(), 0.2));
        upperFramePos = new Vector3d(layering.x, upperFramePos.y, layering.z);
        upperFramePos.add(new Vector3d(0, minigameScale + (gameConfig.barRadius * minigameScale * 0.75f), 0));
        frameUpperModelEntity.getComponent(TransformComponent.getComponentType()).setPosition(upperFramePos.clone());
        TransformUtils.applyBillboardYOnly(gameModels.get("frameUpper"), upperFramePos.clone(), playerHeadPos.clone(), new Vector3f(0, 0, 0), commandBuffer);


        Holder<EntityStore> frameLowerModelEntity = EntityStore.REGISTRY.newHolder();
        UUID frameLowerModelEntityId = UUID.randomUUID();
        frameLowerModelEntity.addComponent(UUIDComponent.getComponentType(), new UUIDComponent(frameLowerModelEntityId));
        gameModels.put("frameLower", frameLowerModelEntityId);

        frameLowerModelEntity.addComponent(TransformComponent.getComponentType(), new TransformComponent());
        Vector3d lowerFramePos = gamePos.clone();
        layering = lowerFramePos.clone().add(TransformUtils.moveAwayFrom(lowerFramePos.clone(), playerPos.clone(), 0.2));
        lowerFramePos = new Vector3d(layering.x, lowerFramePos.y, layering.z);
        lowerFramePos.add(new Vector3d(0, -gameConfig.barRadius * minigameScale * 0.75f, 0));
        frameLowerModelEntity.getComponent(TransformComponent.getComponentType()).setPosition(lowerFramePos.clone());
        TransformUtils.applyBillboardYOnly(gameModels.get("frameLower"), lowerFramePos.clone(), playerHeadPos.clone(), new Vector3f(0, 0, 0), commandBuffer);


        // Add model.
        ModelAsset frameModelAsset = ModelAsset.getAssetMap().getAsset("SSF_MinigameFrame");
        if (frameModelAsset == null) frameModelAsset = ModelAsset.DEBUG;

        Model frameModel = Model.createScaledModel(frameModelAsset, minigameScale * 0.5f);
        frameUpperModelEntity.addComponent(PersistentModel.getComponentType(), new PersistentModel(frameModel.toReference()));
        frameUpperModelEntity.addComponent(ModelComponent.getComponentType(), new ModelComponent(frameModel));
        frameUpperModelEntity.addComponent(BoundingBox.getComponentType(), new BoundingBox(frameModel.getBoundingBox()));

        //Model frameLowerModel = Model.createScaledModel(frameModelAsset, minigameScale * 0.5f);
        frameLowerModelEntity.addComponent(PersistentModel.getComponentType(), new PersistentModel(frameModel.toReference()));
        frameLowerModelEntity.addComponent(ModelComponent.getComponentType(), new ModelComponent(frameModel));
        frameLowerModelEntity.addComponent(BoundingBox.getComponentType(), new BoundingBox(frameModel.getBoundingBox()));


        // Attach network component.

        frameUpperModelEntity.addComponent(NetworkId.getComponentType(), new NetworkId(commandBuffer.getExternalData().takeNextNetworkId()));
        frameLowerModelEntity.addComponent(NetworkId.getComponentType(), new NetworkId(commandBuffer.getExternalData().takeNextNetworkId()));

        // Spawn the model in the world.
        world.execute(() -> {
            commandBuffer.addEntity(frameLowerModelEntity, AddReason.SPAWN);
            commandBuffer.addEntity(frameUpperModelEntity, AddReason.SPAWN);
        });


        // ------ Catch Zone ------------------
        Holder<EntityStore> catchZoneEntity = EntityStore.REGISTRY.newHolder();
        UUID catchZoneEntityId = UUID.randomUUID();
        catchZoneEntity.addComponent(UUIDComponent.getComponentType(), new UUIDComponent(catchZoneEntityId));
        gameModels.put("catchZone", catchZoneEntityId);

        Vector3d catchZonePos = gamePos.clone();
        // Place at first water block from player and bobber.
        Vector3d dirToBobber = bobberPos.clone().subtract(playerPos.clone()).normalize();
        double distToBobber = playerPos.clone().distanceTo(bobberPos.clone());
        //Vector3d underfootBlockPos = playerPos.clone().subtract(new Vector3d(0, 1, 0));
        //AnglersAlmanac.LOGGER.atInfo().log("underfootPos: %s", playerPos.clone());
        for (double i = 0.0; i < distToBobber; i += 0.5) {
            Vector3d checkPos = playerPos.clone().add(dirToBobber.clone().scale(i));
            //AnglersAlmanac.LOGGER.atInfo().log("i: %s, stepping: %s",i, checkPos.clone());
            if (TransformUtils.isInFluid(checkPos.toVector3i(), world)) {
                catchZonePos = checkPos.toVector3i().toVector3d().add(new Vector3d(0.5, 1.0, 0.5));
                //AnglersAlmanac.LOGGER.atInfo().log("in water: %s", catchZonePos.clone());
                break;
            }
        }

        catchZoneEntity.addComponent(TransformComponent.getComponentType(), new TransformComponent());
        catchZoneEntity.getComponent(TransformComponent.getComponentType()).setPosition(catchZonePos.clone());

        ModelAsset catchZoneModelAsset = ModelAsset.getAssetMap().getAsset("AA_FishCatchZone");
        Model catchZoneModel = Model.createScaledModel(catchZoneModelAsset, 2f);
        catchZoneEntity.addComponent(PersistentModel.getComponentType(), new PersistentModel(catchZoneModel.toReference()));
        catchZoneEntity.addComponent(ModelComponent.getComponentType(), new ModelComponent(catchZoneModel));
        catchZoneEntity.addComponent(BoundingBox.getComponentType(), new BoundingBox(catchZoneModel.getBoundingBox()));

        catchZoneEntity.addComponent(NetworkId.getComponentType(), new NetworkId(commandBuffer.getExternalData().takeNextNetworkId()));

        world.execute(() -> {
            commandBuffer.addEntity(catchZoneEntity, AddReason.SPAWN);
        });

    }

    final Cache<UUID, TransformComponent> transformCache = Caffeine.newBuilder()
            .expireAfterAccess(1, TimeUnit.SECONDS) // Auto-cleanup if minigame ends
            .build();

    public void updateMinigameModelPositions(CommandBuffer<EntityStore> commandBuffer, float deltaTime) {

        World world = commandBuffer.getExternalData().getWorld();

        Ref<EntityStore> game = commandBuffer.getExternalData().getWorld().getEntityRef(selfUUID);
        assert game != null;
        //AnglersAlmanac.LOGGER.atInfo().log("game: %s", game.toString());
        Vector3d gamePos = Objects.requireNonNull(commandBuffer.getComponent(game, TransformComponent.getComponentType())).getPosition();
        Vector3d playerPos = commandBuffer.getComponent(ownerRef, TransformComponent.getComponentType()).getPosition().clone();
        float camOffset = commandBuffer.getComponent(ownerRef, ModelComponent.getComponentType()).getModel().getEyeHeight();
        Vector3d playerHeadPos = playerPos.clone().add(new Vector3d(0, camOffset, 0));


        // Do fish logic.3
        Ref<EntityStore> fishModelRef = world.getEntityRef(gameModels.get("fish"));
        if (fishModelRef == null) {
            AnglersAlmanac.LOGGER.atWarning().log("fishModelRef is null");
            return;
        }
        //if(!bobberRef.isValid()) return;
        // Do fish model motion.

        Vector3d newFishPos = gamePos.clone();
        // Adjust fish height based on minigame fishPos.
        newFishPos = newFishPos.add(new Vector3d(0, (fishPos * minigameScale), 0));
        commandBuffer.getComponent(fishModelRef, TransformComponent.getComponentType()).setPosition(newFishPos.clone());
        // Do Fish rotation.
        TransformUtils.applyBillboardYOnly(gameModels.get("fish"), newFishPos, playerHeadPos, new Vector3f(90, 0, 0), commandBuffer);


        //AnglersAlmanac.LOGGER.atInfo().log("gamePos: %s", gamePos);


        // -------- TENSION BAR --------------------------------


        float sectionHeight = 0.015625f * minigameScale;  //0.015625 = 1/64  (0-1 range to block texel size)
        Vector3d newBarPos = gamePos.clone();
        newBarPos.add(new Vector3d(0, (barPos * minigameScale) - ((sectionHeight * barModelEntityIds.size()) / 2), 0));
        Vector3d layering = newBarPos.clone().add(TransformUtils.moveAwayFrom(newBarPos.clone(), playerPos.clone(), 0.2));
        newBarPos = new Vector3d(layering.x, newBarPos.y, layering.z);
        Vector3d currentSegPos = newBarPos.clone();
        //AnglersAlmanac.LOGGER.atInfo().log("newBarPos: %s", newBarPos);

        for (UUID uuid : barModelEntityIds) {
            TransformComponent tc = transformCache.get(uuid, id -> {
                Ref<EntityStore> ref = commandBuffer.getExternalData().getWorld().getEntityRef(id);
                return (ref != null) ? commandBuffer.getComponent(ref, TransformComponent.getComponentType()) : null;
            });

            if (tc == null) continue;
            tc.setPosition(currentSegPos.clone());
            TransformUtils.applyBillboardYOnly(uuid, currentSegPos, playerHeadPos, Vector3f.ZERO, commandBuffer);
            currentSegPos.add(0, sectionHeight, 0);
        }


        // DEBUG BAR MOVEMENT
        /*
        Vector3d debugBarPos = gamePos.clone();
        debugBarPos.add(new Vector3d(0.4 * minigameScale,(barPos * minigameScale) ,0));
        Vector3d debugLayering = debugBarPos.clone().add(TransformUtils.moveAwayFrom(debugBarPos.clone() ,playerPos.clone(), 0.2));
        debugBarPos = new Vector3d(debugLayering.x, debugBarPos.y, debugLayering.z);
        Ref<EntityStore> barModelRef = commandBuffer.getExternalData().getWorld().getEntityRef(gameModels.get("bar_DEBUG"));
        commandBuffer.getComponent(barModelRef, TransformComponent.getComponentType()).setPosition(debugBarPos.clone());
        TransformUtils.applyBillboardYOnly(gameModels.get("bar_DEBUG"), debugBarPos.clone(), playerHeadPos.clone(), new Vector3f(0, 0, 0), commandBuffer);
        */


        // --------- FRAME -------------------------------
        Ref<EntityStore> lowerFrameRef = commandBuffer.getExternalData().getWorld().getEntityRef(gameModels.get("frameLower"));
        if (lowerFrameRef != null) {
            Vector3d lowerFramePos = gamePos.clone();
            layering = lowerFramePos.clone().add(TransformUtils.moveAwayFrom(lowerFramePos.clone(), playerPos.clone(), 0.2));
            lowerFramePos = new Vector3d(layering.x, lowerFramePos.y, layering.z);
            lowerFramePos.add(new Vector3d(0, -gameConfig.barRadius * minigameScale * 0.75f, 0));
            commandBuffer.getComponent(lowerFrameRef, TransformComponent.getComponentType()).setPosition(lowerFramePos.clone());
            TransformUtils.applyBillboardYOnly(gameModels.get("frameLower"), lowerFramePos.clone(), playerHeadPos.clone(), new Vector3f(0, 0, 0), commandBuffer);
        }

        Ref<EntityStore> upperFrameRef = commandBuffer.getExternalData().getWorld().getEntityRef(gameModels.get("frameUpper"));
        if (upperFrameRef != null) {
            Vector3d upperFramePos = gamePos.clone();
            layering = upperFramePos.clone().add(TransformUtils.moveAwayFrom(upperFramePos.clone(), playerPos.clone(), 0.2));
            upperFramePos = new Vector3d(layering.x, upperFramePos.y, layering.z);
            upperFramePos.add(new Vector3d(0, minigameScale + (gameConfig.barRadius * minigameScale * 0.75f), 0));
            commandBuffer.getComponent(upperFrameRef, TransformComponent.getComponentType()).setPosition(upperFramePos.clone());
            TransformUtils.applyBillboardYOnly(gameModels.get("frameUpper"), upperFramePos.clone(), playerHeadPos.clone(), new Vector3f(0, 0, 0), commandBuffer);
        }


        // ----- Bobber -------------
        Vector3d catchZonePos = commandBuffer.getComponent(commandBuffer.getExternalData().getRefFromUUID(gameModels.get("catchZone")), TransformComponent.getComponentType()).getPosition().clone();
        if (bobberRef.isValid()) {
            Vector3d bobberPos = commandBuffer.getComponent(bobberRef, TransformComponent.getComponentType()).getPosition().clone();
            Vector3d vecToCatchZone = catchZonePos.clone().subtract(gamePos.clone());
            Vector3d bobberTargetPos = gamePos.clone().add(vecToCatchZone.scale(fightProgress));
            Vector3d dirToTargetPos = bobberTargetPos.clone().subtract(bobberPos.clone()).normalize();
            double distToTargetPos = bobberPos.distanceTo(bobberTargetPos);

            Vector3d force = dirToTargetPos.scale(distToTargetPos * 20.0 * deltaTime);
            commandBuffer.getComponent(bobberRef, Velocity.getComponentType()).addForce(force);

            // Add a little extra movement bypassing physics to fix bobber stuck on things.
            commandBuffer.getComponent(bobberRef, TransformComponent.getComponentType()).setPosition(bobberPos.clone().add(force.scale(0.1)));
        }
    }

    public void DoInteraction(@NonNull InteractionType interactionType, @NonNull InteractionContext context, @NonNull CooldownHandler cooldownHandler) {
        //AnglersAlmanac.LOGGER.atInfo().log("Running TensionBar Minigame interaction");

        //Move bar up.
        barVelocity = Math.clamp(barVelocity + (AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().barSpeed * AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().barAcceleration)
                        + (AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().barGravity * AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().barAcceleration)
                , -AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().barGravity, AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().barSpeed);
    }


    @Override
    public int getPerformancePercentage() {
        int totalGameTicks = ticksReeling + ticksEscaping;
        //AnglersAlmanac.LOGGER.atInfo().log("Minigame performance percentage = %s", performancePercentage);
        return (int) (((float) ticksReeling / (float) totalGameTicks) * 100);
    }

// ----------  GAME MODIFIER ADAPTION  ----------------------------------------------------------------------------
// ================================================================================================================

    @Override
    public void applyDifficultyModifer(FishLootManager.MinigameStats stats) {
        if (stats == null) {
            gameConfig.fishMaxVeocity += (float) 1 / 20f;
            gameConfig.fishChangeDirectionMaxInterval = gameConfig.fishChangeDirectionMaxInterval / ((float) 1 / 8f);
            return;
        }
        gameConfig.fishMaxVeocity += (float) stats.difficulty / 20f;
        gameConfig.fishChangeDirectionMaxInterval = gameConfig.fishChangeDirectionMaxInterval / ((float) stats.difficulty / 8f);
        //AnglersAlmanac.LOGGER.atInfo().log("Applying fish difficulty! stat: %s, fishMaxVelocity: %s", stats.difficulty, gameConfig.fishMaxVeocity);
    }

    @Override
    public void applyFishBehaviourModifer(FishLootManager.MinigameStats stats) {
        if (stats == null) return;
        switch (stats.behavior) {
            case DARTING:
                // Stays still, then max speed, then still again, repeat.
                // Behaviour is mostly assigned in minigame system when triggering FISHMOVE.
                gameConfig.fishMinSpeed = 1.0f;
                break;
            case FLOATER:
                gameConfig.fishBouyancy += gameConfig.fishMaxVeocity * 0.6f;
                break;
            case SINKER:
                gameConfig.fishBouyancy -= gameConfig.fishMaxVeocity * 0.6f;
                break;
            case HEAVY_SINKER:
                gameConfig.fishMaxVeocity *= 1.2f;
                gameConfig.fishBouyancy -= gameConfig.fishMaxVeocity * 0.9f;

                break;
            case AGGRESSIVE:
                // Never slow, turns rapidly.
                gameConfig.fishMaxVeocity *= 0.9f;
                gameConfig.fishMinSpeed = 0.7f;
                gameConfig.fishChangeDirectionMaxInterval *= 0.5f;
                break;
            case ERRATIC:
                // Changes direction very frequently.
                gameConfig.fishMaxVeocity *= 0.6f;
                gameConfig.fishChangeDirectionMaxInterval *= 0.1f;
                break;
            case STEADY:
                // Rarely changes direction. Very predictable.
                gameConfig.fishMaxVeocity *= 0.7f;
                gameConfig.fishMinSpeed = 0.5f;
                gameConfig.fishChangeDirectionMaxInterval += 0.2f;
                break;

        }
        //AnglersAlmanac.LOGGER.atInfo().log("Applying fish behaviour! %s", stats.behavior);
    }

    @Override
    public void applyFishStaminaModifer(FishLootManager.MinigameStats stats) {
        // 30 represents standard stamina, with stats higher than 45 increasing time to catch, and stats lower than 45 reducing time to catch.
        // The stamina difference is dampened to prevent extreme modifications.
        //AnglersAlmanac.LOGGER.atInfo().log("Base reelRate = %s", gameConfig.fishReelRate);
        float dampeningFactor = 0.33f;
        float baselineStamina = 45f;
        float dampenedStamina = baselineStamina * dampeningFactor;
        if (stats != null) {
            dampenedStamina = stats.stamina + (baselineStamina - stats.stamina) * dampeningFactor;
        }
        float modifier = baselineStamina / dampenedStamina;
        gameConfig.fishReelRate *= modifier;
        //AnglersAlmanac.LOGGER.atInfo().log("Applying fish stamina! stamina = %s, modifier: %s,  reelRate = %s", stats.stamina,modifier,  gameConfig.fishReelRate);
    }

    @Override
    public void applyRodControlModifer(RodStats rodStats) {
        gameConfig.barSpeed *= rodStats.control;
        gameConfig.barGravity *= rodStats.control;
        gameConfig.barAcceleration *= rodStats.control / 2f;
        //AnglersAlmanac.LOGGER.atInfo().log("Applying rod control! barSpeed = %s, barGravity = %s, barAccel = %s", gameConfig.barSpeed, gameConfig.barGravity, gameConfig.barAcceleration);
    }

    @Override
    public void applyRodDifficultyModifer(RodStats rodStats) {
        gameConfig.barRadius /= rodStats.difficulty;
        //AnglersAlmanac.LOGGER.atInfo().log("Applying rod difficulty! barRadius = %s", gameConfig.barRadius);

    }

    @Override
    public void applyRodForgivenessModifer(RodStats rodStats) {
        //AnglersAlmanac.LOGGER.atInfo().log("Applying rod forgiveness");
        gameConfig.fishReelRate *= rodStats.forgiveness / 5f;
    }

    @Override
    public void applyRodStaminaModifer(RodStats rodStats) {
        //AnglersAlmanac.LOGGER.atInfo().log("Applying rod stamina");
    }

    @Override
    public void applyRodFishWeightModifer(RodStats rodStats) {
        //AnglersAlmanac.LOGGER.atInfo().log("Applying fish weight modfifer");
    }

    @Override
    public void applyRodRarityModifer(RodStats rodStats) {
        //AnglersAlmanac.LOGGER.atInfo().log("Applying fish rarity modifier");
    }
}
