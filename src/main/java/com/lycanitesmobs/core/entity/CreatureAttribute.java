package com.lycanitesmobs.core.entity;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;

/**
 * Replacement for vanilla's MobType, which was removed in 1.20.5 in favour of entity type tags.
 * Lycanites creatures set this per class; other entities derive it from vanilla's tags.
 */
public enum CreatureAttribute {
    UNDEFINED,
    UNDEAD,
    ARTHROPOD,
    ILLAGER,
    WATER;

    public static CreatureAttribute of(Entity entity) {
        if (entity instanceof BaseCreatureEntity creature) {
            return creature.getCreatureAttribute();
        }
        if (entity.getType().is(EntityTypeTags.UNDEAD)) return UNDEAD;
        if (entity.getType().is(EntityTypeTags.ARTHROPOD)) return ARTHROPOD;
        if (entity.getType().is(EntityTypeTags.ILLAGER)) return ILLAGER;
        if (entity.getType().is(EntityTypeTags.AQUATIC)) return WATER;
        return UNDEFINED;
    }
}
