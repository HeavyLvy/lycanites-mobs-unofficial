package com.lycanitesmobs.client.renderer.obj;

import com.google.gson.Gson;

import java.io.FileReader;
import java.util.List;

public class ModelData {
    public List<Part> parts;

    public void parseJson(String jsonFilePath) {
        try (FileReader reader = new FileReader(jsonFilePath)) {
            Gson gson = new Gson();
            ModelData modelData = gson.fromJson(reader, ModelData.class);
            this.parts = modelData.parts;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to traverse and process parts
    public void traverseParts(List<Part> parts) {
        for (Part part : parts) {
            System.out.println("Part Name: " + part.getName());
            System.out.println("Center: " + part.center);

            // Process the mesh or material if needed
            if (part.mesh != null) {
                System.out.println("Processing Mesh for: " + part.getName());
                // Add mesh processing logic here
            }

            if (part.material != null) {
                System.out.println("Processing Material for: " + part.getName());
                // Add material processing logic here
            }

            // Recursively traverse child parts
            if (part.children != null && !part.children.isEmpty()) {
                traverseParts(part.children);
            }
        }
    }

    // Convenience method to start traversing from the root parts
    public void traverseParts() {
        if (this.parts != null && !this.parts.isEmpty()) {
            traverseParts(this.parts);
        }
    }
}