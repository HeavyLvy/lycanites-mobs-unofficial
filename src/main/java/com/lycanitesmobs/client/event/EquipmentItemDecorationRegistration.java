package com.lycanitesmobs.client.event;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.renderer.item.EquipmentItemDecorator;
import com.lycanitesmobs.core.manager.ObjectManager;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = LycanitesMobs.MODID, value = Dist.CLIENT)
public class EquipmentItemDecorationRegistration {
    @SubscribeEvent
    public static void registerItemDecorations(RegisterItemDecorationsEvent event) {
        Item equipmentItem = ObjectManager.getItem("equipment");
        if (equipmentItem != null) {
            event.register(equipmentItem, new EquipmentItemDecorator());
        }
    }
}
