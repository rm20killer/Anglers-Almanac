package dev.rm20.anglersalmanac.Interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.Animation;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Interactions.Rod.UseRodInteraction;
import dev.rm20.anglersalmanac.MinigameManager.MinigameManager;
import dev.rm20.anglersalmanac.Metadata.FishingRodData;
import org.jspecify.annotations.NonNull;

import static dev.rm20.anglersalmanac.Interactions.Rod.UseRodInteraction.updateMetadata;

public class MinigameInteraction extends SimpleInstantInteraction {
    public static final BuilderCodec<MinigameInteraction> CODEC = BuilderCodec.builder(
            MinigameInteraction.class, MinigameInteraction::new, SimpleInstantInteraction.CODEC
    ).build();

    @Override
    protected void firstRun(@NonNull InteractionType interactionType, @NonNull InteractionContext context, @NonNull CooldownHandler cooldownHandler) {
        CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
        Ref<EntityStore> playerRef = context.getOwningEntity();
        ItemStack heldItem = context.getHeldItem();
        if (commandBuffer == null || playerRef == null || heldItem == null) return;
        // Cancel interaction if the rod is not in minigame mode.
        FishingRodData meta = heldItem.getFromMetadataOrNull(FishingRodData.KEY, FishingRodData.CODEC);
        if(meta == null){
            context.getState().state = InteractionState.Failed;
            return;
        }
        if(meta.getMode() != 1){
            context.getState().state = InteractionState.Failed;
            return;
        }

        if(meta.getBoundMinigame() == null){
            AnglersAlmanac.LOGGER.atInfo().log("NO bound minigame UUID for DoInteraction");
            return;
        }
        Ref<EntityStore> minigameRef = commandBuffer.getExternalData().getRefFromUUID(meta.getBoundMinigame());
        if(minigameRef == null){
            AnglersAlmanac.LOGGER.atInfo().log("FAILED to get minigameRef for DoInteraction");
            return;
        }

        if(!MinigameManager.DoMinigameInteraction(commandBuffer, minigameRef, interactionType, context, cooldownHandler))
        {
            Player player = commandBuffer.getComponent(playerRef, Player.getComponentType());
            if (player == null) return;
            InventoryComponent.Hotbar inv = player.getReference().getStore().getComponent(player.getReference(), InventoryComponent.Hotbar.getComponentType());
            if(inv ==null)
            {
                return;
            }
            updateMetadata(inv, inv.getActiveSlot(), heldItem, null, null, 0);
            PlayerRef playerRefComp = playerRef.getStore().getComponent(playerRef, PlayerRef.getComponentType());
            if(playerRefComp !=null) {
                AnglersAlmanac.LOGGER.atInfo().log("Fixing busted rod for: "+playerRefComp.getUsername());
            }
        }

    }


}
