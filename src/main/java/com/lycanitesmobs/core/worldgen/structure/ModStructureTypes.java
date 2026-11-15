package com.lycanitesmobs.core.worldgen.structure;

import com.lycanitesmobs.LycanitesMobs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModStructureTypes {
    /** NeoForge freezes registries after RegisterEvent, so this registers through a DeferredRegister (was Registry.register in common setup). */
    private static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, LycanitesMobs.MODID);

    public static final DeferredHolder<StructureType<?>, StructureType<LMDungeonStructure>> LM_DUNGEON =
            STRUCTURE_TYPES.register("lm_dungeon", () -> () -> LMDungeonStructure.CODEC);

    public static void register(IEventBus modEventBus) {
        STRUCTURE_TYPES.register(modEventBus);
    }
}
