package dev.rm20.anglersalmanac.Interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AlmanacBook.AlmanacRepository;
import dev.rm20.anglersalmanac.AlmanacBook.BookPageManager;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Metadata.BookData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static dev.rm20.anglersalmanac.AlmanacBook.AlmanacBook.syncCustomBookDisplay;


public class OpenBookInteraction extends SimpleInstantInteraction {
    public static final BuilderCodec<OpenBookInteraction> CODEC = BuilderCodec.builder(
            OpenBookInteraction.class, OpenBookInteraction::new, SimpleInstantInteraction.CODEC
    ).build();

    private static final String UNINITIALIZED_ID = "Almanac_Book";
    private static final String INITIALIZED_PREFIX = "almanac.book.";

    @Override
    protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldown) {
        CommandBuffer<EntityStore> buffer = context.getCommandBuffer();
        Ref<EntityStore> playerRef = context.getOwningEntity();
        ItemStack heldItem = context.getHeldItem();

        if (buffer == null || heldItem == null) {
            return;
        }

        String itemId = heldItem.getItemId();
        boolean isRawBook = itemId.equals(UNINITIALIZED_ID);
        boolean isInitializedBook = itemId.startsWith(INITIALIZED_PREFIX);
        if (!isRawBook && !isInitializedBook) {
            return;
        }

        Player player = buffer.getComponent(playerRef, Player.getComponentType());
        if (player == null) {
            return;
        }

        BookData data = heldItem.getFromMetadataOrNull(BookData.KEY, BookData.CODEC);
        if(isInitializedBook)
        {
            if(data == null)
            {
                BookData newData = new BookData();
                String UUID = itemId.replace(INITIALIZED_PREFIX, "");
                AlmanacRepository.BookEntry bookEntry = AlmanacRepository.getBookData(UUID);
                if(bookEntry!=null)
                {
                    newData.setPlayerUUID(UUID);
                    newData.setPlayerName(bookEntry.playerName);
                    newData.setPageNumber(0);
                    openAndSyncBook(playerRef, player, newData);
                    return;
                }

            }
            openAndSyncBook(playerRef, player, data);
            return;
        }

        if (data != null) {
            data = upgradeToInitializedBook(playerRef, player, heldItem, data);
        } else {
            data = initializeNewBook(playerRef, player, heldItem);
        }

        if (data == null) {
            return;
        }

        // 3. Centralized Execution
        openAndSyncBook(playerRef, player, data);
    }

    private BookData initializeNewBook(Ref<EntityStore> playerRef, Player player, ItemStack heldItem) {
        UUIDComponent uuidComp = playerRef.getStore().getComponent(playerRef, UUIDComponent.getComponentType());

        if (uuidComp == null) return null;

        String uuidStr = uuidComp.getUuid().toString();


        InventoryComponent.Hotbar inv = player.getReference().getStore().getComponent(player.getReference(), InventoryComponent.Hotbar.getComponentType());
        //ItemStack updatedItem = heldItem.withMetadata(BookData.KEYED_CODEC, newData);
        if(inv == null)
        {
            AnglersAlmanac.LOGGER.atWarning().log("Something went wrong while creating new book. Missing InventoryComponent");
            return null;
        }
        byte slot = inv.getActiveSlot();
        PlayerRef playerRefComp = playerRef.getStore().getComponent(playerRef, PlayerRef.getComponentType());

        BookData newData = new BookData();
        newData.setPlayerUUID(uuidStr);
        newData.setPlayerName(playerRefComp != null ? playerRefComp.getUsername() : "NO NAME FOUND");
        newData.setPageNumber(0);

        Item newbook = syncCustomBookDisplay(playerRefComp, newData.getPlayerUUID(), newData.getPlayerName());
        ItemStack newHeldedItem = new ItemStack(newbook.getId(), 1);
        newHeldedItem.withMetadata(BookData.KEYED_CODEC, newData);
        inv.getInventory().replaceItemStackInSlot(slot, heldItem, newHeldedItem);


        return newData;
    }

    private BookData upgradeToInitializedBook(Ref<EntityStore> playerRef, Player player, ItemStack heldItem, @Nullable BookData existingData) {
        if (existingData == null) {
            return null;
        }
        InventoryComponent.Hotbar inv = player.getReference().getStore().getComponent(player.getReference(), InventoryComponent.Hotbar.getComponentType());
        if(inv == null)
        {
            AnglersAlmanac.LOGGER.atWarning().log("Something went wrong while Updating new book. Missing InventoryComponent");
            return null;
        }
        byte slot = inv.getActiveSlot();
        PlayerRef playerRefComp = playerRef.getStore().getComponent(playerRef, PlayerRef.getComponentType());
        if (playerRefComp != null) {
            Item newbook = syncCustomBookDisplay(playerRefComp, existingData.getPlayerUUID(), existingData.getPlayerName());
            ItemStack newHeldedItem = new ItemStack(newbook.getId(), 1);
            newHeldedItem.withMetadata(BookData.KEYED_CODEC, existingData);
            inv.getInventory().replaceItemStackInSlot(slot, heldItem, newHeldedItem);
        }
        return existingData;
    }

    private void openAndSyncBook(Ref<EntityStore> playerRef, Player player, BookData data) {
        PlayerRef playerRefComp = playerRef.getStore().getComponent(playerRef, PlayerRef.getComponentType());
        if (playerRefComp != null) {
            syncCustomBookDisplay(playerRefComp, data.getPlayerUUID(), data.getPlayerName());
        }
        BookPageManager.OpenPage(player, 0, data.getPlayerUUID(), data.getPlayerName());
    }
}
