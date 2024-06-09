package com.lycanitesmobs.core.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public abstract class TileEntityBase extends BlockEntity implements WorldlyContainer {
    public ResourceLocation registryName = null;
    /**
     * Constructor
     */
    public TileEntityBase(BlockPos pos, BlockState state) {
        super(BlockEntityType.CHEST, pos, state);

    }

    public ResourceLocation getRegistryName() {
        return registryName;
    }
    public ResourceLocation setRegistryName(String modID, String blockName) {
        return registryName = new ResourceLocation(modID, blockName);
    }
    /**
     * Gets the Tile Entity Type used by this Tile Entity. Should be overridden.
     *
     * @return The Tile Entity Type.
     */
    @Override
    public BlockEntityType<?> getType() {
        return super.getType();
    }


    /**
     * The main update called every tick.
     */
    public void tick()  {}

    @Override
    public boolean stillValid(Player player) {
        if (this.isRemoved()) {
            return false;
        }
        return this.getBlockPos().distSqr(player.blockPosition()) < 16F;
    }

    /**
     * Removes this Tile Entity.
     */
    @Override
    public void setRemoved() {
        super.setRemoved();
    }

    /**
     * Called when receiving an event from a client, used for opening GUIs, etc.
     * @param eventID The ID of the event.
     * @param eventArg The argument ID of the event.
     * @return
     */
    @Override
    public boolean triggerEvent(int eventID, int eventArg) {
        return false;
    }

    /**
     * Gets the update packet for this Tile Entity.
     * @return The update packet to use or null if not needed.
     */
    @Override
    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return super.getUpdatePacket();
    }

    /**
     * Called when this Tile Entity receives a Data Packet.
     * @param net The Network Manager.
     * @param pkt The packet received.
     */
    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
    }

    /**
     * Called when a GUI opened by this Tile Entity has a button pressed.
     * @param buttonId The ID of the button pressed.
     */
    public void onGuiButton(int buttonId) {}

    /**
     * Reads from saved NBT data.
     * @param nbtTagCompound The NBT to read from.
     */
    @Override
    public void load(CompoundTag nbtTagCompound) {
        super.load(nbtTagCompound);
    }

   

    /**
     * Writes to NBT data.
     * @param nbtTagCompound The NBT data to write to.
     * @return The written to NBT data.
     */
    @Override
    public void saveAdditional(CompoundTag nbtTagCompound) {
        super.saveAdditional(nbtTagCompound);
    }



    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[] {};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStackIn, @Nullable Direction direction) {
        return false;
    }




    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return false;
    }
}
