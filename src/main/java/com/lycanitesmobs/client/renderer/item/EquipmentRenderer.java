package com.lycanitesmobs.client.renderer.item;

import com.lycanitesmobs.client.manager.ModelManager;
import com.lycanitesmobs.client.model.item.EquipmentModel;
import com.lycanitesmobs.client.renderer.layer.item.LayerItem;
import com.lycanitesmobs.core.item.equipment.ItemEquipment;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EquipmentRenderer extends BlockEntityWithoutLevelRenderer implements IItemModelRenderer {

    public EquipmentRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet models) {
        super(dispatcher, models);
    }

    public EquipmentRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack itemStack,
                             ItemDisplayContext displayContext,
                             PoseStack poseStack,
                             MultiBufferSource buffer,
                             int packedLight,
                             int packedOverlay) {
        if (!(itemStack.getItem() instanceof ItemEquipment itemEquipment)) {
            return;
        }

        InteractionHand hand = null;

        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.scale(0.65f, 0.65f, 0.65f);
            poseStack.translate(-.1F, 1.55F, 0F);
            poseStack.mulPose(Axis.XP.rotationDegrees(120F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-110F));
        } else {
            poseStack.translate(0.5F, .5F, -.9F);
            poseStack.mulPose(Axis.XP.rotationDegrees(40F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(0F));
        }

        if (displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
            poseStack.scale(0.8f, 0.8f, 0.8f);
            poseStack.translate(0.3F, 0.3F, -.1F);
        }
        EquipmentModel equipmentModel = ModelManager.getInstance().getEquipmentModel();

        float loop = 0F;
        if (Minecraft.getInstance().player != null) {
            loop = Minecraft.getInstance().player.tickCount;
        }

        equipmentModel.render(itemStack, hand, poseStack, buffer, this, loop, packedLight);

        poseStack.popPose();

        double manaNormal = (double) itemEquipment.getMana(itemStack) / ItemEquipment.MANA_MAX;
        if (manaNormal < 1D) {
            RenderSystem.disableDepthTest();
            RenderSystem.disableBlend();

            VertexConsumer vertexBuilder = buffer.getBuffer(RenderType.lines());

            double barX = -0.375D;
            double barWidth = 0.825D;
            double barFill = barWidth * manaNormal;

            for (int i = 0; i <= 2; i++) {
                double y = -0.25D + (i * 0.05D);

                vertexBuilder.vertex(barX, y, 1D)
                        .color(0, 0, 0, 255)
                        .normal(0F, 0F, 1F)
                        .endVertex();

                vertexBuilder.vertex(barX + barWidth, y, 1D)
                        .color(0, 0, 0, 255)
                        .normal(0F, 0F, 1F)
                        .endVertex();
            }

            vertexBuilder.vertex(barX, -0.2D, 1D)
                    .color(64, 0, 128, 255)
                    .normal(0F, 0F, 1F)
                    .endVertex();

            vertexBuilder.vertex(barX + barFill, -0.2D, 1D)
                    .color(0, 128, 255, 255)
                    .normal(0F, 0F, 1F)
                    .endVertex();

            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
        }

    }

    protected void drawBar(VertexConsumer vertexBuilder, int x, int y, int width, int height, int r, int g, int b, int a) {
        double z = 1D;
        vertexBuilder.vertex(x, y, z).color(r, g, b, a).endVertex();
        vertexBuilder.vertex(x, y + height, z).color(r, g, b, a).endVertex();
        vertexBuilder.vertex(x + width, y + height, z).color(r, g, b, a).endVertex();
        vertexBuilder.vertex(x + width, y, z).color(r, g, b, a).endVertex();
    }

    @Override
    public void bindItemTexture(ResourceLocation location) {
        if (location == null) {
            return;
        }
        Minecraft.getInstance().getTextureManager().bindForSetup(location);
    }

    @Override
    public List<LayerItem> addLayer(LayerItem renderLayer) {
        return new ArrayList<>();
    }
}
