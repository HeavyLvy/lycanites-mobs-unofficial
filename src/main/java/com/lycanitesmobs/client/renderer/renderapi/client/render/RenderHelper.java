package com.lycanitesmobs.client.renderer.renderapi.client.render;


import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.CreatureObjModel;
import com.lycanitesmobs.client.model.CreatureObjModelOld;
import com.lycanitesmobs.client.model.ModelPart;
import com.lycanitesmobs.client.obj.ObjPart;
import com.lycanitesmobs.client.obj.Vector3o;
import com.lycanitesmobs.client.obj.Vertex;
import com.lycanitesmobs.client.renderer.renderapi.client.render.buffer.WorldBuffer;
import com.lycanitesmobs.client.renderer.renderapi.client.render.shader.ShaderManager;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

import java.awt.*;
import java.lang.Math;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL33.*;


public class RenderHelper {

    public static WorldBuffer startTri(Matrix4f modelViewMatrix, Matrix4f viewMatrix) {
        return new WorldBuffer(GL_TRIANGLES, ShaderManager.getPositionColorShader(), modelViewMatrix, viewMatrix);
    }

    public static void endTri(WorldBuffer buffer) {
        //glDepthRange(0, 0.7);
        glEnable(GL_BLEND);
        glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_CULL_FACE);
        glDepthMask(false);
        buffer.draw();
        glDepthMask(true);
        glEnable(GL_CULL_FACE);
        glDisable(GL_BLEND);
        glDepthRange(0, 1);
    }

    public static Vec3 rotatePartAroundEntity(Vec3 position, float yaw, float pitch, Vector3f center, Vec3 rotationPoint, int animationTicks) {
        // Rotate the part around its own center first
        Vec3 rotatedAroundCenter = rotatePartWithCenter(position, yaw, pitch, center);

        // Translate the part to the rotation point (relative to the entity)
        Vec3 translatedPos = rotatedAroundCenter.subtract(rotationPoint.x, rotationPoint.y, rotationPoint.z);

        // Apply additional rotation using animationTicks around the Y axis
        float additionalYaw = (float) Math.toRadians(animationTicks);
        Vec3 finalRotatedPos = rotateVertex(translatedPos, additionalYaw, 0);

        // Translate the part back to its original position
        return finalRotatedPos.add(rotationPoint.x, rotationPoint.y, rotationPoint.z);
    }


    public static Vec3 rotatePartWithCenter(Vec3 position, float yaw, float pitch, Vector3f center) {
        if (center == null) {
            LycanitesMobs.LOGGER.error("rotatePartWithCenter - Center is null for position: {}", position);
            return position; // Skip rotation and return original position
        }

        Vec3 translatedPos = position.subtract(center.x, center.y, center.z);
        Vec3 rotatedPos = rotateVertex(translatedPos, yaw, pitch);
        return rotatedPos.add(center.x, center.y, center.z);
    }


    public static void renderModelWithRotations(WorldBuffer buffer, CreatureObjModel objModel, Vector4f color, BaseCreatureEntity entity, Map<String, ModelPart> modelParts) {
        float yaw = entity.yBodyRot * ((float) Math.PI / 180F);
        float pitch = entity.xRot * ((float) Math.PI / 180F);

        Vec3 rotationPoint = entity.position();  // You can customize this point based on the entity

        for (ObjPart part : objModel.objParts) {
            if (part.mesh == null) {
                continue;
            }

            ModelPart modelPart = modelParts.get(part.getName());
            if (modelPart == null) {
                continue;  // Skip parts not defined in JSON
            }

            Vector3f center = modelPart.getCenter();
            int[] indices = part.mesh.indices;
            Vertex[] vertices = part.mesh.vertices;

            for (int i = 0; i < indices.length; i += 3) {
                for (int iv = 0; iv < 3; iv++) {
                    Vertex v = vertices[indices[i + iv]];

                    // Apply only initial rotation (yaw and pitch)
                    Vec3 rotatedPos = rotatePartWithCenter(v.getPos().toVec3(), yaw, pitch, center);

                    buffer.vert(
                            (float) (rotatedPos.x() + entity.position().x),
                            (float) (rotatedPos.y() + entity.position().y),
                            (float) (rotatedPos.z() + entity.position().z),
                            color.x(), color.y(), color.z(), color.w()
                    );
                }
            }
        }
        buffer.draw();
    }

    public static Vec3 applyAdditionalRotation(Vec3 position, Vector3f center, Vec3 rotationPoint, int animationTicks) {
        // Translate the part to the rotation point (relative to the entity)
        Vec3 translatedPos = position.subtract(rotationPoint.x, rotationPoint.y, rotationPoint.z);

        // Apply additional rotation using animationTicks around the Y axis
        float additionalYaw = (float) Math.toRadians(animationTicks);
        Vec3 finalRotatedPos = rotateVertex(translatedPos, additionalYaw, 0);

        // Translate the part back to its original position
        return finalRotatedPos.add(rotationPoint.x, rotationPoint.y, rotationPoint.z);
    }

    public static void renderModel(WorldBuffer buffer, CreatureObjModel objPart, Vector4f color, BaseCreatureEntity entity) {
        float yaw = entity.yBodyRot * ((float) Math.PI / 180F);  // Convert degrees to radians
        float pitch = entity.xRot * ((float) Math.PI / 180F);  // Convert degrees to radians

        for (ObjPart part : objPart.objParts) {
            if (part.mesh == null) {
                continue;
            }
            int[] indices = part.mesh.indices;
            Vertex[] vertices = part.mesh.vertices;
            for (int i = 0; i < indices.length; i += 3) {
                for (int iv = 0; iv < 3; iv++) {
                    Vertex v = vertices[indices[i + iv]];
                    Vec3 rotatedPos = rotateVertex(v.getPos().toVec3(), yaw, pitch);
                    buffer.vert(
                            (float) (rotatedPos.x() + entity.position().x),
                            (float) (rotatedPos.y() + entity.position().y),
                            (float) (rotatedPos.z() + entity.position().z),
                            color.x(), color.y(), color.z(), color.w()
                    );
                }
            }
        }
        buffer.draw();
    }

    public static void renderLegacyModel(WorldBuffer buffer, CreatureObjModelOld objPart, Vector4f color, BaseCreatureEntity entity) {
        float yaw = entity.yBodyRot * ((float) Math.PI / 180F);  // Convert degrees to radians
        float pitch = entity.xRot * ((float) Math.PI / 180F);  // Convert degrees to radians

        for (ObjPart part : objPart.wavefrontParts) {
            if (part.mesh == null) {
                continue;
            }
            int[] indices = part.mesh.indices;
            Vertex[] vertices = part.mesh.vertices;
            for (int i = 0; i < indices.length; i += 3) {
                for (int iv = 0; i < 3; iv++) {
                    Vertex v = vertices[indices[i + iv]];
                    Vec3 rotatedPos = rotateVertex(v.getPos().toVec3(), yaw, pitch);
                    buffer.vert(
                            (float) (rotatedPos.x() + entity.position().x),
                            (float) (rotatedPos.y() + entity.position().y),
                            (float) (rotatedPos.z() + entity.position().z),
                            color.x(), color.y(), color.z(), color.w()
                    );
                }
            }
        }
        buffer.draw();
    }

    public static Vec3 rotateVertex(Vec3 pos, float yaw, float pitch) {
        double x = pos.x();
        double y = pos.y();
        double z = pos.z();

        // Rotate around Y axis (yaw)
        double xzLen = Math.sqrt(x * x + z * z);
        double yawAngle = Math.atan2(z, x) + yaw;
        x = xzLen * Math.cos(yawAngle);
        z = xzLen * Math.sin(yawAngle);

        // Rotate around X axis (pitch)
        double yzLen = Math.sqrt(y * y + z * z);
        double pitchAngle = Math.atan2(z, y) + pitch;
        y = yzLen * Math.cos(pitchAngle);
        z = yzLen * Math.sin(pitchAngle);

        return new Vec3(x, y, z);
    }

}
