package net.goulden.gouldensvanillabackpack.client;

import net.goulden.gouldensvanillabackpack.client.particles.ColoredTerrainParticle;
import net.goulden.gouldensvanillabackpack.common.blocks.BackpackBlockEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;

@OnlyIn(Dist.CLIENT)
public class BackpackBlockClientExtensions implements IClientBlockExtensions {

    @Override
    public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine engine) {
        int baseColor = 0xFFFFFFFF;
        int lidColor = 0xFFFFFFFF;

        if (level.getBlockEntity(pos) instanceof BackpackBlockEntity be) {
            if (be.getBaseColor() != 0) baseColor = be.getBaseColor();
            if (be.getLidColor() != 0) lidColor = be.getLidColor();
        }

        VoxelShape shape = state.getShape(level, pos);
        final int fc1 = baseColor;
        final int fc2 = lidColor;

        shape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> {
            double d1 = Math.min(1.0, x2 - x1);
            double d2 = Math.min(1.0, y2 - y1);
            double d3 = Math.min(1.0, z2 - z1);
            int nx = Math.max(2, Mth.ceil(d1 / 0.25));
            int ny = Math.max(2, Mth.ceil(d2 / 0.25));
            int nz = Math.max(2, Mth.ceil(d3 / 0.25));

            for (int lx = 0; lx < nx; lx++) {
                for (int ly = 0; ly < ny; ly++) {
                    for (int lz = 0; lz < nz; lz++) {
                        double dx = (lx + 0.5) / nx;
                        double dy = (ly + 0.5) / ny;
                        double dz = (lz + 0.5) / nz;
                        double wx = pos.getX() + dx * d1 + x1;
                        double wy = pos.getY() + dy * d2 + y1;
                        double wz = pos.getZ() + dz * d3 + z1;

                        int color = (lx + ly + lz) % 2 == 0 ? fc1 : fc2;
                        engine.add(new ColoredTerrainParticle(
                                (ClientLevel) level, wx, wy, wz,
                                dx - 0.5, dy - 0.5, dz - 0.5,
                                state, pos, color).updateSprite(state, pos));
                    }
                }
            }
        });

        return true;
    }
}