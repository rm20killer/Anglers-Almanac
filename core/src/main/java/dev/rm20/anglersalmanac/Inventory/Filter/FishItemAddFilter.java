package dev.rm20.anglersalmanac.Inventory.Filter;

import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterActionType;
import com.hypixel.hytale.server.core.inventory.container.filter.ItemSlotFilter;
import dev.rm20.anglersalmanac.Models.FishLootManager;
import dev.rm20.anglersalmanac.Utils.FishStateManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FishItemAddFilter implements ItemSlotFilter {

    @Override
    public boolean test(@Nonnull FilterActionType actionType, @Nonnull ItemContainer container, short slot, @Nullable ItemStack itemStack) {
        return switch (actionType) {
            case ADD -> test(itemStack != null ? itemStack.getItem() : null);
            case REMOVE, DROP -> false;
        };
    }

    @Override
    public boolean test(@Nullable Item item) {
        if (item == null) {
            return true;
        }

        return FishLootManager.getInternalFishData(item.getId()) != null;
    }
}
