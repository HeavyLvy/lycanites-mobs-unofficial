package com.lycanitesmobs.core.block;

import com.lycanitesmobs.core.container.SummoningPedestalContainerProvider;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.info.ModInfo;
import com.lycanitesmobs.core.tileentity.TileEntitySummoningPedestal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class BlockSummoningPedestal extends BlockBase {
    public enum EnumSummoningPedestal {
        NONE(0),
        CLIENT(1),
        PLAYER(2);

        private int ownerId;
        EnumSummoningPedestal(int ownerId) {
            this.ownerId = ownerId;
        }

        public int getOwnerId() {
            return this.ownerId;
        }
    }

    public static final IntegerProperty PROPERTY_OWNER = IntegerProperty.create("owner", 0, 2);


	// ==================================================
	//                   Constructor
	// ==================================================
	public BlockSummoningPedestal(Block.Properties properties, ModInfo group) {
		super(properties);

        this.group = group;
        this.blockName = "summoningpedestal";
        this.registerDefaultState(this.getStateDefinition().any().setValue(PROPERTY_OWNER, EnumSummoningPedestal.NONE.ownerId));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(PROPERTY_OWNER);
    }

    @Override
    public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        TileEntity tileentity = worldIn.getBlockEntity(pos);
        if(tileentity instanceof TileEntitySummoningPedestal) {
            TileEntitySummoningPedestal tileEntitySummoningPedestal = (TileEntitySummoningPedestal)tileentity;
            tileEntitySummoningPedestal.setOwner(placer);
            if(placer instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity)placer;
                ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
                if(playerExt != null) {
                    tileEntitySummoningPedestal.setSummonSet(playerExt.getSelectedSummonSet());
                }
            }
        }
    }

    @Override
    public boolean hasTileEntity(BlockState blockState) {
	    return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState blockState, IBlockReader world) {
        return new TileEntitySummoningPedestal();
    }

    public static void setState(EnumSummoningPedestal owner, World worldIn, BlockPos pos) {
        BlockState blockState = worldIn.getBlockState(pos);
        TileEntity tileentity = worldIn.getBlockEntity(pos);
        worldIn.setBlock(pos, blockState.getBlock().defaultBlockState().setValue(PROPERTY_OWNER, owner.getOwnerId()), 3);

        if (tileentity != null) {
            tileentity.clearRemoved();
            worldIn.setBlockEntity(pos, tileentity);
        }
    }

    @Override //onBlockActivated()
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if(!world.isClientSide() && player instanceof ServerPlayerEntity) {
            TileEntity tileEntity = world.getBlockEntity(pos);
            if(tileEntity instanceof TileEntitySummoningPedestal) {
                ((ServerPlayerEntity)player).connection.send(tileEntity.getUpdatePacket());
                NetworkHooks.openGui((ServerPlayerEntity)player, new SummoningPedestalContainerProvider((TileEntitySummoningPedestal)tileEntity), buf -> buf.writeBlockPos(pos));
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public boolean triggerEvent(BlockState state, World worldIn, BlockPos pos, int eventID, int eventParam) {
        TileEntity tileEntity = worldIn.getBlockEntity(pos);
        return tileEntity != null && tileEntity.triggerEvent(eventID, eventParam);
    }
}
