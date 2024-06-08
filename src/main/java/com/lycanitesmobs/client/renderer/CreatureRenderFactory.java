package com.lycanitesmobs.client.renderer;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.info.CreatureInfo;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class CreatureRenderFactory<T extends BaseCreatureEntity>{
    protected CreatureInfo creatureInfo;

    public CreatureRenderFactory(CreatureInfo creatureInfo) {
        this.creatureInfo = creatureInfo;
    }

    public EntityRenderer<? super T> createRenderFor(EntityRendererProvider.Context manager) {
        try {
            return new CreatureRenderer<>(this.creatureInfo.getName(), manager, (float) this.creatureInfo.width / 2);
        }
        catch (Exception e) {
            LycanitesMobs.logWarning("", "An exception occurred when creating a creature renderer:");
            e.printStackTrace();
        }
        return null;
    }

}
