package com.lycanitesmobs.client.renderer;

import java.awt.*;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.lycanitesmobs.client.renderer.renderapi.client.render.RenderHelper;
import com.lycanitesmobs.client.renderer.renderapi.client.render.buffer.WorldBuffer;
import com.lycanitesmobs.client.renderer.renderapi.client.render.shader.Shader;
import com.lycanitesmobs.client.renderer.renderapi.client.render.shader.ShaderManager;
import com.lycanitesmobs.core.util.LMHelperClass;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL21;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;

public class VBOBatcher {

    private static final VBOBatcher INSTANCE = new VBOBatcher();
    private final Map<RenderType, List<VBODrawCommand>> batchedDrawCommands = new LinkedHashMap<>();

    public static VBOBatcher getInstance() {
        return INSTANCE;
    }

    public void queue(RenderType renderType, VBODrawCommand drawCommand) {
        batchedDrawCommands.computeIfAbsent(renderType, k -> new LinkedList<>()).add(drawCommand);
    }

    public void endBatches(PoseStack poseStack) {
        batchedDrawCommands.forEach((renderType, drawCommands) -> {
            if (drawCommands.isEmpty()) {
                return;
            }

            renderType.setupRenderState();

            for (VBODrawCommand drawCommand : drawCommands) {
                drawCommand.draw(poseStack);
            }

            renderType.clearRenderState();
            drawCommands.clear();
        });

        batchedDrawCommands.clear();
    }

    public static class VBODrawCommand {

        private final int vbo;
        private final int vertexCount;
        private final VertexFormat format;
        private final Matrix4f matrix;
        private final ResourceLocation texLocation;
        private float red = 1.0F;
        private float green = 1.0F;
        private float blue = 1.0F;
        private float alpha = 1.0F;
        private float textureOffsetX;
        private float textureOffsetY;
        private float overlayOffsetX;
        private float overlayOffsetY;
        private float lightOffsetX;
        private float lightOffsetY;
        private Shader shader;

        public VBODrawCommand(int vbo, int vertexCount, VertexFormat format, Matrix4f matrix, ResourceLocation texLocation) {
            this.vbo = vbo;
            this.vertexCount = vertexCount;
            this.format = format;
            this.matrix = matrix;
            this.texLocation = texLocation;
            ShaderManager.loadShaders(); // Ensure shaders are loaded
            this.shader = ShaderManager.getPositionColorShader();
        }

        private static float correctedColor(float f) {
            return ((int) (f * 255.0F) & 0xFF) / 255.0F;
        }

        public VBODrawCommand setColor(Vector4f color) {
            return setColor(color.x(), color.y(), color.z(), color.w());
        }

        public VBODrawCommand setColor(float red, float green, float blue, float alpha) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
            return this;
        }

        public VBODrawCommand setTextureOffset(Vector2f textureOffset) {
            return setTextureOffset(textureOffset.x * 0.01F, -textureOffset.y * 0.01F);
        }

        public VBODrawCommand setTextureOffset(float textureOffsetX, float textureOffsetY) {
            this.textureOffsetX = textureOffsetX;
            this.textureOffsetY = textureOffsetY;
            return this;
        }

        public VBODrawCommand setOverlayOffset(float white, boolean red) {
            return setOverlayOffset(OverlayTexture.u(white), OverlayTexture.v(red));
        }

        public VBODrawCommand setOverlayOffset(float overlayOffsetX, float overlayOffsetY) {
            this.overlayOffsetX = overlayOffsetX;
            this.overlayOffsetY = overlayOffsetY;
            return this;
        }

        public VBODrawCommand setLightOffset(int packed) {
            return setLightOffset(LightTexture.block(packed) << 4, LightTexture.sky(packed) << 4);
        }

        public VBODrawCommand setLightOffset(float lightOffsetX, float lightOffsetY) {
            this.lightOffsetX = lightOffsetX;
            this.lightOffsetY = lightOffsetY;
            return this;
        }

        public void draw(PoseStack poseStack) {

        }
    }

}
