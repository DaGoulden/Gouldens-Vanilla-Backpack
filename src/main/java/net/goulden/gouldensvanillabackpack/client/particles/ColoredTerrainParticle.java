package net.goulden.gouldensvanillabackpack.client.particles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ColoredTerrainParticle extends TerrainParticle {
    public ColoredTerrainParticle(ClientLevel level, double x, double y, double z,
                                  double vx, double vy, double vz,
                                  BlockState state, BlockPos pos, int color) {
        super(level, x, y, z, vx, vy, vz, state, pos);
        this.rCol = FastColor.ARGB32.red(color) / 255f;
        this.gCol = FastColor.ARGB32.green(color) / 255f;
        this.bCol = FastColor.ARGB32.blue(color) / 255f;

        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getModelManager()
                .getAtlas(TextureAtlas.LOCATION_BLOCKS)
                .getSprite(ResourceLocation.fromNamespaceAndPath(
                        "gouldensvanillabackpack", "block/backpack_particle"));
        this.setSprite(sprite);
    }

    @Override
    public TerrainParticle updateSprite(BlockState state, BlockPos pos) {
        return this;
    }
}