package com.lycanitesmobs.client.renderer;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;

public class RenderFactoryNone<T extends Entity> /*implements IRenderFactory*/ {
    /**
     * Render factory is no longer needed as the render provider directly accesses the renderer through
     * the render registry event
     */
   /* protected Class entityClass;

    public RenderFactoryNone(Class entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public EntityRenderer<? super T> createRenderFor(EntityRendererManager manager) {
        return new NoneRenderer(manager);
    }*/

}
