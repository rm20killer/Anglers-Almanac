package dev.rm20.anglersalmanac.Utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AnglersAlmanac;

public class FishingPowerUtils {


    public static float getTotalFishingPower(Store<EntityStore> store, Ref<EntityStore> ref)
    {
        //level based
        int fishingLevel = AnglersAlmanac.getInstance().skillTree.getFishingLevel(store,ref);

        float maxLevel = 100f;
        float maxPowerAtCap = 5.0f;
        float basePower = 1.0f;
        float fishingPower = basePower + (maxPowerAtCap - basePower) * (float) Math.pow(fishingLevel / maxLevel, 3);

        //luck
        float luckMultiplier = 5.0f;
        double fishingLuck = AnglersAlmanac.getInstance().skillTree.getFishingLuck(store,ref);

        //200% = 10
        float luckBonus = (float) (fishingLuck * luckMultiplier);

        float total = fishingPower + luckBonus;
        if(total>15)
        {
            total = 15;
        }
        else  if(total<=0)
        {
            total = 1;
        }

        return total;
    }
}
