package com.lycanitesmobs.core.entity.spawner.location;

import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.util.helpers.JSONHelper;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.registries.RegistryManager;

import java.util.ArrayList;
import java.util.List;

public class StructureSpawnLocation extends RandomSpawnLocation {

    /**
     * The structure ids to use as spawn locations.
     **/
    public List<ResourceLocation> structureIds = new ArrayList<>();

    /**
     * How close to the player (in blocks) Structures must be. Default: 100.
     **/
    public int structureRange = 100;


    @Override
    public void loadFromJSON(JsonObject json) {
        super.loadFromJSON(json);

        if (json.has("structureIds")) {
            List<String> structureIdStrings = JSONHelper.getJsonStrings(json.getAsJsonArray("structureIds"));
            structureIds.clear();
            for (String structureIdName : structureIdStrings) {
                structureIds.add(new ResourceLocation(structureIdName));
            }
        }

        if (json.has("structureRange"))
            this.structureRange = json.get("structureRange").getAsInt();
    }


    @Override
    public List<BlockPos> getSpawnPositions(Level world, Player player, BlockPos triggerPos) {
        if (!(world instanceof ServerLevel)) {
            return new ArrayList<>();
        }

        List<BlockPos> spawnPositions = new ArrayList<>();
        for (ResourceLocation structureId : this.structureIds) {
            Structure spawnStructure = null;
            if (RegistryManager.ACTIVE.getRegistry(Registries.STRUCTURE.location()) != null) {
                spawnStructure = (Structure) RegistryManager.ACTIVE.getRegistry(Registries.STRUCTURE.location()).getValue(structureId);
            }

            TagKey<Structure> lycaniteStructures = TagKey.create(Registries.STRUCTURE, new ResourceLocation(LycanitesMobs.MODID, "lycanitestructures"));

            if (spawnStructure == null) {
                continue;
            }
            BlockPos structurePos = null;
            try {
                structurePos = ((ServerLevel) world).findNearestMapStructure(lycaniteStructures, triggerPos, this.structureRange, false);
            } catch (Exception e) {
            }

            // No Structure:
            if (structurePos == null) {
                continue;
            }

            // Too Far:
            double structureDistance = Math.sqrt(structurePos.distSqr(triggerPos));
            if (structureDistance > this.structureRange) {
                continue;
            }

            // Structure Found:
            spawnPositions.addAll(super.getSpawnPositions(world, player, structurePos));
        }

        return this.sortSpawnPositions(spawnPositions, world, triggerPos);
    }

}
