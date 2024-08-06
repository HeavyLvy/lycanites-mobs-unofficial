package com.lycanitesmobs.client.renderer.renderapi.client.render.buffer;


import com.lycanitesmobs.client.obj.Vertex;
import com.lycanitesmobs.client.renderer.renderapi.client.render.RenderAPIEvents;
import com.lycanitesmobs.client.renderer.renderapi.client.render.shader.Shader;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.ArrayUtils;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL33.*;

public class WorldBuffer {
    private final List<Vertex> vertices = new ArrayList<>();
    private final int drawMode;
    private final Shader shader;
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


    private void makeProjectionMatrix(Matrix4f projectionMatrix, Matrix4f viewModelMatrix) {
        this.projectionMatrix = projectionMatrix.mul(viewModelMatrix, new Matrix4f()).get(BufferUtils.createFloatBuffer(16));
    }

    public void draw() {
        // Bind necessary buffers and apply projection matrix
        BufferManager.bind();
        BufferManager.bindBuffer();

        // Prepare vertex data and send it to the GPU
        FloatBuffer buffer = getBuffer();
        BufferManager.writeBuffer(buffer);

        glEnable(GL_DEPTH_TEST); // Enable depth testing
        glDepthFunc(GL_LEQUAL);  // Depth function to handle depth comparisons

        glEnable(GL_BLEND);      // Enable blending for handling transparency
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        applyProjectionMatrix();

        // Set up vertex attribute pointers
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 7 * Float.BYTES, 0); // For position

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 4, GL_FLOAT, false, 7 * Float.BYTES, 3 * Float.BYTES); // For color
        glEnableVertexAttribArray(1);

        // Draw all vertices at once
        if (shader != null) {
            shader.bind();
            glDrawArrays(drawMode, 0, vertices.size());
            shader.unbind();
        }

        // Unbind everything and reset states
        BufferManager.unbindBuffer();
        BufferManager.unbind();

        // Disable states after use
        glDisable(GL_BLEND);
        glDisable(GL_DEPTH_TEST);

        // Clear vertices after drawing to prepare for next frame
        vertices.clear();
    }

    private FloatBuffer getBuffer() {
        FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(vertices.size() * 7);
        for (Vertex vertex : vertices) {
            floatBuffer.put(vertex.getX()).put(vertex.getY()).put(vertex.getZ());
            floatBuffer.put(vertex.getR()).put(vertex.getG()).put(vertex.getB()).put(vertex.getA());
        }
        floatBuffer.flip();
        return floatBuffer;
    }

    private void applyProjectionMatrix() {
        if (shader != null) {
            shader.uniformMatrix4f("u_projection", projectionMatrix);
        }
    }
}
