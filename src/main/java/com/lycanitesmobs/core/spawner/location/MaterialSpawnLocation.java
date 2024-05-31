package com.lycanitesmobs.core.spawner.location;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.helpers.JSONHelper;
import com.lycanitesmobs.core.util.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class MaterialSpawnLocation extends BlockSpawnLocation {
    /** A list of block materials to either spawn in or not spawn in depending on if it is a blacklist or whitelist. **/
    public List<BlockBehaviour.Properties> materials = new ArrayList<>();


	@Override
	public void loadFromJSON(JsonObject json) {
		this.materials = JSONHelper.getJsonMaterials(json);

		super.loadFromJSON(json);
	}

	/** Returns if the provided block position is valid. **/
	@Override
	public boolean isValidBlock(Level world, BlockPos blockPos) {
		BlockState blockState = world.getBlockState(blockPos);

		if(!this.surface || !this.underground) {
			if(world.canSeeSkyFromBelowWater(blockPos)) {
				if(!this.surface) {
					return false;
				}
			}
			else {
				if(!this.underground) {
					return false;
				}
			}
		}

		if("blacklist".equalsIgnoreCase(this.listType)) {
			return !this.materials.contains(blockState);
		}
		else {
			return this.materials.contains(blockState);
		}
	}
}
