package dev.rm20.anglersalmanac;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import dev.rm20.anglersalmanac.AlmanacBook.AlmanacDatabase;
import dev.rm20.anglersalmanac.AlmanacBook.BookPageManager;
import dev.rm20.anglersalmanac.Components.BobberComponent;
import dev.rm20.anglersalmanac.Config.AnglersAlmanacConfig;
import dev.rm20.anglersalmanac.Config.MinigameConfig_TensionBar;
import dev.rm20.anglersalmanac.Minigame.MinigameRegistry;
import dev.rm20.anglersalmanac.MinigameManager.Handlers.NoMinigameHandler;
import dev.rm20.anglersalmanac.MinigameManager.Handlers.TensionBarMinigameHandler;
import dev.rm20.anglersalmanac.Models.BookAssetData;
import dev.rm20.anglersalmanac.Models.FishBaitData;
import dev.rm20.anglersalmanac.Models.MinigameRodStats;
import dev.rm20.anglersalmanac.Registration.*;
import dev.rm20.anglersalmanac.Models.FishLootManager;
import dev.rm20.anglersalmanac.Utils.BaitUtils;
import dev.rm20.anglersalmanac.Utils.Intergration.MMOSkillTree;
import dev.rm20.anglersalmanac.Utils.Intergration.ThankmasVaultHook;
import dev.rm20.anglersalmanac.api.AnglersAlmanacAPI;
import dev.rm20.anglersalmanac.triggereffects.GiveRodEffect;
import lombok.Getter;


import javax.annotation.Nonnull;

public class AnglersAlmanac extends JavaPlugin {
    @Getter
    private static AnglersAlmanac instance;
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public ThankmasVaultHook ECONOMY_HOOK;

    public AlmanacDatabase database;
    public FishLootManager fishLootManager;
    public BookAssetData bookAssetData;

    public MMOSkillTree skillTree;
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
        TriggerEffect.CODEC.register("GiveRod", GiveRodEffect.class, GiveRodEffect.CODEC);


        //start database
        this.database = new AlmanacDatabase();
        AnglersAlmanacAPI.getConfig().save();
        AnglersAlmanacAPI.getMinigameConfig().save();

        AnglersAlmanacAPI.setImplementation(database);
        fishLootManager = new FishLootManager();
        AnglersAlmanacAPI.setLootProvider(fishLootManager);

        AnglersAlmanacAPI.setBaitResolver(baitName -> {
            FishBaitData baitAsset = BaitUtils.getBaitData(baitName);
            return baitAsset != null ? baitAsset.modifiers : null;
        });

        AnglersAlmanacAPI.setRodResolver(MinigameRodStats::getModifiersFromRodId);

        AnglersAlmanacAPI.setCacheInvalidator(
                BookPageManager::invalidateCache,
                BookPageManager::invalidateCache
        );

        MinigameRegistry.register("TensionBar", new TensionBarMinigameHandler());
        MinigameRegistry.register("NoMinigame", new NoMinigameHandler());
    }


    @Override
    protected void shutdown() {
        super.shutdown();
        if (this.database != null) {
            this.database.close();
        }
    }


}
