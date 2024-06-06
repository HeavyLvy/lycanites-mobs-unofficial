package com.lycanitesmobs.core.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.stream.Stream;

public class NoFeatureConfig implements FeatureConfiguration {

    public static final NoFeatureConfig INSTANCE = new NoFeatureConfig();
    public static final Codec<NoFeatureConfig> CODEC = Codec.unit(() -> {
        return INSTANCE;
    });


    public NoFeatureConfig() {
    }

}