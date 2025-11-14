package com.lycanitesmobs.core.worldgen.feature;

import com.lycanitesmobs.core.event.SpawnerEventListener;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class ChunkSpawnFeature extends Feature<NoneFeatureConfiguration> {
    public ChunkSpawnFeature(Codec<NoneFeatureConfiguration> configFactory) {
        super(configFactory);
    }

    @Override
    protected void setBlock(LevelWriter worldWriter, BlockPos pos, BlockState state) {
//		if(worldWriter instanceof World) {
//			SpawnerEventListener.getInstance().onChunkGenerate(((World) worldWriter).getDimensionKey().getLocation().toString(), new ChunkPos(pos));
//		}
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
        return false;
    }


    @Override
    public boolean place(NoneFeatureConfiguration p_225029_, WorldGenLevel reader, ChunkGenerator p_225031_, RandomSource p_225032_, BlockPos pos) {
        if (reader instanceof WorldGenRegion) {
            SpawnerEventListener.getInstance().onChunkGenerate(((WorldGenRegion) reader).getLevel().dimension().location().toString(), new ChunkPos(pos));
        }
        return true;
    }
}
