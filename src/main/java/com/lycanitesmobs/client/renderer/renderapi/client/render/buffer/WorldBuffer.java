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

    public void vert(float x, float y, float z, float r, float g, float b, float a) {
        vertices.add(new Vertex(x - (float) cameraPos.x, y - (float) cameraPos.y, z - (float) cameraPos.z, r, g, b, a));
    }

    private void makeProjectionMatrix(Matrix4f projectionMatrix, Matrix4f viewModelMatrix) {
        this.projectionMatrix = new Matrix4f(projectionMatrix).mul(viewModelMatrix).get(BufferUtils.createFloatBuffer(16));
    }

    public void draw() {
        BufferManager.bind();
        BufferManager.bindBuffer();

        BufferManager.writeBuffer(getBuffer());
        applyProjectionMatrix();

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, vertices.size() * 3 * 4L);
        glEnableVertexAttribArray(1);

        BufferManager.unbindBuffer();
        if (shader != null) {
            shader.bind();
            BufferManager.draw(drawMode, this.vertices.size());
            shader.unbind();
        }
        BufferManager.unbind();
    }

    private FloatBuffer getBuffer() {
        FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(vertices.size() * 7);
        ArrayList<Float> floats = new ArrayList<>();
        for (Vertex vertex : vertices) {
            floats.add(vertex.getX());
            floats.add(vertex.getY());
            floats.add(vertex.getZ());
        }
        for (Vertex vertex : vertices) {
            floats.add(vertex.getR());
            floats.add(vertex.getG());
            floats.add(vertex.getB());
            floats.add(vertex.getA());
        }
        Float[] floatArray = new Float[floats.size()];
        floats.toArray(floatArray);
        floatBuffer.put(ArrayUtils.toPrimitive(floatArray));
        return floatBuffer.flip();
    }


    private void applyProjectionMatrix() {
        if (shader == null) return;
        shader.uniformMatrix4f("u_projection", projectionMatrix);
    }
}
