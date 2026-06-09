package dev.rm20.anglersalmanac.triggereffects;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.inventory.InventoryComponent.Hotbar;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.util.InventoryHelper;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GiveRodEffect extends TriggerEffect {
    public static final BuilderCodec<GiveRodEffect> CODEC;
    @Nullable
    private Boolean removeRod;
    static {
        CODEC = BuilderCodec.builder(
                GiveRodEffect.class,
                GiveRodEffect::new,
                BASE_CODEC
        ).append(
                new KeyedCodec<>("RemoveRod", Codec.BOOLEAN, false),
                (e, v) -> e.removeRod = v,
                (e) -> e.removeRod
        ).add().build();
    }

    @Override
    public void execute(@Nonnull TriggerContext context) {
        String rodId = "AA_Rod_Iron";

        Ref<EntityStore> player = context.getEntityRef();
        if (player == null) {
            return;
        }

        Hotbar inv = player.getStore().getComponent(player, Hotbar.getComponentType());
        if (inv == null) {
            return;
        }

        var container = inv.getInventory();
        if (container != null) {
            int existingCount = container.countItemStacks(new Predicate<ItemStack>() {
                @Override
                public boolean test(ItemStack itemStack) {
                    return itemStack != null && rodId.equals(itemStack.getItemId());
                }
            });

            if (existingCount == 0) {
                ItemStack rod = InventoryHelper.createItem(rodId);
                if (rod != null) {
                    container.addItemStack(rod);
                }
            }
        }
    }
}