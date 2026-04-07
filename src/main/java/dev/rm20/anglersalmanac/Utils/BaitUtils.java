package dev.rm20.anglersalmanac.Utils;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.util.InventoryHelper;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Models.FishBaitData;

import javax.annotation.Nullable;

public class BaitUtils {
    public static ItemStack findBait(ComponentAccessor<EntityStore> accessor, Ref<EntityStore> entityRef) {
        CombinedItemContainer searchContainer = InventoryComponent.getCombined(
                accessor,
                entityRef,
                InventoryComponent.Utility.getComponentType(),
                InventoryComponent.Hotbar.getComponentType()
        );
        for (short i = 0; i < searchContainer.getCapacity(); i++) {
            ItemStack stack = searchContainer.getItemStack(i);
            if (stack != null && getBaitData(stack) != null) {
                return stack;
            }
        }
        return null;
    }

    @Nullable
    public static FishBaitData getBaitData(ItemStack stack) {
        String itemId = stack.getItemId();
        return FishBaitData.getAssetStore().getAssetMap().getAssetMap().values().stream()
                .filter(data -> data.itemId != null && data.itemId.equals(itemId))
                .findFirst()
                .orElse(null);
    }

    private static boolean isFishBait(ItemStack stack) {
        return getBaitData(stack) != null;
    }
    public static void giveBait(Player player, String item, CommandBuffer<EntityStore> commandBuffer)
    {
        ItemStack itemStack;
        itemStack = InventoryHelper.createItem(item);

        if (itemStack == null) {
            return;
        }
        giveBait(player,itemStack,commandBuffer);

    }

    public static void giveBait(Player player, ItemStack stack, CommandBuffer<EntityStore> commandBuffer)
    {
        TransformComponent transform = player.getReference().getStore().getComponent(player.getReference(), TransformComponent.getComponentType());
        ItemUtils.interactivelyPickupItem(player.getReference(), stack, transform.getPosition(),commandBuffer);
    }

    public static void removeBait(Player player, String targetItemId) {
        CombinedItemContainer searchContainer = InventoryComponent.getCombined(
                player.getReference().getStore(),
                player.getReference(),
                InventoryComponent.Utility.getComponentType(),
                InventoryComponent.Hotbar.getComponentType()
        );
        for (short i = 0; i < searchContainer.getCapacity(); i++) {
            ItemStack stack = searchContainer.getItemStack(i);

            if (stack != null && stack.getItemId().equals(targetItemId)) {
                int newQuantity = stack.getQuantity() - 1;

                if (newQuantity <= 0) {
                    searchContainer.replaceItemStackInSlot(i, stack, null);
                } else {
                    ItemStack updatedStack = stack.withQuantity(newQuantity);
                    searchContainer.replaceItemStackInSlot(i, stack, updatedStack);
                }
                AnglersAlmanac.LOGGER.atInfo().log("Consumed 1 %s from slot %d", targetItemId, i);
                break;
            }
        }
    }



}
