package com.lycanitesmobs.client.renderer;

import com.lycanitesmobs.client.ModelManager;
import com.lycanitesmobs.client.model.AnimationPart;
import com.lycanitesmobs.client.model.ItemObjModel;
import com.lycanitesmobs.client.obj.VBOObjModel;
import com.lycanitesmobs.client.renderer.layer.LayerItem;
import com.lycanitesmobs.client.renderer.layer.LayerItemDye;
import com.lycanitesmobs.core.item.equipment.ItemEquipmentPart;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

public class EquipmentPartRenderer extends BlockEntityWithoutLevelRenderer implements IItemModelRenderer {
    protected List<LayerItem> renderLayers = new ArrayList<>();

    public EquipmentPartRenderer(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
        super(p_172550_, p_172551_);
    }

    public EquipmentPartRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack itemStack, ItemDisplayContext ctx, PoseStack pose,
                             MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (!(itemStack.getItem() instanceof ItemEquipmentPart part)) return;

        ItemObjModel model = ModelManager.getInstance().getEquipmentPartModel(part);
        if (model == null) return;

        this.renderLayers.clear();
        model.addCustomLayers(this);

        float loop = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.tickCount : 0f;

        pose.pushPose();

        pose.translate(1.7F, -0.5F, 0.5F);
        pose.mulPose(Axis.XP.rotationDegrees(190f));
        pose.mulPose(Axis.YP.rotationDegrees(-45f));
        pose.mulPose(Axis.ZP.rotationDegrees(10f));
        pose.translate(-0.5f, 0f, 0f);
        pose.pushPose();
        if ("head".equalsIgnoreCase(part.slotType)) {
            pose.translate(0f, 0f, 0.5f);
        } else if ("blade".equalsIgnoreCase(part.slotType)
                || "pike".equalsIgnoreCase(part.slotType)
                || "axe".equalsIgnoreCase(part.slotType)
                || "jewel".equalsIgnoreCase(part.slotType)) {
            pose.translate(0f, 0.5f, 1f);
        }

        if (ctx == ItemDisplayContext.GUI) {
            // Fix upside-down items in inventory: rotate 180° around Z
            pose.mulPose(Axis.ZP.rotationDegrees(180.0F));

            // Now compute the AABB and center/offset as you already do
            org.joml.Matrix4f matSoFar = new org.joml.Matrix4f(pose.last().pose());
            AA2D aabb = aabb2DInPose(model, matSoFar);
            if (aabb.valid()) {
                float cx = 0.5f * (aabb.minX + aabb.maxX);
                float cy = 0.5f * (aabb.minY + aabb.maxY);
                float maxNudge = 0.25f;
                float dx = Mth.clamp(-cx, -maxNudge, maxNudge);
                float dy = Mth.clamp(-cy, -maxNudge, maxNudge);
                pose.translate(dx, dy, 0f);
            }
        }

        model.generateAnimationFrames(itemStack, null, loop, null);
        this.renderModel(model, itemStack, null, pose, buffers, null, null, loop, packedLight);
        for (LayerItem layer : this.renderLayers) {
            this.renderModel(model, itemStack, null, pose, buffers, layer, null, loop, packedLight);
        }
        this.renderModel(model, itemStack, null, pose, buffers, new LayerItemDye(this, "dye"), null, loop, packedLight);
        model.clearAnimationFrames();

        pose.popPose();
        pose.popPose();

        VBOBatcher.getInstance().endBatches();
    }


    private static final class AA2D {
        float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;

        boolean valid() {
            return minX <= maxX && minY <= maxY;
        }
    }

    private static AA2D aabb2DInPose(ItemObjModel model, org.joml.Matrix4f poseMat) {
        AA2D a = new AA2D();
        for (var part : model.objParts) {
            var mesh = part.mesh;
            if (mesh == null || mesh.vertices == null || mesh.indices == null) continue;
            for (int idx : mesh.indices) {
                var p = mesh.vertices[idx].getPos();
                float x = p.x(), y = p.y(), z = p.z();
                float tx = poseMat.m00() * x + poseMat.m10() * y + poseMat.m20() * z + poseMat.m30();
                float ty = poseMat.m01() * x + poseMat.m11() * y + poseMat.m21() * z + poseMat.m31();
                if (tx < a.minX) a.minX = tx; if (tx > a.maxX) a.maxX = tx;
                if (ty < a.minY) a.minY = ty; if (ty > a.maxY) a.maxY = ty;
            }
        }
        return a;
    }


    /**
     * Renders the main model.
     *
     * @param itemStack        The item stack to render.
     * @param hand             The hand that is holding the item or null if in the inventory instead.
     * @param matrixStack      The matrix stack for animation.
     * @param renderTypeBuffer The render type buffer for rendering with.
     * @param layer            The layer to render, the base layer is null.
     * @param offsetObjPart    A ModelObjPart, if not null this model is offset by it, used by assembled equipment pieces to create a full model.
     * @param loop             A constant tick for looping animations.
     * @param brightness       The brightness of the mob based on block location, etc.
     */
    protected void renderModel(ItemObjModel model, ItemStack itemStack, InteractionHand hand, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, LayerItem layer, AnimationPart offsetObjPart, float loop, int brightness) {
        ResourceLocation texture = model.getTexture(itemStack, layer);
        RenderType renderType = CustomRenderStates.getObjVBORenderType(model.getBlending(itemStack, layer), model.getGlow(itemStack, layer));
//		if(layer instanceof LayerItemDye) {
//			renderType = CustomRenderStates.getObjColorOnlyRenderType(texture, model.getBlending(itemStack, layer), model.getGlow(itemStack, layer));
//		}

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
