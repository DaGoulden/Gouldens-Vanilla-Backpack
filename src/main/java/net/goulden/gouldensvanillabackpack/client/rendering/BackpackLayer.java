package net.goulden.gouldensvanillabackpack.client.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.goulden.gouldensvanillabackpack.GouldensVanillaBackpack;
import net.goulden.gouldensvanillabackpack.registry.BPDataAttachments;
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

import static net.goulden.gouldensvanillabackpack.registry.BPDataAttachments.OPEN_COUNT;
import static net.goulden.gouldensvanillabackpack.registry.BPDataAttachments.OPEN_TICKS;

@OnlyIn(Dist.CLIENT)
public class BackpackLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {

    private static final ResourceLocation OVERLAY_TEXTURE = ResourceLocation.fromNamespaceAndPath(GouldensVanillaBackpack.MODID,
            "textures/model/backpack_overlay.png");

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

        ItemStack itemStack = livingEntity.getData(BPDataAttachments.BACKPACK_SLOT);
        if (itemStack.getItem() != BPItems.BACKPACK.asItem()) return;
        Integer lidColor = itemStack.get(BPDataAttachments.LID_COLOR.get());
        Integer baseColor = itemStack.get(BPDataAttachments.BASE_COLOR.get());
        if (lidColor == null && baseColor == null) return;

        poseStack.pushPose();

        // LID ANIMATION
        float lidRot = 0;
        boolean isOpen = livingEntity.getData(OPEN_COUNT) > 0;
        int openTicks = livingEntity.getData(OPEN_TICKS);
        if (isOpen && openTicks < 10) {
            float t = openTicks + partialTicks;
            lidRot = (float) Math.pow(2, -1 * t) * Mth.sin((t - 0.75F) * 0.5F) + 1;
        } else if (openTicks == 10) {
            lidRot = 1;
        } else if (openTicks > 0) {
            float t = openTicks - partialTicks;
            lidRot = (float) -Math.pow(2, t - 10) * Mth.sin((t - 10.75F) * 0.5F);
        }

        // BODY-ANCHORED
        this.backpackModel.copyFrom(parentBody);
        ModelPart base = this.backpackModel.getChild("base");
        ModelPart lid = base.getChild("lid");
        lid.xRot = lidRot;

        // BASE COLOR RENDER
        if (baseColor != null) {
            VertexConsumer vcBase = ItemRenderer.getArmorFoilBuffer(buffer, RenderType.armorCutoutNoCull(OVERLAY_TEXTURE), itemStack.hasFoil());
            this.backpackModel.render(poseStack, vcBase, packedLight, OverlayTexture.NO_OVERLAY, FastColor.ARGB32.opaque(baseColor));
        }

        // LID COLOR RENDER
        if (lidColor != null) {
            poseStack.pushPose();
            this.backpackModel.translateAndRotate(poseStack);
            base.translateAndRotate(poseStack);
            VertexConsumer vcLid = ItemRenderer.getArmorFoilBuffer(buffer, RenderType.armorCutoutNoCull(OVERLAY_TEXTURE), itemStack.hasFoil());
            lid.render(poseStack, vcLid, packedLight, OverlayTexture.NO_OVERLAY, FastColor.ARGB32.opaque(lidColor));
            poseStack.popPose();
        }

        poseStack.popPose();
    }
}