package dev.rm20.anglersalmanac.Config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AnglersAlmanac;

import javax.annotation.Nonnull;

public class ConfigUI extends InteractiveCustomUIPage<ConfigUI.BindingData> {

    public ConfigUI(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime) {
        super(playerRef, lifetime, BindingData.CODEC);
    }

    public static class BindingData {
        public static final BuilderCodec<BindingData> CODEC = BuilderCodec.builder(BindingData.class, BindingData::new)
                .addField(new KeyedCodec<>("@BaitRequired", Codec.BOOLEAN), (data, s) -> data.BaitRequired = s, data -> data.BaitRequired)
                .addField(new KeyedCodec<>("@XpEnabled", Codec.BOOLEAN), (data, s) -> data.XpEnabled = s, data -> data.XpEnabled)
                .addField(new KeyedCodec<>("@TensionBarEnabled", Codec.BOOLEAN), (data, s) -> data.TensionBarEnabled = s, data -> data.TensionBarEnabled)
                .build();

        public Boolean BaitRequired;
        public Boolean XpEnabled;
        public Boolean TensionBarEnabled;
    }


    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("Almanac/Config/AlmanacConfig.ui");

        var config = AnglersAlmanac.MOD_CONFIG;

        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#BaitRequired #CheckBox", EventData.of("@BaitRequired", "#BaitRequired #CheckBox.Value"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#XpEnabled #CheckBox", EventData.of("@XpEnabled", "#XpEnabled #CheckBox.Value"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#TensionBarEnabled #CheckBox", EventData.of("@TensionBarEnabled", "#TensionBarEnabled #CheckBox.Value"), false);

        uiCommandBuilder.set("#BaitRequired #CheckBox.Value", config.get().getShouldUseBait());
        uiCommandBuilder.set("#XpEnabled #CheckBox.Value", true);
        if(config.get().getMinigameToUse().equalsIgnoreCase("TensionBar"))
        {
            uiCommandBuilder.set("#TensionBarEnabled #CheckBox.Value", true);
        }
        else
        {
            uiCommandBuilder.set("#TensionBarEnabled #CheckBox.Value", false);
        }

    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull BindingData data) {
        super.handleDataEvent(ref, store, data);

        var config = AnglersAlmanac.MOD_CONFIG;
        if (data.BaitRequired != null) {
            // Logic for @BaitRequired
            config.get().setShouldUseBait(data.BaitRequired);
            this.playerRef.sendMessage(Message.raw("[AA] Bait required set to: " + data.BaitRequired));

        }
        if (data.XpEnabled != null) {
            // Logic for @XpEnabled
            this.playerRef.sendMessage(Message.raw("@XpEnabled updated to: " + data.XpEnabled));
        }
        if(data.TensionBarEnabled != null)
        {
            if(data.TensionBarEnabled)
            {
                config.get().setMinigameToUse("TensionBar");
                this.playerRef.sendMessage(Message.raw("[AA] Tension bar Minigame Enabled"));
            }
            else
            {
                config.get().setMinigameToUse("NoMinigame");
                this.playerRef.sendMessage(Message.raw("[AA] No Minigame Enabled"));
            }
        }
        AnglersAlmanac.MOD_CONFIG.save();
    }
}
