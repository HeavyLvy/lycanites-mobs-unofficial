package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateElemental;
import com.lycanitesmobs.client.obj.ObjPart;
import com.lycanitesmobs.client.renderer.CreatureRenderer;
import com.lycanitesmobs.client.renderer.CustomRenderStates;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureBase;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureEffect;
import com.lycanitesmobs.client.renderer.obj.ModelData;
import com.lycanitesmobs.client.renderer.obj.Part;
import com.lycanitesmobs.client.renderer.renderapi.client.render.buffer.WorldBuffer;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector4f;

@OnlyIn(Dist.CLIENT)
public class ModelAegis extends ModelTemplateElemental {

    // ==================================================
    //                 Animate Part
    // ==================================================
    float maxLeg = 0F;
    // Model data for storing the parsed parts
    private ModelData modelData;

    // ==================================================
    //                    Constructors
    // ==================================================
    public ModelAegis() {
        this(1.0F);
    }

    public ModelAegis(float shadowSize) {

        // Load Model:
        this.initModel("aegis", LycanitesMobs.modInfo, "entity/aegis");

        // Load JSON data for model parts
        this.modelData = new ModelData();
        this.modelData.parseJson("path/to/aegis_parts.json");  // Make sure the path is correct

        // Trophy:
        this.trophyScale = 1.2F;
        this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
    }

    // ==================================================
    //             Add Custom Render Layers
    // ==================================================
    @Override
    public void addCustomLayers(CreatureRenderer renderer) {
        super.addCustomLayers(renderer);
        renderer.addLayer(new LayerCreatureEffect(renderer, "", true, CustomRenderStates.BLEND.NORMAL.id, true));
    }

    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
        super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);

        // Retrieve the part from the modelData
        Part part = getPartByName(partName);
        if (part == null) {
            return;  // If the part isn't found, skip the animation
        }

        // Shields:
        if (partName.contains("shieldupper")) {
            this.shiftOrigin(part.getName(), "body");
            this.rotate(0, loop * 8, 0);
            this.shiftOriginBack(part.getName(), "body");
        }

        // Sword Mode:
        if (entity instanceof BaseCreatureEntity) {
            BaseCreatureEntity entityCreature = (BaseCreatureEntity) entity;
            if (!entityCreature.isBlocking()) {

                if (partName.equals("core")) {
                    //this.rotate(0, loop * 16, 0);
                }

                if (partName.contains("shieldupper")) {
                    float orbit = loop * 16;
                    if (partName.contains("left")) {
                        orbit += 90;
                    }
                    this.shiftOrigin(part.getName(), "body");
                    this.rotate(0, orbit, 0);
                    this.shiftOriginBack(part.getName(), "body");

                    this.translate(0, -0.25f, 0);
                    this.scale(0.5f, 1, 1);
                    if ("shieldupperleft01".equals(partName) || "shieldupperright01".equals(partName)) {
                        this.rotate(-90, 0, 0);
                    } else if ("shieldupperleft02".equals(partName) || "shieldupperright02".equals(partName)) {
                        this.rotate(90, 0, 0);
                    }
                }

                if (partName.contains("shieldlower")) {
                    this.scale(0.5f, 1, 1);
                }
            }
        }
    }

    // Helper method to get part by name
    private Part getPartByName(String partName) {
        for (Part part : this.modelData.parts) {
            Part foundPart = findPartRecursive(part, partName);
            if (foundPart != null) {
                return foundPart;
            }
        }
        return null;
    }

    @Override
    public void renderPart(WorldBuffer buffer, ObjPart part, Vector4f color, Vec3 entityPos, Matrix4f animationMatrix) {
        // Custom rendering logic for Aegis with animations
        if (part.getName().contains("shieldupper")) {
            this.shiftOrigin(part.getName(), "body");
            animationMatrix.rotate(loop * 8, 0, 1, 0);
            this.shiftOriginBack(part.getName(), "body");
        }

        // Call the default rendering logic with the modified matrix
        super.renderPart(buffer, part, color, entityPos, animationMatrix);
    }

    // Recursively search for a part by name
    private Part findPartRecursive(Part part, String partName) {
        if (part.getName().equals(partName)) {
            return part;
        }
        if (part.children != null) {
            for (Part child : part.children) {
                Part foundPart = findPartRecursive(child, partName);
                if (foundPart != null) {
                    return foundPart;
                }
            }
        }
        return null;
    }

    // ==================================================
    //                Get Part Color
    // ==================================================

    /**
     * Returns the coloring to be used for this part and layer.
     **/
    @Override
    public Vector4f getPartColor(String partName, Entity entity, LayerCreatureBase layer, boolean trophy, float loop) {
        if (!this.isArmorPart(partName)) {
            float glowSpeed = 40;
            float glow = loop * glowSpeed % 360;
            float color = ((float) Math.cos(Math.toRadians(glow)) * 0.1f) + 0.9f;
            return new Vector4f(color, color, color, 1);
        }

        return super.getPartColor(partName, entity, layer, trophy, loop);
    }

    // ==================================================
    //                Can Render Part
    // ==================================================
    @Override
    public boolean canRenderPart(String partName, Entity entity, LayerCreatureBase layer, boolean trophy) {
        if (this.isArmorPart(partName)) {
            return layer == null;
        }
        return layer instanceof LayerCreatureEffect;
    }

    protected boolean isArmorPart(String partName) {
        return "shoulders".equals(partName) || "helm".equals(partName) || partName.contains("shield");
    }

}