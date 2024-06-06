package com.lycanitesmobs.core.capabilities;

import com.lycanitesmobs.core.entity.ExtendedEntity;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;

public class ExtendedEntityStorage  {

    // ==================================================
    //                        NBT
    // ==================================================
    // ========== Read ===========
    /** Reads a list of Creature Knowledge from a player's NBTTag. **/
    public void readNBT(Capability<ExtendedEntity> capability, ExtendedEntity instance, Direction facing, CompoundTag nbt) {
        if(!(instance instanceof ExtendedEntity) || !(nbt instanceof CompoundTag))
            return;
        instance.readNBT(nbt);
    }

    // ========== Write ==========
    /** Writes a list of Creature Knowledge to a player's NBTTag. **/
    public CompoundTag writeNBT(Capability<ExtendedEntity> capability, ExtendedEntity instance, Direction facing) {
        if(!(instance instanceof ExtendedEntity))
            return null;
        CompoundTag extTagCompound = new CompoundTag();
        instance.writeNBT(extTagCompound);
        return extTagCompound;
    }
}
