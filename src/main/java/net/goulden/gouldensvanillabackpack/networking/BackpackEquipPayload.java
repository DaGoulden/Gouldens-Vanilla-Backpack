package net.goulden.gouldensvanillabackpack.networking;

import net.goulden.gouldensvanillabackpack.GouldensVanillaBackpack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record BackpackEquipPayload(int entityId, ItemStack stack) implements CustomPacketPayload {
    public static final Type<BackpackEquipPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(GouldensVanillaBackpack.MODID, "backpack_equip"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BackpackEquipPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, BackpackEquipPayload::entityId,
                    ItemStack.OPTIONAL_STREAM_CODEC, BackpackEquipPayload::stack,
                    BackpackEquipPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}