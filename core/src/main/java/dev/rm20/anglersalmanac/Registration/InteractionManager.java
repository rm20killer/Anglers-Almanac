package dev.rm20.anglersalmanac.Registration;

import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Interactions.Rod.CastBobberInteraction;
import dev.rm20.anglersalmanac.Interactions.Rod.ReelBobberInteraction;
import dev.rm20.anglersalmanac.Interactions.Rod.UseRodInteraction;
import dev.rm20.anglersalmanac.Interactions.MinigameInteraction;
import dev.rm20.anglersalmanac.Interactions.OpenBookInteraction;

public class InteractionManager {
    public static void registerInteractions(AnglersAlmanac plugin) {
        plugin.getCodecRegistry(Interaction.CODEC).register("use_rod_interaction", UseRodInteraction.class, UseRodInteraction.CODEC);
        plugin.getCodecRegistry(Interaction.CODEC).register("cast_bobber_interaction", CastBobberInteraction.class, CastBobberInteraction.CODEC);
        plugin.getCodecRegistry(Interaction.CODEC).register("reel_bobber_interaction", ReelBobberInteraction.class, ReelBobberInteraction.CODEC);
        plugin.getCodecRegistry(Interaction.CODEC).register("minigame_interaction", MinigameInteraction.class, MinigameInteraction.CODEC);
        plugin.getCodecRegistry(Interaction.CODEC).register("open_almanac_interaction", OpenBookInteraction.class, OpenBookInteraction.CODEC);
    }
}
