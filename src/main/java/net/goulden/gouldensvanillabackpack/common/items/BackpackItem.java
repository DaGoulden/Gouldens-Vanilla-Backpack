package net.goulden.gouldensvanillabackpack.common.items;

import net.goulden.gouldensvanillabackpack.registry.BPDataComponents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class BackpackItem extends BlockItem {
    public BackpackItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (Boolean.TRUE.equals(stack.get(BPDataComponents.REINFORCED.get()))) {
            entity.setInvulnerable(true);
        }
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }
}
