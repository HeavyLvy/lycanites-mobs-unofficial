package com.lycanitesmobs.client.model;

import com.lycanitesmobs.LycanitesMobs;
import org.joml.Vector3f;

import org.joml.Vector3f;

public class ModelPart {
    private String name;
    private String parent;
    private Vector3f center;

    public ModelPart(String name, String parent, String centerX, String centerY, String centerZ) {
        this.name = name;
        this.parent = parent;

        LycanitesMobs.LOGGER.info("ModelPart Constructor - name: '{}', centerX: '{}', centerY: '{}', centerZ: '{}'", name, centerX, centerY, centerZ);

        this.center = new Vector3f(
                parseFloat(centerX, 0.0f),
                parseFloat(centerY, 0.0f),
                parseFloat(centerZ, 0.0f)
        );

        LycanitesMobs.LOGGER.info("ModelPart Constructor - '{}' center: {}", name, this.center);
    }

    private float parseFloat(String value, float defaultValue) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            LycanitesMobs.LOGGER.error("ModelPart - Failed to parse '{}' as float. Using default value: {}", value, defaultValue);
            return defaultValue;
        }
    }

    public String getName() {
        return name;
    }

    public String getParent() {
        return parent;
    }

    public Vector3f getCenter() {
        return center;
    }
}