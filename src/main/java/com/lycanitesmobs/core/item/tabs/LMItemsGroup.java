package com.lycanitesmobs.core.item.tabs;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.manager.ObjectManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LMItemsGroup {
    public CreativeModeTab.Builder builder = CreativeModeTab.builder();
    public static final List<String> itemNames = new ArrayList<>();

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
                    // Sort the itemNames list alphabetically
                    Collections.sort(itemNames);
                    for (String name : itemNames) {
                        if (ObjectManager.getItem(name) != null) {
                            entries.accept(ObjectManager.getItem(name));
                        }
                    }
                });
    }

    // ========== Tab Icon ==========
    @OnlyIn(Dist.CLIENT)
    public ItemStack getIconItem() {
        if (ObjectManager.getItem("soulgazer") != null)
            return new ItemStack(ObjectManager.getItem("soulgazer"));
        else if (ObjectManager.getItem("poisongland") != null)
            return new ItemStack(ObjectManager.getItem("poisongland"));
        else
            return new ItemStack(Items.EMERALD);
    }
}