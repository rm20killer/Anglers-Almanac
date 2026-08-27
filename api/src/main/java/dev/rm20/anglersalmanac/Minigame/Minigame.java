package dev.rm20.anglersalmanac.Minigame;

import dev.rm20.anglersalmanac.Metadata.MinigamePRating;
import dev.rm20.anglersalmanac.Metadata.RodStats;
import dev.rm20.anglersalmanac.Models.FishLoot;import lombok.Getter;
import lombok.Setter;

public abstract class Minigame {

    // How long a player has been playing
    @Getter
    @Setter
    private float TimePlayed = 0;
    // Total points that the player has
    @Getter
    @Setter
    private int Points = 0;
    // PerfectScore
    @Getter
    @Setter
    private float perfectScore = 100;

    public FishLoot fishHooked;

    public int getPerformancePercentage(){
        return (int) (Points / perfectScore) * 100;
    }

    public static MinigamePRating.PerformanceRating getPerformanceRating(int performancePercentage){
        if(performancePercentage >= 95){
            return MinigamePRating.PerformanceRating.PERFECT;
        }
        if(performancePercentage >= 80){
            return MinigamePRating.PerformanceRating.GREAT;
        }
        if(performancePercentage >= 40){
            return MinigamePRating.PerformanceRating.GOOD;
        }
        if(performancePercentage == -1)
        {
            return MinigamePRating.PerformanceRating.NIL;
        }

       return MinigamePRating.PerformanceRating.FAIL;
    }


    public abstract void applyDifficultyModifer(FishLoot.MinigameStats stats);
    public abstract void applyFishBehaviourModifer(FishLoot.MinigameStats stats);
    public abstract void applyFishStaminaModifer(FishLoot.MinigameStats stats);
    public void applyFishModifiers(FishLoot.MinigameStats stats){
        applyDifficultyModifer(stats);
        applyFishBehaviourModifer(stats);
        applyFishStaminaModifer(stats);
    }

    public abstract void applyRodControlModifer(RodStats rodStats);
    public abstract void applyRodDifficultyModifer(RodStats rodStats);
    public abstract void applyRodForgivenessModifer(RodStats rodStats);
    public abstract void applyRodStaminaModifer(RodStats rodStats);
    public abstract void applyRodFishWeightModifer(RodStats rodStats);
    public abstract void applyRodRarityModifer(RodStats rodStats);
    public void applyRodModifiers(RodStats rodStats){
        applyRodControlModifer(rodStats);
        applyRodDifficultyModifer(rodStats);
        applyRodForgivenessModifer(rodStats);
        applyRodStaminaModifer(rodStats);
        applyRodFishWeightModifer(rodStats);
        applyRodRarityModifer(rodStats);
    }



}
