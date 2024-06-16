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

public class LMEquipmentPartsGroup {
	public CreativeModeTab.Builder builder = CreativeModeTab.builder();

	private ItemStack iconStack = ItemStack.EMPTY;
	private boolean fallbackIcon = false;

	public LMEquipmentPartsGroup() {
		init();
	}
	public static CreativeModeTab.Builder getBuilder() {
		return new LMEquipmentPartsGroup().builder;
	}
	public void init() {
		builder = builder.title(Component.translatable("itemGroup." + LycanitesMobs.MODID + ".equipmentparts"))
				.icon(this::getIconItem)
				.displayItems((enabledFeatures, entries) -> {
					for(ItemInfo info : ItemManager.items.values()) {
						entries.accept(info.getItem());
					}
				});
	}
	@OnlyIn(Dist.CLIENT)
	public ItemStack getIconItem() {
		this.fallbackIcon = false;
		if(ObjectManager.getItem("equipmentpart_eechetikarm") != null)
			return new ItemStack(ObjectManager.getItem("equipmentpart_eechetikarm"));
		if(ObjectManager.getItem("equipmentpart_darklingskull") != null)
			return new ItemStack(ObjectManager.getItem("equipmentpart_darklingskull"));
		if(ObjectManager.getItem("equipmentpart_grueclaw") != null)
			return new ItemStack(ObjectManager.getItem("equipmentpart_grueclaw"));
		if(ObjectManager.getItem("equipmentpart_xaphanspine") != null)
			return new ItemStack(ObjectManager.getItem("equipmentpart_xaphanspine"));
		if(ObjectManager.getItem("equipmentpart_geonachfist") != null)
			return new ItemStack(ObjectManager.getItem("equipmentpart_geonachfist"));

		this.fallbackIcon = true;
		if(ObjectManager.getItem("geistliver") != null)
			return new ItemStack(ObjectManager.getItem("geistliver"));
		return new ItemStack(Items.BONE);
	}
}