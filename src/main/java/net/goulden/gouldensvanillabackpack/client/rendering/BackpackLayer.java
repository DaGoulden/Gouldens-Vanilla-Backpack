package net.goulden.gouldensvanillabackpack.client.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.goulden.gouldensvanillabackpack.GouldensVanillaBackpack;
import net.goulden.gouldensvanillabackpack.registry.BPAttachments;
import net.goulden.gouldensvanillabackpack.registry.BPDataComponents;
import net.goulden.gouldensvanillabackpack.registry.BPItems;
import net.goulden.gouldensvanillabackpack.registry.BPLayers;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BackpackLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(GouldensVanillaBackpack.MODID,
            "textures/model/backpack.png");
    private static final ResourceLocation DEPTHS_TEXTURE = ResourceLocation.fromNamespaceAndPath(GouldensVanillaBackpack.MODID,
            "textures/model/backpack_depths.png");
    private static final ResourceLocation REINFORCED_TEXTURE = ResourceLocation.fromNamespaceAndPath(GouldensVanillaBackpack.MODID,
            "textures/model/backpack_reinforced.png");
    private static final ResourceLocation LOCKED_TEXTURE = ResourceLocation.fromNamespaceAndPath(GouldensVanillaBackpack.MODID,
            "textures/model/backpack_locked.png");

    private final ModelPart backpackModel;
    private final ModelPart parentBody;

    public BackpackLayer(RenderLayerParent renderer, EntityModelSet entityModelSet) {
        super(renderer);
        this.backpackModel = entityModelSet.bakeLayer(BPLayers.BACKPACK);
        this.parentBody = this.getParentModel().body;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T livingEntity,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float headYaw, float headPitch) {

        ItemStack itemStack = livingEntity.getData(BPAttachments.BACKPACK_SLOT);
        if (itemStack.getItem() != BPItems.BACKPACK.asItem()) return;
        Integer lidColor = itemStack.get(BPDataComponents.LID_COLOR.get());
        Integer baseColor = itemStack.get(BPDataComponents.BASE_COLOR.get());
        if (lidColor == null && baseColor == null) return;

        poseStack.pushPose();

        float lidRot = 0;
        boolean isOpen = livingEntity.getData(BPAttachments.IS_OPEN);
        int openTicks = livingEntity.getData(BPAttachments.OPEN_TICKS);
        if (isOpen && openTicks < 10) {
            float t = openTicks + partialTicks;
            lidRot = (float) Math.pow(2, -1 * t) * Mth.sin((t - 0.75F) * 0.5F) + 1;
        } else if (openTicks == 10) {
            lidRot = 1;
        } else if (openTicks > 0) {
            float t = openTicks - partialTicks;
            lidRot = (float) -Math.pow(2, t - 10) * Mth.sin((t - 10.75F) * 0.5F);
        }

        this.backpackModel.copyFrom(parentBody);
        ModelPart base = this.backpackModel.getChild("base");
        ModelPart lid = base.getChild("lid");
        lid.xRot = lidRot;

        if (baseColor != null) {
            VertexConsumer vcBase = ItemRenderer.getArmorFoilBuffer(buffer, RenderType.armorCutoutNoCull(TEXTURE), itemStack.hasFoil());
            this.backpackModel.render(poseStack, vcBase, packedLight, OverlayTexture.NO_OVERLAY, FastColor.ARGB32.opaque(baseColor));
        }

        if (lidColor != null) {
            poseStack.pushPose();
            this.backpackModel.translateAndRotate(poseStack);
            base.translateAndRotate(poseStack);
            VertexConsumer vcLid = ItemRenderer.getArmorFoilBuffer(buffer, RenderType.armorCutoutNoCull(TEXTURE), itemStack.hasFoil());
            lid.render(poseStack, vcLid, packedLight, OverlayTexture.NO_OVERLAY, FastColor.ARGB32.opaque(lidColor));
            poseStack.popPose();
        }

        VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(buffer,
                RenderType.armorCutoutNoCull(DEPTHS_TEXTURE), itemStack.hasFoil());
        this.backpackModel.render(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);

        Boolean reinforced = itemStack.get(BPDataComponents.REINFORCED.get());
        if (reinforced != null && reinforced) {
            VertexConsumer vcR = ItemRenderer.getArmorFoilBuffer(buffer,
                    RenderType.armorCutoutNoCull(REINFORCED_TEXTURE), itemStack.hasFoil());
            this.backpackModel.render(poseStack, vcR, packedLight, OverlayTexture.NO_OVERLAY);
        }

        Boolean locked = itemStack.get(BPDataComponents.LOCKED.get());
        if (locked != null && locked) {
            VertexConsumer vcR = ItemRenderer.getArmorFoilBuffer(buffer,
                    RenderType.armorCutoutNoCull(LOCKED_TEXTURE), itemStack.hasFoil());
            this.backpackModel.render(poseStack, vcR, packedLight, OverlayTexture.NO_OVERLAY);
        }

        poseStack.popPose();
    }
}