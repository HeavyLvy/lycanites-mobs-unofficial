package com.lycanitesmobs.client.renderer.renderapi.client.render;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.CreatureObjModel;
import com.lycanitesmobs.client.model.CreatureObjModelOld;
import com.lycanitesmobs.client.model.creature.TestModel;
import com.lycanitesmobs.client.renderer.renderapi.client.render.buffer.BufferManager;
import com.lycanitesmobs.client.renderer.renderapi.client.render.buffer.WorldBuffer;
import com.lycanitesmobs.client.renderer.renderapi.client.render.shader.ShaderManager;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.util.ContextUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.lycanitesmobs.client.renderer.renderapi.client.render.RenderHelper.precomputeColors;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class RenderAPIEvents {
    public static final Map<Integer, ContextUtils.AnimationContext> animationContexts = new HashMap<>();
    public static final Map<Integer, CreatureObjModel> entities = new HashMap<>();
    public static final Map<Integer, CreatureObjModelOld> entitiesLegacy = new HashMap<>();

    @Mod.EventBusSubscriber(modid = LycanitesMobs.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModAPIEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Schedule shader loading on the render thread
            RenderSystem.recordRenderCall(() -> {
                //Shader setup
                ShaderManager.loadShaders();
                BufferManager.vao = glGenVertexArrays();
                BufferManager.vbo = glGenBuffers();
            });
        }
    }

    public static CreatureObjModel testModel = new TestModel();

    @Mod.EventBusSubscriber(modid = LycanitesMobs.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeAPIEvents {
        @SubscribeEvent
        public static void onRenderLevel(RenderLevelStageEvent event) {
            if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;

            var player = Minecraft.getInstance().player;
            if (player == null) {
                return;
            }
            var poseStack = event.getPoseStack();
            Matrix4f projectionMatrix = event.getProjectionMatrix();
            Matrix4f viewMatrix = event.getPoseStack().last().pose();
            WorldBuffer buffer = RenderHelper.startTri(projectionMatrix, viewMatrix);
            Frustum frustum = event.getFrustum();
            frustum.prepare(player.getX(), player.getY(), player.getZ());
            Vector4f color = new Vector4f(1.0f, 1.0f, 1.0f, 0.5f);
            final double extend = 25.0;


            /*if (player.getMainHandItem().getItem() == Items.APPLE) {
                // This is the test method for the test model
                RenderHelper.renderModel(buffer, testModel, poseStack);
            }*/
            if (player.getMainHandItem().getItem() != Items.ANVIL) {
                return;
            }

            Iterator<Map.Entry<Integer, CreatureObjModel>> iterator = entities.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, CreatureObjModel> entityMap = iterator.next();
                Entity entity = player.level().getEntity(entityMap.getKey());
                var context = animationContexts.get(entityMap.getKey());

                if (entity == null) {
                    iterator.remove();
                    animationContexts.remove(entityMap.getKey());
                    continue;
                }

                if (entity instanceof BaseCreatureEntity baseCreature) {
                    if (frustum.isVisible(entity.getBoundingBox().inflate(extend))) {
                        entityMap.getValue().generateAnimationFrames(context.entity, context.time, context.distance, context.loop, context.lookY, context.lookX, context.scale, context.brightness);
                        var objModel = entityMap.getValue();
                        if (!objModel.areColorsPrecomputed()) {
                            ResourceLocation texture = context.texture;
                            int textureID = getTextureId(texture);
                            precomputeColors(objModel, textureID);
                            objModel.setColorsPrecomputed(true);
                        }
                        RenderHelper.renderModel(buffer, entityMap.getValue(), baseCreature);
                        entityMap.getValue().clearAnimationFrames();
                    }
                }
            }
            RenderHelper.endTri(buffer);
        }

    }

    public static int getTextureId(ResourceLocation texture) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        AbstractTexture abstractTexture = textureManager.getTexture(texture);
        if (abstractTexture == null) {
            textureManager.register(texture, new SimpleTexture(texture));
            abstractTexture = textureManager.getTexture(texture);
        }
        return abstractTexture.getId();
    }
}
