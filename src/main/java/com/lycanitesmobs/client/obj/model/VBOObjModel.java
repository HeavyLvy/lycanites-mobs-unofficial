package com.lycanitesmobs.client.obj.model;

import com.lycanitesmobs.client.obj.geometry.ObjPart;
import com.lycanitesmobs.client.renderer.util.CustomRenderStates;
import com.lycanitesmobs.client.renderer.util.VBOBatcher;
import com.lycanitesmobs.client.renderer.util.VBOBatcher.VBODrawCommand;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class VBOObjModel extends ObjModel {

    public static RenderType renderType;
    public static ResourceLocation tex;
    public static boolean renderNormal;
    public static boolean renderOutline;

    public VBOObjModel(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    @Override
    public void renderPart(VertexConsumer vertexBuilder, Matrix3f matrix3f, Matrix4f matrix4f,
                           int brightness, int fade, ObjPart objPart, Vector4f color, Vector2f textureOffset) {
        if (vertexBuilder != null) {
            super.renderPart(vertexBuilder, matrix3f, matrix4f, brightness, fade, objPart, color, textureOffset);
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
                    .setLightOffset(brightness);

            if (renderType == CustomRenderStates.OBJ_CUTOUT) {
                cmd.setAlphaCutoff(0.1F);
            }

            VBOBatcher.getInstance().queue(renderType, cmd);
        }
        if (renderOutline) {
            VBOBatcher.getInstance().queue(CustomRenderStates.OBJ_OUTLINE_RENDER_TYPE,
                    new VBODrawCommand(objPart.mesh.getVbo(), objPart.mesh.indices.length,
                            renderType.format(), matrix4f, tex));
        }
    }


}
