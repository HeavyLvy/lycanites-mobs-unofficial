package com.lycanitesmobs.core.event;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.worldgen.WorldGeneratorBiomeModifier;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.JsonCodecProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = LycanitesMobs.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BiomeModifierEventListener {
    public static <T> HolderSet.Direct<T> createDirectHolderSet(List<Holder<T>> holders) {
        return HolderSet.direct(holders);
    }

    public static <T> List<Holder<T>> wrapInHolder(List<T> elements) {
        return elements.stream()
                .map(Holder::direct)
                .collect(Collectors.toList());
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) throws ExecutionException, InterruptedException {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        HolderLookup.Provider lookupProvider = event.getLookupProvider().get();

        // Use HolderLookup.Provider to get the necessary registries

        Optional<HolderLookup.RegistryLookup<Biome>> biomeLookup = lookupProvider.lookup(ForgeRegistries.BIOMES.getRegistryKey());
        Optional<HolderLookup.RegistryLookup<PlacedFeature>> featureLookup = lookupProvider.lookup(Registries.PLACED_FEATURE);

        List<Biome> biomes = biomeLookup.get().listElements().map(Holder.Reference::get).toList();
        List<Holder<Biome>> biomeHolders = wrapInHolder(biomes);
        HolderSet<Biome> biomeHolderSet = createDirectHolderSet(biomeHolders);

        if (biomeLookup.isPresent() && featureLookup.isPresent()) {
            JsonCodecProvider<WorldGeneratorBiomeModifier> biomeModifierProvider = new JsonCodecProvider<>(
                    output,
                    existingFileHelper,
                    LycanitesMobs.MODID,
                    JsonOps.INSTANCE,
                    PackType.SERVER_DATA,
                    "biome_modifier",
                    WorldGeneratorBiomeModifier.CODEC,
                    Map.of(
                            new ResourceLocation(LycanitesMobs.MODID, "world_generator"), new WorldGeneratorBiomeModifier(
                                    biomeHolderSet,
                                    featureLookup.get().get(ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(LycanitesMobs.MODID, "chunk_spawn_feature"))).orElseThrow(),
                                    featureLookup.get().get(ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(LycanitesMobs.MODID, "dungeon_feature"))).orElseThrow(),
                                    featureLookup.get().get(ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(LycanitesMobs.MODID, "poison_lake_feature"))).orElseThrow(),
                                    featureLookup.get().get(ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(LycanitesMobs.MODID, "poison_spring_feature"))).orElseThrow(),
                                    featureLookup.get().get(ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(LycanitesMobs.MODID, "acid_lake_feature"))).orElseThrow(),
                                    featureLookup.get().get(ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(LycanitesMobs.MODID, "acid_spring_feature"))).orElseThrow(),
                                    featureLookup.get().get(ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(LycanitesMobs.MODID, "ooze_lake_feature"))).orElseThrow(),
                                    featureLookup.get().get(ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(LycanitesMobs.MODID, "ooze_spring_feature"))).orElseThrow(),
                                    featureLookup.get().get(ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(LycanitesMobs.MODID, "moglava_lake_feature"))).orElseThrow(),
                                    featureLookup.get().get(ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(LycanitesMobs.MODID, "moglava_spring_feature"))).orElseThrow()
                            )
                    )
            );
            generator.addProvider(event.includeServer(), (DataProvider.Factory<JsonCodecProvider<WorldGeneratorBiomeModifier>>) pO -> biomeModifierProvider);
        } else {
            throw new IllegalStateException("Biome or feature lookup not found.");
        }
    }
}




