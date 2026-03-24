package com.lycanitesmobs.client.renderer.util;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

import static com.lycanitesmobs.client.renderer.util.CustomRenderStates.POS_TEX_NORMAL_SHADER;

public class VBOBatcher {

    public static class VBODrawCommand {
        private final VertexBuffer vbo;
        private final int vertexCount;
        private final VertexFormat format;
        private final Matrix4f modelMatrix;
        private final ResourceLocation texLocation;

        public static int minimumBrightness = 0;

        private float red = 1.0F, green = 1.0F, blue = 1.0F, alpha = 1.0F;
        private float textureOffsetX, textureOffsetY;
        private float overlayOffsetX, overlayOffsetY;
        private float lightOffsetX, lightOffsetY;
        private float baseR = 1.0F, baseG = 1.0F, baseB = 1.0F, baseRange = 0.1F;
        private float variantR = 1.0F, variantG = 1.0F, variantB = 1.0F, variantAmount = 0.0F;
        private float tintR = 1.0F;
        private float tintG = 1.0F;
        private float tintB = 1.0F;
        private float tintAmount = 0.0F;

        /**
         * Keeps the old ctor shape: (vbo, vertexCount, format, matrix, tex)
         */
        public VBODrawCommand(VertexBuffer vbo,
                              int vertexCount,
                              VertexFormat format,
                              Matrix4f matrix,
                              ResourceLocation texLocation) {
            this.vbo = vbo;
            this.vertexCount = vertexCount;
            this.format = format;
            this.modelMatrix = new Matrix4f(matrix);
            this.texLocation = texLocation;
        }

        private float alphaCutoff = 0.0F;

        public VBODrawCommand setAlphaCutoff(float cutoff) {
            this.alphaCutoff = cutoff;
            return this;
        }

        public VBODrawCommand setColor(Vector4f color) {
            return setColor(color.x, color.y, color.z, color.w);
        }

        public VBODrawCommand setVariantPalette(
                float baseR, float baseG, float baseB, float baseRange,
                float variantR, float variantG, float variantB, float variantAmount) {
            this.baseR = baseR;
            this.baseG = baseG;
            this.baseB = baseB;
            this.baseRange = baseRange;
            this.variantR = variantR;
            this.variantG = variantG;
            this.variantB = variantB;
            this.variantAmount = variantAmount;
            return this;
        }

        public VBODrawCommand setVariantTint(float r, float g, float b, float amount) {
            this.tintR = r;
            this.tintG = g;
            this.tintB = b;
            this.tintAmount = amount;
            return this;
        }

        public VBODrawCommand setRecolorBand(float r, float g, float b, float range) {
            this.baseR = r;
            this.baseG = g;
            this.baseB = b;
            this.baseRange = range;
            return this;
        }

        public VBODrawCommand setColor(float r, float g, float b, float a) {
            this.red = r;
            this.green = g;
            this.blue = b;
            this.alpha = a;
            return this;
        }

        public VBODrawCommand setTextureOffset(Vector2f offset) {
            return setTextureOffset(offset.x * 0.01F, -offset.y * 0.01F);
        }

        public VBODrawCommand setTextureOffset(float u, float v) {
            this.textureOffsetX = u;
            this.textureOffsetY = v;
            return this;
        }

        public VBODrawCommand setOverlayOffset(float white, boolean red) {
            return setOverlayOffset(OverlayTexture.u(white), OverlayTexture.v(red));
        }

        public VBODrawCommand setOverlayOffset(float u, float v) {
            this.overlayOffsetX = u;
            this.overlayOffsetY = v;
            return this;
        }

        public VBODrawCommand setLightOffset(int packed) {
            return setLightOffset(LightTexture.block(packed) << 4, LightTexture.sky(packed) << 4);
        }

        public VBODrawCommand setLightOffset(float u, float v) {
            this.lightOffsetX = u;
            this.lightOffsetY = v;
            return this;
        }

        private static boolean loggedOnce;

        private static void logShaderMissingOnce(VertexFormat fmt) {
            if (!loggedOnce) {
                loggedOnce = true;
                org.apache.logging.log4j.LogManager.getLogger().warn(
                        "[Lycanites Mobs] No shader bound for format '{}'. " +
                                "Bind a shader in RenderType or switch to a vanilla format (e.g. POSITION_TEX_COLOR_NORMAL). Skipping draw to avoid crash.",
                        fmt);
            }
        }

        private static java.util.function.Supplier<ShaderInstance> pickShaderForFormat(VertexFormat fmt) {
            if (fmt == DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL)
                return GameRenderer::getPositionTexColorNormalShader;
            if (fmt == DefaultVertexFormat.POSITION_COLOR)
                return GameRenderer::getPositionColorShader;
            if (fmt == DefaultVertexFormat.POSITION_TEX)
                return GameRenderer::getPositionTexShader;
            if (fmt == DefaultVertexFormat.POSITION_COLOR_TEX)
                return GameRenderer::getPositionColorTexShader;
            if (fmt == DefaultVertexFormat.POSITION_TEX_COLOR)
                return GameRenderer::getPositionTexColorShader;
            if (fmt == DefaultVertexFormat.POSITION)
                return GameRenderer::getPositionShader;
            if (fmt == CustomRenderStates.POS_TEX_NORMAL
                    && POS_TEX_NORMAL_SHADER != null) {
                return () -> POS_TEX_NORMAL_SHADER;
            }
            return null;
        }

        public void draw() {
            if (vbo == null)
                return;

            RenderSystem.setShaderTexture(0, texLocation);
            RenderSystem.setShaderColor(corrected(red), corrected(green), corrected(blue), corrected(alpha));

            ShaderInstance shader = RenderSystem.getShader();

            if (format == CustomRenderStates.POS_TEX_NORMAL && POS_TEX_NORMAL_SHADER != null) {
                if (shader != POS_TEX_NORMAL_SHADER) {
                    RenderSystem.setShader(() -> POS_TEX_NORMAL_SHADER);
                    shader = POS_TEX_NORMAL_SHADER;
                }
            } else if (shader == null) {
                var pick = pickShaderForFormat(format);
                if (pick != null) {
                    RenderSystem.setShader(pick);
                    shader = pick.get();
                }
            }

            if (shader == null) {
                logShaderMissingOnce(format);
                return;
            }

            var uv = shader.getUniform("UvOffset");
            if (uv != null)
                uv.set(textureOffsetX, textureOffsetY);

            var overlay = shader.getUniform("OverlayUv");
            if (overlay != null)
                overlay.set(overlayOffsetX, overlayOffsetY);

            var light = shader.getUniform("LightUv");
            if (light != null) {
                float u = lightOffsetX;
                float v = lightOffsetY;

                Minecraft mc = Minecraft.getInstance();
                Entity cam = mc.getCameraEntity();

                float nv = 0.0F;

                if (cam instanceof LivingEntity le && mc.level != null
                        && (le.hasEffect(MobEffects.NIGHT_VISION) || le.hasEffect(MobEffects.CONDUIT_POWER))) {
                    nv = GameRenderer.getNightVisionScale(le, mc.getFrameTime());
                }

                if (nv > 0.0F) {
                    u = Mth.lerp(nv, u, 240.0F);
                    v = Mth.lerp(nv, v, 240.0F);
                }

                float minU = LightTexture.block(minimumBrightness) << 4;
                float minV = LightTexture.sky(minimumBrightness) << 4;
                u = Math.max(u, minU);
                v = Math.max(v, minV);

                light.set(u, v);
            }

            var cutoff = shader.getUniform("AlphaCutoff");
            if (cutoff != null)
                cutoff.set(alphaCutoff);

            var baseColor = shader.getUniform("BaseColor");
            if (baseColor != null)
                baseColor.set(baseR, baseG, baseB);

            var baseRangeUni = shader.getUniform("BaseRange");
            if (baseRangeUni != null)
                baseRangeUni.set(baseRange);

            var tintColor = shader.getUniform("VariantColor");
            if (tintColor != null)
                tintColor.set(tintR, tintG, tintB);

            var tintAmt = shader.getUniform("VariantAmount");
            if (tintAmt != null)
                tintAmt.set(tintAmount);

            var modelView = new org.joml.Matrix4f(RenderSystem.getModelViewMatrix()).mul(this.modelMatrix);
            var projection = RenderSystem.getProjectionMatrix();

            var sunDir = shader.getUniform("SunDirectionVS");
            if (sunDir != null)
                sunDir.set(0.0F, 1.0F, 0.0F);

            var specColor = shader.getUniform("SpecularColor");
            if (specColor != null)
                specColor.set(1.0F, 1.0F, 1.0F);

            var specIntensity = shader.getUniform("SpecularIntensity");
            if (specIntensity != null)
                specIntensity.set(0.15F);

            var specPower = shader.getUniform("SpecularPower");
            if (specPower != null)
                specPower.set(32.0F);

            vbo.bind();
            vbo.drawWithShader(modelView, projection, shader);
            VertexBuffer.unbind();

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        private static float corrected(float f) {
            return ((int) (f * 255.0F) & 0xFF) / 255.0F;
        }
    }

    private static final VBOBatcher INSTANCE = new VBOBatcher();
    private final Map<RenderType, List<VBODrawCommand>> batchedDrawCommands = new LinkedHashMap<>();

    public static VBOBatcher getInstance() {
        return INSTANCE;
    }

    public void queue(RenderType renderType, VBODrawCommand drawCommand) {
        batchedDrawCommands.computeIfAbsent(renderType, k -> new LinkedList<>()).add(drawCommand);
    }

    public void endBatches() {
        batchedDrawCommands.forEach((renderType, drawCommands) -> {
            if (drawCommands.isEmpty())
                return;

            renderType.setupRenderState();

            for (VBODrawCommand cmd : drawCommands) {
                cmd.draw();
            }

            renderType.clearRenderState();
            drawCommands.clear();
        });

        batchedDrawCommands.clear();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

}
