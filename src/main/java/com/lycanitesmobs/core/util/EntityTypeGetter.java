package com.lycanitesmobs.core.util;

import net.minecraft.resources.ResourceLocation;

public interface EntityTypeGetter {
    ResourceLocation getRegistryName();
    void setRegistryName(ResourceLocation registryName);
}
