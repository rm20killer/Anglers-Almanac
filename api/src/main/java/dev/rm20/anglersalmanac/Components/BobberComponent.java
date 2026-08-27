package dev.rm20.anglersalmanac.Components;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BobberComponent implements Component<EntityStore> {
    private static final int MAX_CATCH_TIME = 120;

    // ComponentType registered by the core plugin
    private static ComponentType<EntityStore, BobberComponent> componentType;

    public static ComponentType<EntityStore, BobberComponent> getComponentType() {
        return componentType;
    }

    public static void setComponentType(ComponentType<EntityStore, BobberComponent> type) {
        componentType = type;
    }

    private float bobberAge;
    private float timeUntilCatch;
    private boolean canCatch;
    private float catchTimer;
    private boolean inWater;
    private int waterDepth;
    private Player player;
    private String baitName;
    public ItemStack fishingRod = null;
    public byte slot = 0;
    private boolean minigameActive = false;
    private String minigameId = null;
    private Ref<EntityStore> hookedEntity = null;

    public BobberComponent() {
        this.bobberAge = 0;
        this.canCatch = false;
        this.timeUntilCatch = -1;
        this.catchTimer = 0;
        this.inWater = false;
        this.waterDepth = 0;
        this.minigameActive = false;
        this.minigameId = null;
        this.baitName = null;
    }

    public BobberComponent(String bait) {
        this();
        this.baitName = bait;
    }

    public void setCanCatch(boolean canCatch) {
        this.canCatch = canCatch;
        if (canCatch) {
            this.catchTimer = MAX_CATCH_TIME;
        } else {
            this.catchTimer = 0;
        }
    }

    public void resetTimeUntilCatch() {
        this.timeUntilCatch = -1;
    }


    public boolean canCatchFish() {
        return this.canCatch && this.catchTimer > 0;
    }

    public boolean InWater()
    {
        return this.inWater;
    }

    public boolean isHookedToEntity() {
        return this.hookedEntity != null && this.hookedEntity.isValid();
    }

    @Override
    public Component<EntityStore> clone() {
        BobberComponent component = new BobberComponent();
        component.bobberAge = this.bobberAge;
        component.canCatch = this.canCatch;
        component.timeUntilCatch = this.timeUntilCatch;
        component.catchTimer = this.catchTimer;
        component.inWater = this.inWater;
        component.waterDepth = this.waterDepth;
        component.player = this.player;
        component.baitName = this.baitName;
        component.fishingRod = this.fishingRod;
        component.slot = this.slot;
        component.hookedEntity = this.hookedEntity;
        component.minigameActive = this.minigameActive;
        component.minigameId = this.minigameId;
        return component;
    }
}