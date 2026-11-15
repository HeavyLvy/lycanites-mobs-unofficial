package com.lycanitesmobs.core.worldgen;

import com.mojang.serialization.MapCodec;
import com.lycanitesmobs.core.worldgen.config.NoPlacementConfig;
import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class AlwaysPlacement implements PlacementModifierType<NoPlacementConfig> {
    @Override
    public MapCodec<NoPlacementConfig> codec() {
        return NoPlacementConfig.CODEC;
    }
}
