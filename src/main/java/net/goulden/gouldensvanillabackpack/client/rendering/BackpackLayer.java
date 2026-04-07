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
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import static net.goulden.gouldensvanillabackpack.registry.BPDataAttachments.OPEN_COUNT;
import static net.goulden.gouldensvanillabackpack.registry.BPDataAttachments.OPEN_TICKS;

@OnlyIn(Dist.CLIENT)
public class BackpackLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M>{
    private final ModelPart backpackModel;

    private ModelPart model;

    private final ModelPart parentBody;

    private static ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(GouldensVanillaBackpack.MODID, "textures/model/backpack.png");
    private static ResourceLocation OVERLAY_TEXTURE = ResourceLocation.fromNamespaceAndPath(GouldensVanillaBackpack.MODID, "textures/model/backpack_overlay.png");

    public BackpackLayer(RenderLayerParent renderer, EntityModelSet entityModelSet) {
        super(renderer);
        this.backpackModel = entityModelSet.bakeLayer(BPLayers.BACKPACK);
        this.parentBody = this.getParentModel().body;
    }

    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float headYaw, float headPitch) {
        ItemStack itemStack = livingEntity.getData(BPDataAttachments.BACKPACK_SLOT);

        if (itemStack.getItem() == BPItems.BACKPACK.asItem()) {

            this.model = backpackModel;

            renderBaseLayer(poseStack, buffer, packedLight, livingEntity, partialTicks, itemStack, true);
        }
    }

    private void renderBaseLayer(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T livingEntity, float partialTicks, ItemStack itemStack, boolean copyPose) {
        poseStack.pushPose();
        float lidRot = 0;
        boolean isOpen = livingEntity.getData(OPEN_COUNT) > 0;
        int openTicks = livingEntity.getData(OPEN_TICKS);

        if (isOpen && openTicks < 10) {
            float t = ((float)openTicks + partialTicks);
            lidRot = (float) Math.pow(2, -1 * t) * Mth.sin((t - 0.75F) * 0.5F) +1;
        } else if (openTicks == 10){
            lidRot = 1;
        } else if (openTicks > 0){
            float t = ((float)openTicks - partialTicks);
            lidRot = (float) -Math.pow(2, t -10) * Mth.sin((t - 10.75F) * 0.5F);
        }

        this.model.getChild("base").getChild("lid").xRot = lidRot;
        if (copyPose) { this.backpackModel.copyFrom(parentBody); }
        VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(buffer, RenderType.armorCutoutNoCull(TEXTURE), itemStack.hasFoil());
        this.model.render(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
        renderColoredLayer(poseStack, buffer, packedLight, itemStack);
        poseStack.popPose();
    }

    private void renderColoredLayer(PoseStack poseStack, MultiBufferSource buffer, int packedLight, ItemStack itemStack) {
        int i = DyedItemColor.getOrDefault(itemStack, 0);
        if (FastColor.ARGB32.alpha(i) == 0) {
            return;
        }

        VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(buffer, RenderType.armorCutoutNoCull(OVERLAY_TEXTURE), itemStack.hasFoil());

        this.model.render(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, FastColor.ARGB32.opaque(i));
    }
}
