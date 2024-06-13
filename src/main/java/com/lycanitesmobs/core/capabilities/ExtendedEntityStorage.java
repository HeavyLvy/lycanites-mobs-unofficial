package com.lycanitesmobs.core.capabilities;

import com.lycanitesmobs.core.entity.ExtendedEntity;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExtendedEntityStorage {

    // ==================================================
    //                        NBT
    // ==================================================
    // ========== Read ===========
    /** Reads a list of Creature Knowledge from a player's NBTTag. **/
    public void readNBT(Capability<ExtendedEntity> capability, ExtendedEntity instance, Direction facing, CompoundTag nbt) {
        if(!(instance instanceof ExtendedEntity) || !(nbt instanceof CompoundTag))
            return;
        ExtendedEntity extendedEntity = (ExtendedEntity)instance;
        CompoundTag extTagCompound = (CompoundTag)nbt;
        extendedEntity.readNBT(extTagCompound);
    }

    // ========== Write ==========
    /** Writes a list of Creature Knowledge to a player's NBTTag. **/
    public CompoundTag writeNBT(Capability<ExtendedEntity> capability, ExtendedEntity instance, Direction facing) {
        if(!(instance instanceof ExtendedEntity))
            return null;
        ExtendedEntity extendedEntity = instance;
        CompoundTag extTagCompound = new CompoundTag();
        extendedEntity.writeNBT(extTagCompound);
        return extTagCompound;
    }

}
