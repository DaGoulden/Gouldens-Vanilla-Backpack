package net.goulden.gouldensvanillabackpack.common.crafting;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class BackpackRecipeSerializer implements RecipeSerializer<BackpackRecipe> {

    private static final RecipeSerializer<ShapedRecipe> SHAPED = RecipeSerializer.SHAPED_RECIPE;

    @Override
    public MapCodec<BackpackRecipe> codec() {
        return SHAPED.codec().xmap(BackpackRecipe::new, recipe -> recipe.base);
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, BackpackRecipe> streamCodec() {
        return SHAPED.streamCodec().map(BackpackRecipe::new, recipe -> recipe.base);
    }
}