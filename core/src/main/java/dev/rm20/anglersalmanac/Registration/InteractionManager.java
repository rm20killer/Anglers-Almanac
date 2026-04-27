package dev.rm20.anglersalmanac.Registration;

import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Interactions.LaunchBobberInteraction;
import dev.rm20.anglersalmanac.Interactions.MinigameInteraction;
import dev.rm20.anglersalmanac.Interactions.OpenBookInteraction;

public class InteractionManager {
    public static void registerInteractions(AnglersAlmanac plugin) {
        plugin.getCodecRegistry(Interaction.CODEC).register("launch_bobber_interaction", LaunchBobberInteraction.class, LaunchBobberInteraction.CODEC);
        plugin.getCodecRegistry(Interaction.CODEC).register("minigame_interaction", MinigameInteraction.class, MinigameInteraction.CODEC);
        plugin.getCodecRegistry(Interaction.CODEC).register("open_almanac_interaction", OpenBookInteraction.class, OpenBookInteraction.CODEC);
    }
}
