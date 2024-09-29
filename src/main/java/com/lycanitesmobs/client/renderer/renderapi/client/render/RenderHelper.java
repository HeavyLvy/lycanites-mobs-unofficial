package com.lycanitesmobs.client.renderer.renderapi.client.render;


import com.lycanitesmobs.client.model.AnimationPart;
import com.lycanitesmobs.client.model.CreatureObjModel;
import com.lycanitesmobs.client.model.CreatureObjModelOld;
import com.lycanitesmobs.client.model.ModelObjAnimationFrame;
import com.lycanitesmobs.client.obj.ObjPart;
import com.lycanitesmobs.client.obj.Vector3o;
import com.lycanitesmobs.client.obj.Vertex;
import com.lycanitesmobs.client.renderer.renderapi.client.render.buffer.WorldBuffer;
import com.lycanitesmobs.client.renderer.renderapi.client.render.shader.ShaderManager;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

import java.awt.*;
import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL33.*;


public class RenderHelper {
    public static WorldBuffer startTri() {
        return new WorldBuffer(GL_TRIANGLES, ShaderManager.getPositionColorShader());
    }

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

    public static void drawTri(WorldBuffer buffer, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, Color color) {
        buffer.vert(x1, y1, z1, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        buffer.vert(x2, y2, z2, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        buffer.vert(x3, y3, z3, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
    }

    public static void renderModel(WorldBuffer buffer, CreatureObjModel objModel, Vector4f color, BaseCreatureEntity entity) {
        PoseStack poseStack = new PoseStack();
       /* poseStack.mulPose(Axis.YP.rotationDegrees(-entity.yBodyRot));  // Adjust yaw
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.xRot));*/       // Adjust pitch

        for (ObjPart part : objModel.objParts) {
            if (part.mesh == null) {
                continue;
            }

            AnimationPart animationPart = objModel.animationParts.get(part.getName().toLowerCase());
            if (animationPart != null) {
                applyAnimationFrames(poseStack, animationPart);
            }

            int[] indices = part.mesh.indices;
            Vertex[] vertices = part.mesh.vertices;
            for (int i = 0; i < indices.length; i += 3) {
                for (int iv = 0; iv < 3; iv++) {
                    Vertex v = vertices[indices[i + iv]];
                    Vector4f transformedPos = transformVertex(poseStack, v.getPos().toVec3());
                    buffer.vert(
                            (float) (transformedPos.x() + entity.position().x),
                            (float) (transformedPos.y() + entity.position().y),
                            (float) (transformedPos.z() + entity.position().z),
                            color.x(), color.y(), color.z(), color.w()
                    );
                }
            }
        }
        buffer.draw();
    }

    public static void renderLegacyModel(WorldBuffer buffer, CreatureObjModelOld objModel, Vector4f color, BaseCreatureEntity entity) {
        PoseStack poseStack = new PoseStack();
       /* poseStack.mulPose(Axis.YP.rotationDegrees(-entity.yBodyRot));  // Adjust yaw
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.xRot)); */      // Adjust pitch

        for (ObjPart part : objModel.wavefrontParts) {
            if (part.mesh == null) {
                continue;
            }

            /*AnimationPart animationPart = objModel.animationParts.get(part.getName().toLowerCase());
            if (animationPart != null) {
                applyAnimationFrames(poseStack, animationPart);
            }*/

            int[] indices = part.mesh.indices;
            Vertex[] vertices = part.mesh.vertices;
            for (int i = 0; i < indices.length; i += 3) {
                for (int iv = 0; iv < 3; iv++) {
                    Vertex v = vertices[indices[i + iv]];
                    Vector4f transformedPos = transformVertex(poseStack, v.getPos().toVec3());
                    buffer.vert(
                            (float) (transformedPos.x() + entity.position().x),
                            (float) (transformedPos.y() + entity.position().y),
                            (float) (transformedPos.z() + entity.position().z),
                            color.x(), color.y(), color.z(), color.w()
                    );
                }
            }
        }
        buffer.draw();
    }

    public static void applyAnimationFrames(PoseStack poseStack, AnimationPart animationPart) {
        if (animationPart.parent != null) {
            applyAnimationFrames(poseStack, animationPart.parent);
        }

        for (ModelObjAnimationFrame frame : animationPart.animationFrames) {
            frame.apply(poseStack);
        }
    }

    public static Vector4f transformVertex(PoseStack poseStack, Vec3 vertexPos) {
        Matrix4f transformMatrix = poseStack.last().pose();
        Vector4f transformedPos = new Vector4f((float) vertexPos.x(), (float) vertexPos.y(), (float) vertexPos.z(), 1.0F);
        transformedPos.mul(transformMatrix);
        return transformedPos;
    }

    // Helper method to apply animation frames
    /*private static void applyAnimationFrames(PoseStack poseStack, AnimationPart animationPart) {
        for (ModelObjAnimationFrame frame : animationPart.animationFrames) {
            if ("angle".equals(frame.type)) {

                poseStack.mulPose(new Quaternionf(frame.x, frame.y, frame.z, frame.amount));
            } else if ("rotate".equals(frame.type)) {
                poseStack.mulPose(Axis.XP.rotationDegrees(frame.x * frame.amount));
                poseStack.mulPose(Axis.YP.rotationDegrees(frame.y * frame.amount));
                poseStack.mulPose(Axis.ZP.rotationDegrees(frame.z * frame.amount));
            } else if ("translate".equals(frame.type)) {
                poseStack.translate(frame.x * frame.amount, frame.y * frame.amount, frame.z * frame.amount);
            } else if ("scale".equals(frame.type)) {
                poseStack.scale(frame.x * frame.amount, frame.y * frame.amount, frame.z * frame.amount);
            }
        }
    }

    // Helper method to transform vertex positions
    private static Vector4f transformVertex(PoseStack poseStack, Vector3f pos) {
        Matrix4f matrix = poseStack.last().pose();
        Vector4f vector = new Vector4f(pos.x(), pos.y(), pos.z(), 1.0F);
        vector.mul(matrix);
        return vector;
    }*/

    /*public static void renderModel(WorldBuffer buffer, CreatureObjModel objPart, Vector4f color, BaseCreatureEntity entity) {
        for (ObjPart part : objPart.objParts) {
            if (part.mesh == null) {
                continue;
            }
            int[] indices = part.mesh.indices;
            Vertex[] vertices = part.mesh.vertices;
            for (int i = 0; i < indices.length; i += 3) {
                for (int iv = 0; iv < 3; iv++) {
                    Vertex v = vertices[indices[i + iv]];
                    buffer.vert(
                            (float) (v.getPos().x() + entity.position().x), (float) (v.getPos().y() + entity.position().y), (float) (v.getPos().z() + entity.position().z),
                            color.x(), color.y(), color.z(), color.w()
                    );
                }
            }
        }
        buffer.draw();
    }

    public static void renderLegacyModel(WorldBuffer buffer, CreatureObjModelOld objPart, Vector4f color, BaseCreatureEntity entity) {
        for (ObjPart part : objPart.wavefrontParts) {
            if (part.mesh == null) {
                return;
            }
            int[] indices = part.mesh.indices;
            Vertex[] vertices = part.mesh.vertices;
            for (int i = 0; i < indices.length; i += 3) {
                for (int iv = 0; iv < 3; iv++) {
                    Vertex v = vertices[indices[i + iv]];
                    buffer.vert(
                            (float) (v.getPos().x() + entity.position().x), (float) (v.getPos().y() + entity.position().y), (float) (v.getPos().z() + entity.position().z),
                            color.x(), color.y(), color.z(), color.w()
                    );
                }
            }
        }
        buffer.draw();
    }*/
   /* public static void renderModel(WorldBuffer buffer, CreatureObjModel objPart, Vector4f color, BaseCreatureEntity entity) {
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
    }*/

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


    private static Vector3o getNormal(Vector3o p1, Vector3o p2, Vector3o p3) {
        Vector3o output = new Vector3o();

        Vector3f calU = new Vector3f(p2.x() - p1.x(), p2.y() - p1.y(), p2.z() - p1.z());
        Vector3f calV = new Vector3f(p3.x() - p1.x(), p3.y() - p1.y(), p3.z() - p1.z());

        output.set(
                calU.y() * calV.z() - calU.z() * calV.y(),
                calU.z() * calV.x() - calU.x() * calV.z(),
                calU.x() * calV.y() - calU.y() * calV.x()
        );

        output.normalize();
        return output;
    }

    public static void renderPart(WorldBuffer buffer, ObjPart objPart, Vector4f color, Vec3 entityPos) {
        if (objPart.mesh == null) {
            return;
        }

        int[] indices = objPart.mesh.indices;
        Vertex[] vertices = objPart.mesh.vertices;
        if (objPart.mesh.normals == null) {
            objPart.mesh.normals = new Vector3o[indices.length];
        }

        for (int i = 0; i < indices.length; i += 3) {
            Vector3o normal = objPart.mesh.normals[i];
            if (normal == null) {
                normal = getNormal(vertices[indices[i]].getPos(), vertices[indices[i + 1]].getPos(), vertices[indices[i + 2]].getPos());
                objPart.mesh.normals[i] = normal;
            }

            for (int iv = 0; iv < 3; iv++) {
                Vertex v = vertices[indices[i + iv]];
                buffer.vert(
                        (float) (v.getPos().x() + entityPos.x()),
                        (float) (v.getPos().y() + entityPos.y()),
                        (float) (v.getPos().z() + entityPos.z()),
                        color.x(), color.y(), color.z(), color.w()
                );
            }
        }

        buffer.draw();
    }

    public WorldBuffer startLines() {
        glEnable(GL_LINE_SMOOTH);
        return new WorldBuffer(GL_LINES, ShaderManager.getPositionColorShader());
    }

    public void endLines(WorldBuffer buffer) {
        glEnable(GL_BLEND);
        glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        glDepthMask(false);
        buffer.draw();
        glDepthMask(true);
        glDisable(GL_BLEND);
    }

    public void drawLine(WorldBuffer buffer, float x1, float y1, float z1, float x2, float y2, float z2, Color color) {
        buffer.vert(x1, y1, z1, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        buffer.vert(x2, y2, z2, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
    }


}
