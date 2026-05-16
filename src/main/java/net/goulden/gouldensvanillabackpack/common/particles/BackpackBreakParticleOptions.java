package net.goulden.gouldensvanillabackpack.common.particles;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.goulden.gouldensvanillabackpack.registry.BPParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record BackpackBreakParticleOptions(int color) implements ParticleOptions {

    public static final MapCodec<BackpackBreakParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst ->
            inst.group(
                    com.mojang.serialization.Codec.INT.fieldOf("color").forGetter(BackpackBreakParticleOptions::color)
            ).apply(inst, BackpackBreakParticleOptions::new)
    );

    public static final StreamCodec<ByteBuf, BackpackBreakParticleOptions> STREAM_CODEC =
            ByteBufCodecs.INT.map(BackpackBreakParticleOptions::new, BackpackBreakParticleOptions::color);

    @Override
    public ParticleType<?> getType() {
        return BPParticles.BACKPACK_BREAK.get();
    }
}