package com.lycanitesmobs.client.renderer;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.util.LMHelperClass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.*;

import static com.lycanitesmobs.LycanitesMobs.logError;
import static com.lycanitesmobs.LycanitesMobs.logInfo;

public class VBOBatcher {

    private static final VBOBatcher INSTANCE = new VBOBatcher();
    private final Map<RenderType, List<VBODrawCommand>> batchedDrawCommands = new LinkedHashMap<>();

    public static VBOBatcher getInstance() {
        return INSTANCE;
    }

    public void queue(RenderType renderType, VBODrawCommand drawCommand) {
        batchedDrawCommands.computeIfAbsent(renderType, k -> new LinkedList<>()).add(drawCommand);
    }

    public void endBatches() {
        batchedDrawCommands.forEach((renderType, drawCommands) -> {
            if (drawCommands.isEmpty()) {
                return;
            }

            renderType.setupRenderState();
            for (VBODrawCommand drawCommand : drawCommands) {
                drawCommand.draw();
                RenderSystem.setTextureMatrix(drawCommand.matrix);
            }

            renderType.clearRenderState();
            drawCommands.clear();
        });

        batchedDrawCommands.clear();
    }

    public static class VBODrawCommand {

        public final Matrix4f matrix;
        private final int vbo;
        private final int vertexCount;
        private final VertexFormat format;
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

        public VBODrawCommand(int vbo, int vertexCount, VertexFormat format, Matrix4f matrix, ResourceLocation texLocation) {
            this.vbo = vbo;
            this.vertexCount = vertexCount;
            this.format = format;
            this.matrix = matrix;
            this.texLocation = texLocation;
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
            return setTextureOffset(textureOffset.x() * 0.01F, -textureOffset.y() * 0.01F);
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

        public void draw() {
            try {
                if (true) return;
                RenderSystem.assertOnRenderThread();

                Minecraft.getInstance().getTextureManager().bindForSetup(texLocation);

                RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
                RenderSystem.setShaderColor(correctedColor(red), correctedColor(green), correctedColor(blue), correctedColor(alpha));

                if (textureOffsetX != 0.0F || textureOffsetY != 0.0F) {
                    Matrix4f textureMatrix = new Matrix4f().translation(textureOffsetX, textureOffsetY, 0.0F);
                    RenderSystem.setTextureMatrix(textureMatrix);
                }

                RenderSystem.setShaderTexture(1, OverlayTexture.pack((int) overlayOffsetX, (int) overlayOffsetY));
                RenderSystem.setShaderTexture(2, LightTexture.pack((int) lightOffsetX, (int) lightOffsetY));

                final int vao = GL30.glGenVertexArrays();
                GL30.glBindVertexArray(vao);

                RenderSystem.glBindBuffer(GL21.GL_ARRAY_BUFFER, () -> vbo);

                format.setupBufferState();

                GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexCount);
                int error = GL11.glGetError();
                if (error != GL11.GL_NO_ERROR) {
                    logError("Error after glDrawArrays: " + error);
                }

                format.clearBufferState();
                RenderSystem.glBindBuffer(GL21.GL_ARRAY_BUFFER, () -> 0);

                GL30.glBindVertexArray(0);
                GL30.glDeleteVertexArrays(vao);

                if (textureOffsetX != 0.0F || textureOffsetY != 0.0F) {
                    RenderSystem.resetTextureMatrix();
                }

            } catch (Exception e) {
                logError("Exception in draw method: " + e.getMessage());
            }

            flushBufferBuilder();
        }

        private void flushBufferBuilder() {
            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();

            if (bufferBuilder.building()) {
                try {
                    BufferBuilder.RenderedBuffer renderedBuffer = bufferBuilder.end();

                    VertexBuffer vertexBuffer = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
                    vertexBuffer.upload(renderedBuffer);

                    vertexBuffer.draw();
                    vertexBuffer.close();
                } catch (Exception e) {
                    logError("Exception in flushBufferBuilder: " + e.getMessage());
                }
            }
        }


    }
}
