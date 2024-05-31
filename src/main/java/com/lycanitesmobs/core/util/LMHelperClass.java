package com.lycanitesmobs.core.util;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
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
        if (input instanceof ResourceLocation) {
            return (ResourceLocation) input;
        } else if (input instanceof String) {
            return new ResourceLocation((String) input);
        }else if (input instanceof Item item) {
            return ForgeRegistries.ITEMS.getKey(item);
        }else if (input instanceof Block block) {
            return ForgeRegistries.BLOCKS.getKey(block);
        }else if (input instanceof EntityType<?> entityType) {
            return ForgeRegistries.ENTITY_TYPES.getKey(entityType);
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
            return p_151237_0_ > p_151237_4_ ? p_151237_4_ : p_151237_0_;
        }
    }
    public static String getItemId(Item item) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
        return itemId.toString();
    }
    public static String getBiomeId(Biome biome) {
        ResourceLocation biomeId = ForgeRegistries.BIOMES.getKey(biome);
        return biomeId.toString();
    }
    public static String getBlockId(Block block) {
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);
        return blockId.toString();
    }
}
