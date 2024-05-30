package com.lycanitesmobs.core.util;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;

public class DamageSourceHelper {
    public static DamageSource IN_FIRE;
    public static DamageSource LIGHTNING_BOLT;
    public static DamageSource ON_FIRE;
    public static DamageSource LAVA;
    public static DamageSource HOT_FLOOR;
    public static DamageSource IN_WALL;
    public static DamageSource CRAMMING;
    public static DamageSource DROWN;
    public static DamageSource STARVE;
    public static DamageSource CACTUS;
    public static DamageSource FALL;
    public static DamageSource FLY_INTO_WALL;
    public static DamageSource FELL_OUT_OF_WORLD;
    public static DamageSource GENERIC;
    public static DamageSource MAGIC;
    public static DamageSource WITHER;
    public static DamageSource DRAGON_BREATH;
    public static DamageSource DRY_OUT;
    public static DamageSource SWEET_BERRY_BUSH;
    public static DamageSource FREEZE;
    public static DamageSource STALAGMITE;
    public static DamageSource FALLING_BLOCK;
    public static DamageSource FALLING_ANVIL;
    public static DamageSource FALLING_STALACTITE;
    public static DamageSource STING;
    public static DamageSource MOB_ATTACK;
    public static DamageSource MOB_ATTACK_NO_AGGRO;
    public static DamageSource PLAYER_ATTACK;
    public static DamageSource ARROW;
    public static DamageSource TRIDENT;
    public static DamageSource MOB_PROJECTILE;
    public static DamageSource FIREWORKS;
    public static DamageSource FIREBALL;
    public static DamageSource UNATTRIBUTED_FIREBALL;
    public static DamageSource WITHER_SKULL;
    public static DamageSource THROWN;
    public static DamageSource INDIRECT_MAGIC;
    public static DamageSource THORNS;
    public static DamageSource EXPLOSION;
    public static DamageSource PLAYER_EXPLOSION;
    public static DamageSource SONIC_BOOM;
    public static DamageSource BAD_RESPAWN_POINT;
    public static DamageSource OUTSIDE_BORDER;
    public static DamageSource GENERIC_KILL;

    static {
        // Initialize the damage sources
        initializeDamageSources();
    }

    private static void initializeDamageSources() {
        // Get the Minecraft server instance
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            RegistryAccess registryAccess = server.registryAccess();
            IN_FIRE = createDamageSource(registryAccess, DamageTypes.IN_FIRE);
            LIGHTNING_BOLT = createDamageSource(registryAccess, DamageTypes.LIGHTNING_BOLT);
            ON_FIRE = createDamageSource(registryAccess, DamageTypes.ON_FIRE);
            LAVA = createDamageSource(registryAccess, DamageTypes.LAVA);
            HOT_FLOOR = createDamageSource(registryAccess, DamageTypes.HOT_FLOOR);
            IN_WALL = createDamageSource(registryAccess, DamageTypes.IN_WALL);
            CRAMMING = createDamageSource(registryAccess, DamageTypes.CRAMMING);
            DROWN = createDamageSource(registryAccess, DamageTypes.DROWN);
            STARVE = createDamageSource(registryAccess, DamageTypes.STARVE);
            CACTUS = createDamageSource(registryAccess, DamageTypes.CACTUS);
            FALL = createDamageSource(registryAccess, DamageTypes.FALL);
            FLY_INTO_WALL = createDamageSource(registryAccess, DamageTypes.FLY_INTO_WALL);
            FELL_OUT_OF_WORLD = createDamageSource(registryAccess, DamageTypes.FELL_OUT_OF_WORLD);
            GENERIC = createDamageSource(registryAccess, DamageTypes.GENERIC);
            MAGIC = createDamageSource(registryAccess, DamageTypes.MAGIC);
            WITHER = createDamageSource(registryAccess, DamageTypes.WITHER);
            DRAGON_BREATH = createDamageSource(registryAccess, DamageTypes.DRAGON_BREATH);
            DRY_OUT = createDamageSource(registryAccess, DamageTypes.DRY_OUT);
            SWEET_BERRY_BUSH = createDamageSource(registryAccess, DamageTypes.SWEET_BERRY_BUSH);
            FREEZE = createDamageSource(registryAccess, DamageTypes.FREEZE);
            STALAGMITE = createDamageSource(registryAccess, DamageTypes.STALAGMITE);
            FALLING_BLOCK = createDamageSource(registryAccess, DamageTypes.FALLING_BLOCK);
            FALLING_ANVIL = createDamageSource(registryAccess, DamageTypes.FALLING_ANVIL);
            FALLING_STALACTITE = createDamageSource(registryAccess, DamageTypes.FALLING_STALACTITE);
            STING = createDamageSource(registryAccess, DamageTypes.STING);
            MOB_ATTACK = createDamageSource(registryAccess, DamageTypes.MOB_ATTACK);
            MOB_ATTACK_NO_AGGRO = createDamageSource(registryAccess, DamageTypes.MOB_ATTACK_NO_AGGRO);
            PLAYER_ATTACK = createDamageSource(registryAccess, DamageTypes.PLAYER_ATTACK);
            ARROW = createDamageSource(registryAccess, DamageTypes.ARROW);
            TRIDENT = createDamageSource(registryAccess, DamageTypes.TRIDENT);
            MOB_PROJECTILE = createDamageSource(registryAccess, DamageTypes.MOB_PROJECTILE);
            FIREWORKS = createDamageSource(registryAccess, DamageTypes.FIREWORKS);
            FIREBALL = createDamageSource(registryAccess, DamageTypes.FIREBALL);
            UNATTRIBUTED_FIREBALL = createDamageSource(registryAccess, DamageTypes.UNATTRIBUTED_FIREBALL);
            WITHER_SKULL = createDamageSource(registryAccess, DamageTypes.WITHER_SKULL);
            THROWN = createDamageSource(registryAccess, DamageTypes.THROWN);
            INDIRECT_MAGIC = createDamageSource(registryAccess, DamageTypes.INDIRECT_MAGIC);
            THORNS = createDamageSource(registryAccess, DamageTypes.THORNS);
            EXPLOSION = createDamageSource(registryAccess, DamageTypes.EXPLOSION);
            PLAYER_EXPLOSION = createDamageSource(registryAccess, DamageTypes.PLAYER_EXPLOSION);
            SONIC_BOOM = createDamageSource(registryAccess, DamageTypes.SONIC_BOOM);
            BAD_RESPAWN_POINT = createDamageSource(registryAccess, DamageTypes.BAD_RESPAWN_POINT);
            OUTSIDE_BORDER = createDamageSource(registryAccess, DamageTypes.OUTSIDE_BORDER);
            GENERIC_KILL = createDamageSource(registryAccess, DamageTypes.GENERIC_KILL);
        }
    }

    private static DamageSource createDamageSource(RegistryAccess registryAccess, ResourceKey<DamageType> damageTypeResourceKey) {
        Registry<DamageType> damageTypeRegistry = registryAccess.registryOrThrow(Registries.DAMAGE_TYPE);
        Holder<DamageType> holder = damageTypeRegistry.getHolderOrThrow(damageTypeResourceKey);
        return new DamageSource(holder);
    }

    // Method to reinitialize damage sources if needed
    public static void reinitializeDamageSources(ServerLevel level) {
        RegistryAccess registryAccess = level.registryAccess();
        IN_FIRE = createDamageSource(registryAccess, DamageTypes.IN_FIRE);
        LIGHTNING_BOLT = createDamageSource(registryAccess, DamageTypes.LIGHTNING_BOLT);
        ON_FIRE = createDamageSource(registryAccess, DamageTypes.ON_FIRE);
        LAVA = createDamageSource(registryAccess, DamageTypes.LAVA);
        HOT_FLOOR = createDamageSource(registryAccess, DamageTypes.HOT_FLOOR);
        IN_WALL = createDamageSource(registryAccess, DamageTypes.IN_WALL);
        CRAMMING = createDamageSource(registryAccess, DamageTypes.CRAMMING);
        DROWN = createDamageSource(registryAccess, DamageTypes.DROWN);
        STARVE = createDamageSource(registryAccess, DamageTypes.STARVE);
        CACTUS = createDamageSource(registryAccess, DamageTypes.CACTUS);
        FALL = createDamageSource(registryAccess, DamageTypes.FALL);
        FLY_INTO_WALL = createDamageSource(registryAccess, DamageTypes.FLY_INTO_WALL);
        FELL_OUT_OF_WORLD = createDamageSource(registryAccess, DamageTypes.FELL_OUT_OF_WORLD);
        GENERIC = createDamageSource(registryAccess, DamageTypes.GENERIC);
        MAGIC = createDamageSource(registryAccess, DamageTypes.MAGIC);
        WITHER = createDamageSource(registryAccess, DamageTypes.WITHER);
        DRAGON_BREATH = createDamageSource(registryAccess, DamageTypes.DRAGON_BREATH);
        DRY_OUT = createDamageSource(registryAccess, DamageTypes.DRY_OUT);
        SWEET_BERRY_BUSH = createDamageSource(registryAccess, DamageTypes.SWEET_BERRY_BUSH);
        FREEZE = createDamageSource(registryAccess, DamageTypes.FREEZE);
        STALAGMITE = createDamageSource(registryAccess, DamageTypes.STALAGMITE);
        FALLING_BLOCK = createDamageSource(registryAccess, DamageTypes.FALLING_BLOCK);
        FALLING_ANVIL = createDamageSource(registryAccess, DamageTypes.FALLING_ANVIL);
        FALLING_STALACTITE = createDamageSource(registryAccess, DamageTypes.FALLING_STALACTITE);
        STING = createDamageSource(registryAccess, DamageTypes.STING);
        MOB_ATTACK = createDamageSource(registryAccess, DamageTypes.MOB_ATTACK);
        MOB_ATTACK_NO_AGGRO = createDamageSource(registryAccess, DamageTypes.MOB_ATTACK_NO_AGGRO);
        PLAYER_ATTACK = createDamageSource(registryAccess, DamageTypes.PLAYER_ATTACK);
        ARROW = createDamageSource(registryAccess, DamageTypes.ARROW);
        TRIDENT = createDamageSource(registryAccess, DamageTypes.TRIDENT);
        MOB_PROJECTILE = createDamageSource(registryAccess, DamageTypes.MOB_PROJECTILE);
        FIREWORKS = createDamageSource(registryAccess, DamageTypes.FIREWORKS);
        FIREBALL = createDamageSource(registryAccess, DamageTypes.FIREBALL);
        UNATTRIBUTED_FIREBALL = createDamageSource(registryAccess, DamageTypes.UNATTRIBUTED_FIREBALL);
        WITHER_SKULL = createDamageSource(registryAccess, DamageTypes.WITHER_SKULL);
        THROWN = createDamageSource(registryAccess, DamageTypes.THROWN);
        INDIRECT_MAGIC = createDamageSource(registryAccess, DamageTypes.INDIRECT_MAGIC);
        THORNS = createDamageSource(registryAccess, DamageTypes.THORNS);
        EXPLOSION = createDamageSource(registryAccess, DamageTypes.EXPLOSION);
        PLAYER_EXPLOSION = createDamageSource(registryAccess, DamageTypes.PLAYER_EXPLOSION);
        SONIC_BOOM = createDamageSource(registryAccess, DamageTypes.SONIC_BOOM);
        BAD_RESPAWN_POINT = createDamageSource(registryAccess, DamageTypes.BAD_RESPAWN_POINT);
        OUTSIDE_BORDER = createDamageSource(registryAccess, DamageTypes.OUTSIDE_BORDER);
        GENERIC_KILL = createDamageSource(registryAccess, DamageTypes.GENERIC_KILL);
    }
}

