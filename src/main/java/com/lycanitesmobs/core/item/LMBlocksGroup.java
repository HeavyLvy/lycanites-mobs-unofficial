package com.lycanitesmobs.core.item;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.info.ItemInfo;
import com.lycanitesmobs.core.info.ItemManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LMBlocksGroup{
	public CreativeModeTab.Builder builder = CreativeModeTab.builder();

	// ========== Constructor ==========
	public LMBlocksGroup() {
		init();
	}
	public void init() {
		builder.title(Component.translatable("itemGroup." + LycanitesMobs.MODID + ".blocks"))
				.icon(this::getIconItem)
				.displayItems((enabledFeatures, entries) -> {
					for(ItemInfo info : ItemManager.items.values()) {
						entries.accept(info.item);
					}
				}).build();
	}
	public static CreativeModeTab.Builder getBuilder() {
		return new LMBlocksGroup().builder;
	}
	// ========== Tab Icon ==========
	@OnlyIn(Dist.CLIENT)
	public ItemStack getIconItem() {
		if(ObjectManager.getBlock("shadowcrystal") != null)
			return new ItemStack(Item.byBlock(ObjectManager.getBlock("shadowcrystal")));
		else if(ObjectManager.getBlock("summoningpedestal") != null)
			return new ItemStack(Item.byBlock(ObjectManager.getBlock("summoningpedestal")));
		else if(ObjectManager.getBlock("demoncrystal") != null)
			return new ItemStack(Item.byBlock(ObjectManager.getBlock("demoncrystal")));
		else
			return new ItemStack(Item.byBlock(Blocks.OBSIDIAN));
	}
}