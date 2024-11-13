package com.lycanitesmobs.core.util;

import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.resources.ResourceLocation;

public class ContextUtils {
    public static class AnimationContext {
        public final BaseCreatureEntity entity;
        public final float time;
        public final float distance;
        public final float loop;
        public final float lookY;
        public final float lookX;
        public final float scale;
        public final int brightness;
        public final ResourceLocation texture;

        public AnimationContext(BaseCreatureEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale, int brightness, ResourceLocation texture) {
            this.entity = entity;
            this.time = time;
            this.distance = distance;
            this.loop = loop;
            this.lookY = lookY;
            this.lookX = lookX;
            this.scale = scale;
            this.brightness = brightness;
            this.texture = texture;
        }
    }
}
