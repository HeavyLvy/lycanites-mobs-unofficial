package com.lycanitesmobs.client.event;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.client.model.blocky.BalayangBlockyModel;
import com.lycanitesmobs.client.model.blocky.GrueBlockyModel;
import com.lycanitesmobs.client.model.blocky.NymphBlockyModel;
import com.lycanitesmobs.client.model.blocky.WraithBlockyModel;
import com.lycanitesmobs.client.renderer.entity.creature.BlockyRenderer;
import com.lycanitesmobs.client.renderer.entity.creature.CreatureRenderer;
import com.lycanitesmobs.client.renderer.entity.projectile.ProjectileRenderFactory;
import com.lycanitesmobs.client.renderer.misc.NoneRenderer;
import com.lycanitesmobs.client.renderer.util.CustomRenderStates;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.projectile.generic.CustomProjectileEntity;
import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.manager.ItemManager;
import com.lycanitesmobs.core.data.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.data.info.projectile.ProjectileManager;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Objects;


@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientModEvents {
    public static ShaderInstance POS_TEX_NORMAL_SHADER;

    @SubscribeEvent
    public static void onRegisterShaders(net.minecraftforge.client.event.RegisterShadersEvent event) throws java.io.IOException {
        var rp = event.getResourceProvider();
        var shader = new ShaderInstance(rp, new net.minecraft.resources.ResourceLocation(LycanitesMobs.MODID, "pos_tex_normal"), CustomRenderStates.POS_TEX_NORMAL);
        event.registerShader(shader, s -> POS_TEX_NORMAL_SHADER = s);
    }

    public void registerModelLoaders() {
        //ModelLoaderRegistry.registerLoader(new ResourceLocation(LycanitesMobs.MODID), new ModelLoader()); Not needed.
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(ClientModEvents::registerEntityRenders);
        modBus.addListener(ClientModEvents::registerLayers);
    }

    public static void init() {


    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BalayangBlockyModel.LAYER_LOCATION, BalayangBlockyModel::createBodyLayer);
        event.registerLayerDefinition(NymphBlockyModel.LAYER_LOCATION, NymphBlockyModel::createBodyLayer);
        event.registerLayerDefinition(GrueBlockyModel.LAYER_LOCATION, GrueBlockyModel::createBodyLayer);
        event.registerLayerDefinition(WraithBlockyModel.LAYER_LOCATION, WraithBlockyModel::createBodyLayer);
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
                event.registerEntityRenderer(LMHelperClass.cast(creatureInfo.getEntityType()), context -> new BlockyRenderer(context, new ResourceLocation(LycanitesMobs.MODID, "textures/entity/blocky/nymph.png"), new NymphBlockyModel<>(context.bakeLayer(NymphBlockyModel.LAYER_LOCATION))));
                continue;
            } else if (Objects.equals(creatureInfo.getName(), "grue")) {
                LogUtils.getLogger().info("Registering renderer for: " + creatureInfo.getName());
                event.registerEntityRenderer(LMHelperClass.cast(creatureInfo.getEntityType()), context -> new BlockyRenderer(context, new ResourceLocation(LycanitesMobs.MODID, "textures/entity/blocky/grue.png"), new GrueBlockyModel<>(context.bakeLayer(GrueBlockyModel.LAYER_LOCATION))));
                continue;
            } else if (Objects.equals(creatureInfo.getName(), "wraith")) {
                LogUtils.getLogger().info("Registering renderer for: " + creatureInfo.getName());
                event.registerEntityRenderer(LMHelperClass.cast(creatureInfo.getEntityType()), context -> new BlockyRenderer(context, new ResourceLocation(LycanitesMobs.MODID, "textures/entity/blocky/wraith.png"), new WraithBlockyModel<>(context.bakeLayer(WraithBlockyModel.LAYER_LOCATION))));
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