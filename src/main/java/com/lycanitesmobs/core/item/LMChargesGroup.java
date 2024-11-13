package com.lycanitesmobs.core.item;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.info.ItemInfo;
import com.lycanitesmobs.core.info.ItemManager;
import com.lycanitesmobs.core.util.LMHelperClass;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

public class LMChargesGroup {
    public static final List<String> chargeNames = new ArrayList<>();
    public CreativeModeTab.Builder builder = CreativeModeTab.builder();

    // ========== Constructor ==========
    public LMChargesGroup() {
        init();
    }

    public static CreativeModeTab.Builder getBuilder() {
        return new LMChargesGroup().builder;
    }

    public void init() {
        builder = builder.title(Component.translatable("itemGroup." + LycanitesMobs.MODID + ".charges"))
                .icon(this::getIconItem)
                .displayItems((enabledFeatures, entries) -> {
                    for (String info : chargeNames) {
                        if (ObjectManager.getItem(info) != null) {
                            LMHelperClass.logInfoMessage("Are charges empty?: " + String.valueOf(chargeNames.isEmpty()));
                            entries.accept(ObjectManager.getItem(info));
                        }
                    }
                });
    }

    // ========== Tab Icon ==========
    @OnlyIn(Dist.CLIENT)
    public ItemStack getIconItem() {
        if (ObjectManager.getItem("hellfireballcharge") != null)
            return new ItemStack(ObjectManager.getItem("hellfireballcharge"));
        else if (ObjectManager.getItem("venomshotcharge") != null)
            return new ItemStack(ObjectManager.getItem("venomshotcharge"));
        else
            return new ItemStack(Items.FIRE_CHARGE);
    }
}