package com.lycanitesmobs.client.renderer.renderapi.client.render;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.renderer.renderapi.client.render.buffer.BufferManager;
import com.lycanitesmobs.client.renderer.renderapi.client.render.shader.ShaderManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

@Mod.EventBusSubscriber(modid = LycanitesMobs.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {
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
