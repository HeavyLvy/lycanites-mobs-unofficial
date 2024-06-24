package com.lycanitesmobs.client.renderer.renderapi.client.render.buffer;



import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL33.*;


public class BufferManager {
    public static int vao;
    public static int vbo;

    public static int prevVao;



    public static void bindBuffer() {
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
    }

    public static void unbindBuffer() {
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public static void writeBuffer(FloatBuffer buffer) {
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
    }

    public static void draw(int drawMode, int verts) {
        glDrawArrays(drawMode, 0, verts);
    }

    public static void bind() {
        prevVao = glGetInteger(GL_VERTEX_ARRAY_BINDING);
        glBindVertexArray(vao);
    }

    public static void unbind() {
        glBindVertexArray(prevVao);
    }
}
