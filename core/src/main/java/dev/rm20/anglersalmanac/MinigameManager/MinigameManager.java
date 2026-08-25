package dev.rm20.anglersalmanac.MinigameManager;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.Components.BobberComponent;
import dev.rm20.anglersalmanac.Metadata.*;
import dev.rm20.anglersalmanac.Minigame.FishingMinigameHandler;
import dev.rm20.anglersalmanac.Minigame.MinigameRegistry;
import dev.rm20.anglersalmanac.Utils.*;
import dev.rm20.anglersalmanac.api.AnglersAlmanacAPI;
import org.jspecify.annotations.NonNull;

public class MinigameManager {

    public static void StartGame(Ref<EntityStore> bobberRef, Player player, CommandBuffer<EntityStore> commandBuffer, int depth) {
        InventoryComponent.Hotbar hotbarComp = player.getReference().getStore().getComponent(
                player.getReference(), InventoryComponent.Hotbar.getComponentType()
        );
        if (hotbarComp == null) {
            return;
        }

        ItemStack fishingRod = hotbarComp.getActiveItem();
        if (fishingRod == null) {
            return;
        }

        String minigameKey = resolveMinigame();

        BobberComponent bobberComp = bobberRef.getStore().getComponent(bobberRef, BobberComponent.getComponentType());
        if (bobberComp != null) {
            bobberComp.setMinigameActive(true);
            bobberComp.setMinigameId(minigameKey);
        }
        FishingMinigameHandler handler = MinigameRegistry.get(minigameKey)
                .orElseGet(() -> MinigameRegistry.get("NoMinigame").orElseThrow());

        handler.startGame(bobberRef, player, commandBuffer, depth, fishingRod);
    }

    public static void CancelGame(CommandBuffer<EntityStore> commandBuffer, Ref<EntityStore> minigameRef) {
        String minigameKey = resolveMinigame();

        MinigameRegistry.get(minigameKey).ifPresent(handler -> handler.cancelGame(commandBuffer, minigameRef));
    }

    public static boolean DoMinigameInteraction(CommandBuffer<EntityStore> commandBuffer, Ref<EntityStore> minigameRef, @NonNull InteractionType interactionType, @NonNull InteractionContext context, @NonNull CooldownHandler cooldownHandler) {
        String minigameKey = resolveMinigame();

        return MinigameRegistry.get(minigameKey)
                .map(handler -> handler.handleInteraction(commandBuffer, minigameRef, interactionType, context, cooldownHandler))
                .orElse(false);
    }



    private static String resolveMinigame() {
        return AnglersAlmanacAPI.getConfig().get().getMinigameToUse();
    }
}
