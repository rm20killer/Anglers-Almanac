package dev.rm20.anglersalmanac.Components;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AnglersAlmanac;

public class BobberComponent implements Component<EntityStore> {
    private static final int MAX_CATCH_TIME = 120;

    private float bobberAge;
    private float timeUntilCatch;
    private boolean canCatch;
    private float catchTimer;
    private boolean InWater;
    private int WaterDepth;
    private Player player;
    private String baitName;

    public BobberComponent() {
        this.bobberAge = 0;
        this.canCatch = false;
        this.timeUntilCatch = -1;
        this.catchTimer = 0;
        this.InWater = false;
        this.WaterDepth = 0;
        this.baitName = null;
    }

    public BobberComponent(String Bait) {
        this.bobberAge = 0;
        this.canCatch = false;
        this.timeUntilCatch = -1;
        this.catchTimer = 0;
        this.InWater = false;
        this.WaterDepth = 0;
        this.baitName = Bait;
    }


    public static ComponentType<EntityStore, BobberComponent> getComponentType() {
        return AnglersAlmanac.bobberComponent;
    }

    public float getBobberAge() {
        return bobberAge;
    }

    public void setBobberAge(float bobberAge) {
        this.bobberAge = bobberAge;
    }

    public boolean isCanCatch() {
        return canCatch;
    }

    public void setCanCatch(boolean canCatch) {
        this.canCatch = canCatch;
        if (canCatch) {
            this.catchTimer = MAX_CATCH_TIME;
        } else {
            this.catchTimer = 0;
        }
    }

    public void setCatchTimer(float catchTimer) {
        this.catchTimer = catchTimer;
    }

    public float getTimeUntilCatch() {
        return timeUntilCatch;
    }

    public void setTimeUntilCatch(float timeUntilCatch) {
        this.timeUntilCatch = timeUntilCatch;
    }

    public void resetTimeUntilCatch() {
        this.timeUntilCatch = -1;
    }

    public float getCatchTimer() {
        return catchTimer;
    }

    public boolean canCatchFish() {
        return this.canCatch && this.catchTimer > 0;
    }
    public void setInWater(boolean inWater)
    {
        this.InWater = inWater;
    }
    public boolean InWater()
    {
        return this.InWater;
    }
    public int getWaterDepth() {
        return WaterDepth;
    }

    public void setWaterDepth(int waterDepth) {
        WaterDepth = waterDepth;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public String getBaitName() {
        return baitName;
    }

    public void setBaitName(String baitName) {
        this.baitName = baitName;
    }

    @Override
    public Component<EntityStore> clone() {
        BobberComponent component = new BobberComponent();
        component.bobberAge = this.bobberAge;
        component.canCatch = this.canCatch;
        component.timeUntilCatch = this.timeUntilCatch;
        component.catchTimer = this.catchTimer;
        component.InWater = this.InWater;
        component.WaterDepth = this.WaterDepth;
        component.player = this.player;
        component.baitName = this.baitName;
        return component;
    }
}