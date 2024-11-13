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

import java.util.ArrayList;
import java.util.List;

public class LMCreaturesGroup {
    public static final List<String> creatureNames = new ArrayList<>();
    public CreativeModeTab.Builder builder = CreativeModeTab.builder();
    private ItemStack iconStack = ItemStack.EMPTY;
    private boolean fallbackIcon = false;

    public LMCreaturesGroup() {
        init();
    }

    public void init() {
        builder = builder.title(Component.translatable("itemGroup." + LycanitesMobs.MODID + ".creatures"))
                .icon(this::getIconItem)
                .displayItems((enabledFeatures, entries) -> {
                    for (String info : creatureNames) {
                        if (ObjectManager.getItem(info) != null) {
                            entries.accept(ObjectManager.getItem(info));
                        }
                    }
                });
    }

    public static CreativeModeTab.Builder getBuilder() {
        return new LMCreaturesGroup().builder;
    }

    @OnlyIn(Dist.CLIENT)
    public ItemStack getIconItem() {
        this.fallbackIcon = false;
        if (ObjectManager.getItem("beastspawn") != null)
            return new ItemStack(ObjectManager.getItem("beastspawn"));
        if (ObjectManager.getItem("demonspawn") != null)
            return new ItemStack(ObjectManager.getItem("demonspawn"));
        if (ObjectManager.getItem("avianspawn") != null)
            return new ItemStack(ObjectManager.getItem("avianspawn"));
        if (ObjectManager.getItem("arthropodspawn") != null)
            return new ItemStack(ObjectManager.getItem("arthropodspawn"));

        this.fallbackIcon = true;
        return new ItemStack(Items.CREEPER_SPAWN_EGG);
    }

}