package com.lycanitesmobs.core.block;


import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.info.ModInfo;
import com.lycanitesmobs.core.util.Material;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class BlockMaker {
    public static final Map<String, Supplier<? extends Block>> blocks = new HashMap<>();
    // ==================================================
    //                Add Dungeon Blocks
    // ==================================================

    /**
     * Creates a set of themed dungeon blocks such as tiles, bricks, pillars, etc as well as a crystal light source. The block name is added to a list that is used to automatically add each recipe at Post Init.
     *
     * @param stoneName The name of the stone block, such as "demon" or "shadow", etc. (stone and crystal are appended).
     **/
    public static void addDungeonBlocks(String stoneName) {
        Block.Properties properties = Block.Properties.of().mapColor(MapColor.STONE).sound(SoundType.STONE).strength(2F, 1200.0F);
        Block.Properties crystalProperties = Block.Properties.of().mapColor(MapColor.NONE).sound(SoundType.GLASS).strength(0.5F, 1200.0F).lightLevel((BlockState blockState) -> {
            return 15;
        });

        Supplier<? extends Block> stoneBlock = () -> new BlockBase(properties, stoneName + "stone");
        Supplier<? extends Block> crystalStair = () -> new BlockStairsCustom(properties, (BlockBase) stoneBlock.get());
        Supplier<? extends Block> crystalSlab = () -> new BlockSlabCustom(properties, (BlockBase) stoneBlock.get());
        ObjectManager.addBlock(stoneName + "stone", stoneBlock, false);
        ObjectManager.addBlock(stoneName + "stone_stairs", crystalStair, false);
        ObjectManager.addBlock(stoneName + "stone_slab", crystalSlab, false);

        Supplier<? extends Block> stoneBrickBlock = () -> new BlockBase(properties, stoneName + "stonebrick");
        Supplier<? extends Block> stoneBrickStairs = () -> new BlockStairsCustom(properties, (BlockBase) stoneBrickBlock.get());
        Supplier<? extends Block> stoneBrickSlab = () -> new BlockSlabCustom(properties, (BlockBase) stoneBrickBlock.get());
        Supplier<? extends Block> stoneBrickFence = () -> new BlockFenceCustom(properties, (BlockBase) stoneBrickBlock.get());
        Supplier<? extends Block> stoneBrickWall = () -> new BlockWallCustom(properties, (BlockBase) stoneBrickBlock.get());
        ObjectManager.addBlock(stoneName + "stonebrick", stoneBrickBlock, false);
        ObjectManager.addBlock(stoneName + "stonebrick_stairs", stoneBrickStairs, false);
        ObjectManager.addBlock(stoneName + "stonebrick_slab", stoneBrickSlab, false);
        ObjectManager.addBlock(stoneName + "stonebrick_fence", stoneBrickFence, false);
        ObjectManager.addBlock(stoneName + "stonebrick_wall", stoneBrickWall, false);

        Supplier<? extends Block> stoneTileBlock = () -> new BlockBase(properties, stoneName + "stonetile");
        Supplier<? extends Block> stoneTilePolished = () -> new BlockBase(properties, stoneName + "stonepolished");
        Supplier<? extends Block> stoneTileChiseled = () -> new BlockBase(properties, stoneName + "stonechiseled");
        Supplier<? extends Block> stoneTileCrystal = () -> new BlockBase(crystalProperties, stoneName + "crystal");
        Supplier<? extends Block> stoneTileStairs = () -> new BlockStairsCustom(properties, (BlockBase) stoneTileBlock.get());
        Supplier<? extends Block> stoneTileSlab = () -> new BlockSlabCustom(properties, (BlockBase) stoneTileBlock.get());
        Supplier<? extends Block> stoneTilePillar = () -> new BlockPillar(properties, stoneName + "stonepillar");
        ObjectManager.addBlock(stoneName + "stonetile", stoneTileBlock, false);
        ObjectManager.addBlock(stoneName + "stonetile_stairs", stoneTileStairs, false);
        ObjectManager.addBlock(stoneName + "stonetile_slab", stoneTileSlab, false);

        ObjectManager.addBlock(stoneName + "stonepolished", stoneTilePolished, false);
        ObjectManager.addBlock(stoneName + "stonechiseled", stoneTileChiseled, false);
        ObjectManager.addBlock(stoneName + "stonepillar", stoneTilePillar, false);

        ObjectManager.addBlock(stoneName + "crystal", stoneTileCrystal, false);
    }
}
