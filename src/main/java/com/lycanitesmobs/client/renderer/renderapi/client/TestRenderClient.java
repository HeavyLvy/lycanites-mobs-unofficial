package com.lycanitesmobs.client.renderer.renderapi.client;


import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.renderer.renderapi.client.render.RenderHelper;
import com.lycanitesmobs.client.renderer.renderapi.client.render.buffer.WorldBuffer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;

@Mod.EventBusSubscriber(modid = LycanitesMobs.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TestRenderClient {

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
