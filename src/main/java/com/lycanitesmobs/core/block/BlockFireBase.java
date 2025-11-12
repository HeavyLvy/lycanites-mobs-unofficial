package com.lycanitesmobs.core.block;


import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.info.ItemManager;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Map;

public class BlockFireBase extends BlockBase {
    public static final BooleanProperty PERMANENT = BooleanProperty.create("permanent");
    public static final BooleanProperty STATIC = BooleanProperty.create("static");
    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty EAST = PipeBlock.EAST;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty WEST = PipeBlock.WEST;
    public static final BooleanProperty UP = PipeBlock.UP;
    private static final Map<Direction, BooleanProperty> FACING_TO_PROPERTY_MAP = PipeBlock.PROPERTY_BY_DIRECTION.entrySet().stream().filter((p_199776_0_) -> p_199776_0_.getKey() != Direction.DOWN).collect(Util.toMap());

    public boolean dieInRain = true;
    public boolean triggerTNT = true;
    public int agingRate = 3;
    public float spreadChance = 1;
    public boolean removeOnNoFireTick;

    public BlockFireBase(Block.Properties properties, String name) {
        super(properties, name);

        this.removeOnTick = false;
        this.removeOnNoFireTick = false;
        this.loopTicks = true;
        this.canBeCrushed = true;

        this.noBreakCollision = false;

        this.tickRate = 30; // Default tick rate, configs can set this to 1 to remove this fire block from worlds.

        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(AGE, 0)
                .setValue(PERMANENT, false)
                .setValue(STATIC, false)
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false));

        ItemManager.getInstance().cutoutBlocks.add(this);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, PERMANENT, STATIC, NORTH, EAST, SOUTH, WEST, UP);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.getStateForPlacement(context.getLevel(), context.getClickedPos());
    }


    public BlockState getStateForPlacement(BlockGetter world, BlockPos blockPos) {
        BlockPos lowerBlockPos = blockPos.below();
        BlockState lowerBlockState = world.getBlockState(lowerBlockPos);
        if (!this.canCatchFire(world, blockPos, Direction.UP) && lowerBlockState.isFaceSturdy(world, lowerBlockPos, Direction.UP)) {
            BlockState s = this.defaultBlockState();
            for (Direction d : Direction.values()) {
                BooleanProperty p = FACING_TO_PROPERTY_MAP.get(d);
                if (p != null) s = s.setValue(p, this.canCatchFire(world, blockPos.relative(d), d.getOpposite()));
            }
            return s.setValue(PERMANENT, false);
        }
        return this.defaultBlockState();
    }


    /**
     * Returns true if this block can place another block at the specified location.
     **/
    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        BlockPos blockpos = pos.below();
        return worldIn.getBlockState(blockpos).isFaceSturdy(worldIn, blockpos, Direction.UP) || this.areNeighborsFlammable(worldIn, pos);
    }


    protected boolean areNeighborsFlammable(BlockGetter worldIn, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (this.canCatchFire(worldIn, pos.relative(direction), direction.getOpposite())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (!this.canSurvive(state, worldIn, currentPos)) return Blocks.AIR.defaultBlockState();
        return this.getStateForPlacement(worldIn, currentPos)
                .setValue(AGE, state.getValue(AGE))
                .setValue(PERMANENT, state.getValue(PERMANENT))
                .setValue(STATIC, state.getValue(STATIC));
    }


    @Override
    public int tickRate(LevelAccessor world) {
        return this.tickRate;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (this.tickRate > 0) level.scheduleTick(pos, this, getFireTickDelay(level.random));
    }

    private int getFireTickDelay(RandomSource rand) {
        return this.tickRate + rand.nextInt(10);
    }

    @Override
    public void tick(BlockState blockState, ServerLevel world, BlockPos pos, RandomSource rand) {
        if (blockState.getValue(STATIC) || !world.isAreaLoaded(pos, 2)) return;

        boolean permanent = blockState.getValue(PERMANENT);
        if (!world.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
            if (this.removeOnNoFireTick && !permanent) world.removeBlock(pos, false);
            return;
        }

        if (this.removeOnTick) {
            world.removeBlock(pos, false);
            return;
        }

        int age = blockState.getValue(AGE);
        BlockState below = world.getBlockState(pos.below());
        boolean isOnFireSource = permanent || this.isBlockFireSource(below, world, pos.below(), Direction.UP);

        if (!isOnFireSource && this.canDie(world, pos) && rand.nextFloat() < 0.2F + age * 0.03F) {
            world.removeBlock(pos, false);
            return;
        }

        if (age < 15) {
            int inc = Math.round((float) rand.nextInt(this.agingRate) / 2F);
            int nextAge = Math.min(age + inc, 15);
            if (nextAge != age) {
                blockState = blockState.setValue(AGE, nextAge);
                world.setBlock(pos, blockState, 4);
                age = nextAge;
            }
        }

        if (this.loopTicks) world.scheduleTick(pos, this, getFireTickDelay(rand));

        if (!isOnFireSource) {
            if (!this.canNeighborCatchFire(world, pos)) {
                if (age > 3) world.removeBlock(pos, false);
                return;
            }
            if (!this.canCatchFire(world, pos.below(), Direction.UP) && age == 15 && rand.nextInt(4) == 0) {
                world.removeBlock(pos, false);
                return;
            }
        }

        if (this.spreadChance <= 0 || permanent) return;

        boolean highHumidity = world.getBiome(pos).get().getModifiedClimateSettings().downfall() > 0.85F;
        int humidityChance = highHumidity ? -50 : 0;

        this.tryCatchFire(world, pos.east(), 300 + humidityChance, rand, age, Direction.WEST);
        this.tryCatchFire(world, pos.west(), 300 + humidityChance, rand, age, Direction.EAST);
        this.tryCatchFire(world, pos.below(), 250 + humidityChance, rand, age, Direction.UP);
        this.tryCatchFire(world, pos.above(), 250 + humidityChance, rand, age, Direction.DOWN);
        this.tryCatchFire(world, pos.north(), 300 + humidityChance, rand, age, Direction.SOUTH);
        this.tryCatchFire(world, pos.south(), 300 + humidityChance, rand, age, Direction.NORTH);

        for (int ox = -1; ox <= 1; ++ox) {
            for (int oz = -1; oz <= 1; ++oz) {
                for (int oy = -1; oy <= 4; ++oy) {
                    if (ox == 0 && oy == 0 && oz == 0) continue;

                    int chance = 100;
                    if (oy > 1) chance += (oy - 1) * 100;

                    BlockPos p = pos.offset(ox, oy, oz);
                    int enc = this.getNeighborEncouragement(world, p);
                    if (enc <= 0) continue;

                    int i2 = (enc + 40 + world.getDifficulty().getId() * 7) / (age + 30);
                    if (highHumidity) i2 /= 2;

                    if (i2 > 0 && rand.nextInt(chance) <= i2 && (!world.isRaining() || !this.canDie(world, p))) {
                        int newAge = Math.min(15, age + rand.nextInt(5) / 4);
                        this.burnBlockReplace(world, p, newAge);
                    }
                }
            }
        }
    }


    /**
     * Returns true if any adjacent blocks can catch fire.
     **/
    protected boolean canNeighborCatchFire(Level worldIn, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (this.canCatchFire(worldIn, pos.relative(direction), direction.getOpposite())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the flammability of nearby blocks, highly flammable blocks that are near each other will help spread fire faster.
     **/
    protected int getNeighborEncouragement(Level worldIn, BlockPos pos) {
        if (!worldIn.isEmptyBlock(pos)) return 0;
        int i = 0;
        for (Direction d : Direction.values()) {
            BlockPos n = pos.relative(d);
            i = Math.max(worldIn.getBlockState(n).getFireSpreadSpeed(worldIn, n, d.getOpposite()), i);
        }
        return i;
    }


    /**
     * Attempts to ignite the position.
     **/
    protected void tryCatchFire(Level world, BlockPos pos, int chance, RandomSource random, int age, Direction face) {
        BlockState target = world.getBlockState(pos);
        int flammability = target.getFlammability(world, pos, face);

        int chanceAdj = chance;
        if (this.spreadChance != 1f) {
            float f = Math.max(0.001f, this.spreadChance);
            chanceAdj = (int) Math.max(1, Math.round(chance / f));
        }

        if (random.nextInt(chanceAdj) < flammability) {
            target.onCaughtFire(world, pos, face, null);
            if (random.nextInt(chanceAdj + 10) < 5 && !world.isRainingAt(pos)) {
                int newAge = Math.min(15, age + random.nextInt(5) / 4);
                this.burnBlockReplace(world, pos, newAge);
            } else {
                this.burnBlockDestroy(world, pos);
            }
            if (this.triggerTNT && target.getBlock() instanceof TntBlock) {
                TntBlock.explode(world, pos);
            }
        }
    }


    /**
     * Burns away a block, typically replacing it with this fire block, but can change it to other things depending on the type of fire block.
     **/
    public void burnBlockReplace(Level world, BlockPos pos, int newFireAge) {
        world.setBlock(pos, this.defaultBlockState().setValue(AGE, newFireAge).setValue(PERMANENT, false).setValue(STATIC, false), 3);
    }


    /**
     * Burns away a block, typically setting it to air but can change it to other things depending on the type of fire block.
     **/
    public void burnBlockDestroy(Level world, BlockPos pos) {
        world.removeBlock(pos, false);
    }

    /**
     * Returns true if the block at the provided position and face can catch fire.
     **/
    public boolean canCatchFire(BlockGetter world, BlockPos pos, Direction face) {
        return world.getBlockState(pos).isFlammable(world, pos, face);
    }

    /**
     * Checks if the provided block is a fire source, can be overridden for custom sources.
     **/
    public boolean isBlockFireSource(BlockState state, LevelAccessor world, BlockPos pos, Direction side) {
        return state.isFireSource(world, pos, side);
    }

    /**
     * Returns how flammable the target block is.
     **/
    public int getBlockFlammability(BlockGetter world, BlockPos pos, Direction face) {
        return world.getBlockState(pos).getFlammability(world, pos, face);
    }

    /**
     * Returns true if this fire block should be extinguished, can check for rain and position, etc.
     **/
    protected boolean canDie(Level world, BlockPos pos) {
        return world.isRainingAt(pos) || world.isRainingAt(pos.west()) || world.isRainingAt(pos.east()) || world.isRainingAt(pos.north()) || world.isRainingAt(pos.south());
    }

    /**
     * Client side animation and sounds.
     **/
    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        if (random.nextInt(24) == 0) {
            world.playLocalSound(x + 0.5D, y + 0.5D, z + 0.5D, ObjectManager.getSound(this.blockName), SoundSource.BLOCKS, 0.5F + random.nextFloat(), random.nextFloat() * 0.7F + 0.3F, false);
        }
        super.animateTick(state, world, pos, random);
    }
}
