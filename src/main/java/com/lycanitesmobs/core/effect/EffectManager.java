package com.lycanitesmobs.core.effect;

import com.google.common.base.Predicate;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.api.IGroupBoss;
import com.lycanitesmobs.core.data.config.ConfigExtra;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.capabilities.entity.ExtendedEntity;
import com.lycanitesmobs.core.entity.special.FearEntity;
import com.lycanitesmobs.core.data.info.creature.CreatureGroup;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.network.message.MessageEntityVelocity;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.UUID;

public class EffectManager {

    // Global Settings:
    public boolean disableNausea = false;

    // ==================================================
    //                    Initialize
    // ==================================================
    public EffectManager() {
        ObjectManager.addPotionEffect("paralysis", true, 0xFFFF00, false);
        ObjectManager.addPotionEffect("penetration", true, 0x222222, false);
        ObjectManager.addPotionEffect("recklessness", true, 0xFF0044, false); // TODO Implement
        ObjectManager.addPotionEffect("rage", true, 0xFF4400, false); // TODO Implement
        ObjectManager.addPotionEffect("weight", true, 0x000022, false);
        ObjectManager.addPotionEffect("fear", true, 0x220022, false);
        ObjectManager.addPotionEffect("decay", true, 0x110033, false);
        ObjectManager.addPotionEffect("insomnia", true, 0x002222, false);
        ObjectManager.addPotionEffect("instability", true, 0x004422, false);
        ObjectManager.addPotionEffect("lifeleak", true, 0x0055FF, false);
        ObjectManager.addPotionEffect("bleed", true, 0xFF2222, false);
        ObjectManager.addPotionEffect("plague", true, 0x220066, false);
        ObjectManager.addPotionEffect("aphagia", true, 0xFFDDDD, false);
        ObjectManager.addPotionEffect("smited", true, 0xDDDDFF, false);
        ObjectManager.addPotionEffect("smouldering", true, 0xDD0000, false);

        ObjectManager.addPotionEffect("leech", false, 0x00FF99, true);
        ObjectManager.addPotionEffect("swiftswimming", false, 0x0000FF, true);
        ObjectManager.addPotionEffect("fallresist", false, 0xDDFFFF, true);
        ObjectManager.addPotionEffect("rejuvenation", false, 0x99FFBB, true);
        ObjectManager.addPotionEffect("immunization", false, 0x66FFBB, true);
        ObjectManager.addPotionEffect("cleansed", false, 0x66BBFF, true);
        ObjectManager.addPotionEffect("repulsion", false, 0xBC532E, true);
        ObjectManager.addPotionEffect("heataura", false, 0x996600, true); // TODO Implement
        ObjectManager.addPotionEffect("staticaura", false, 0xFFBB551, true); // TODO Implement
        ObjectManager.addPotionEffect("freezeaura", false, 0x55BBFF, true); // TODO Implement
        ObjectManager.addPotionEffect("envenom", false, 0x44DD66, true); // TODO Implement

        // Event Listener:
        MinecraftForge.EVENT_BUS.register(this);

        // Effect Sounds:
        ObjectManager.addSound("effect_fear", "effect.fear");
    }

    public static EffectManager INSTANCE = new EffectManager();

    public static EffectManager getInstance() {
        return INSTANCE;
    }


    // ==================================================
    //                     Utility
    // ==================================================

    /**
     * Get entities that are near the provided entity.
     **/
    public <T extends Entity> List<T> getNearbyEntities(Entity searchEntity, Class<? extends T> clazz, final Class filterClass, double range) {

        return (List<T>) searchEntity.getCommandSenderWorld().getEntitiesOfClass(clazz, searchEntity.getBoundingBox().inflate(range, range, range), (Predicate<Entity>) entity -> {
            if (filterClass == null)
                return true;
            return filterClass.isAssignableFrom(entity.getClass());
        });
    }

    /**
     * Determines if the provided entity is considered a boss.
     *
     * @param entity The entity to check.
     * @return True if the entity is a boss.
     */
    public boolean isBoss(Entity entity) {
        if (entity instanceof EnderDragon || entity instanceof WitherBoss) {
            return true;
        }
        if (entity instanceof BaseCreatureEntity) {
            BaseCreatureEntity creature = (BaseCreatureEntity) entity;
            return creature.isBoss();
        }
        CreatureGroup bossGroup = CreatureManager.getInstance().getCreatureGroup("boss");
        if (bossGroup != null) {
            return bossGroup.hasEntity(entity);
        }
        return false;
    }
}
