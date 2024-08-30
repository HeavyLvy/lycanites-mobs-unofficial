package com.lycanitesmobs.client.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lycanitesmobs.LycanitesMobs;
import org.joml.Vector3f;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.joml.Vector3f;

public class ModelPartLoader {

    public static Map<String, ModelPart> loadModelParts(String jsonFilePath) {
        Gson gson = new Gson();
        Map<String, ModelPart> modelParts = new HashMap<>();

        try (InputStreamReader reader = new InputStreamReader(ModelPartLoader.class.getResourceAsStream(jsonFilePath))) {
            JsonArray jsonArray = gson.fromJson(reader, JsonArray.class);

            for (JsonElement element : jsonArray) {
                JsonObject jsonObject = element.getAsJsonObject();

                String name = jsonObject.get("name").getAsString();
                String parent = jsonObject.get("parent").getAsString();
                String centerX = jsonObject.has("centerX") ? jsonObject.get("centerX").getAsString() : null;
                String centerY = jsonObject.has("centerY") ? jsonObject.get("centerY").getAsString() : null;
                String centerZ = jsonObject.has("centerZ") ? jsonObject.get("centerZ").getAsString() : null;

                // Log the raw values being read
                LycanitesMobs.LOGGER.info("JSON Raw - name: '{}', parent: '{}', centerX: '{}', centerY: '{}', centerZ: '{}'", name, parent, centerX, centerY, centerZ);

                ModelPart part = new ModelPart(name, parent, centerX, centerY, centerZ);
                modelParts.put(name, part);

                // Log the created ModelPart instance
                if (part.getCenter() == null) {
                    LycanitesMobs.LOGGER.error("loadModelParts - Part '{}' has a null center.", name);
                } else {
                    LycanitesMobs.LOGGER.info("loadModelParts - Part '{}' initialized with center: {}", name, part.getCenter());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return modelParts;
    }
}