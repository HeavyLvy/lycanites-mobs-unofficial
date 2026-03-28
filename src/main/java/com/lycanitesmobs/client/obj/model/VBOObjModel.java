package com.lycanitesmobs.client.obj.model;

import com.lycanitesmobs.client.model.creature.base.ModelObjState;
import com.lycanitesmobs.client.obj.geometry.ObjPart;
import com.lycanitesmobs.client.renderer.util.CustomRenderStates;
import com.lycanitesmobs.client.renderer.util.RecolorTextureCache;
import com.lycanitesmobs.client.renderer.util.VBOBatcher;
import com.lycanitesmobs.client.renderer.util.VBOBatcher.VBODrawCommand;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class VBOObjModel extends ObjModel {

    public static RenderType renderType;
    public static ResourceLocation tex;
    public static boolean renderNormal;
    public static boolean renderOutline;

    /** When true, use the entity-format VBO (for Iris/Oculus compatibility). */
    public static boolean useEntityFormat;
    /** The Iris-compatible entity render type (set by CreatureRenderer). */
    public static RenderType irisRenderType;

    public float baseR = 1.0F, baseG = 1.0F, baseB = 1.0F, baseRange = 0.1F;
    public float variantR = 1.0F, variantG = 1.0F, variantB = 1.0F, variantAmount = 0.0F;


    public VBOObjModel(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    public VBOObjModel(ResourceLocation resourceLocation, ResourceManager resourceManager) {
        super(resourceLocation, resourceManager);
    }

    public void applyVariantFromState(ModelObjState state) {
        if (state == null) return;
        this.baseR = state.baseR;
        this.baseG = state.baseG;
        this.baseB = state.baseB;
        this.baseRange = state.baseRange;
        this.variantR = state.variantR;
        this.variantG = state.variantG;
        this.variantB = state.variantB;
        this.variantAmount = state.variantAmount;
    }

    @Override
    public void renderPart(VertexConsumer vertexBuilder, Matrix3f matrix3f, Matrix4f matrix4f,
                           int brightness, int fade, ObjPart objPart, Vector4f color, Vector2f textureOffset) {
        if (vertexBuilder != null) {
            super.renderPart(vertexBuilder, matrix3f, matrix4f, brightness, fade, objPart, color, textureOffset);
            return;
        }

        if (useEntityFormat && irisRenderType != null) {
            ResourceLocation recoloredTex = tex;
            if (variantAmount > 0.01F) {
                recoloredTex = RecolorTextureCache.getRecoloredTexture(
                        tex, baseR, baseG, baseB, baseRange,
                        variantR, variantG, variantB, variantAmount);
            }

            VBODrawCommand cmd = new VBODrawCommand(
                    objPart.mesh.getEntityVbo(),
                    objPart.mesh.indices.length,
                    DefaultVertexFormat.NEW_ENTITY,
                    matrix4f,
                    recoloredTex
            )
                    .setColor(color)
                    .setTextureOffset(textureOffset)
                    .setOverlayOffset(0.0F, 10.0F - fade)
                    .setLightOffset(brightness);

            VBOBatcher.getInstance().queue(irisRenderType, cmd);
            return;
        }

        if (renderNormal) {
            VBODrawCommand cmd = new VBODrawCommand(
                    objPart.mesh.getVbo(),
                    objPart.mesh.indices.length,
                    renderType.format(),
                    matrix4f,
                    tex
            )
                    .setColor(color)
                    .setTextureOffset(textureOffset)
                    .setOverlayOffset(0.0F, 10.0F - fade)
                    .setLightOffset(brightness)
                    .setRecolorBand(
                            baseR,
                            baseG,
                            baseB,
                            baseRange
                    )
                    .setVariantTint(
                            variantR,
                            variantG,
                            variantB,
                            variantAmount
                    );

            if (renderType == CustomRenderStates.OBJ_CUTOUT) {
                cmd.setAlphaCutoff(0.1F);
            }

            VBOBatcher.getInstance().queue(renderType, cmd);
        }

        if (renderOutline) {
            VBOBatcher.getInstance().queue(
                    CustomRenderStates.OBJ_OUTLINE_RENDER_TYPE,
                    new VBODrawCommand(
                            objPart.mesh.getVbo(),
                            objPart.mesh.indices.length,
                            renderType != null ? renderType.format() : CustomRenderStates.POS_TEX_NORMAL,
                            matrix4f,
                            tex
                    )
            );
        }
    }

    public static float[] hexToRGB(int hex) {
        float r = ((hex >> 16) & 0xFF) / 255.0F;
        float g = ((hex >> 8) & 0xFF) / 255.0F;
        float b = (hex & 0xFF) / 255.0F;
        return new float[]{r, g, b};
    }
}
