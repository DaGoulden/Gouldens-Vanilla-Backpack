package net.goulden.gouldensvanillabackpack.client.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.goulden.gouldensvanillabackpack.GouldensVanillaBackpack;
import net.goulden.gouldensvanillabackpack.common.blocks.BackpackBlock;
import net.goulden.gouldensvanillabackpack.common.blocks.BackpackBlockEntity;
import net.goulden.gouldensvanillabackpack.registry.BPLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BackpackBlockRenderer implements BlockEntityRenderer<BackpackBlockEntity> {

    private static final ResourceLocation OVERLAY_TEXTURE = ResourceLocation.fromNamespaceAndPath(GouldensVanillaBackpack.MODID,
            "textures/entity/backpack_overlay.png");

    private final ModelPart base;
    private final ModelPart lid;

    public BackpackBlockRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart backpack = context.bakeLayer(BPLayers.BACKPACK_BLOCK);
        this.base = backpack.getChild("base");
        this.lid = base.getChild("lid");
    }

    @Override
    public void render(BackpackBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {

        poseStack.pushPose();

        boolean isFloating = blockEntity.getBlockState().getValue(BackpackBlock.FLOATING);
        float dir = blockEntity.getBlockState().getValue(BackpackBlock.FACING).toYRot();
        float lidRot = 0;
        float baseRotX = 0;
        float baseRotZ = 0;
        float basePosY = 24;

        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-dir));
        poseStack.scale(1.0F, -1.0F, -1.0F);
        poseStack.translate(0.0F, isFloating ? -0.8F : -1.0F, 0.0F);

        if (blockEntity.open && blockEntity.openTicks < 10) {
            float t = blockEntity.openTicks + partialTick;
            lidRot = (float) Math.pow(2, -1 * t) * Mth.sin((t - 0.75F) * 0.7F) + 1;
        } else if (blockEntity.openTicks == 10) {
            lidRot = 1;
        } else if (blockEntity.openTicks > 0) {
            float t = blockEntity.openTicks - partialTick;
            lidRot = (float) -Math.pow(2, t - 10) * Mth.sin((t - 10.75F) * 0.7F);
        }

        if (blockEntity.placeTicks <= 3) {
            float t = (blockEntity.placeTicks + partialTick) / 4;
            basePosY = t * t * 4 + 20;
        }
        if (blockEntity.placeTicks <= 7) {
            float t = (blockEntity.placeTicks + partialTick) / 8;
            baseRotX = Mth.sin(t * 10) * 0.1F * (1 - t);
            baseRotZ = Mth.cos(t * 10) * 0.1F * (1 - t);
        }

        if (isFloating) {
            float t = blockEntity.floatTicks + partialTick;
            basePosY += Mth.sin((t + 20) * Mth.DEG_TO_RAD * 4) * 0.75F;
            baseRotX += Mth.sin(t * Mth.DEG_TO_RAD * 4) * 0.02F;
            baseRotZ += Mth.cos(t * Mth.DEG_TO_RAD * 4) * 0.02F;
        }

        this.lid.xRot = lidRot * 1.5F;
        this.base.xRot = baseRotX;
        this.base.zRot = baseRotZ;
        this.base.y = basePosY;

        // base.render() renderiza el cuerpo + tapa (lid es hijo de base)
        if (blockEntity.getBaseColor() != 0) {
            VertexConsumer vcBase = buffer.getBuffer(RenderType.entityCutoutNoCull(OVERLAY_TEXTURE));
            this.base.render(poseStack, vcBase, packedLight, packedOverlay, FastColor.ARGB32.opaque(blockEntity.getBaseColor()));
        }

        // Para renderizar solo la tapa con lidColor hay que entrar al espacio de base primero
        if (blockEntity.getLidColor() != 0) {
            poseStack.pushPose();
            this.base.translateAndRotate(poseStack);
            VertexConsumer vcLid = buffer.getBuffer(RenderType.entityCutoutNoCull(OVERLAY_TEXTURE));
            this.lid.render(poseStack, vcLid, packedLight, packedOverlay, FastColor.ARGB32.opaque(blockEntity.getLidColor()));
            poseStack.popPose();
        }

        poseStack.popPose();
    }
}