package dev.rm20.anglersalmanac.Systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.IEvents.FishingFailedEvent;
import dev.rm20.anglersalmanac.Metadata.MinigamePRating;
import dev.rm20.anglersalmanac.MinigameManager.Minigame;
import dev.rm20.anglersalmanac.MinigameManager.MinigameManager;
import dev.rm20.anglersalmanac.Components.AudioPlayerComponent;
import dev.rm20.anglersalmanac.Components.BobberComponent;
import dev.rm20.anglersalmanac.Components.MinigameComponent_TensionBar;
import dev.rm20.anglersalmanac.Interactions.LaunchBobberInteraction;
//import dev.rm20.anglersalmanac.models.FishingRodData;
import dev.rm20.anglersalmanac.Models.FishLootManager;
import dev.rm20.anglersalmanac.Utils.TransformUtils;
import dev.rm20.anglersalmanac.Metadata.FishingRodData;
import dev.rm20.anglersalmanac.Utils.Validator.MinigameBehaviour;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class MinigameSystem_TensionBar extends EntityTickingSystem<EntityStore> {
    ItemStack fishingRod = null;
    Byte Slot = null;
    //List<String> soundAssetKeys = Arrays.asList("AA_Fishing_Reel_Slow0", "AA_Fishing_Reel_Slow1", "AA_Fishing_Reel_Slow2", "AA_Fishing_Reel_Slow3");


    @Override
    public void tick(float deltaTime, int i, @NonNull ArchetypeChunk<EntityStore> archetypeChunk, @NonNull Store<EntityStore> store, @NonNull CommandBuffer<EntityStore> commandBuffer) {

        MinigameComponent_TensionBar game = commandBuffer.getComponent(archetypeChunk.getReferenceTo(i), MinigameComponent_TensionBar.COMPONENT_TYPE);

        Ref<EntityStore> playerRef = null;
        if (game == null) {
            AnglersAlmanac.LOGGER.atSevere().log("Something went horribly wrong with the minigame system");
            AnglersAlmanac.LOGGER.atSevere().log("Attempting to minigame");
            store.getExternalData().getWorld().execute(() -> {
                Ref<EntityStore> ref = archetypeChunk.getReferenceTo(i);
                    if (ref.isValid()) {
                        try {
                            store.removeEntity(ref, RemoveReason.REMOVE);
                        } catch (RuntimeException e) {
                            AnglersAlmanac.LOGGER.atWarning().withCause(e).log("Failed to remove: "+ref.toString());
                        }
                    }
            });
            return;
        }


        playerRef = game.ownerRef;
        Player player = commandBuffer.getComponent(playerRef, Player.getComponentType());
        ItemStack rodItem = player.getInventory().getActiveHotbarItem(); // TODO ensure that this is always actually the rod. (cancel minigame if switched off)
        Vector3d playerPos = commandBuffer.getComponent(playerRef, TransformComponent.getComponentType()).getPosition().clone();

        if(rodItem == null)
        {
            AnglersAlmanac.LOGGER.atWarning().log("rodItem is null");
            return;
        }
        FishingRodData rodMeta = rodItem.getFromMetadataOrNull(FishingRodData.KEYED_CODEC);
        if(rodMeta != null)
        {
            fishingRod = rodItem;
            Slot = player.getInventory().getActiveHotbarSlot();
        }


        switch (game.stateTrigger){
            case DONE:
                World world = store.getExternalData().getWorld();
                AnglersAlmanac.LOGGER.atWarning().log("Something went wrong- Minigame still alive. ");
                AnglersAlmanac.LOGGER.atWarning().log("Attempted to despawn minigame "+ game.selfUUID);
                game.despawnSelf(world);
                return;
            case FISHMOVE:

                // Reset timers for the next move.
                game.nextFishMoveTime = new Random().nextFloat() * game.gameConfig.fishChangeDirectionMaxInterval;
                game.fishMoveTimer = 0f;

                // Set up important maths parameters.
                float maxFishVel = game.gameConfig.fishMaxVeocity + game.gameConfig.fishBouyancy;
                float minFishVel = (game.gameConfig.fishMaxVeocity*-1f) + game.gameConfig.fishBouyancy;
                float strength = new Random().nextFloat();
                // Apply minSpeed by pushing strength further away from 0.5 by factor of minSpeed/2.
                if(strength > 0.5f - (game.gameConfig.fishMinSpeed / 2f) && strength <= 0.5f) strength = 0.5f - (game.gameConfig.fishMinSpeed / 2f);
                if(strength < 0.5f + game.gameConfig.fishMinSpeed / 2f && strength > 0.5f) strength = 0.5f + (game.gameConfig.fishMinSpeed / 2f);

                //strength = Math.clamp(strength, game.gameConfig.fishMinSpeed, 1.0f);
                //AnglersAlmanac.LOGGER.atInfo().log("minFishVel: %s, maxFishVel: %s, fishMinSpeed: %s, strength: %s", minFishVel, maxFishVel, game.gameConfig.fishMinSpeed, strength);


                // Override parameters for fish with "darting" behaviour.
                if(game.fishHooked.getMinigameStats().behavior == MinigameBehaviour.DARTING){
                    // Toggle between max speed and stopped.
                    if(Math.abs(game.fishTargetVelocity) >= game.gameConfig.fishMaxVeocity ){
                        maxFishVel = game.gameConfig.fishMaxVeocity * 0.1f;
                        minFishVel = -game.gameConfig.fishMaxVeocity * 0.1f;
                        //AnglersAlmanac.LOGGER.atInfo().log("Darting fish is calm");
                    }else{
                        strength = 1.0f;
                        //AnglersAlmanac.LOGGER.atInfo().log("Darting fish go brrr");
                    }
                }

                // Calculate random movement based on fish parameters.
                game.fishTargetVelocity = ((minFishVel) + strength * (maxFishVel - minFishVel));
                //AnglersAlmanac.LOGGER.atInfo().log("FISHMOVE new velocity: %s", game.fishTargetVelocity);

                // Always ensure that fish moves away from edges if near top / bottom.
                //AnglersAlmanac.LOGGER.atInfo().log("fishPos: %s", game.fishPos);
                if(game.fishPos <= 0.1){game.fishTargetVelocity = Math.abs(game.fishTargetVelocity); }
                if(game.fishPos >= 0.9){ game.fishTargetVelocity = Math.abs(game.fishTargetVelocity) * -1f; }

                game.stateTrigger = MinigameComponent_TensionBar.Trigger.NOTRIGGER;
                break;
            case FAIL:
                //AnglersAlmanac.LOGGER.atInfo().log("YOU FAIL");
                // Reel in the rod which the bobber owner is using.
                var eventBus = HytaleServer.get().getEventBus();
                FishingFailedEvent mainEvent = new FishingFailedEvent(game.fishHooked,player);
                eventBus.dispatchFor(FishingFailedEvent.class).dispatch(mainEvent);
                
                LaunchBobberInteraction.cancelFishing(commandBuffer, player, fishingRod);
                break;
            case SUCCESS:
                //AnglersAlmanac.LOGGER.atInfo().log("YOU WIN");
                MinigamePRating.PerformanceRating  rating = Minigame.getPerformanceRating(game.getPerformancePercentage());
                //AnglersAlmanac.LOGGER.atInfo().log("Minigame performance rating = %s", rating);
                if(rating == MinigamePRating.PerformanceRating.FAIL) LaunchBobberInteraction.cancelFishing(commandBuffer, player, fishingRod);
                // Deal rewards.

                if(game.fishHooked!=null)
                {
                    MinigameManager.DropLoot(game.fishHooked, player, commandBuffer,game.bobberRef,game.getPerformancePercentage());
                }
                else {
                    FishLootManager lootID = MinigameManager.FirstRoll(game.bobberRef, player, commandBuffer, store.getComponent(game.bobberRef, BobberComponent.getComponentType()).getWaterDepth());
                    MinigameManager.DropLoot(lootID, player, commandBuffer,game.bobberRef,game.getPerformancePercentage());
                }
                if(rating == MinigamePRating.PerformanceRating.PERFECT){
                    // TODO Deal chance of bonus loot.
                }

                // Finish fishing.
                game.stateTrigger = MinigameComponent_TensionBar.Trigger.DONE;
                LaunchBobberInteraction.cancelFishing(commandBuffer, player, fishingRod,Slot);
                break;
        }


        // Do minigame logic.

        PlayerRef playerRefObj = commandBuffer.getComponent(playerRef, PlayerRef.getComponentType());
        AudioPlayerComponent apc = commandBuffer.getComponent(commandBuffer.getExternalData().getRefFromUUID(game.audioPlayerId), AudioPlayerComponent.getComponentType());
        apc.autoplayAsRandom = true;

        // Check if bar is over the fish and check win state.
        if(game.fishPos < game.barPos +  game.gameConfig.barRadius && game.fishPos > game.barPos - game.gameConfig.barRadius){
            game.fightProgress += game.gameConfig.fishReelRate * deltaTime;

            // Remove escape audio
            if(apc.hasSound(game.escapeSounds[0])) {
                apc.removeSounds(game.escapeSounds);
                //AnglersAlmanac.LOGGER.atInfo().log("Removed escape sound");
            }

            // Add reel in audio
            if(!apc.hasSound(game.reelInSounds[0])){
                apc.addSounds(game.reelInSounds);
            }

            // Increment tick tracker.
            game.ticksReeling++;

            // Check win condition.
            if(game.fightProgress >= 1.0f){
                game.stateTrigger = MinigameComponent_TensionBar.Trigger.SUCCESS;
                return;
            }
        }else{
            game.fightProgress -= game.gameConfig.fishEscapeRate * deltaTime;

            // Remove escape audio
            if(apc.hasSound(game.reelInSounds[0])) {
                apc.removeSounds(game.reelInSounds);
                //AnglersAlmanac.LOGGER.atInfo().log("Removed escape sound");
            }

            // Add reel in audio
            if(!apc.hasSound(game.escapeSounds[0])){
                apc.addSounds(game.escapeSounds);
            }

            // Increment tick tracker.
            game.ticksEscaping++;

            // Check lose condition.
            if(game.fightProgress <= 0f){
                game.stateTrigger = MinigameComponent_TensionBar.Trigger.FAIL;
                return;
            }
        }

        // Check if fish will change velocity or direction.
        if(game.fishMoveTimer >= game.nextFishMoveTime){
            game.stateTrigger = MinigameComponent_TensionBar.Trigger.FISHMOVE;
        }

        // Apply bar gravity motion. (Rising is computed in MinigameInteraction by changing barVelocity)
        game.barVelocity = Math.clamp(game.barVelocity - (game.gameConfig.barGravity*game.gameConfig.barAcceleration), -game.gameConfig.barGravity, game.gameConfig.barSpeed);
        game.barPos = Math.clamp(game.barPos + (game.barVelocity * deltaTime), game.gameConfig.barRadius * 0.5f, 1.0f - (game.gameConfig.barRadius * 0.5f));

        // Apply fish movement.
        float fishAccelStep = game.gameConfig.fishAcceleration * deltaTime * 10f;
        game.fishVelocity = TransformUtils.lerp(game.fishVelocity, game.fishTargetVelocity, fishAccelStep);
        game.fishPos = Math.clamp(game.fishPos + (game.fishVelocity*deltaTime), 0f, 1.0f);

        // DEBUG
        //game.fightProgress = 0.5f;
        //game.fishPos = 0.5f;

        game.updateMinigameModelPositions(commandBuffer, deltaTime);
        game.fishMoveTimer += deltaTime;

    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return MinigameComponent_TensionBar.COMPONENT_TYPE;
    }
}
