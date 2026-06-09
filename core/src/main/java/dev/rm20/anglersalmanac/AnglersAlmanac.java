package dev.rm20.anglersalmanac;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.Components.BobberComponent;
import dev.rm20.anglersalmanac.Config.AnglersAlmanacConfig;
import dev.rm20.anglersalmanac.Config.MinigameConfig_TensionBar;
import dev.rm20.anglersalmanac.Registration.*;
import dev.rm20.anglersalmanac.Models.FishLootManager;
import dev.rm20.anglersalmanac.api.AnglersAlmanacAPI;
import lombok.Getter;


import javax.annotation.Nonnull;

public class AnglersAlmanac extends JavaPlugin {
    @Getter
    private static AnglersAlmanac instance;
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static ComponentType<EntityStore, BobberComponent> bobberComponent;


    public FishLootManager fishLootManager;

    public AnglersAlmanac(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
        AnglersAlmanacAPI.setMinigameConfig(this.withConfig(MinigameConfig_TensionBar.KEY, MinigameConfig_TensionBar.CODEC));
        AnglersAlmanacAPI.setConfig(this.withConfig(AnglersAlmanacConfig.KEY, AnglersAlmanacConfig.CODEC));
    }


    @Override
    protected void setup() {
        LOGGER.atInfo().log("Setting up plugin " + this.getName()+":"+getManifest().getVersion().toString());
        RegisterManager.registerCommands(this);
        RegisterManager.registerEvents(this);
        AssetRegisterManager.registerAll(this);

        // Register Components
        ComponentManager.registerComponent(this);
        // Register Interaction Codecs
        InteractionManager.registerInteractions(this);

        //System Interaction
        SystemRegisteration.registerSystem(this);


        //start database
        AnglersAlmanacAPI.getConfig().save();
        AnglersAlmanacAPI.getMinigameConfig().save();

        fishLootManager = new FishLootManager();
        AnglersAlmanacAPI.setLootProvider(fishLootManager);
    }


    @Override
    protected void shutdown() {
        super.shutdown();
    }


}
