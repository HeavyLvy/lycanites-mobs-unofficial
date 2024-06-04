package com.lycanitesmobs.client.renderer;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class RenderFactoryNone<T extends Entity> extends EntityRenderer<T> {
    protected Class entityClass;

    public RenderFactoryNone(Class entityClass) {
        super(entityClass);
        this.entityClass = entityClass;
    }

    @Override
    public EntityRenderer<? super T> getRenderer(EntityRendererProvider.Context manager) {
        return new NoneRenderer(manager);
    }

    @Override
    public ResourceLocation getTextureLocation(Entity p_114482_) {
        return null;
    }
}
