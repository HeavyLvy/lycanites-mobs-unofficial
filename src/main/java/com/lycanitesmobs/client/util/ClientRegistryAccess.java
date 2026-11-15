package com.lycanitesmobs.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;

import javax.annotation.Nullable;

/** Client-only half of RegistryHelper; only loaded on the physical client. */
public final class ClientRegistryAccess {
    private ClientRegistryAccess() {}

    @Nullable
    public static RegistryAccess get() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            return minecraft.level.registryAccess();
        }
        if (minecraft.getConnection() != null) {
            return minecraft.getConnection().registryAccess();
        }
        return null;
    }
}
