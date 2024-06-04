package com.lycanitesmobs.core.block.fluid;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.info.ElementInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;

public class BaseFluidBlock extends LiquidBlock {
	public String blockName;
	protected ElementInfo element;
	protected boolean destroyItems = true;
    public ResourceLocation registryName;

    public BaseFluidBlock(Supplier<? extends FlowingFluid> p_54694_, Properties p_54695_) {
        super(p_54694_, p_54695_);
    }

	public BaseFluidBlock(Supplier<? extends FlowingFluid> fluidSupplier, BlockBehaviour.Properties properties, String name, ElementInfo element, boolean destroyItems) {
        super(fluidSupplier,properties);
        this.setRegistryName(LycanitesMobs.MODID, name);
        this.blockName = name;
        this.element = element;
        this.destroyItems = destroyItems;
	}
    public ResourceLocation getRegistryName() {
        return registryName;
    }
    public ResourceLocation setRegistryName(String modID, String blockName) {
        return registryName = new ResourceLocation(modID, blockName);
    }
    public void setup() {
        this.setRegistryName(LycanitesMobs.MODID, this.blockName);
    }
	public ElementInfo getElement() {
		return this.element;
	}

	@Override
    public void neighborChanged(BlockState blockState, Level world, BlockPos blockPos, Block neighborBlock, BlockPos neighborBlockPos, boolean someBoolean) {
	    super.neighborChanged(blockState, world, blockPos, neighborBlock, neighborBlockPos, someBoolean);
		if (neighborBlock == this) {
	        return;
        }
        BlockState neighborBlockState = world.getBlockState(neighborBlockPos);
		if (neighborBlockState.getBlock() == this) {
		    return;
        }
        if (this.shouldSpreadLiquid(world, neighborBlockPos, blockState)) {
            world.getFluidTicks().schedule(new ScheduledTick<>(blockState.getFluidState().getType(),blockPos, this.getFluid().getTickDelay(world), 0));
        }
    }

    public boolean shouldSpreadLiquid(Level world, BlockPos neighborBlockPos, BlockState blockState) {
        BlockState neighborBlockState = world.getBlockState(neighborBlockPos);
        if (neighborBlockState.getBlock() instanceof LiquidBlock) {
            return false;
        }
        return true;
    }


    @Override
    public void entityInside(BlockState blockState, Level world, BlockPos pos, Entity entity) {
		if (this.destroyItems && (entity instanceof ItemEntity || entity instanceof ExperienceOrb)) {
			entity.kill();
		}
        super.entityInside(blockState, world, pos, entity);
    }

    /** Client side animation and sounds. **/
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
		double x = pos.getX();
		double y = pos.getY();
		double z = pos.getZ();
		if(random.nextInt(52) == 0) {
			world.playLocalSound(x + 0.5D, y + 0.5D, z + 0.5D, Objects.requireNonNull(ObjectManager.getSound(this.blockName)), SoundSource.BLOCKS, 0.5F + random.nextFloat(), random.nextFloat() * 0.7F + 0.3F, false);
		}
		super.animateTick(state, world, pos, random);
    }
}
