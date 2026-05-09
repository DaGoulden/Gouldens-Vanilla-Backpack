package net.goulden.gouldensvanillabackpack.registry;

import com.mojang.serialization.Codec;
import net.goulden.gouldensvanillabackpack.GouldensVanillaBackpack;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class BPAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(
            NeoForgeRegistries.ATTACHMENT_TYPES, GouldensVanillaBackpack.MODID);

    public static final Supplier<AttachmentType<ItemStack>> BACKPACK_SLOT = ATTACHMENT_TYPES.register(
            "backpack_slot", () -> AttachmentType.builder(() -> ItemStack.EMPTY).serialize(ItemStack.OPTIONAL_CODEC).build()
    );

    public static final Supplier<AttachmentType<Integer>> OPEN_COUNT = ATTACHMENT_TYPES.register(
            "open_count", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build()
    );

    public static final Supplier<AttachmentType<Integer>> OPEN_TICKS = ATTACHMENT_TYPES.register(
            "open_ticks", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build()
    );
}
