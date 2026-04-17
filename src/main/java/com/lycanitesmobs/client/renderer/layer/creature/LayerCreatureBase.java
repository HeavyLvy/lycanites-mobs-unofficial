package com.lycanitesmobs.client.renderer.layer.creature;

import com.lycanitesmobs.client.model.creature.base.CreatureModel;
import com.lycanitesmobs.client.renderer.entity.creature.CreatureRenderer;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector2f;
import org.joml.Vector4f;

@OnlyIn(Dist.CLIENT)
public class LayerCreatureBase<T extends BaseCreatureEntity> extends RenderLayer<T, CreatureModel<T>> {
    public CreatureRenderer renderer;
    public String name;
    public boolean glow = false;
    public int blending = 0;

    /**
     * Constructor
     * @param renderer The renderer that is rendering this layer.
     */
    public LayerCreatureBase(CreatureRenderer renderer) {
        super(renderer);
        this.renderer = renderer;
        this.name = "Layer";
    }

    /**
     * The default render call function, this is not used in favor of a different custom method.
     */
    @Override
    public void render(PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int ticks, BaseCreatureEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        // This isn't used by the custom renderer.
    }

    /**
     * Returns if this layer should be rendered.
     * @param entity The entity being rendered.
     * @param scale The scale the entity is rendered at.
     * @return True if this layer should render.
     */
    public boolean canRenderLayer(BaseCreatureEntity entity, float scale) {
        if(entity == null)
            return false;
        if(entity.isInvisible() && entity.isInvisibleTo(Minecraft.getInstance().player))
            return false;
        return true;
    }

    /**
     * Gets the texture that this layer should use.
     * @param entity The entity to get the texture for.
     * @return The layer specific texture or null if the base texture should be used.
     */
    public ResourceLocation getLayerTexture(BaseCreatureEntity entity) {
        return null;
    }

    /**
     * Returns if this layer can render the provided model part.
     * @param partName The name of the model part.
     * @param entity The entity to render.
     * @param trophy If true, the entity is being rendered as a trophy block, etc.
     * @return True if this layer can render the part.
     */
    public boolean canRenderPart(String partName, BaseCreatureEntity entity, boolean trophy) {
        if(this.renderer.getMainModel() != null) {
            this.renderer.getMainModel().canBaseRenderPart(partName, entity, trophy);
        }
        return true;
    }

    /**
     *  Returns the color that this layer should render the provided part at.
     * @param partName The name of the model part.
     * @param entity The entity to render.
     * @param trophy If true, the entity is being rendered as a trophy block, etc.
     * @return The part color.
     */
    public Vector4f getPartColor(String partName, BaseCreatureEntity entity, boolean trophy) {
        return new Vector4f(1, 1, 1, 1);
    }

    /**
     *  Returns the texture offset that this layer should render the provided part at.
     * @param partName The name of the model part.
     * @param entity The entity to render.
     * @param trophy If true, the entity is being rendered as a trophy block, etc.
     * @return The part texture offset.
     */
    public Vector2f getTextureOffset(String partName, BaseCreatureEntity entity, boolean trophy, float loop) {
        return new Vector2f(0, 0);
    }

    /**
     * Returns the brightness that this layer should use.
     * @param partName The name of the model part.
     * @param entity The entity to render.
     * @param brightness The base brightness.
     * @return The part brightness.
     */
    public int getBrightness(String partName, BaseCreatureEntity entity, int brightness) {
        return brightness;
    }

    /**
     * Returns the lightmap-response range scale this layer should use. 1.0 = unchanged,
     * &lt;1.0 compresses the day/night swing toward the midpoint. Default passes through the
     * provided default (which is the entity body value). Override in a subclass to opt a
     * layer's parts out of body tone-mapping — e.g. equipment layers return 1.0F so they
     * aren't flattened by the creature-wide compression.
     * @param partName The name of the model part.
     * @param entity The entity to render.
     * @param defaultScale The base value being applied to the creature body.
     * @return The light range scale this layer wants for this part.
     */
    public float getLightRangeScale(String partName, BaseCreatureEntity entity, float defaultScale) {
        return defaultScale;
    }

    /**
     * Returns the highlight soft-clip strength this layer should use. 0.0 = off (hard clip),
     * higher rolls bright outputs down more. Default passes through the provided default
     * (which is the entity body value). Override in a subclass to opt out — e.g. equipment
     * layers return 0.0F so they aren't desaturated by the creature-wide highlight roll-off.
     * @param partName The name of the model part.
     * @param entity The entity to render.
     * @param defaultStrength The base value being applied to the creature body.
     * @return The highlight compression this layer wants for this part.
     */
    public float getHighlightCompression(String partName, BaseCreatureEntity entity, float defaultStrength) {
        return defaultStrength;
    }

    /**
     * Returns the blending type that this layer should use, see CustomRenderStates.BLEND.
     * @param entity The entity to render.
     * @return The part blending type.
     */
    public int getBlending(BaseCreatureEntity entity) {
        return this.blending;
    }

    /**
     * Returns if this layer should glow where it ignores shading.
     * @param entity The entity to render.
     * @return True for glowing (shadeless rendering).
     */
    public boolean getGlow(BaseCreatureEntity entity) {
        return this.glow;
    }
}
