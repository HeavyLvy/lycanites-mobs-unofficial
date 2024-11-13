package com.lycanitesmobs.client.renderer.renderapi.client.render.buffer;


import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.obj.Vector3o;
import com.lycanitesmobs.client.obj.Vertex;
import com.lycanitesmobs.client.renderer.renderapi.client.render.shader.Shader;
import com.lycanitesmobs.core.util.LMHelperClass;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.lycanitesmobs.client.renderer.renderapi.client.render.RenderHelper.loadTexture;
import static org.lwjgl.opengl.GL33.*;

public class WorldBuffer {
    public final List<Vertex> vertices = new ArrayList<>();
    public final int drawMode;
    public final Shader shader;
    private final Vec3 cameraPos;
    private FloatBuffer projectionMatrix;

    public WorldBuffer(int drawMode, Shader shader) {
        this.drawMode = drawMode;
        this.shader = shader;
        this.cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        makeProjectionMatrix(RenderSystem.getProjectionMatrix(), RenderSystem.getModelViewStack().last().pose());
    }

    public WorldBuffer(int drawMode, Shader shader, Matrix4f projectionMatrix, Matrix4f viewMatrix) {
        this.drawMode = drawMode;
        this.shader = shader;
        this.cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        makeProjectionMatrix(projectionMatrix, viewMatrix);
    }

    public void vert(float x, float y, float z, float r, float g, float b, float a) {
        // Subtract camera position for view-relative coordinates
        vertices.add(new Vertex(x - (float) cameraPos.x, y - (float) cameraPos.y, z - (float) cameraPos.z, r, g, b, a));
    }

    public void vert(float x, float y, float z, float r, float g, float b, float a, float u, float v) {
        Vector3o position = new Vector3o(x - (float) cameraPos.x, y - (float) cameraPos.y, z - (float) cameraPos.z);
        Vector2f texCoords = new Vector2f(u, v);
        vertices.add(new Vertex(position, texCoords, null, null, r, g, b, a));
    }


    private void makeProjectionMatrix(Matrix4f projectionMatrix, Matrix4f viewModelMatrix) {
        this.projectionMatrix = projectionMatrix.mul(viewModelMatrix, new Matrix4f()).get(BufferUtils.createFloatBuffer(16));
    }

    public void draw() {
        BufferManager.bind();
        BufferManager.bindBuffer();

        FloatBuffer buffer = getBuffer();
        BufferManager.writeBuffer(buffer);

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        applyProjectionMatrix();
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 7 * Float.BYTES, 0); // For position

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 4, GL_FLOAT, false, 7 * Float.BYTES, 3 * Float.BYTES); // For color
        glEnableVertexAttribArray(1);

/*
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 7 * Float.BYTES, 0);         // Position
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 4, GL_FLOAT, false, 7 * Float.BYTES, 3 * Float.BYTES); // Color
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 7 * Float.BYTES, 7 * Float.BYTES); // Texture Coord
        glEnableVertexAttribArray(2);*/
        if (shader != null) {
            shader.bind();
            glDrawArrays(drawMode, 0, vertices.size());
            shader.unbind();
        }

        /*if (shader != null) {
            var location = new ResourceLocation(LycanitesMobs.MODID, "textures/entity/bobeko.png");
            int textureID = loadTexture(location);
            glBindTexture(GL_TEXTURE_2D, textureID);
            shader.bind();
            shader.uniform1i("u_texture", 0);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, textureID);

            glDrawArrays(drawMode, 0, vertices.size());
            shader.unbind();
        }
*/
        BufferManager.unbindBuffer();
        BufferManager.unbind();

        glDisable(GL_BLEND);
        glDisable(GL_DEPTH_TEST);

        vertices.clear();
    }


    /*public FloatBuffer getBuffer() {
        FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(vertices.size() * 9);
        for (Vertex vertex : vertices) {
            floatBuffer.put(vertex.getX()).put(vertex.getY()).put(vertex.getZ());
            floatBuffer.put(vertex.getR()).put(vertex.getG()).put(vertex.getB()).put(vertex.getA());
            //floatBuffer.put(vertex.getU()).put(vertex.getV());
        }
        floatBuffer.flip();
        return floatBuffer;
    }*/

    public FloatBuffer getBuffer() {
        FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(vertices.size() * 7);
        for (Vertex vertex : vertices) {
            floatBuffer.put(vertex.getX()).put(vertex.getY()).put(vertex.getZ());
            floatBuffer.put(vertex.getR()).put(vertex.getG()).put(vertex.getB()).put(vertex.getA());
        }
        floatBuffer.flip();
        return floatBuffer;
    }

    public void applyProjectionMatrix() {
        if (shader != null) {
            shader.uniformMatrix4f("u_projection", projectionMatrix);
        }
    }
}
