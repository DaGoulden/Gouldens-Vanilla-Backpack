package net.goulden.gouldensvanillabackpack.common.crafting;

import net.goulden.gouldensvanillabackpack.registry.BPDataComponents;
import net.goulden.gouldensvanillabackpack.registry.BPItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

public class BackpackRecipe extends ShapedRecipe {

    final ShapedRecipe base;

    public BackpackRecipe(ShapedRecipe base) {
        super(base.getGroup(), base.category(),
                getPattern(base), getResult(base), base.showNotification());
        this.base = base;
    }

    private static ShapedRecipePattern getPattern(ShapedRecipe recipe) {
        try {
            var field = ShapedRecipe.class.getDeclaredField("pattern");
            field.setAccessible(true);
            return (ShapedRecipePattern) field.get(recipe);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ItemStack getResult(ShapedRecipe recipe) {
        try {
            var field = ShapedRecipe.class.getDeclaredField("result");
            field.setAccessible(true);
            return (ItemStack) field.get(recipe);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack result = new ItemStack(BPItems.BACKPACK.get());

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!(stack.getItem() instanceof DyeItem dyeItem)) continue;

            int row = i / input.width();
            if (row == 0) {
                result.set(BPDataComponents.LID_COLOR.get(),
                        dyeItem.getDyeColor().getTextureDiffuseColor());
            } else if (row == 2) {
                result.set(BPDataComponents.BASE_COLOR.get(),
                        dyeItem.getDyeColor().getTextureDiffuseColor());
            }
        }

        return result;
    }
}
