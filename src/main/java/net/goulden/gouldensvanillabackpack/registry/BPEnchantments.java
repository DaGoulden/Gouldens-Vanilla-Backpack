package net.goulden.gouldensvanillabackpack.registry;

import net.goulden.gouldensvanillabackpack.GouldensVanillaBackpack;
import net.goulden.gouldensvanillabackpack.common.blocks.BackpackBlockEntity;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class BPEnchantments {
    public static final ResourceKey<Enchantment> HOLDING = ResourceKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(GouldensVanillaBackpack.MODID, "holding")
    );

    public static int getHoldingLevel(ItemStack stack, RegistryAccess registryAccess) {
        var enchantments = stack.get(DataComponents.ENCHANTMENTS);
        if (enchantments == null) return 0;
        var holder = registryAccess.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(HOLDING);
        return enchantments.getLevel(holder);
    }

    public static int getHoldingLevel(ItemEnchantments enchantments, RegistryAccess registryAccess) {
        var holder = registryAccess.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(HOLDING);
        return enchantments.getLevel(holder);
    }

    public static int getSlotCount(ItemStack stack, RegistryAccess registryAccess) {
        return BackpackBlockEntity.DEFAULT_SLOTS + getHoldingLevel(stack, registryAccess) * 9;
    }
}