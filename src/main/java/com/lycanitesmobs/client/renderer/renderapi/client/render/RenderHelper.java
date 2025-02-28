package com.lycanitesmobs.client.renderer.renderapi.client.render;


import com.lycanitesmobs.client.model.*;
import com.lycanitesmobs.client.obj.IndexedModel;
import com.lycanitesmobs.client.obj.ObjPart;
import com.lycanitesmobs.client.obj.Vector3o;
import com.lycanitesmobs.client.obj.Vertex;
import com.lycanitesmobs.client.renderer.renderapi.client.render.buffer.WorldBuffer;
import com.lycanitesmobs.client.renderer.renderapi.client.render.shader.ShaderManager;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.util.LMHelperClass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.*;
import org.lwjgl.BufferUtils;

import java.awt.*;
import java.lang.Math;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lycanitesmobs.client.model.CreatureObjModel.MODEL_OFFSET_POS_Y;
import static com.lycanitesmobs.client.model.CreatureObjModel.MODEL_OFFSET_ROT_X;
import static org.lwjgl.opengl.GL33.*;


public class RenderHelper {
    public static WorldBuffer startTri() {
        return new WorldBuffer(GL_TRIANGLES, ShaderManager.getPositionColorShader());
    }

    public static WorldBuffer startTri(Matrix4f modelViewMatrix, Matrix4f viewMatrix) {
        return new WorldBuffer(GL_TRIANGLES, ShaderManager.getPositionColorShader(), modelViewMatrix, viewMatrix);
    }

    public static void endTri(WorldBuffer buffer) {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);

        buffer.draw();

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        glBindTexture(GL_TEXTURE_2D, 0);
        glDepthRange(0, 1);
    }

    /*public static void endTri(WorldBuffer buffer) {
        //glDepthRange(0, 0.7);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        buffer.draw();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        glDepthRange(0, 1);
    }*/

    public static void drawTri(WorldBuffer buffer, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, Color color) {
        buffer.vert(x1, y1, z1, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        buffer.vert(x2, y2, z2, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        buffer.vert(x3, y3, z3, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
    }

    public static void applyAnimationFrames(PoseStack poseStack, AnimationPart animationPart) {
        if (animationPart.parent != null) {
            applyAnimationFrames(poseStack, animationPart.parent);
        }

        for (ModelObjAnimationFrame frame : animationPart.animationFrames) {
            frame.apply(poseStack);
        }
    }


    /**
     * This is the test method we'll be using to render a certain color per texcoord
     *
     * @param buffer
     * @param objModel
     */
    public static void renderModel(WorldBuffer buffer, CreatureObjModel objModel, PoseStack poseStack) {
        try {
            for (ObjPart part : objModel.objParts) {
                if (part.mesh == null) {
                    continue;
                }
                int[] indices = part.mesh.indices;
                Vertex[] vertices = part.mesh.vertices;

                for (int i = 0; i < indices.length; i += 3) {
                    for (int iv = 0; iv < 3; iv++) {
                        Vertex v = vertices[indices[i + iv]];

                        Vector4f transformedPos = transformVertex(poseStack, v.getPos().toVec3());

                        float r = v.getTexCoords().x;
                        float g = v.getTexCoords().y;
                        float b = 1.0f - (r + g) / 2.0f;
                        float a = 1.0f;

                        buffer.vert(
                                (float) (transformedPos.x() + 0),
                                (float) (transformedPos.y() + 0),
                                (float) (transformedPos.z() + 0),
                                r, g, b, a
                        );
                    }
                }
            }
            buffer.draw();
        } catch (Exception e) {
            LMHelperClass.logErrorMessageOnceCatchable("Error drawing model with texcoord coloring", e);
        }
    }

    /**
     * Main Draw Method
     *
     * @param buffer
     * @param objModel
     * @param entity
     */
    public static void renderModel(WorldBuffer buffer, CreatureObjModel objModel, BaseCreatureEntity entity) {
        var poseStack = objModel.matrixStack;
        var animator = objModel.animator;
        var minecraft = Minecraft.getInstance();
        var cameraYRot = minecraft.getCameraEntity().yRot;
        var cameraXRot = minecraft.getCameraEntity().xRot;
        poseStack.pushPose();
        // poseStack.mulPose(Axis.YP.rotationDegrees(0));
        poseStack.mulPose(Axis.YP.rotationDegrees(-cameraYRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(cameraXRot));
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        var rot = -entity.yBodyRot;
        //LMHelperClass.logInfoMessageOnce("yrot: " + rot);
        poseStack.mulPose(Axis.YP.rotationDegrees(rot));

        for (ObjPart part : objModel.objParts) {
            if (part.mesh == null) {
                continue;
            }

            float[][] cachedColors = objModel.colorCache.get(part);
            if (cachedColors == null) {
                continue;
            }
            poseStack.pushPose();

            AnimationPart animationPart = objModel.animationParts.get(part.getName().toLowerCase());
            if (animationPart != null) {
                animationPart.applyAnimationFrames(animator);
            }

            int[] indices = part.mesh.indices;
            Vertex[] vertices = part.mesh.vertices;

            for (int i = 0; i < indices.length; i += 3) {
                for (int iv = 0; iv < 3; iv++) {
                    Vertex v = vertices[indices[i + iv]];

                    Vector4f transformedPos = transformVertex(poseStack, v.getPos().toVec3());
                    float[] color = cachedColors[i + iv];

                    buffer.vert(
                            (float) (transformedPos.x() + entity.position().x),
                            (float) (transformedPos.y() + entity.position().y),
                            (float) (transformedPos.z() + entity.position().z),
                            color[0], color[1], color[2], color[3]
                    );
                }
            }
            poseStack.popPose();
        }
        poseStack.popPose();

        buffer.draw();
    }

    public static void precomputeColors(CreatureObjModel objModel, int textureID) {
        Map<Vector2f, float[]> colorCache = new HashMap<>();

        for (ObjPart part : objModel.objParts) {
            if (part.mesh == null) {
                continue;
            }

            IndexedModel indexedModel = objModel.objModel.partToIndexedModelMap.get(part);
            if (indexedModel == null) {
                continue;
            }

            int[] indices = part.mesh.indices;
            List<Vector2f> texCoords = indexedModel.getTexCoords();

            float[][] colors = new float[indices.length][4];

            for (int i = 0; i < indices.length; i++) {
                Vector2f texCoord = texCoords.get(indices[i]);

                float[] cachedColor = colorCache.get(texCoord);
                if (cachedColor == null) {
                    cachedColor = readColorAtTexCoord(textureID, texCoord);
                    colorCache.put(texCoord, cachedColor);
                }

                colors[i] = cachedColor;
            }

            objModel.colorCache.put(part, colors);
        }
    }


    public static float[] readColorAtTexCoord(int textureID, Vector2f texCoord) {
        glBindTexture(GL_TEXTURE_2D, textureID);

        int width = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH);
        int height = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT);

        if (width == 0 || height == 0) {
            LMHelperClass.logInfoMessageOnce("Failed to retrieve texture dimensions for texture ID: " + textureID);
            return new float[]{1.0f, 1.0f, 1.0f, 1.0f};
        }
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
        glGetTexImage(GL_TEXTURE_2D, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        int x = (int) (texCoord.x * (width - 1));
        int y = (int) ((1.0f - texCoord.y) * (height - 1));

        int pixelIndex = (y * width + x) * 4;

        float r = (buffer.get(pixelIndex) & 0xFF) / 255.0f;
        float g = (buffer.get(pixelIndex + 1) & 0xFF) / 255.0f;
        float b = (buffer.get(pixelIndex + 2) & 0xFF) / 255.0f;
        float a = (buffer.get(pixelIndex + 3) & 0xFF) / 255.0f;

        return new float[]{r, g, b, a};
    }

    /**
     * Logs the color at a given texture coordinate on a PNG texture.
     *
     * @param textureID The OpenGL texture ID.
     * @param texCoord  The texture coordinate (normalized between 0 and 1).
     */
    public static void logColorAtTexCoord(int textureID, Vector2f texCoord) {
        glBindTexture(GL_TEXTURE_2D, textureID);

        // Get texture dimensions
        int width = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH);
        int height = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT);

        if (width == 0 || height == 0) {
            LMHelperClass.logInfoMessage("Failed to retrieve texture dimensions for texture ID: " + textureID);
            return;
        }

        // Read the texture data
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4); // RGBA = 4 bytes per pixel
        glGetTexImage(GL_TEXTURE_2D, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

        // Calculate pixel position
        int x = (int) (texCoord.x * (width - 1));
        int y = (int) (texCoord.y * (height - 1));
        int pixelIndex = (y * width + x) * 4;

        // Retrieve color values
        float r = (buffer.get(pixelIndex) & 0xFF) / 255.0f;
        float g = (buffer.get(pixelIndex + 1) & 0xFF) / 255.0f;
        float b = (buffer.get(pixelIndex + 2) & 0xFF) / 255.0f;
        float a = (buffer.get(pixelIndex + 3) & 0xFF) / 255.0f;

        // Determine general color name
        String colorName = determineColorName(r, g, b);

        // Log the color
        LMHelperClass.logInfoMessage(String.format(
                "Color at texCoord (%.2f, %.2f): R=%.2f, G=%.2f, B=%.2f, A=%.2f - %s",
                texCoord.x, texCoord.y, r, g, b, a, colorName
        ));
    }

    /**
     * Determines a general color name based on RGB values.
     *
     * @param r Red component (0.0 - 1.0).
     * @param g Green component (0.0 - 1.0).
     * @param b Blue component (0.0 - 1.0).
     * @return A general color name.
     */
    private static String determineColorName(float r, float g, float b) {
        if (r > 0.8f && g < 0.2f && b < 0.2f) {
            return "Red";
        } else if (g > 0.8f && r < 0.2f && b < 0.2f) {
            return "Green";
        } else if (b > 0.8f && r < 0.2f && g < 0.2f) {
            return "Blue";
        } else if (r > 0.8f && g > 0.8f && b < 0.2f) {
            return "Yellow";
        } else if (r > 0.8f && b > 0.8f && g < 0.2f) {
            return "Magenta";
        } else if (g > 0.8f && b > 0.8f && r < 0.2f) {
            return "Cyan";
        } else if (r > 0.6f && g > 0.6f && b > 0.6f) {
            return "White";
        } else if (r < 0.2f && g < 0.2f && b < 0.2f) {
            return "Black";
        } else {
            return "Unknown";
        }
    }


    public static void renderLegacyModel(WorldBuffer buffer, CreatureObjModelOld objModel, Vector4f color, BaseCreatureEntity entity) {
        PoseStack poseStack = new PoseStack();
        for (ObjPart part : objModel.wavefrontParts) {
            if (part.mesh == null) {
                continue;
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


    public static Vector4f transformVertex(PoseStack poseStack, Vec3 vertexPos) {
        Matrix4f transformMatrix = poseStack.last().pose();
        Vector4f transformedPos = new Vector4f((float) vertexPos.x(), (float) vertexPos.y(), (float) vertexPos.z(), 1.0F);
        transformedPos.mul(transformMatrix);
        return transformedPos;
    }


    public static Vec3 rotateVertex(Vec3 pos, float yaw, float pitch) {
        double x = pos.x();
        double y = pos.y();
        double z = pos.z();

        double xzLen = Math.sqrt(x * x + z * z);
        double yawAngle = Math.atan2(z, x) + yaw;
        x = xzLen * Math.cos(yawAngle);
        z = xzLen * Math.sin(yawAngle);

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
