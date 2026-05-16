package net.goulden.gouldensvanillabackpack.client.particles;

import net.goulden.gouldensvanillabackpack.common.particles.BackpackBreakParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.util.FastColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BackpackBreakParticle extends TextureSheetParticle {

    protected BackpackBreakParticle(ClientLevel level, double x, double y, double z,
                                    double vx, double vy, double vz, int color) {
        super(level, x, y, z, vx, vy, vz);
        this.rCol = FastColor.ARGB32.red(color) / 255f;
        this.gCol = FastColor.ARGB32.green(color) / 255f;
        this.bCol = FastColor.ARGB32.blue(color) / 255f;
        this.gravity = 1.0f;
        this.lifetime = (int) (4.0 / (random.nextDouble() * 0.9 + 0.1));
        this.hasPhysics = true;
        this.quadSize = random.nextFloat() * 0.1f + 0.05f;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<BackpackBreakParticleOptions> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(BackpackBreakParticleOptions options, ClientLevel level,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            BackpackBreakParticle particle = new BackpackBreakParticle(
                    level, x, y, z, vx, vy, vz, options.color());
            particle.pickSprite(sprites);
            return particle;
        }
    }
}