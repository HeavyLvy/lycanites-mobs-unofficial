package com.lycanitesmobs.client.renderer;

import java.nio.FloatBuffer;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.obj.Vertex;
import com.lycanitesmobs.client.renderer.renderapi.client.render.buffer.BufferManager;
import com.lycanitesmobs.client.renderer.renderapi.client.render.buffer.WorldBuffer;
import com.lycanitesmobs.client.renderer.renderapi.client.render.shader.Shader;
import com.lycanitesmobs.core.util.LMHelperClass;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import static com.lycanitesmobs.LycanitesMobs.logError;
import static com.lycanitesmobs.LycanitesMobs.logInfo;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL20.*;

public class VBOBatcher {

    private static final VBOBatcher INSTANCE = new VBOBatcher();
    private final Map<RenderType, List<VBODrawCommand>> batchedDrawCommands = new LinkedHashMap<>();

    public static VBOBatcher getInstance() {
        return INSTANCE;
    }

    public void queue(RenderType renderType, VBODrawCommand drawCommand) {
        batchedDrawCommands.computeIfAbsent(renderType, k -> new LinkedList<>()).add(drawCommand);
    }

    public void endBatches(WorldBuffer worldBuffer) {
        batchedDrawCommands.forEach((renderType, drawCommands) -> {
            if (drawCommands.isEmpty()) {
                return;
            }

            renderType.setupRenderState();
            for (VBODrawCommand drawCommand : drawCommands) {
                drawCommand.draw(worldBuffer);
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

        public void draw(WorldBuffer worldBuffer) {
            if (true) return;
            try {
                Shader shader = worldBuffer.shader;
                List<Vertex> vertices = worldBuffer.vertices;
                int drawMode = worldBuffer.drawMode;

                // Bind buffers
                BufferManager.bind();
                BufferManager.bindBuffer();

                // Prepare and send vertex data
                FloatBuffer buffer = getBuffer(vertices);
                BufferManager.writeBuffer(buffer);

                glEnable(GL_DEPTH_TEST);
                glDepthFunc(GL_LEQUAL);

                glEnable(GL_BLEND);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

                shader.bind();
                worldBuffer.applyProjectionMatrix();

                // Set up vertex attribute pointers
                int stride = 9 * Float.BYTES; // x, y, z, r, g, b, a, u, v

                glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0); // Position
                glEnableVertexAttribArray(0);

                glVertexAttribPointer(1, 4, GL_FLOAT, false, stride, 3 * Float.BYTES); // Color
                glEnableVertexAttribArray(1);

                glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, 7 * Float.BYTES); // Texture coords
                glEnableVertexAttribArray(2);
                glVertexAttribPointer(0, 3, GL_FLOAT, false, 7 * Float.BYTES, 0); // For position

                glEnableVertexAttribArray(0);
                glVertexAttribPointer(1, 4, GL_FLOAT, false, 7 * Float.BYTES, 3 * Float.BYTES); // For color
                glEnableVertexAttribArray(1);
                // Bind texture
                int textureId = loadTexture(texLocation);
                glActiveTexture(GL_TEXTURE0);
                GlStateManager._bindTexture(textureId);
                GlStateManager._glUniform1i(GL_TEXTURE_2D, 0);

                // Draw
                glDrawArrays(drawMode, 0, vertices.size());

                // Cleanup
                GL11.glBindTexture(GL_TEXTURE_2D, 0);
                glDisableVertexAttribArray(0);
                glDisableVertexAttribArray(1);
                glDisableVertexAttribArray(2);

                shader.unbind();
                BufferManager.unbindBuffer();
                BufferManager.unbind();

                glDisable(GL_BLEND);
                glDisable(GL_DEPTH_TEST);

                vertices.clear();
            } catch (Exception e) {
                logError("Exception in draw method: " + e.getMessage());
            }
        }

        public static int loadTexture(ResourceLocation textureLocation) {
            TextureManager textureManager = Minecraft.getInstance().getTextureManager();
            AbstractTexture texture = textureManager.getTexture(textureLocation);
            if (texture == null) {
                textureManager.register(textureLocation, new SimpleTexture(textureLocation));
                texture = textureManager.getTexture(textureLocation);
            }
            return texture.getId();
        }

        private FloatBuffer getBuffer(List<Vertex> vertices) {
            FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(vertices.size() * 9);
            for (Vertex vertex : vertices) {
                floatBuffer.put(vertex.getX()).put(vertex.getY()).put(vertex.getZ());
                floatBuffer.put(vertex.getR()).put(vertex.getG()).put(vertex.getB()).put(vertex.getA());
                //floatBuffer.put(vertex.getU()).put(vertex.getV());
            }
            floatBuffer.flip();
            return floatBuffer;
        }

    }
}
