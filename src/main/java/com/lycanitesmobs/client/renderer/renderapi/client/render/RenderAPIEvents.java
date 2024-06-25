package com.lycanitesmobs.client.renderer.renderapi.client.render;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.renderer.renderapi.client.render.buffer.BufferManager;
import com.lycanitesmobs.client.renderer.renderapi.client.render.buffer.WorldBuffer;
import com.lycanitesmobs.client.renderer.renderapi.client.render.shader.ShaderManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.awt.*;

import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class RenderAPIEvents {
    @Mod.EventBusSubscriber(modid = LycanitesMobs.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModAPIEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LycanitesMobs.logInfo("", "FML Client Setup event!");
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
    public class ForgeAPIEvents {

        @SubscribeEvent
        public static void onRenderLevel(RenderLevelStageEvent event) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) {return;}
            if (player.getMainHandItem().getItem() != Items.ANVIL) {return;}

            RenderContext context = new RenderContext(event.getLevelRenderer(),event.getCamera());
            WorldBuffer buffer = RenderHelper.startTri(context);
            RenderHelper.drawTri(buffer, 0, 100, 0, 0, 101, 0, 0, 100, 1, new Color(255, 255, 0, 100));
            RenderHelper.endTri(buffer);
        }

        public static class RenderContext {
            public final LevelRenderer levelRenderer;

            public final Camera camera;

            public RenderContext(LevelRenderer levelRenderer, Camera camera) {
                this.levelRenderer = levelRenderer;
                this.camera = camera;
            }
        }
    }
}
