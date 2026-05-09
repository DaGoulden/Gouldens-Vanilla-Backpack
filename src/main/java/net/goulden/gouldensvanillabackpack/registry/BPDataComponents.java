package net.goulden.gouldensvanillabackpack.registry;

import com.mojang.serialization.Codec;
import net.goulden.gouldensvanillabackpack.GouldensVanillaBackpack;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class BPDataComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, GouldensVanillaBackpack.MODID);

    public static final Supplier<DataComponentType<Integer>> LID_COLOR = COMPONENTS.register(
            "lid_color", () -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT).build()
    );

    public static final Supplier<DataComponentType<Integer>> BASE_COLOR = COMPONENTS.register(
            "base_color", () -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT).build()
    );

}
