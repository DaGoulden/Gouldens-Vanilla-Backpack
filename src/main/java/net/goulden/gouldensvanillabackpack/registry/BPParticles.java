package net.goulden.gouldensvanillabackpack.registry;

import net.goulden.gouldensvanillabackpack.GouldensVanillaBackpack;
import net.goulden.gouldensvanillabackpack.common.particles.BackpackBreakParticleType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class BPParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, GouldensVanillaBackpack.MODID);

    public static final Supplier<BackpackBreakParticleType> BACKPACK_BREAK =
            PARTICLES.register("backpack_break", BackpackBreakParticleType::new);
}