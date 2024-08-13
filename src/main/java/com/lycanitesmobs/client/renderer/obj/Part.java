package com.lycanitesmobs.client.renderer.obj;

import com.lycanitesmobs.client.obj.Material;
import com.lycanitesmobs.client.obj.Mesh;
import org.joml.Vector3f;

import java.util.List;

public class Part {
    public List<Part> children;
    public Mesh mesh;
    public Material material;
    public Vector3f center;
    String name;

    public Part(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}