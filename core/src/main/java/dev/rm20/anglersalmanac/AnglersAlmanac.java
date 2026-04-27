package dev.rm20.anglersalmanac;

import com.al3x.HStats;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import dev.rm20.anglersalmanac.AlmanacBook.AlmanacBook;
import dev.rm20.anglersalmanac.AlmanacBook.AlmanacDatabase;
import dev.rm20.anglersalmanac.AlmanacBook.AlmanacRepository;
import dev.rm20.anglersalmanac.Components.BobberComponent;
import dev.rm20.anglersalmanac.Config.AnglersAlmanacConfig;
import dev.rm20.anglersalmanac.Config.MinigameConfig_TensionBar;
import dev.rm20.anglersalmanac.Models.BookAssetData;
import dev.rm20.anglersalmanac.Registration.*;
import dev.rm20.anglersalmanac.Models.FishLootManager;


import javax.annotation.Nonnull;

public class AnglersAlmanac extends JavaPlugin {
    private static AnglersAlmanac instance;
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static ComponentType<EntityStore, BobberComponent> bobberComponent;

    public static Config<MinigameConfig_TensionBar> MINIGAME_CONFIG_TENSIONBAR;
    public static Config<AnglersAlmanacConfig> MOD_CONFIG;
    public AlmanacDatabase database;
    public AlmanacRepository Book_IDs;
    public FishLootManager fishLootManager;
    public BookAssetData bookAssetData;
    public AnglersAlmanac(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
        MINIGAME_CONFIG_TENSIONBAR = this.withConfig(MinigameConfig_TensionBar.KEY, MinigameConfig_TensionBar.CODEC);
        MOD_CONFIG = this.withConfig(AnglersAlmanacConfig.KEY, AnglersAlmanacConfig.CODEC);
    }
    public static AnglersAlmanac getInstance() {
        return instance;
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
        this.database = new AlmanacDatabase();
        this.Book_IDs = new AlmanacRepository();
        MOD_CONFIG.save();
        MINIGAME_CONFIG_TENSIONBAR.save();

        // Plugin Mod Analytics
        new HStats("55078602-d7a1-4794-b30c-f42529f3d1d4", getManifest().getVersion().toString());
    }


    @Override
    protected void shutdown() {
        super.shutdown();
        if (this.database != null) {
            this.database.close();
        }
        if(this.Book_IDs != null)
        {
            this.Book_IDs.close();
        }
    }


}
