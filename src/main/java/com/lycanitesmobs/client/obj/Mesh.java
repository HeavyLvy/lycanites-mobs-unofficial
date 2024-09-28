package com.lycanitesmobs.client.obj;

import com.lycanitesmobs.LycanitesMobs;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.apache.logging.log4j.LogManager;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL21;

import com.lycanitesmobs.client.renderer.CustomRenderStates;

import java.nio.ByteBuffer;
import java.util.Arrays;


public class Mesh {

    public int[] indices;
    public Vertex[] vertices;
    public Vector3o[] normals;
    private int vbo = -1;

    private static Vector3o calcNormal(Vector3o v1, Vector3o v2, Vector3o v3) {
        Vector3o output = new Vector3o();

        Vector3f calU = new Vector3f(v2.x() - v1.x(), v2.y() - v1.y(), v2.z() - v1.z());
        Vector3f calV = new Vector3f(v3.x() - v1.x(), v3.y() - v1.y(), v3.z() - v1.z());

        output.setX(calU.y() * calV.z() - calU.z() * calV.y());
        output.setY(calU.z() * calV.x() - calU.x() * calV.z());
        output.setZ(calU.x() * calV.y() - calU.y() * calV.x());

        output.normalize();
        return output;
    }

    public int getVbo() {
        if (this.vbo == -1) {
            if (this.normals == null) {
                this.normals = new Vector3o[this.indices.length];
                for (int i = 0; i < this.normals.length / 3; i++) {
                    Vector3o v1 = this.vertices[this.indices[i * 3]].getPos();
                    Vector3o v2 = this.vertices[this.indices[i * 3 + 1]].getPos();
                    Vector3o v3 = this.vertices[this.indices[i * 3 + 2]].getPos();
                    Vector3o normal = calcNormal(v1, v2, v3);
                    this.normals[i * 3] = normal;
                    this.normals[i * 3 + 1] = normal;
                    this.normals[i * 3 + 2] = normal;
                }
            }

            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuilder();

            bufferBuilder.begin(VertexFormat.Mode.TRIANGLES, CustomRenderStates.POS_TEX_NORMAL);
            for (int i = 0; i < this.indices.length; i++) {
                Vertex vertex = this.vertices[this.indices[i]];
                Vector3o normal = this.normals[i];
                bufferBuilder.vertex(vertex.getPos().x(), vertex.getPos().y(), vertex.getPos().z());
                bufferBuilder.uv(vertex.getTexCoords().x, 1.0F - vertex.getTexCoords().y);
                bufferBuilder.normal(normal.x(), normal.y(), normal.z());
                bufferBuilder.endVertex();
            }
            BufferBuilder.RenderedBuffer renderedBuffer = bufferBuilder.end();
            ByteBuffer buffer = renderedBuffer.vertexBuffer();

            this.vbo = GL21.glGenBuffers();
            GL21.glBindBuffer(GL21.GL_ARRAY_BUFFER, vbo);
            GL21.glBufferData(GL21.GL_ARRAY_BUFFER, buffer, GL21.GL_STATIC_DRAW);
            GL21.glBindBuffer(GL21.GL_ARRAY_BUFFER, 0);

            int error = GL11.glGetError();
            if (error != GL11.GL_NO_ERROR) {
                LycanitesMobs.logError("OpenGL error after uploading buffer data: " + error);
                this.vbo = -1;
            }
        }

        return this.vbo;
    }

    public void delete() {
        if (vbo != -1) {
            GL21.glDeleteBuffers(vbo);
            vbo = -1;
        }
    }

}
