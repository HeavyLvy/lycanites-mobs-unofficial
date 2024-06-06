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

public class CapabilityProviderPlayer implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {
	private final ExtendedPlayer instance = null;
	private final LazyOptional<ExtendedPlayer> optional= LazyOptional.of(this::createInstance);;


	public ExtendedPlayer createInstance() {
		if (instance == null) {
			return new ExtendedPlayer();
		}
		return this.instance;
	}
	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
		return capability == LycanitesMobs.EXTENDED_PLAYER ? optional.cast() : LazyOptional.empty();
	}

	@Override
	public CompoundTag serializeNBT() {
		ExtendedPlayerStorage storage = new ExtendedPlayerStorage();
		return storage.writeNBT(LycanitesMobs.EXTENDED_PLAYER, this.getCapability(LycanitesMobs.EXTENDED_PLAYER, null).orElse(null), null);
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		ExtendedPlayerStorage storage = new ExtendedPlayerStorage();
		storage.readNBT(LycanitesMobs.EXTENDED_PLAYER, this.getCapability(LycanitesMobs.EXTENDED_PLAYER, null).orElse(null), null, nbt);
	}
}
