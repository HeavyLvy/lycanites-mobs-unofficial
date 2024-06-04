package com.lycanitesmobs.core.util;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.StatType;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class LMHelperClass {
    public static Object convertObjectToDesired(Object input, String outputType) {
        return switch (outputType.toLowerCase()) {
            case "integer" -> convertToInteger(input);
            case "double" -> convertToDouble(input);
            case "float" -> convertToFloat(input);
            case "boolean" -> convertToBoolean(input);
            case "resourcelocation" -> convertToResourceLocation(input);
            case "vec3i" -> convertToVec3i(input);
            default -> input;
        };
    }
    public static Vector3d convertToVec3d(Object input) {
        if (input instanceof Vec3 v) {

            return new Vector3d(convertToDouble(v.x), convertToDouble(v.y), convertToDouble(v.z));
        }else if (input instanceof Vector3f v) {
            return new Vector3d(convertToDouble(v.x), convertToDouble(v.y), convertToDouble(v.z));
        }else if (input instanceof Vector3d v) {
            return new Vector3d(convertToDouble(v.x), convertToDouble(v.y), convertToDouble(v.z));
        }
        return null;
    }
    public static Vec3i convertToVec3i(Object input) {
        if (input instanceof Vec3 v) {

            return new Vec3i(convertToInteger(v.x), convertToInteger(v.y), convertToInteger(v.z));
        }else if (input instanceof Vector3f v) {
            return new Vec3i(convertToInteger(v.x), convertToInteger(v.y), convertToInteger(v.z));
        }else if (input instanceof Vector3d v) {
            return new Vec3i(convertToInteger(v.x), convertToInteger(v.y), convertToInteger(v.z));
        }
        return null;
    }
    public static ResourceLocation convertToResourceLocation(Object input) {
        if (input == null) {
            return null;
        }
        if (input instanceof ResourceLocation) {
            return (ResourceLocation) input;
        } else if (input instanceof String) {
            return new ResourceLocation((String) input);
        } else if (input instanceof Item item) {
            return ForgeRegistries.ITEMS.getKey(item);
        } else if (input instanceof Block block) {
            return ForgeRegistries.BLOCKS.getKey(block);
        } else if (input instanceof EntityType<?> entityType) {
            return ForgeRegistries.ENTITY_TYPES.getKey(entityType);
        } else if (input instanceof Biome biome) {
            return ForgeRegistries.BIOMES.getKey(biome);
        } else if (input instanceof Fluid fluid) {
            return ForgeRegistries.FLUIDS.getKey(fluid);
        } else if (input instanceof MobEffect mobEffect) {
            return ForgeRegistries.MOB_EFFECTS.getKey(mobEffect);
        } else if (input instanceof SoundEvent soundEvent) {
            return ForgeRegistries.SOUND_EVENTS.getKey(soundEvent);
        } else if (input instanceof Potion potion) {
            return ForgeRegistries.POTIONS.getKey(potion);
        } else if (input instanceof Enchantment enchantment) {
            return ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
        } else if (input instanceof BlockEntityType<?> blockEntityType) {
            return ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(blockEntityType);
        } else if (input instanceof ParticleType<?> particleType) {
            return ForgeRegistries.PARTICLE_TYPES.getKey(particleType);
        } else if (input instanceof MenuType<?> menuType) {
            return ForgeRegistries.MENU_TYPES.getKey(menuType);
        } else if (input instanceof PaintingVariant paintingVariant) {
            return ForgeRegistries.PAINTING_VARIANTS.getKey(paintingVariant);
        } else if (input instanceof RecipeType<?> recipeType) {
            return ForgeRegistries.RECIPE_TYPES.getKey(recipeType);
        } else if (input instanceof RecipeSerializer<?> recipeSerializer) {
            return ForgeRegistries.RECIPE_SERIALIZERS.getKey(recipeSerializer);
        } else if (input instanceof Attribute attribute) {
            return ForgeRegistries.ATTRIBUTES.getKey(attribute);
        } else if (input instanceof StatType<?> statType) {
            return ForgeRegistries.STAT_TYPES.getKey(statType);
        } else if (input instanceof ArgumentTypeInfo<?, ?> argumentTypeInfo) {
            return ForgeRegistries.COMMAND_ARGUMENT_TYPES.getKey(argumentTypeInfo);
        } else if (input instanceof VillagerProfession villagerProfession) {
            return ForgeRegistries.VILLAGER_PROFESSIONS.getKey(villagerProfession);
        } else if (input instanceof PoiType poiType) {
            return ForgeRegistries.POI_TYPES.getKey(poiType);
        } else if (input instanceof MemoryModuleType<?> memoryModuleType) {
            return ForgeRegistries.MEMORY_MODULE_TYPES.getKey(memoryModuleType);
        } else if (input instanceof SensorType<?> sensorType) {
            return ForgeRegistries.SENSOR_TYPES.getKey(sensorType);
        } else if (input instanceof Schedule schedule) {
            return ForgeRegistries.SCHEDULES.getKey(schedule);
        } else if (input instanceof Activity activity) {
            return ForgeRegistries.ACTIVITIES.getKey(activity);
        } else if (input instanceof WorldCarver<?> worldCarver) {
            return ForgeRegistries.WORLD_CARVERS.getKey(worldCarver);
        } else if (input instanceof Feature<?> feature) {
            return ForgeRegistries.FEATURES.getKey(feature);
        } else if (input instanceof ChunkStatus chunkStatus) {
            return ForgeRegistries.CHUNK_STATUS.getKey(chunkStatus);
        } else if (input instanceof BlockStateProviderType<?> blockStateProviderType) {
            return ForgeRegistries.BLOCK_STATE_PROVIDER_TYPES.getKey(blockStateProviderType);
        } else if (input instanceof FoliagePlacerType<?> foliagePlacerType) {
            return ForgeRegistries.FOLIAGE_PLACER_TYPES.getKey(foliagePlacerType);
        } else if (input instanceof TreeDecoratorType<?> treeDecoratorType) {
            return ForgeRegistries.TREE_DECORATOR_TYPES.getKey(treeDecoratorType);
        }

        return null;
    }
    public static Boolean convertToBoolean(Object input) {
        if (input instanceof Boolean) {
            return (Boolean) input;
        } else if (input instanceof String) {
            String stringValue = ((String) input).toLowerCase();
            if ("true".equals(stringValue)) {
                return true;
            } else if ("false".equals(stringValue)) {
                return false;
            }
        }
        return null;
    }


    public static Integer convertToInteger(Object input) {
        if (input instanceof Integer) {
            return (Integer) input;
        } else if (input instanceof Double || input instanceof Float) {
            return ((Number) input).intValue();
        } else {
            return null;
        }
    }

    public static Double convertToDouble(Object input) {
        if (input instanceof Double) {
            return (Double) input;
        } else if (input instanceof Integer || input instanceof Float) {
            return ((Number) input).doubleValue();
        } else {
            return null;
        }
    }

    public static Float convertToFloat(Object input) {
        if (input instanceof Float) {
            return (Float) input;
        } else if (input instanceof Integer || input instanceof Double) {
            return ((Number) input).floatValue();
        } else {
            return null;
        }
    }
    public static DamageSource damageSourceFromResourceKey(Level level, ResourceKey<DamageType> damageTypeResourceKey) {
        ResourceKey<Registry<DamageType>> damageTypeRegistryKey = ResourceKey.createRegistryKey(new ResourceLocation("damage_type"));
        Registry<DamageType> damageTypeRegistry = level.registryAccess().registryOrThrow(damageTypeRegistryKey);
        Holder<DamageType> holder = damageTypeRegistry.getHolderOrThrow(damageTypeResourceKey);
        return new DamageSource(holder);
    }
    public static double wrapDegrees(double p_76138_0_) {
        double lvt_2_1_ = p_76138_0_ % 360.0;
        if (lvt_2_1_ >= 180.0) {
            lvt_2_1_ -= 360.0;
        }
        if (lvt_2_1_ < -180.0) {
            lvt_2_1_ += 360.0;
        }
        return lvt_2_1_;
    }
    public static double clamp(double p_151237_0_, double p_151237_2_, double p_151237_4_) {
        if (p_151237_0_ < p_151237_2_) {
            return p_151237_2_;
        } else {
            return Math.min(p_151237_0_, p_151237_4_);
        }
    }
    public static boolean hasTag(Object obj, Object tag) {
        if (obj instanceof BlockState block) {
            if (tag instanceof TagKey<?> tagKey) {
                return block.getTags().anyMatch(blockTagKey -> blockTagKey == tagKey);
            }else if (tag instanceof String string){
                return block.getTags().anyMatch(blockTagKey -> blockTagKey.location().equals(new ResourceLocation(string)));
            }
        }
        return false;
    }
    public static float getBrightness(Entity entity) {
        BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos(entity.getX(), 0.0, entity.getZ());
        if (entity.level().hasChunkAt(blockpos$mutable)) {
            blockpos$mutable.setY((int) Math.floor(entity.getEyeY()));
            return entity.level().getBrightness(LightLayer.BLOCK,blockpos$mutable);
        } else {
            return 0.0F;
        }
    }
    public static class Materials {
        public static boolean isLeaves(Block block) {
            return hasTag(block, BlockTags.LEAVES);
        }
        public static boolean isDirt(Block block) {
            return hasTag(block, BlockTags.DIRT);
        }
        public static boolean isCoral(Block block) {
            return hasTag(block, BlockTags.CORAL_PLANTS);
        }
        public static boolean isCrop(Block block) {
            return hasTag(block, BlockTags.CROPS);
        }
        public static boolean isPlant(Block block) {
            return block instanceof IPlantable;
        }
        public static boolean isReplaceablePlant(Block block) {
            return hasTag(block, BlockTags.REPLACEABLE) && isPlant(block);
        }
        public static boolean isUnderwaterPlant(Block block) {
            return hasTag(block, BlockTags.UNDERWATER_BONEMEALS) && isPlant(block);
        }
    }
}
