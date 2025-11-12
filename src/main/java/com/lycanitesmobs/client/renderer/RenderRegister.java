package com.lycanitesmobs.client.renderer;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.client.model.blocky.Balayang;
import com.lycanitesmobs.client.model.blocky.GrueModel;
import com.lycanitesmobs.client.model.blocky.NymphModel;
import com.lycanitesmobs.client.model.blocky.WraithModel;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.CustomProjectileEntity;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureKnowledge;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.ItemManager;
import com.lycanitesmobs.core.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.info.projectile.ProjectileManager;
import com.lycanitesmobs.core.util.LMHelperClass;
import com.mojang.logging.LogUtils;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Objects;

/*import static com.lycanitesmobs.LycanitesMobs.ABIA;
import static com.lycanitesmobs.LycanitesMobs.getAbia;*/

@Mod.EventBusSubscriber(modid = LycanitesMobs.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RenderRegister {

    public void registerModelLoaders() {
        //ModelLoaderRegistry.registerLoader(new ResourceLocation(LycanitesMobs.MODID), new ModelLoader()); Not needed.
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(RenderRegister::registerEntityRenders);
        modBus.addListener(RenderRegister::registerLayers);
    }

    public static void init() {


    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(Balayang.LAYER_LOCATION, Balayang::createBodyLayer);
        event.registerLayerDefinition(NymphModel.LAYER_LOCATION, NymphModel::createBodyLayer);
        event.registerLayerDefinition(GrueModel.LAYER_LOCATION, GrueModel::createBodyLayer);
        event.registerLayerDefinition(WraithModel.LAYER_LOCATION, WraithModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
        LycanitesMobs.LOGGER.info("LycanitesMobs: Registering Renderers!" + CreatureManager.getInstance().creatures.size());
        // Creatures:
        for (CreatureInfo creatureInfo : CreatureManager.getInstance().creatures.values()) {
            //LMHelperClass.logInfoMessage("CI Name: " + creatureInfo.getName());
            if (creatureInfo.dummy) {
                event.registerEntityRenderer(LMHelperClass.cast(creatureInfo.getEntityType()), (dispatcher) -> new NoneRenderer<BaseProjectileEntity>(dispatcher));
            } else if (Objects.equals(creatureInfo.getName(), "nymph")) {
                event.registerEntityRenderer(LMHelperClass.cast(creatureInfo.getEntityType()), context -> new BlockyRenderer(context, new ResourceLocation(LycanitesMobs.MODID, "textures/entity/blocky/nymph.png"), new NymphModel<>(context.bakeLayer(NymphModel.LAYER_LOCATION))));
                continue;
            } else if (Objects.equals(creatureInfo.getName(), "grue")) {
                LogUtils.getLogger().info("Registering renderer for: " + creatureInfo.getName());
                event.registerEntityRenderer(LMHelperClass.cast(creatureInfo.getEntityType()), context -> new BlockyRenderer(context, new ResourceLocation(LycanitesMobs.MODID, "textures/entity/blocky/grue.png"), new GrueModel<>(context.bakeLayer(GrueModel.LAYER_LOCATION))));
                continue;
            } else if (Objects.equals(creatureInfo.getName(), "wraith")) {
                LogUtils.getLogger().info("Registering renderer for: " + creatureInfo.getName());
                event.registerEntityRenderer(LMHelperClass.cast(creatureInfo.getEntityType()), context -> new BlockyRenderer(context, new ResourceLocation(LycanitesMobs.MODID, "textures/entity/blocky/wraith.png"), new WraithModel<>(context.bakeLayer(WraithModel.LAYER_LOCATION))));
                continue;
            }
            event.registerEntityRenderer(LMHelperClass.cast(creatureInfo.getEntityType()), (dispatcher) -> new CreatureRenderer<>(creatureInfo.getName(), dispatcher, (float) creatureInfo.width / 2));
        }

        // Projectiles:
        for (ProjectileInfo projectileInfo : ProjectileManager.getInstance().projectiles.values()) {
            if (projectileInfo.modelClassName != null) {
                //Here we retain the factory since we are choosing the renderer based off the projectileInfo. Later on we can get rid of this and do individual renderers.
                event.registerEntityRenderer(LMHelperClass.cast(projectileInfo.getEntityType()), (dispatcher) -> new ProjectileRenderFactory<CustomProjectileEntity>(projectileInfo).createRenderFor(dispatcher));
            } else {
                event.registerEntityRenderer(LMHelperClass.cast(projectileInfo.getEntityType()), (dispatcher) -> new ProjectileRenderFactory<CustomProjectileEntity>(projectileInfo).createRenderFor(dispatcher));
            }
        }

        // Old Sprite Projectiles:
        for (String projectileName : ProjectileManager.getInstance().oldSpriteProjectiles.keySet()) {
            Class projectileClass = ProjectileManager.getInstance().oldSpriteProjectiles.get(projectileName);
            event.registerEntityRenderer(LMHelperClass.cast(ProjectileManager.getInstance().oldProjectileTypes.get(projectileClass)), (dispatcher) -> new ProjectileRenderFactory<BaseProjectileEntity>(projectileName, projectileClass, false).createRenderFor(dispatcher));
        }

        // Old Model Projectiles:
        for (String projectileName : ProjectileManager.getInstance().oldModelProjectiles.keySet()) {
            Class projectileClass = ProjectileManager.getInstance().oldModelProjectiles.get(projectileName);
            event.registerEntityRenderer(ProjectileManager.getInstance().oldProjectileTypes.get(projectileClass), (dispatcher) -> new ProjectileRenderFactory<BaseProjectileEntity>(projectileName, projectileClass, true).createRenderFor(dispatcher));
        }

        // Special Entities:
        for (Class specialClass : ObjectManager.specialEntities.values()) {
            event.registerEntityRenderer(LMHelperClass.cast(ObjectManager.specialEntityTypes.get(specialClass)), (dispatcher) -> new NoneRenderer<BaseProjectileEntity>(dispatcher));
        }

    }

    public void registerBlockRenderLayers() {
        if (true) return;
        for (Block block : ItemManager.getInstance().cutoutBlocks) {
            ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutout());
        }
    }
}
