package dev.rm20.anglersalmanac.api;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.Models.FishLoot;

public interface ICatchManager {
    FishLoot firstRoll(Ref<EntityStore> bobberRef, Player player, CommandBuffer<EntityStore> commandBuffer, int depth);
    void dropItem(ItemStack loot, Player player, CommandBuffer<EntityStore> commandBuffer, Ref<EntityStore> bobberRef);
    void dropLoot(FishLoot loot, Player player, CommandBuffer<EntityStore> commandBuffer, Ref<EntityStore> bobberRef, int rating);
}
