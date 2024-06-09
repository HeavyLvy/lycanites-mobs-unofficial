package com.lycanitesmobs.client.renderer.layer.specific;

import com.lycanitesmobs.client.TextureManager;
import com.lycanitesmobs.client.renderer.CreatureRenderer;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureBase;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector2f;
import org.joml.Vector4f;

@OnlyIn(Dist.CLIENT)
public class LayerZephyr extends LayerCreatureBase {

    // ==================================================
    //                   Constructor
    // ==================================================
    public LayerZephyr(CreatureRenderer renderer) {
        super(renderer);
    }


    // ==================================================
    //                      Visuals
    // ==================================================
    @Override
    public boolean canRenderPart(String partName, BaseCreatureEntity entity, boolean trophy) {
        return partName.contains("ribbon");
    }

    @Override
    public ResourceLocation getLayerTexture(BaseCreatureEntity entity) {
		String textureName = entity.getTextureName();
		if(entity.getVariant() != null) {
			textureName += "_" + entity.getVariant().color;
		}
		textureName += "_ribbon";
		if(TextureManager.getTexture(textureName) == null)
			TextureManager.addTexture(textureName, entity.creatureInfo.modInfo, "textures/entity/" + textureName.toLowerCase() + ".png");
		return TextureManager.getTexture(textureName);
    }

	@Override
	public Vector2f getTextureOffset(String partName, BaseCreatureEntity entity, boolean trophy, float loop) {
		return new Vector2f(-loop * 25, 0);
	}

	@Override
	public Vector4f getPartColor(String partName, BaseCreatureEntity entity, boolean trophy) {
		return new Vector4f(1, 1, 1, 0.75f);
	}
}
