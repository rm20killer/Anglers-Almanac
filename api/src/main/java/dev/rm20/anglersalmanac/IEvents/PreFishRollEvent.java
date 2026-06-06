package dev.rm20.anglersalmanac.IEvents;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.system.CancellableEcsEvent;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.Metadata.FishingContext;

import javax.annotation.Nonnull;


/**
 * Dispatched by CatchUtils when an item is being randomly picked
 * <p>
 * Allows you to rig the item the player gets before the plugin even can pick it.
 * Setting the overriddenLootId will override it.
 * overriddenLootId must the id used FishLootManager (aka name of the file)
 * </p>
 */
public class PreFishRollEvent extends CancellableEcsEvent implements IEvent<Void> {
    private final Ref<EntityStore> bobberRef;
    private final Player player;
    private final FishingContext context;
    private String overriddenLootId = null;
    private String overridingModName = "Unknown Mod";
    public PreFishRollEvent(Ref<EntityStore> bobberRef, Player player, FishingContext context) {
        super();
        this.bobberRef = bobberRef;
        this.player = player;
        this.context = context;
    }

    public Ref<EntityStore> getBobberRef() { return bobberRef; }
    public Player getPlayer() { return player; }
    public FishingContext getContext() { return context; }


    public String getOverriddenLootId() {
        return overriddenLootId;
    }

    public String getOverridingModName() {
        return overridingModName;
    }

    public void setOverriddenLootId(@Nonnull String loot, @Nonnull String modName) {
        this.overriddenLootId = loot;
        this.overridingModName = !modName.isEmpty() ? modName : "Anonymous Mod";
        this.setCancelled(true);
    }
}