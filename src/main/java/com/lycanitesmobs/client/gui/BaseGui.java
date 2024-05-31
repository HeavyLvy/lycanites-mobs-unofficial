package com.lycanitesmobs.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;

public abstract class BaseGui extends Screen {
    public BaseGui(Component screenName) {
        super(screenName);
    }

    public static void renderLivingEntity(GuiGraphics guiGraphics, int x, int y, float scale, float lookX, float lookY, LivingEntity entity) {
        PoseStack matrixStack = guiGraphics.pose();
        float lookXRot = (float)Math.atan((double)(lookX / 40.0F));
        float lookYRot = (float)Math.atan((double)(lookY / 40.0F));
        matrixStack.pushPose();
        matrixStack.translate((float)x, (float)y, 1500.0F);
        matrixStack.scale(1.0F, 1.0F, -1.0F);
        matrixStack.translate(0.0D, 0.0D, 1000.0D);
        matrixStack.scale(scale, scale, scale);
        Quaternionf modelRotationRoll = Axis.ZP.rotationDegrees(180.0F);
        Quaternionf modelRotationPitch = Axis.XP.rotationDegrees(lookYRot * 20.0F);
        modelRotationRoll.mul(modelRotationPitch);
        matrixStack.mulPose(modelRotationRoll);
        matrixStack.mulPose(Axis.YN.rotationDegrees(180.0F));

        float renderYawOffset = entity.yBodyRot;
        float rotationYaw = entity.yRotO;
        float rotationPitch = entity.xRotO;
        float prevRotationYawHead = entity.yHeadRotO;
        float rotationYawHead = entity.yHeadRot;
        entity.yBodyRot = lookXRot * 20.0F;
        entity.yRotO = lookXRot * 40.0F;
        entity.xRotO = -lookYRot * 20.0F;
        entity.yHeadRot = entity.yRotO;
        entity.yHeadRotO = entity.yRotO;
        entity.setOnGround(true);

        EntityRenderDispatcher renderManager = Minecraft.getInstance().getEntityRenderDispatcher();
        modelRotationPitch.conjugate();
        renderManager.overrideCameraOrientation(modelRotationPitch);
        renderManager.setRenderShadow(false);
        MultiBufferSource.BufferSource renderTypeBuffer = Minecraft.getInstance().renderBuffers().bufferSource();
        renderManager.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrixStack, renderTypeBuffer, 15728880);
        renderTypeBuffer.endBatch();
        renderManager.setRenderShadow(true);
        entity.yBodyRot = renderYawOffset;
        entity.yRotO = rotationYaw;
        entity.xRotO = rotationPitch;
        entity.yHeadRotO = prevRotationYawHead;
        entity.yHeadRot = rotationYawHead;
        matrixStack.popPose();
    }
}
