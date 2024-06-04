package com.lycanitesmobs.client.obj;


import org.joml.Vector3f;

public class ObjPart {

    private String name;
    public Mesh mesh;
    public Material material;
    public Vector3f center;

    public ObjPart(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}
