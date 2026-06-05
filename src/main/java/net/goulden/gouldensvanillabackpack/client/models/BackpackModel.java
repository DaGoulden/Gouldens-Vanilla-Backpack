package net.goulden.gouldensvanillabackpack.client.models;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class BackpackModel {

    // BLOCK MODEL
    public static LayerDefinition createBlockLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();

        PartDefinition base = meshdefinition.getRoot().addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -3.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition lid = base.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 14).addBox(-4.5F, -3.75F, -1.0F, 9.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -7.75F, -2.5F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    // PLAYER MODEL
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();

        PartDefinition base = meshdefinition.getRoot().addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -3.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(32, 0).addBox(-4.0F, -12.5F, -7.0F, 8.0F, 10.0F, 4.0F, new CubeDeformation(0.6F)), PartPose.offset(0.0F, 12.5F, 5.0F));

        PartDefinition lid = base.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 14).addBox(-4.5F, -3.75F, -1.0F, 9.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -7.75F, -2.5F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }
}
