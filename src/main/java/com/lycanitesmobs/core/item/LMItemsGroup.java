package com.lycanitesmobs.core.item;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.info.ItemInfo;
import com.lycanitesmobs.core.info.ItemManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LMItemsGroup {
	public CreativeModeTab.Builder builder = CreativeModeTab.builder();
	// ========== Constructor ==========
	public LMItemsGroup() {
		init();
	}
	public static CreativeModeTab.Builder getBuilder() {
		return new LMItemsGroup().builder;
	}
	public void init() {
			builder = builder
					.title(Component.translatable("itemGroup." + LycanitesMobs.MODID + ".items"))
					.icon(this::getIconItem)
					.displayItems((enabledFeatures, entries) -> {
						for(ItemInfo info : ItemManager.items.values()) {
							entries.accept(info.item);
						}
					});
	}
	// ========== Tab Icon ==========
	@OnlyIn(Dist.CLIENT)
	public ItemStack getIconItem() {
		if(ObjectManager.getItem("soulgazer") != null)
			return new ItemStack(ObjectManager.getItem("soulgazer"));
		else if(ObjectManager.getItem("poisongland") != null)
			return new ItemStack(ObjectManager.getItem("poisongland"));
		else
			return new ItemStack(Items.EMERALD);
	}
}