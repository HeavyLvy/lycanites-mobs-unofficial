package com.lycanitesmobs.core.worldgen;

import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WorldGenerator {
	/*@SubscribeEvent
	public void registerFeatures(BiomeLoadingEvent event) {
		WorldGenManager manager = WorldGenManager.getInstance();

		// Chunk Spawner:
		event.getGeneration().addFeature(GenerationStage.Decoration.TOP_LAYER_MODIFICATION.ordinal(),
				() -> manager.chunkSpawnFeature.configured(FeatureConfiguration.NONE).decorated(manager.alwaysPlacement.configured(NoPlacementConfig.INSTANCE)));

		// Dungeons:
		event.getGeneration().addFeature(GenerationStage.Decoration.TOP_LAYER_MODIFICATION.ordinal(),
				() -> manager.dungeonFeature.configured(FeatureConfiguration.NONE).decorated(manager.alwaysPlacement.configured(NoPlacementConfig.INSTANCE)));

		// Lakes:
		if (event.getCategory() == Biome.Category.SWAMP || event.getCategory() == Biome.Category.MUSHROOM) {
			event.getGeneration().addFeature(GenerationStage.Decoration.LAKES, manager.fluidConfiguredFeatures.get("poison_lake"));
			event.getGeneration().addFeature(GenerationStage.Decoration.LAKES, manager.fluidConfiguredFeatures.get("poison_spring"));
		}
		else if (event.getCategory() == Biome.Category.DESERT || event.getCategory() == Biome.Category.MESA || event.getCategory() == Biome.Category.THEEND) {
			event.getGeneration().addFeature(GenerationStage.Decoration.LAKES, manager.fluidConfiguredFeatures.get("acid_lake"));
			event.getGeneration().addFeature(GenerationStage.Decoration.LAKES, manager.fluidConfiguredFeatures.get("acid_spring"));
		}
		else if (event.getCategory() == Biome.Category.ICY || event.getCategory() == Biome.Category.EXTREME_HILLS) {
			event.getGeneration().addFeature(GenerationStage.Decoration.LAKES, manager.fluidConfiguredFeatures.get("ooze_lake"));
			event.getGeneration().addFeature(GenerationStage.Decoration.LAKES, manager.fluidConfiguredFeatures.get("ooze_spring"));
		}
		else if (event.getCategory() == Biome.Category.NETHER || event.getCategory() == Biome.Category.JUNGLE) {
			event.getGeneration().addFeature(GenerationStage.Decoration.LAKES, manager.fluidConfiguredFeatures.get("moglava_lake"));
			event.getGeneration().addFeature(GenerationStage.Decoration.LAKES, manager.fluidConfiguredFeatures.get("moglava_spring"));
		}
	}*/
}
