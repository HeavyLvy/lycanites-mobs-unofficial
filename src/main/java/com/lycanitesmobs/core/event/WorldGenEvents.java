package com.lycanitesmobs.core.event;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.manager.WorldGenManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(modid = LycanitesMobs.MODID)
public class WorldGenEvents {
    @SubscribeEvent
    public void registerFeatures(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.FEATURES,
                helper -> {
                    helper.register(new ResourceLocation(LycanitesMobs.modInfo.modid, "chunkspawn"), WorldGenManager.getInstance().chunkSpawnFeature);
                    helper.register(new ResourceLocation(LycanitesMobs.modInfo.modid, "dungeon"), WorldGenManager.getInstance().dungeonFeature);
                }
        );
    }
}
