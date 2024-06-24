package com.lycanitesmobs.client.obj;


import org.joml.Vector2f;
import org.joml.Vector3f;

public class Vertex {
    private Vector3o pos;
    private Vector2f texCoords;
    private Vector3o normal;
    private Vector3f tangent;

    public Vertex(Vector3o pos, Vector2f texCoords, Vector3o normal, Vector3f tangent) {
        this.pos = pos;
        this.texCoords = texCoords;
        this.normal = normal;
        this.tangent = tangent;
    }

    public Vector3o getPos() {
        return this.pos;
    }

    public Vector2f getTexCoords() {
        return this.texCoords;
    }

    /** Returns per vertex normal for smoother shading. **/
    public Vector3o getNormal() {
        return this.normal;
    }

    public Vector3f getTangent() {
        return tangent;
    }
}
