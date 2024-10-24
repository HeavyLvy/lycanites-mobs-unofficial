package com.lycanitesmobs.client.renderer.renderapi.client.render;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.AnimationPart;
import com.lycanitesmobs.client.model.CreatureObjModel;
import com.lycanitesmobs.client.model.CreatureObjModelOld;
import com.lycanitesmobs.client.model.creature.ModelAbaia;
import com.lycanitesmobs.client.model.creature.ModelBobeko;
import com.lycanitesmobs.client.obj.ObjPart;
import com.lycanitesmobs.client.renderer.renderapi.client.render.buffer.BufferManager;
import com.lycanitesmobs.client.renderer.renderapi.client.render.buffer.WorldBuffer;
import com.lycanitesmobs.client.renderer.renderapi.client.render.shader.ShaderManager;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.util.ContextUtils;
import com.lycanitesmobs.core.util.LMHelperClass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
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

    @Mod.EventBusSubscriber(modid = LycanitesMobs.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeAPIEvents {
        @SubscribeEvent
        public static void onRenderLevel(RenderLevelStageEvent event) {
            if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;

            var player = Minecraft.getInstance().player;
            if (player == null) {
                return;
            }
            if (player.getMainHandItem().getItem() != Items.ANVIL) {
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
                        RenderHelper.renderModel(buffer, entityMap.getValue(), color, baseCreature);
                        entityMap.getValue().clearAnimationFrames();

                    }
                }
            }

            // Render legacy models
    /*
    Iterator<Map.Entry<Integer, CreatureObjModelOld>> legacyIterator = entitiesLegacy.entrySet().iterator();
    while (legacyIterator.hasNext()) {
        Map.Entry<Integer, CreatureObjModelOld> entityMap = legacyIterator.next();
        Entity entity = player.level().getEntity(entityMap.getKey());
        if (entity == null) {
            legacyIterator.remove();
            continue;
        }
        if (entity instanceof BaseCreatureEntity baseCreature) {
            if (frustum.isVisible(entity.getBoundingBox().inflate(extend))) {
                var context = animationContexts.get(entityMap.getKey());
                entityMap.getValue().generateAnimationFrames(context.entity, context.time, context.distance, context.loop, context.lookY, context.lookX, context.scale, context.brightness);
                RenderHelper.renderLegacyModel(buffer, entityMap.getValue(), color, baseCreature);
                entityMap.getValue().clearAnimationFrames();
            }
        }
    }
    */

            RenderHelper.endTri(buffer);
        }

    }
}
