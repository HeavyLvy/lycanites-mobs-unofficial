package com.lycanitesmobs.core.util;

import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
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
}
