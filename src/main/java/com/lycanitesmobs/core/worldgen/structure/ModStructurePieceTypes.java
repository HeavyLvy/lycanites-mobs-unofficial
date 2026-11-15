package com.lycanitesmobs.core.worldgen.structure;

import com.lycanitesmobs.LycanitesMobs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModStructurePieceTypes {
    /** NeoForge freezes registries after RegisterEvent, so this registers through a DeferredRegister (was Registry.register in common setup). */
    private static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES = DeferredRegister.create(Registries.STRUCTURE_PIECE, LycanitesMobs.MODID);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> LM_DUNGEON_PIECE =
            STRUCTURE_PIECES.register("lm_dungeon_piece", () -> (StructurePieceType.ContextlessType) LMDungeonPiece::new);

    public static void register(IEventBus modEventBus) {
        STRUCTURE_PIECES.register(modEventBus);
    }
}
