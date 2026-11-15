package com.lycanitesmobs.core.util.helpers;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * 1.21 needs registries (HolderLookup.Provider) to save/load item stacks. For code paths without a Level
 * (e.g. equipment part lists stored in item data) this picks the best available registry access.
 */
public final class RegistryHelper {
    private RegistryHelper() {}

    public static RegistryAccess access() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return server.registryAccess();
        }
        if (FMLEnvironment.dist.isClient()) {
            RegistryAccess client = com.lycanitesmobs.client.util.ClientRegistryAccess.get();
            if (client != null) {
                return client;
            }
        }
        return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }
}
