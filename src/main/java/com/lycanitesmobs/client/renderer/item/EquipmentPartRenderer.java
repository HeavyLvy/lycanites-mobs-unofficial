package com.lycanitesmobs.client.renderer.item;

import com.lycanitesmobs.client.manager.ModelManager;
import com.lycanitesmobs.client.model.animation.AnimationPart;
import com.lycanitesmobs.client.model.item.ItemObjModel;
import com.lycanitesmobs.client.obj.model.VBOObjModel;
import com.lycanitesmobs.client.renderer.layer.item.LayerItem;
import com.lycanitesmobs.client.renderer.util.CustomRenderStates;
import com.lycanitesmobs.client.renderer.util.VBOBatcher;
import com.lycanitesmobs.core.item.equipment.ItemEquipmentPart;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
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
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EquipmentPartRenderer extends BlockEntityWithoutLevelRenderer implements IItemModelRenderer {

    protected final List<LayerItem> renderLayers = new ArrayList<>();

    public EquipmentPartRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet models) {
        super(dispatcher, models);
    }

    public EquipmentPartRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack itemStack,
                             ItemDisplayContext displayContext,
                             PoseStack matrixStack,
                             MultiBufferSource renderTypeBuffer,
                             int brightness,
                             int packedOverlay) {
        if (!(itemStack.getItem() instanceof ItemEquipmentPart itemEquipmentPart)) {
            return;
        }

        InteractionHand hand = null;

        ItemObjModel itemObjModel = ModelManager.getInstance().getEquipmentPartModel(itemEquipmentPart);
        if (itemObjModel == null) {
            return;
        }

        this.renderLayers.clear();
        itemObjModel.addCustomLayers(this);

        float loop = 0;
        if (Minecraft.getInstance().player != null) {
            loop = Minecraft.getInstance().player.tickCount;
        }

        matrixStack.translate(0F, 0F, 0F);

        matrixStack.mulPose(Axis.YN.rotationDegrees(45F));
        matrixStack.mulPose(Axis.YP.rotationDegrees(-90F));
        matrixStack.mulPose(Axis.XN.rotationDegrees(-45F));
        matrixStack.mulPose(Axis.XP.rotationDegrees(0F));
        matrixStack.mulPose(Axis.ZN.rotationDegrees(0F));
        matrixStack.mulPose(Axis.ZP.rotationDegrees(-180F));


        matrixStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            var itemName = Arrays.stream(itemEquipmentPart.itemName.split("_")).toList().get(1);

            if (itemName.equals("eechetikarm")) {
                matrixStack.translate(1F, 0F, 0.3F);
            } else if (itemName.equals("argustail")) {
                matrixStack.translate(1F, 0.1F, 0.3F);
                matrixStack.mulPose(Axis.ZP.rotationDegrees(30F));
                matrixStack.mulPose(Axis.ZN.rotationDegrees(40F));
            } else if (itemName.equals("belphegorarm")) {
                matrixStack.translate(1F, 0F, 0.3F);
            } else if (itemName.equals("cinderblade")) {
                matrixStack.translate(0.5F, 0F, 0.3F);
            } else if (itemName.equals("entarm")) {
                matrixStack.translate(1F, 0F, 0.3F);
            } else if (itemName.equals("epionwing")) {
                matrixStack.scale(0.75f, 0.75f, 0.75f);
                matrixStack.translate(0.3F, -0.4F, 0.1F);
                matrixStack.mulPose(Axis.XN.rotationDegrees(-25F));
            } else if (itemName.equals("woodenpaxel")) {
                matrixStack.scale(0.75f, 0.75f, 0.75f);

                matrixStack.translate(2F, -0.5F, 0.1F);
                matrixStack.mulPose(Axis.XN.rotationDegrees(90F));
            } else if (itemName.equals("wildkinarm")) {
                matrixStack.scale(0.75f, 0.75f, 0.75f);

                matrixStack.translate(2F, -0.5F, 0.1F);
                matrixStack.mulPose(Axis.XN.rotationDegrees(90F));
            } else if (itemName.equals("wendigoantler")) {
                matrixStack.translate(1.5F, -0.7F, 0.8F);
            } else if (itemName.equals("sutiramustinger")) {
                matrixStack.scale(0.75f, 0.75f, 0.75f);

                matrixStack.translate(3F, -2F, 0F);
                matrixStack.mulPose(Axis.XN.rotationDegrees(90F));
            } else if (itemName.equals("stryderheart")) {
                matrixStack.scale(0.75f, 0.75f, 0.75f);

                matrixStack.translate(1.5F, .2F, 0F);
                matrixStack.mulPose(Axis.XN.rotationDegrees(90F));
            } else if (itemName.equals("spectretendril")) {
                matrixStack.scale(0.75f, 0.75f, 0.75f);

                matrixStack.translate(1.1F, .2F, 0F);
            } else if (itemName.equals("remobrawing")) {
                matrixStack.translate(1F, .1F, .3F);
                matrixStack.mulPose(Axis.XN.rotationDegrees(45F));
            } else if (itemName.equals("reiverhorns")) {
                matrixStack.translate(.4F, 0.5F, -1F);
                matrixStack.mulPose(Axis.XN.rotationDegrees(90F));
            } else if (itemName.equals("reaperclaw")) {
                matrixStack.translate(.4F, 0.7F, -1F);
                matrixStack.mulPose(Axis.XN.rotationDegrees(90F));
            } else if (itemName.equals("raidrablade")) {
                matrixStack.scale(0.8f, 0.8f, 0.8f);

                matrixStack.translate(.8F, .5F, -.5F);
                matrixStack.mulPose(Axis.XN.rotationDegrees(90F));
                matrixStack.mulPose(Axis.ZP.rotationDegrees(90F));
            } else if (itemName.equals("lacedonhead")) {
                matrixStack.translate(0.2F, -.7F, .2F);
                matrixStack.mulPose(Axis.ZP.rotationDegrees(90F));
            } else if (itemName.equals("ironpaxel")) {
                matrixStack.translate(0.2F, .8F, -1F);
                matrixStack.mulPose(Axis.XN.rotationDegrees(90F));
            } else if (itemName.equals("ioraystinger")) {
                matrixStack.translate(-.2F, 1.5F, -.9F);
            } else if (itemName.equals("frostweaverleg")) {
                matrixStack.translate(-.3F, 0.2F, 0.1F);
            } else if (itemName.equals("ettinclub")) {
                matrixStack.scale(0.9f, 0.9f, 0.9f);

                matrixStack.translate(-.2F, 0F, 0F);
            } else if (itemName.equals("erepededrill")) {
                matrixStack.scale(0.9f, 0.9f, 0.9f);

                matrixStack.translate(0F, 0.1F, .2F);
            } else if (itemName.equals("cherufecore")) {
                matrixStack.scale(0.9f, 0.9f, 0.9f);

                matrixStack.translate(0.1F, 0.1F, 0.05F);
            } else if (itemName.equals("bruchaquill")) {
                matrixStack.scale(0.9f, 0.9f, 0.9f);

                matrixStack.translate(-.2F, 0F, 0F);
            } else if (itemName.equals("behemophethand")) {
                matrixStack.scale(0.9f, 0.9f, 0.9f);

                matrixStack.translate(0F, 0.4F, -0.1F);
            } else if (itemName.equals("apollyonclaw")) {
                matrixStack.translate(0.1F, 0.3F, -0.8F);
                matrixStack.mulPose(Axis.XP.rotationDegrees(45F));
                matrixStack.mulPose(Axis.YP.rotationDegrees(45F));
            } else if (itemName.equals("sylphwing")) {
                matrixStack.scale(0.9f, 0.9f, 0.9f);

                matrixStack.translate(0.5F, 0F, -.1F);
            } else if (itemName.equals("astarothclaw")) {

                matrixStack.translate(0F, 0.45F, 0F);
            } else {
                matrixStack.translate(.7F, .2F, 0.3F);
            }
            matrixStack.translate(0F, -1.5F, -1.7F);
        }

        itemObjModel.generateAnimationFrames(itemStack, null, loop, null);
        this.renderModel(itemObjModel, itemStack, hand, matrixStack, renderTypeBuffer, null, null, loop, brightness);
        for (LayerItem renderLayer : this.renderLayers) {
            this.renderModel(itemObjModel, itemStack, hand, matrixStack, renderTypeBuffer, renderLayer, null, loop, brightness);
        }
        itemObjModel.clearAnimationFrames();

        matrixStack.popPose();

        VBOBatcher.getInstance().endBatches();
    }

    protected void renderModel(ItemObjModel model,
                               ItemStack itemStack,
                               InteractionHand hand,
                               PoseStack matrixStack,
                               MultiBufferSource renderTypeBuffer,
                               LayerItem layer,
                               AnimationPart offsetObjPart,
                               float loop,
                               int brightness) {
        ResourceLocation texture = model.getTexture(itemStack, layer);
        RenderType renderType = CustomRenderStates.getObjVBORenderType(
                model.getBlending(itemStack, layer),
                model.getGlow(itemStack, layer)
        );

        VBOObjModel.renderType = renderType;
        VBOObjModel.renderNormal = true;
        VBOObjModel.tex = texture;
        model.render(itemStack, hand, matrixStack, null, this, offsetObjPart, layer, loop, brightness);
        VBOObjModel.tex = null;
        VBOObjModel.renderNormal = false;
        VBOObjModel.renderType = null;
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
        if (!this.renderLayers.contains(renderLayer)) {
            this.renderLayers.add(renderLayer);
        }
        return this.renderLayers;
    }
}
