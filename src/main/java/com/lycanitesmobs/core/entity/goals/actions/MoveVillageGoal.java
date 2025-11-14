package com.lycanitesmobs.core.entity.goals.actions;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.util.EnumSet;


public class MoveVillageGoal extends Goal {
	// Targets:
    private BaseCreatureEntity host;
    
    // Properties:
    private int frequency = 200;
    private boolean isNocturnal = true;
    private BlockPos blockPos;
	
	// ==================================================
 	//                    Constructor
 	// ==================================================
    public MoveVillageGoal(BaseCreatureEntity setHost) {
        this.host = setHost;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }
    
    
    // ==================================================
  	//                  Set Properties
  	// ==================================================
    public MoveVillageGoal setFrequency(int frequency) {
    	this.frequency = frequency;
    	return this;
    }
    public MoveVillageGoal setNocturnal(boolean flag) {
    	this.isNocturnal = flag;
    	return this;
    }
	
    
	// ==================================================
 	//                  Should Execute
 	// ==================================================
	@Override
    public boolean canUse() {
        if (this.host.isVehicle()) {
            return false;
        } else if (this.isNocturnal && this.host.level().isDay()) {
            return false;
        } else if (this.host.getRandom().nextInt(this.frequency) != 0) {
            return false;
        } else {
            ServerLevel serverWorld = (ServerLevel)this.host.level();
            BlockPos blockPos = new BlockPos(this.host.blockPosition());
            if (!serverWorld.isCloseToVillage(blockPos, 6)) {
                return false;
            } else {
                Vec3 lvt_3_1_ = LandRandomPos.getPos(this.host, 15, 7, (p_220755_1_) -> {
                    return (-serverWorld.getBlockFloorHeight(p_220755_1_));
                });
                if (lvt_3_1_ == null) return false;
                var x = LMHelperClass.convertObjectToDesired(lvt_3_1_.x, "integer");
                var y = LMHelperClass.convertObjectToDesired(lvt_3_1_.y, "double");
                var z = LMHelperClass.convertObjectToDesired(lvt_3_1_.z, "double");
                this.blockPos = new BlockPos((Integer) x, (Integer)y, (Integer)z);
                return true;
            }
        }
    }
	
    
	// ==================================================
 	//                Continue Executing
 	// ==================================================
	@Override
    public boolean canContinueToUse() {
        return this.blockPos != null && !this.host.getNavigation().isDone() && this.host.getNavigation().getTargetPos().equals(this.blockPos);
    }

    
    // ==================================================
    //                     Update
    // ==================================================
    @Override
    public void tick() {
        if (this.blockPos != null) {
            PathNavigation lvt_1_1_ = this.host.getNavigation();
            Vec3i v3 = (Vec3i) LMHelperClass.convertObjectToDesired(this.host.position(), "vec3i");
            if (lvt_1_1_.isDone() && !this.blockPos.closerThan(v3, 10.0D)) {
                Vector3d lvt_2_1_ = new Vector3d(this.blockPos.getX(), this.blockPos.getY(), this.blockPos.getZ());
                Vector3d lvt_3_1_ = new Vector3d(this.host.position().x(), this.host.position().y(), this.host.position().z());
                Vector3d lvt_4_1_ = lvt_3_1_.sub(lvt_2_1_);
                lvt_2_1_ = lvt_4_1_.set(0.4D).add(lvt_2_1_);
                Vector3d lvt_5_1_ = lvt_2_1_.sub(lvt_3_1_).normalize().set(10.0D).add(lvt_3_1_);
                Vec3i vec3i = (Vec3i) LMHelperClass.convertObjectToDesired(lvt_5_1_, "vec3i");
                BlockPos lvt_6_1_ = new BlockPos(vec3i);
                lvt_6_1_ = this.host.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, lvt_6_1_);
                if (!lvt_1_1_.moveTo(lvt_6_1_.getX(), lvt_6_1_.getY(), lvt_6_1_.getZ(), 1.0D)) {
                    this.moveRandomly();
                }
            }

        }
    }

    private void moveRandomly() {
        RandomSource lvt_1_1_ = this.host.getRandom();
        BlockPos lvt_2_1_ = this.host.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.host.blockPosition().offset(-8 + lvt_1_1_.nextInt(16), 0, -8 + lvt_1_1_.nextInt(16)));
        this.host.getNavigation().moveTo((double)lvt_2_1_.getX(), (double)lvt_2_1_.getY(), (double)lvt_2_1_.getZ(), 1.0D);
    }
}
