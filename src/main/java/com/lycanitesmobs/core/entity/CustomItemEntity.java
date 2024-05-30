package com.lycanitesmobs.core.entity;

import com.lycanitesmobs.core.util.LMHelperClass;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CustomItemEntity extends ItemEntity {
	protected boolean canBurn = true;

	// ==================================================
   	//                     Constructor
   	// ==================================================
	public CustomItemEntity(Level world) {
		super(EntityType.ITEM, world);
	}
    public CustomItemEntity(Level world, double x, double y, double z, ItemStack itemStack) {
        super(world, x, y, z, itemStack);
    }
    

	// ==================================================
   	//                   Taking Damage
   	// ==================================================
    public boolean hurt(DamageSource damageSource, float damageAmount) {
		DamageSource fireDamage = LMHelperClass.damageSourceFromResourceKey(this.level(),DamageTypes.ON_FIRE);
    	if(this.isInvulnerableTo(fireDamage) || !this.canBurn) {
    		if(this.level().damageSources().onFire() == damageSource ||
					this.level().damageSources().inFire() == damageSource) {
    			return false;
    		}
    	}
        return super.hurt(damageSource, damageAmount);
    }

    public void setCanBurn(boolean canBurn) {
		this.canBurn = canBurn;
	}
    

	// ==================================================
   	//                    Immunities
   	// ==================================================
	// TODO Fire Immunity handled by EntityType
    
    
    // ==================================================
   	//                  Network Flags
   	// ==================================================
    protected void setSharedFlag(int flagID, boolean value) {
		DamageSource fireDamage = LMHelperClass.damageSourceFromResourceKey(this.level(),DamageTypes.ON_FIRE);
		if(flagID == 0 && this.isInvulnerableTo(fireDamage))
    		value = false;
        super.setSharedFlag(flagID, value);
    }
}
