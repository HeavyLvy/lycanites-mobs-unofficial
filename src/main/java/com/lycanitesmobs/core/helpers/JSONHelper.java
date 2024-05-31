package com.lycanitesmobs.core.helpers;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.util.Material;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.util.*;

public class JSONHelper {

	public static Vec3i getVector3i(JsonObject json, String memberName) {
		if(json.has(memberName)) {
			JsonArray jsonArray = json.get(memberName).getAsJsonArray();
			Iterator<JsonElement> jsonIterator = jsonArray.iterator();
			int[] coords = new int[3];
			int i = 0;
			while (jsonIterator.hasNext() && i < coords.length) {
				coords[i] = jsonIterator.next().getAsInt();
				i++;
			}
			return new Vec3i(coords[0], coords[1], coords[2]);
		}
		return new Vec3i(0, 0, 0);
	}

	public static Vector3d getVector3d(JsonObject json, String memberName, Vector3d defaultVec) {
		if(json.has(memberName)) {
			JsonArray jsonArray = json.get(memberName).getAsJsonArray();
			Iterator<JsonElement> jsonIterator = jsonArray.iterator();
			double[] coords = new double[3];
			int i = 0;
			while (jsonIterator.hasNext() && i < coords.length) {
				coords[i] = jsonIterator.next().getAsDouble();
				i++;
			}
			return new Vector3d(coords[0], coords[1], coords[2]);
		}
		return defaultVec;
	}

	public static List<String> getJsonStrings(JsonArray jsonArray) {
		List<String> strings = new ArrayList<>();
		Iterator<JsonElement> jsonIterator = jsonArray.iterator();
		while (jsonIterator.hasNext()) {
			String string = jsonIterator.next().getAsString();
			strings.add(string);
		}
		return strings;
	}

	public static List<Block> getJsonBlocks(JsonObject json) {
		List<Block> blocks = new ArrayList<>();
		if(json.has("blocks")) {
			blocks = getJsonBlocks(json.get("blocks").getAsJsonArray());
		}
		return blocks;
	}

	public static List<Block> getJsonBlocks(JsonArray jsonArray) {
		List<Block> blocks = new ArrayList<>();
		Iterator<JsonElement> jsonIterator = jsonArray.iterator();
		while (jsonIterator.hasNext()) {
			Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(jsonIterator.next().getAsString()));
			if(block != null) {
				blocks.add(block);
			}
		}
		return blocks;
	}

	public static List<Item> getJsonItems(JsonArray jsonArray) {
		List<Item> items = new ArrayList<>();
		Iterator<JsonElement> jsonIterator = jsonArray.iterator();
		while (jsonIterator.hasNext()) {
			Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(jsonIterator.next().getAsString()));
			if(item != null) {
				items.add(item);
			}
		}
		return items;
	}

	public static List<BlockBehaviour.Properties> getJsonMaterials(JsonObject json) {
		List<BlockBehaviour.Properties> materials = new ArrayList<>();
		if(json.has("materials")) {
			JsonArray jsonArray = json.get("materials").getAsJsonArray();
			Iterator<JsonElement> jsonIterator = jsonArray.iterator();
			while (jsonIterator.hasNext()) {
				BlockBehaviour.Properties material = null;
				String materialName = jsonIterator.next().getAsString();

				if("air".equalsIgnoreCase(materialName)) {
					material = Material.AIR;
				}
				else if("lava".equalsIgnoreCase(materialName)) {
					material = Material.LAVA;
				}
				else if("fire".equalsIgnoreCase(materialName)) {
					material = Material.FIRE;
				}
				else if("water".equalsIgnoreCase(materialName)) {
					material = Material.WATER;
				}
				else if("ground".equalsIgnoreCase(materialName)) {
					material = Material.DIRT;
				}
				else if("earth".equalsIgnoreCase(materialName)) {
					material = Material.DIRT;
				}
				else if("sand".equalsIgnoreCase(materialName)) {
					material = Material.SAND;
				}
				else if("claw".equalsIgnoreCase(materialName)) {
					material = Material.CLAY;
				}
				else if("wood".equalsIgnoreCase(materialName)) {
					material = Material.WOOD;
				}
				else if("rock".equalsIgnoreCase(materialName)) {
					material = Material.STONE;
				}
				else if("grass".equalsIgnoreCase(materialName)) {
					material = Material.REPLACEABLE_PLANT;
				}
				else if("tallplants".equalsIgnoreCase(materialName)) {
					material = Material.REPLACEABLE_PLANT;
				}
				else if("vine".equalsIgnoreCase(materialName)) {
					material = Material.REPLACEABLE_PLANT;
				}
				else if("plants".equalsIgnoreCase(materialName)) {
					material = Material.PLANT;
				}
				else if("leaves".equalsIgnoreCase(materialName)) {
					material = Material.LEAVES;
				}
				else if("cactus".equalsIgnoreCase(materialName)) {
					material = Material.CACTUS;
				}
				else if("snow".equalsIgnoreCase(materialName)) {
					material = Material.TOP_SNOW;
				}
				else if("ice".equalsIgnoreCase(materialName)) {
					material = Material.ICE;
				}
				else if("iron".equalsIgnoreCase(materialName)) {
					material = Material.METAL;
				}
				else if("web".equalsIgnoreCase(materialName)) {
					material = Material.WEB;
				}

				if(material != null) {
					materials.add(material);
				}
			}
		}
		return materials;
	}

	public static List<String> getBiomesFromTags(List<String> biomeTags) {
		List<String> biomeList = new ArrayList<>();
		for(String biomeEntry : biomeTags) {

			// Determine Function:
			boolean additive = true;
			if (biomeEntry.charAt(0) == '-' || biomeEntry.charAt(0) == '+') {
				if (biomeEntry.charAt(0) == '-')
					additive = false;
				biomeEntry = biomeEntry.substring(1);
			}


			List<Biome> selectedBiomes = new ArrayList<>();
			if ("ALL".equalsIgnoreCase(biomeEntry)) {
				for (BiomeManager.BiomeType biomeType : BiomeManager.BiomeType.values()) {
					ImmutableList<BiomeManager.BiomeEntry> selectedBiomesSet = BiomeManager.getBiomes(biomeType);
					for (BiomeManager.BiomeEntry selectedBiomesKey : selectedBiomesSet) {
						Biome biome = ForgeRegistries.BIOMES.getValue(selectedBiomesKey.getKey().registry());
						if(biome != null) {
							selectedBiomes.add(biome);
						}
					}
				}
			}
			else if (!"NONE".equalsIgnoreCase(biomeEntry)) {
				BiomeManager.BiomeType biomeType = null;
				try {
					biomeType = BiomeManager.BiomeType.valueOf(biomeEntry.toUpperCase(Locale.ENGLISH));
				} catch (Exception e) {
					LycanitesMobs.logWarning("", "[Spawning] Unknown biome type " + biomeEntry + " this will be ignored and treated as NONE.");
				}
				if (biomeType != null) {
					ImmutableList<BiomeManager.BiomeEntry> selectedBiomesSet = BiomeManager.getBiomes(biomeType);
					for (BiomeManager.BiomeEntry selectedBiomesKey : selectedBiomesSet) {
						Biome biome = ForgeRegistries.BIOMES.getValue(selectedBiomesKey.getKey().registry());
						if(biome != null) {
							selectedBiomes.add(biome);
						}
					}
				}
			}

			for (Biome biome : selectedBiomes) {
				if (additive) {
					if (!biomeList.contains(biome.toString())) {
						biomeList.add(String.valueOf(ForgeRegistries.BIOMES.getKey(biome)));
					}
				} else {
					biomeList.remove(String.valueOf(ForgeRegistries.BIOMES.getKey(biome)));
				}
			}
		}

		return biomeList;
	}

	public static List<Biome> getBiomes(List<String> biomeIds) {
		List<Biome> biomes = new ArrayList<>();
		for(String biomeId : biomeIds) {
			Biome biome = ForgeRegistries.BIOMES.getValue(new ResourceLocation(biomeId));
			if(biome != null) {
				biomes.add(biome);
			}
		}
		return biomes;
	}
}
