package com.lycanitesmobs.client.renderer;

import com.lycanitesmobs.LycanitesMobs;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientModEvents {
    public static ShaderInstance POS_TEX_NORMAL_SHADER;

    @SubscribeEvent
    public static void onRegisterShaders(net.minecraftforge.client.event.RegisterShadersEvent event) throws java.io.IOException {
        var rp = event.getResourceProvider();
        var shader = new ShaderInstance(rp, new net.minecraft.resources.ResourceLocation(LycanitesMobs.MODID, "pos_tex_normal"), CustomRenderStates.POS_TEX_NORMAL);
        event.registerShader(shader, s -> POS_TEX_NORMAL_SHADER = s);
    }
}