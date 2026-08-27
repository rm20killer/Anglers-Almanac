package dev.rm20.anglersalmanac.Config;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Minigame.MinigameRegistry;
import dev.rm20.anglersalmanac.api.AnglersAlmanacAPI;
import dev.rm20.codecannotation.Annotations.CodecAnnotations;
import dev.rm20.codecannotation.AutoCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import javax.annotation.Nonnull;

public class ConfigUI extends InteractiveCustomUIPage<ConfigUI.BindingData> {

    public ConfigUI(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime) {
        super(playerRef, lifetime, BindingData.CODEC);
    }

    public static class BindingData {
        public static final BuilderCodec<BindingData> CODEC =
                AutoCodecBuilder.create(BindingData.class, BindingData::new);

        @CodecAnnotations.Field("@BaitRequired")
        public Boolean BaitRequired;

        @CodecAnnotations.Field("@Minigame")
        public String Minigame;

        @CodecAnnotations.Field("@LocationCheck")
        public Boolean LocationCheck;

        @CodecAnnotations.Field("@EnvironmentCheck")
        public Boolean EnvironmentCheck;

        @CodecAnnotations.Field("@HookEntites")
        public Boolean HookEntities;

        public BindingData() {
        }
    }


    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("Almanac/Config/AlmanacConfig.ui");

        uiCommandBuilder.set("#Title483b3ca6.Text", Message.translation("anglersalmanac.config.label"));

        uiCommandBuilder.set("#MinigameLabel.TooltipText", Message.translation("anglersalmanac.config.tensionBar.tooltip"));
        uiCommandBuilder.set("#BaitRequired.TooltipText", Message.translation("anglersalmanac.config.baitRequired.tooltip"));
        uiCommandBuilder.set("#LocationCheck.TooltipText", Message.translation("anglersalmanac.config.LocationCheck.tooltip"));
        uiCommandBuilder.set("#EnvironmentCheck.TooltipText", Message.translation("anglersalmanac.config.EnvironmentCheck.tooltip"));
        uiCommandBuilder.set("#HookEntites.TooltipText", Message.translation("anglersalmanac.config.HookEntities.tooltip"));


        var config = AnglersAlmanacAPI.getConfig();
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#BaitRequired #CheckBox", EventData.of("@BaitRequired", "#BaitRequired #CheckBox.Value"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#LocationCheck #CheckBox", EventData.of("@LocationCheck", "#LocationCheck #CheckBox.Value"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#EnvironmentCheck #CheckBox", EventData.of("@EnvironmentCheck", "#EnvironmentCheck #CheckBox.Value"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#HookEntites #CheckBox", EventData.of("@HookEntites", "#HookEntites #CheckBox.Value"), false);


        uiCommandBuilder.set("#BaitRequired #CheckBox.Value", config.get().getShouldUseBait());
        uiCommandBuilder.set("#LocationCheck #CheckBox.Value", config.get().getShouldHabCheck());
        uiCommandBuilder.set("#EnvironmentCheck #CheckBox.Value", config.get().getShouldEnvironmentCheck());
        uiCommandBuilder.set("#HookEntites #CheckBox.Value", config.get().getHookEntities());


        ObjectArrayList<DropdownEntryInfo> entries = new ObjectArrayList<>();
        for (String id : MinigameRegistry.getIds()) {
            entries.add(new DropdownEntryInfo(
                    LocalizableString.fromString(id),
                    id
            ));
        }

        String currentMinigame = config.get().getMinigameToUse();
        if (currentMinigame == null || !MinigameRegistry.contains(currentMinigame)) {
            currentMinigame = MinigameRegistry.getIds().stream().findFirst().orElse("TensionBar");
        }

        uiCommandBuilder.set("#MinigameDropdown.Entries", entries);
        uiCommandBuilder.set("#MinigameDropdown.Value", currentMinigame);

        // Minigame Dropdown Event
        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.ValueChanged,
                "#MinigameDropdown",
                EventData.of("@Minigame", "#MinigameDropdown.Value"),
                false
        );
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull BindingData data) {
        super.handleDataEvent(ref, store, data);

        var config = AnglersAlmanacAPI.getConfig();
        if (data.BaitRequired != null) {
            // Logic for @BaitRequired
            config.get().setShouldUseBait(data.BaitRequired);
            this.playerRef.sendMessage(Message.raw("[AA] Bait required set to: " + data.BaitRequired));

        }

        if (data.Minigame != null) {
            config.get().setMinigameToUse(data.Minigame);
            this.playerRef.sendMessage(Message.raw("[AA] Fishing minigame set to: " + data.Minigame));
        }

        if(data.LocationCheck != null)
        {
            config.get().setShouldHabCheck(data.LocationCheck);
            this.playerRef.sendMessage(Message.raw("[AA] Location check set to: " + data.LocationCheck));
        }
        if(data.EnvironmentCheck != null)
        {
            config.get().setShouldEnvironmentCheck(data.EnvironmentCheck);
            this.playerRef.sendMessage(Message.raw("[AA] Environment check set to: " + data.EnvironmentCheck));
        }
        if(data.HookEntities != null)
        {
            config.get().setHookEntities(data.HookEntities);
            this.playerRef.sendMessage(Message.raw("[AA] Hook Entities set to: " + data.HookEntities));

        }
        AnglersAlmanacAPI.getConfig().save();
    }
}
