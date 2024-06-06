package com.lycanitesmobs.core.capabilities;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.ExtendedEntity;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityProviderEntity implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {
	private final ExtendedEntity instance = null;
	private final LazyOptional<ExtendedEntity> optional = LazyOptional.of(this::createInstance);;

	public ExtendedEntity createInstance() {
			if (instance == null) {
				return new ExtendedEntity();
			}
			return this.instance;
	}
	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
		return capability == LycanitesMobs.EXTENDED_ENTITY ? optional.cast() : LazyOptional.empty();
	}

	@Override
	public CompoundTag serializeNBT() {
		ExtendedEntityStorage storage = new ExtendedEntityStorage();
		return storage.writeNBT(LycanitesMobs.EXTENDED_ENTITY, createInstance(), null);
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		ExtendedEntityStorage storage = new ExtendedEntityStorage();
		storage.readNBT(LycanitesMobs.EXTENDED_ENTITY, createInstance(), null, nbt);
	}
}
