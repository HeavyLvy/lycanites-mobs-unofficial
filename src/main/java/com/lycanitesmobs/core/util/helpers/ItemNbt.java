package com.lycanitesmobs.core.util.helpers;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import javax.annotation.Nullable;

/**
 * Replacement for the ItemStack NBT methods removed in 1.20.5 (getTag/hasTag/setTag).
 * Lycanites item data now lives in the minecraft:custom_data component.
 * getTag returns a copy, so changes must be written back with setTag (which all call sites already do).
 */
public final class ItemNbt {
    private ItemNbt() {}

    public static boolean hasTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && !data.isEmpty();
    }

    @Nullable
    public static CompoundTag getTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? null : data.copyTag();
    }

    public static void setTag(ItemStack stack, @Nullable CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
            return;
        }
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
    }
}
