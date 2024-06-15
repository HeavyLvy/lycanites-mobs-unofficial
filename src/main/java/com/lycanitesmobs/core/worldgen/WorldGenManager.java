package com.lycanitesmobs.core.worldgen;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.block.fluid.BaseFluidBlock;
import com.lycanitesmobs.core.info.ItemManager;
import com.lycanitesmobs.core.util.Material;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Mod.EventBusSubscriber(modid = LycanitesMobs.MODID)
public class WorldGenManager {
	private static WorldGenManager INSTANCE;

	public static WorldGenManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new WorldGenManager();
		}
		return INSTANCE;
	}

	// Placements:
	public static AlwaysPlacement alwaysPlacement = new AlwaysPlacement();

	// Features:
	public ChunkSpawnFeature chunkSpawnFeature = new ChunkSpawnFeature(NoneFeatureConfiguration.CODEC);
	public DungeonFeature dungeonFeature = new DungeonFeature(NoneFeatureConfiguration.CODEC);
	public static Map<String, ConfiguredFeature<?, ?>> fluidConfiguredFeatures = new HashMap<>();


	/**
	 * Called on startup after all LM assets have been defined but before mob startup events.
	 */
	public static void startup() {
		if (ItemManager.getInstance().worldgenFluidBlocks == null) return;
		for (String fluidName : ItemManager.getInstance().worldgenFluidBlocks.keySet()) {
			BaseFluidBlock fluidBlock = ItemManager.getInstance().worldgenFluidBlocks.get(fluidName);

			List<Holder<Block>> list = new ArrayList<>();
			list.add(Holder.direct(Blocks.STONE));
			list.add(Holder.direct(Blocks.GRANITE));
			list.add(Holder.direct(Blocks.DIORITE));
			list.add(Holder.direct(Blocks.ANDESITE));
			LakeFeature.Configuration lakeConfiguration = new LakeFeature.Configuration(BlockStateProvider.simple(fluidBlock.defaultBlockState()), BlockStateProvider.simple(Blocks.BEDROCK.defaultBlockState()));
			SpringConfiguration springConfig = new SpringConfiguration(fluidBlock.getFluid().defaultFluidState(), true, 4, 1, BiomeModifierProvider.createDirectHolderSet(list));
			ConfiguredFeature<?, ?> lakeFeature = new ConfiguredFeature<>(Feature.LAKE,lakeConfiguration);
			ConfiguredFeature<?, ?> springFeature = new ConfiguredFeature<>(Feature.SPRING,springConfig);
			ResourceKey<ConfiguredFeature<?, ?>> lakeFeatureKey = FeatureUtils.createKey(fluidName + "_lake");
			ResourceKey<ConfiguredFeature<?, ?>> springFeatureKey = FeatureUtils.createKey(fluidName + "_spring");

			/*if (Material.WATER.contains(fluidBlock.defaultBlockState().getBlock())) {
				lakeFeature = new ConfiguredFeature<FeatureConfiguration, ?>(Feature.LAKE,lakeConfiguration);
				springFeature = new ConfiguredFeature<FeatureConfiguration, ?>(Feature.SPRING,springConfig);

			} else {
				lakeFeature = new ConfiguredFeature<FeatureConfiguration, ?>(Feature.LAKE,lakeConfiguration);
				springFeature = new ConfiguredFeature<FeatureConfiguration, ?>(Feature.SPRING,springConfig);
			}*/
			fluidConfiguredFeatures.put(fluidName + "_lake", lakeFeature);
			fluidConfiguredFeatures.put(fluidName + "_spring", springFeature);
		}
	}


	/**
	 * Called on startup after all LM assets have been defined but before mob startup events.
	 */
	/*public void startup() {
		for (String fluidName : ItemManager.getInstance().worldgenFluidBlocks.keySet()) {
			BaseFluidBlock fluidBlock = ItemManager.getInstance().worldgenFluidBlocks.get(fluidName);
			ConfiguredFeature<?, ?> lakeFeature = null;
			ConfiguredFeature<?, ?> springFeature = null;
			LiquidsConfig springConfig = new LiquidsConfig(fluidBlock.getFluid().defaultFluidState(), true, 4, 1, ImmutableSet.of(Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE));

			if (Material.WATER.contains(fluidBlock.defaultBlockState().getBlock())) {
				lakeFeature = Feature.LAKE.configuredCodec(new BlockStateConfiguration(fluidBlock.defaultBlockState())).decorated(PlacementModifierType.SURFACE_WATER_DEPTH_FILTER.configured(new ChanceConfig(40)));
				springFeature = Feature.SPRING.configuredCodec(springConfig).decorated(Placement.RANGE_BIASED.configured(new TopSolidRangeConfig(8, 8, 256))).squared().count(20);
			}
			else {
				lakeFeature = Feature.LAKE.configuredCodec(new BlockStateFeatureConfig(fluidBlock.defaultBlockState())).decorated(Placement.LAVA_LAKE.configured(new ChanceConfig(40)));
				springFeature = Feature.SPRING.configuredCodec(springConfig).decorated(Placement.RANGE_VERY_BIASED.configured(new TopSolidRangeConfig(8, 16, 256))).squared().count(20);
			}

			fluidConfiguredFeatures.put(fluidName + "_lake", lakeFeature);
			fluidConfiguredFeatures.put(fluidName + "_spring", springFeature);
		}
	}*/
	public static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURES = DeferredRegister.create(Registries.CONFIGURED_FEATURE, LycanitesMobs.MODID);

	private static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, LycanitesMobs.MODID);
	public static void registerPlacements(IEventBus event) {
		PLACEMENT_MODIFIERS.register(event);
		PLACEMENT_MODIFIERS.register("always", () -> alwaysPlacement);
		CONFIGURED_FEATURES.register(event);
		/*CONFIGURED_FEATURES.register("poison_lake",() -> fluidConfiguredFeatures.get("poison_lake"));CONFIGURED_FEATURES.register("poison_spring",() -> fluidConfiguredFeatures.get("poison_spring"));
		CONFIGURED_FEATURES.register("acid_lake",() -> fluidConfiguredFeatures.get("acid_lake"));
		CONFIGURED_FEATURES.register("acid_spring",() -> fluidConfiguredFeatures.get("acid_spring"));
		CONFIGURED_FEATURES.register("ooze_lake",() -> fluidConfiguredFeatures.get("ooze_lake"));
		CONFIGURED_FEATURES.register("ooze_spring",() -> fluidConfiguredFeatures.get("ooze_spring"));
		CONFIGURED_FEATURES.register("acid_lake",() -> fluidConfiguredFeatures.get("acid_lake"));
		CONFIGURED_FEATURES.register("moglava_lake",() -> fluidConfiguredFeatures.get("moglava_lake"));
		CONFIGURED_FEATURES.register("moglava_spring",() -> fluidConfiguredFeatures.get("moglava_spring"));*/
	}

	@SubscribeEvent
	public void registerFeatures(RegisterEvent event) {
		event.register(ForgeRegistries.Keys.FEATURES,
				helper -> {
					helper.register(new ResourceLocation(LycanitesMobs.modInfo.modid, "chunkspawn"), this.chunkSpawnFeature);
					helper.register(new ResourceLocation(LycanitesMobs.modInfo.modid, "dungeon"), this.dungeonFeature);
				}
		);
	}
}
