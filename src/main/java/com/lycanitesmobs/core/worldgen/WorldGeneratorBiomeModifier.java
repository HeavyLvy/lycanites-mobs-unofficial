package com.lycanitesmobs.core.worldgen;

import com.lycanitesmobs.LycanitesMobs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.lycanitesmobs.LycanitesMobs.MODID;

public record WorldGeneratorBiomeModifier(

        HolderSet<Biome> biomes,
        Holder<PlacedFeature> chunkSpawnFeature,
        Holder<PlacedFeature> dungeonFeature,
        Holder<PlacedFeature> poisonLakeFeature,
        Holder<PlacedFeature> poisonSpringFeature,
        Holder<PlacedFeature> acidLakeFeature,
        Holder<PlacedFeature> acidSpringFeature,
        Holder<PlacedFeature> oozeLakeFeature,
        Holder<PlacedFeature> oozeSpringFeature,
        Holder<PlacedFeature> moglavaLakeFeature,
        Holder<PlacedFeature> moglavaSpringFeature) implements BiomeModifier {
    // Biome Management:
    public static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, MODID);

    public static final RegistryObject<Codec<WorldGeneratorBiomeModifier>> WORLD_GENERATOR_CODEC = BIOME_MODIFIER_SERIALIZERS.register("world_generator", () ->
            RecordCodecBuilder.create(instance -> instance.group(
                    Biome.LIST_CODEC.fieldOf("biomes").forGetter(WorldGeneratorBiomeModifier::biomes),
                    PlacedFeature.CODEC.fieldOf("chunk_spawn_feature").forGetter(WorldGeneratorBiomeModifier::chunkSpawnFeature),
                    PlacedFeature.CODEC.fieldOf("dungeon_feature").forGetter(WorldGeneratorBiomeModifier::dungeonFeature),
                    PlacedFeature.CODEC.fieldOf("poison_lake_feature").forGetter(WorldGeneratorBiomeModifier::poisonLakeFeature),
                    PlacedFeature.CODEC.fieldOf("poison_spring_feature").forGetter(WorldGeneratorBiomeModifier::poisonSpringFeature),
                    PlacedFeature.CODEC.fieldOf("acid_lake_feature").forGetter(WorldGeneratorBiomeModifier::acidLakeFeature),
                    PlacedFeature.CODEC.fieldOf("acid_spring_feature").forGetter(WorldGeneratorBiomeModifier::acidSpringFeature),
                    PlacedFeature.CODEC.fieldOf("ooze_lake_feature").forGetter(WorldGeneratorBiomeModifier::oozeLakeFeature),
                    PlacedFeature.CODEC.fieldOf("ooze_spring_feature").forGetter(WorldGeneratorBiomeModifier::oozeSpringFeature),
                    PlacedFeature.CODEC.fieldOf("moglava_lake_feature").forGetter(WorldGeneratorBiomeModifier::moglavaLakeFeature),
                    PlacedFeature.CODEC.fieldOf("moglava_spring_feature").forGetter(WorldGeneratorBiomeModifier::moglavaSpringFeature)
            ).apply(instance, WorldGeneratorBiomeModifier::new))
    );
    public static final Codec<WorldGeneratorBiomeModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(WorldGeneratorBiomeModifier::biomes),
            PlacedFeature.CODEC.fieldOf("chunk_spawn_feature").forGetter(WorldGeneratorBiomeModifier::chunkSpawnFeature),
            PlacedFeature.CODEC.fieldOf("dungeon_feature").forGetter(WorldGeneratorBiomeModifier::dungeonFeature),
            PlacedFeature.CODEC.fieldOf("poison_lake_feature").forGetter(WorldGeneratorBiomeModifier::poisonLakeFeature),
            PlacedFeature.CODEC.fieldOf("poison_spring_feature").forGetter(WorldGeneratorBiomeModifier::poisonSpringFeature),
            PlacedFeature.CODEC.fieldOf("acid_lake_feature").forGetter(WorldGeneratorBiomeModifier::acidLakeFeature),
            PlacedFeature.CODEC.fieldOf("acid_spring_feature").forGetter(WorldGeneratorBiomeModifier::acidSpringFeature),
            PlacedFeature.CODEC.fieldOf("ooze_lake_feature").forGetter(WorldGeneratorBiomeModifier::oozeLakeFeature),
            PlacedFeature.CODEC.fieldOf("ooze_spring_feature").forGetter(WorldGeneratorBiomeModifier::oozeSpringFeature),
            PlacedFeature.CODEC.fieldOf("moglava_lake_feature").forGetter(WorldGeneratorBiomeModifier::moglavaLakeFeature),
            PlacedFeature.CODEC.fieldOf("moglava_spring_feature").forGetter(WorldGeneratorBiomeModifier::moglavaSpringFeature)
    ).apply(instance, WorldGeneratorBiomeModifier::new));


    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase == Phase.ADD && biomes.contains(biome)) {
            builder.getGenerationSettings().addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, chunkSpawnFeature);
            builder.getGenerationSettings().addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, dungeonFeature);
            builder.getGenerationSettings().addFeature(GenerationStep.Decoration.LAKES, poisonLakeFeature);
            builder.getGenerationSettings().addFeature(GenerationStep.Decoration.LAKES, poisonSpringFeature);
            builder.getGenerationSettings().addFeature(GenerationStep.Decoration.LAKES, acidLakeFeature);
            builder.getGenerationSettings().addFeature(GenerationStep.Decoration.LAKES, acidSpringFeature);
            builder.getGenerationSettings().addFeature(GenerationStep.Decoration.LAKES, oozeLakeFeature);
            builder.getGenerationSettings().addFeature(GenerationStep.Decoration.LAKES, oozeSpringFeature);
            builder.getGenerationSettings().addFeature(GenerationStep.Decoration.LAKES, moglavaLakeFeature);
            builder.getGenerationSettings().addFeature(GenerationStep.Decoration.LAKES, moglavaSpringFeature);
        }
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return WORLD_GENERATOR_CODEC.get();
    }
}
