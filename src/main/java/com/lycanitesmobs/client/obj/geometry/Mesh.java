package com.lycanitesmobs.client.obj.geometry;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.util.math.Vector3o;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;

import com.lycanitesmobs.client.renderer.util.CustomRenderStates;

import java.util.ArrayList;
import java.util.List;

public class Mesh {

    public int[] indices;
    public Vertex[] vertices;
    public Vector3o[] normals;

    private VertexBuffer vbo = null;

    public static List<String> logs = new ArrayList<>();

    /**
     * 1.20.1 equivalent of the old getVbo():
     * - Computes normals once if missing
     * - Builds a POSITION/UV/NORMAL buffer (triangles)
     * - Uploads to a VertexBuffer
     */
    public VertexBuffer getVbo() {
        try {
            if (this.vbo == null) {
                if (this.normals == null) {
                    this.normals = new Vector3o[this.indices.length];
                    for (int tri = 0; tri < this.indices.length / 3; tri++) {
                        Vector3o v1 = this.vertices[this.indices[tri * 3]].getPos();
                        Vector3o v2 = this.vertices[this.indices[tri * 3 + 1]].getPos();
                        Vector3o v3 = this.vertices[this.indices[tri * 3 + 2]].getPos();
                        Vector3o normal = calcNormal(v1, v2, v3);
                        this.normals[tri * 3] = normal;
                        this.normals[tri * 3 + 1] = normal;
                        this.normals[tri * 3 + 2] = normal;
                    }
                }

                Tesselator tess = Tesselator.getInstance();
                BufferBuilder buf = tess.getBuilder();
                buf.begin(VertexFormat.Mode.TRIANGLES, CustomRenderStates.POS_TEX_NORMAL);

                for (int i = 0; i < this.indices.length; i++) {
                    Vertex vertex = this.vertices[this.indices[i]];
                    Vector3o normal = this.normals[i];

                    float px = vertex.getPos().x();
                    float py = vertex.getPos().y();
                    float pz = vertex.getPos().z();

                    float u = vertex.getTexCoords().x;
                    float v = 1.0F - vertex.getTexCoords().y;

                    if (normal == null) {
                        String message = "[Lycanites Mobs Debug]: Normal vector is null";
                        if (!logs.contains(message)) {
                            LMHelperClass.logWarning("", message);
                            logs.add(message);
                        }
                        normal = new Vector3o(0, 0, 0);
                    }

                    buf.vertex(px, py, pz)
                            .uv(u, v)
                            .normal(normal.x(), normal.y(), normal.z())
                            .endVertex();
                }

                VertexBuffer vb = new VertexBuffer(VertexBuffer.Usage.STATIC);
                vb.bind();
                vb.upload(buf.end());
                VertexBuffer.unbind();

                this.vbo = vb;
            }
        } catch (Exception e) {
            LMHelperClass.logErrorMessageOnceCatchable("Error in Mesh class. ", e);
        }
        return this.vbo;
    }

    private static Vector3o calcNormal(Vector3o v1, Vector3o v2, Vector3o v3) {
        Vector3o u = new Vector3o(v2).sub(v1);
        Vector3o v = new Vector3o(v3).sub(v1);
        Vector3o out = new Vector3o(
                u.y() * v.z() - u.z() * v.y(),
                u.z() * v.x() - u.x() * v.z(),
                u.x() * v.y() - u.y() * v.x()
        );
        out.normalize();
        return out;
    }

    public void delete() {
        if (this.vbo != null) {
            this.vbo.close();
            this.vbo = null;
        }
    }
}
