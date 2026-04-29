package net.goulden.gouldensvanillabackpack.registry;

import net.goulden.gouldensvanillabackpack.GouldensVanillaBackpack;
import net.goulden.gouldensvanillabackpack.common.crafting.BackpackRecipe;
import net.goulden.gouldensvanillabackpack.common.crafting.BackpackRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class BPRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, GouldensVanillaBackpack.MODID);

    public static final Supplier<RecipeSerializer<BackpackRecipe>> BACKPACK_SERIALIZER =
            SERIALIZERS.register("backpack", BackpackRecipeSerializer::new);
}