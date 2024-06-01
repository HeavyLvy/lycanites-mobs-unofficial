package com.lycanitesmobs.core.container;

import com.lycanitesmobs.core.tileentity.TileEntitySummoningPedestal;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SummoningPedestalContainerProvider implements Nameable, MenuProvider {
	public TileEntitySummoningPedestal summoningPedestal;

	public SummoningPedestalContainerProvider(@Nonnull TileEntitySummoningPedestal summoningPedestal) {
		this.summoningPedestal = summoningPedestal;
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
		return new SummoningPedestalContainer(windowId, playerInventory, this.summoningPedestal);
	}

	@Override
	public Component getName() {
		return getDisplayName();
	}

	@Override
	public MutableComponent getDisplayName() {
		return this.summoningPedestal.getName();
	}
}
