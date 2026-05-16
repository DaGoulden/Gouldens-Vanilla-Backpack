package net.goulden.gouldensvanillabackpack.common.particles;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class BackpackBreakParticleType extends ParticleType<BackpackBreakParticleOptions> {

    public BackpackBreakParticleType() {
        super(false);
    }

    @Override
    public MapCodec<BackpackBreakParticleOptions> codec() {
        return BackpackBreakParticleOptions.CODEC;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, BackpackBreakParticleOptions> streamCodec() {
        return BackpackBreakParticleOptions.STREAM_CODEC;
    }
}