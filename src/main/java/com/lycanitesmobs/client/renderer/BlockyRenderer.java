package com.lycanitesmobs.client.renderer;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.blocky.Balayang;
import com.lycanitesmobs.core.entity.creature.EntityBalayang;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;

public class BlockyRenderer extends MobRenderer<Mob, HierarchicalModel<Mob>> {
    private final ResourceLocation texture;

    public BlockyRenderer(EntityRendererProvider.Context pContext, ResourceLocation texture, HierarchicalModel entityModel) {
        super(pContext, entityModel, 0.3f);
        this.texture = texture;
    }

    @Override
    public ResourceLocation getTextureLocation(Mob entity) {
        return texture;
    }

    @Override
    protected void setupRotations(Mob entity, PoseStack pPoseStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
        super.setupRotations(entity, pPoseStack, pAgeInTicks, pRotationYaw, pPartialTicks);
       /* float bodyYaw = entity.yBodyRot;
        float headYaw = entity.getYHeadRot();
        float deltaYaw = *//*headYaw - *//*bodyYaw;

        if (entity.getPose() == Pose.SLEEPING) {
            return;
        }

        pPoseStack.mulPose(Axis.YP.rotationDegrees(headYaw));*/
    }
}
