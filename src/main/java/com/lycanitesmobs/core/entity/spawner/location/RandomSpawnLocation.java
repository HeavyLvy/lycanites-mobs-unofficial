package com.lycanitesmobs.core.entity.spawner.location;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.List;

public class RandomSpawnLocation extends BlockSpawnLocation {

    /**
     * How many random positions to select.
     **/
    protected int limit = 32;

    /**
     * If true positions require a solid walkable block underneath rather than using insideBlock.
     **/
    protected boolean solidGround = false;

    /**
     * This scales the x and z range values by this amount when on Easy or Peaceful Difficulty.
     **/
    protected double easyDifficultyRangeScale = 1.5D;

    /**
     * This scales the x and z range values by this amount when on Normal Difficulty.
     **/
    protected double normalDifficultyRangeScale = 1D;

    /**
     * This scales the x and z range values by this amount when on Hard and above Difficulties.
     **/
    protected double hardDifficultyRangeScale = 0.5D;


    @Override
    public void loadFromJSON(JsonObject json) {
        if (json.has("limit"))
            this.limit = json.get("limit").getAsInt();

        if (json.has("solidGround"))
            this.solidGround = json.get("solidGround").getAsBoolean();

        if (json.has("easyDifficultyRangeScale"))
            this.easyDifficultyRangeScale = json.get("easyDifficultyRangeScale").getAsDouble();

        if (json.has("normalDifficultyRangeScale"))
            this.normalDifficultyRangeScale = json.get("normalDifficultyRangeScale").getAsDouble();

        if (json.has("hardDifficultyRangeScale"))
            this.hardDifficultyRangeScale = json.get("hardDifficultyRangeScale").getAsDouble();

        this.listType = "whitelist";
        this.blockIds.add(LMHelperClass.convertToResourceLocation(Blocks.AIR).toString());
        this.blockIds.add(LMHelperClass.convertToResourceLocation(Blocks.CAVE_AIR).toString());
        this.blockIds.add(LMHelperClass.convertToResourceLocation(Blocks.TALL_GRASS).toString());
        super.loadFromJSON(json);
    }

    @Override
    public List<BlockPos> getSpawnPositions(Level world, Player player, BlockPos triggerPos) {
        List<BlockPos> spawnPositions = new ArrayList<>();
        LMHelperClass.logDebug("JSONSpawner", "Getting " + this.limit + " Random Spawn Positions");

        for (int i = 0; i < this.limit; i++) {
            BlockPos randomPos = this.getRandomPosition(world, player, triggerPos);
            if (randomPos != null) {
                spawnPositions.add(randomPos);
            }
        }

        return this.sortSpawnPositions(spawnPositions, world, triggerPos);
    }


    /**
     * Gets a random spawn position. Handles three cases:
     * 1. Surface-only (surface=true, underground=false): Uses heightmap to find ground level.
     * 2. Underground-only (surface=false, underground=true): Uses Y scan centered on trigger Y.
     * 3. Both (surface=true, underground=true): Uses heightmap for surface, Y scan centered on
     * heightmap Y (actual terrain level) for underground, with surface as fallback.
     *
     * @param world      The world to search for coordinates in.
     * @param player     Player that triggered the spawn.
     * @param triggerPos The trigger position to search around.
     * @return Returns a BlockPos or null if no coord was found.
     */
    public BlockPos getRandomPosition(Level world, Player player, BlockPos triggerPos) {
        int[] xz = this.getRandomXZCoord(world, triggerPos);
        int x = xz[0];
        int z = xz[1];

        // Try to find a surface spawn position using the heightmap when surface spawning is allowed.
        BlockPos surfacePos = null;
        if (this.surface) {
            surfacePos = this.findSurfaceSpawnPos(world, x, z);
        }

        // Surface-only: return the surface result directly (or null if none found).
        if (this.surface && !this.underground) {
            return surfacePos;
        }

        // Underground-only or both surface+underground: use Y scan for underground positions.
        // Key fix: when surface is allowed, center the Y scan on the heightmap Y (actual terrain level)
        // rather than the trigger Y (player position), so the scan window covers real terrain.
        // MOTION_BLOCKING_NO_LEAVES is preferred over WORLD_SURFACE here: it ignores leaf canopies,
        // so in forests the scan origin is the actual ground, not the treetop.
        int scanOriginY;
        if (this.surface) {
            scanOriginY = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z)).getY();
        } else {
            scanOriginY = triggerPos.getY();
        }

        int y = this.getRandomYCoord(world, new BlockPos(x, scanOriginY, z));
        if (y <= -1) {
            // Y scan found nothing; fall back to surface position if we have one.
            if (surfacePos != null) {
                LMHelperClass.logDebug("JSONSpawner", "Y scan found nothing, using surface fallback at " + surfacePos);
                return surfacePos;
            }
            LMHelperClass.logDebug("JSONSpawner", "Rejected candidate: no valid Y at XZ " + x + ", " + z + " (scanOriginY=" + scanOriginY + ")");
            return null;
        }

        BlockPos feetPos = new BlockPos(x, y, z).below();
        BlockPos headPos = feetPos.above();

        if (!this.isValidBlock(world, feetPos)) {
            if (surfacePos != null) {
                LMHelperClass.logDebug("JSONSpawner", "Underground feetPos invalid, using surface fallback at " + surfacePos);
                return surfacePos;
            }
            LMHelperClass.logDebug("JSONSpawner", "Rejected candidate: feetPos invalid " + feetPos);
            return null;
        }

        if (!super.isValidBlock(world, headPos)) {
            if (surfacePos != null) {
                LMHelperClass.logDebug("JSONSpawner", "Underground headPos invalid, using surface fallback at " + surfacePos);
                return surfacePos;
            }
            LMHelperClass.logDebug("JSONSpawner", "Rejected candidate: headPos invalid " + headPos);
            return null;
        }

        LMHelperClass.logDebug("JSONSpawner", "Accepted underground spawn at " + feetPos);
        return feetPos;
    }


    /**
     * Finds a valid surface spawn position at the given XZ using the heightmap.
     *
     * @param world The world to search in.
     * @param x     The X coordinate.
     * @param z     The Z coordinate.
     * @return A valid surface BlockPos, or null if no valid position found.
     */
    private BlockPos findSurfaceSpawnPos(Level world, int x, int z) {
        BlockPos groundPos = world.getHeightmapPos(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                new BlockPos(x, 0, z)
        );

        // MOTION_BLOCKING_NO_LEAVES includes fluids (water, lava). When solidGround is required,
        // the block at groundPos must itself be solid (canOcclude), otherwise this XZ lands over
        // a lake or river and mobs can't stand here.
        if (this.solidGround && !world.getBlockState(groundPos.below()).canOcclude()) {
            LMHelperClass.logDebug("JSONSpawner", "Rejected surface candidate at XZ " + x + ", " + z
                    + ": ground block is non-solid (" + world.getBlockState(groundPos).getBlock() + " at Y=" + groundPos.getY() + ")");
            return null;
        }

        int feetY = groundPos.getY();

        if (feetY < world.getMinBuildHeight() || feetY >= world.getMaxBuildHeight()) {
            LMHelperClass.logDebug("JSONSpawner", "Rejected surface candidate at XZ " + x + ", " + z + ": Y " + feetY + " out of world bounds");
            return null;
        }

        if (this.yMin >= 0 && feetY < this.yMin) {
            LMHelperClass.logDebug("JSONSpawner", "Rejected surface candidate at XZ " + x + ", " + z + ": below yMin (" + feetY + " < " + this.yMin + ")");
            return null;
        }
        if (this.yMax >= 0 && feetY > this.yMax) {
            LMHelperClass.logDebug("JSONSpawner", "Rejected surface candidate at XZ " + x + ", " + z + ": above yMax (" + feetY + " > " + this.yMax + ")");
            return null;
        }

        BlockPos feetPos = new BlockPos(x, feetY, z);
        BlockPos headPos = feetPos.above();

        if (!this.isValidBlock(world, feetPos)) {
            LMHelperClass.logDebug("JSONSpawner", "Rejected surface candidate: feetPos invalid at " + feetPos + " block=" + world.getBlockState(feetPos).getBlock());
            return null;
        }

        if (!super.isValidBlock(world, headPos)) {
            LMHelperClass.logDebug("JSONSpawner", "Rejected surface candidate: headPos invalid at " + headPos + " block=" + world.getBlockState(headPos).getBlock());
            return null;
        }

        LMHelperClass.logDebug("JSONSpawner", "Found surface spawn position at " + feetPos + " (groundY=" + groundPos.getY() + ")");
        return feetPos;
    }

    /**
     * Gets a random XZ position from the trigger position.
     *
     * @param world      The world that the coordinates are being selected in, mainly for getting Random.
     * @param triggerPos The trigger position to randomize around.
     * @return An integer array containing two ints the X and Z position.
     */
    public int[] getRandomXZCoord(Level world, BlockPos triggerPos) {
        double difficultyScale = this.normalDifficultyRangeScale;
        if (world.getDifficulty().getId() <= 1) {
            difficultyScale = this.easyDifficultyRangeScale;
        } else if (world.getDifficulty().getId() >= 3) {
            difficultyScale = this.hardDifficultyRangeScale;
        }

        int xPos = 0;
        int rangeMaxX = Math.round((float) this.rangeMax.getX() * (float) difficultyScale);
        int rangeMinX = Math.round((float) this.rangeMin.getX() * (float) difficultyScale);
        if (rangeMaxX * difficultyScale > 0) {
            xPos = world.random.nextInt(rangeMaxX);
            if (world.random.nextBoolean()) {
                xPos += rangeMinX;
            } else {
                xPos = -xPos - rangeMinX;
            }
        }

        int zPos = 0;
        int rangeMaxZ = Math.round((float) this.rangeMax.getZ() * (float) difficultyScale);
        int rangeMinZ = Math.round((float) this.rangeMin.getZ() * (float) difficultyScale);
        if (rangeMaxZ * difficultyScale > 0) {
            zPos = world.random.nextInt(rangeMaxZ);
            if (world.random.nextBoolean()) {
                zPos += rangeMinZ;
            } else {
                zPos = -zPos - rangeMinZ;
            }
        }

        return new int[]{triggerPos.getX() + xPos, triggerPos.getZ() + zPos};
    }

    /**
     * Gets a random Y position from the provided XYZ position using the provided range and range max radii.
     *
     * @param world      The world that the coordinates are being selected in, mainly for getting Random.
     * @param triggerPos The position to search from using XZ coords and up and down within range of the Y coord.
     * @return The y position, -1 if a valid position could not be found.
     */
    public int getRandomYCoord(Level world, BlockPos triggerPos) {
        double difficultyScale = this.normalDifficultyRangeScale;
        if (world.getDifficulty().getId() <= 1) {
            difficultyScale = this.easyDifficultyRangeScale;
        } else if (world.getDifficulty().getId() >= 3) {
            difficultyScale = this.hardDifficultyRangeScale;
        }
        int rangeMaxY = Math.round((float) this.rangeMax.getY() * (float) difficultyScale);
        int rangeMinY = Math.round((float) this.rangeMin.getY() * (float) difficultyScale);

        int originX = triggerPos.getX();
        int originY = triggerPos.getY();
        int originZ = triggerPos.getZ();

        int minY = Math.max(originY - rangeMaxY, 0); // Start from this y pos
        if (this.yMin >= 0) {
            minY = Math.max(minY, this.yMin);
        }
        int maxY = Math.min(originY + rangeMaxY, world.getMaxBuildHeight() - 1); // Search up to this y pos
        if (this.yMax >= 0) {
            maxY = Math.min(maxY, this.yMax);
        }

        List<Integer> yCoordsLow = new ArrayList<>();
        List<Integer> yCoordsHigh = new ArrayList<>();

        // Get Every Valid Y Pos:
        for (int nextY = minY; nextY <= maxY; nextY++) {
            // If the next y pos to check is within the min range area, move it up out of it:
            if (nextY > originY - rangeMinY && nextY < originY + rangeMinY)
                nextY = originY + rangeMinY;

            BlockPos spawnPos = new BlockPos(originX, nextY, originZ);

            // If the pos is valid to spawn at:
            if (this.isValidBlock(world, spawnPos)) {
                boolean lastYPos = false;

                // If can see sky:
                if (world.canSeeSkyFromBelowWater(spawnPos)) {
                    // Get random floating position if not searching for solid ground:
                    if (!this.solidGround) {
                        int floatRange = maxY - nextY;
                        if (floatRange > 1) {
                            if (world.getBlockState(spawnPos).getBlock() != Blocks.AIR) {
                                floatRange = this.getValidBlockHeight(world, spawnPos, maxY);
                            }
                            nextY += world.random.nextInt(floatRange + 1) - 1;
                        }
                    }
                    lastYPos = true;
                }

                if (super.isValidBlock(world, spawnPos.above())) {
                    if (nextY <= 64) {
                        yCoordsLow.add(nextY);
                    } else {
                        yCoordsHigh.add(nextY);
                    }
                }

                if (lastYPos) {
                    break;
                }
            }
        }

        // Pick Random Y Pos:
        int y = -1;
        if (yCoordsHigh.size() > 0 && (yCoordsLow.size() <= 0 || world.random.nextFloat() > 0.25F)) {
            if (yCoordsHigh.size() == 1)
                y = yCoordsHigh.get(0);
            else
                y = yCoordsHigh.get(world.random.nextInt(yCoordsHigh.size() - 1));
        } else if (yCoordsLow.size() > 0) {
            if (yCoordsLow.size() == 1)
                y = yCoordsLow.get(0);
            else
                y = yCoordsLow.get(world.random.nextInt(yCoordsLow.size() - 1));
        }

        return y;
    }

    /**
     * Returns if the provided block position is valid.
     **/
    @Override
    public boolean isValidBlock(Level world, BlockPos blockPos) {
        if (!super.isValidBlock(world, blockPos)) {
            //LMHelperClass.logDebug("JSONSpawner", "isValidBlock: super rejected " + blockPos);
            return false;
        }
        if (this.solidGround) {
            boolean groundOk = this.posHasGround(world, blockPos);
           /* if (!groundOk) {
                LMHelperClass.logDebug("JSONSpawner", "isValidBlock: no solid ground under " + blockPos);
            }*/
            return groundOk;
        }
        return true;
    }


    /**
     * Returns true if the specified position has a block underneath it that a mob can safely stand on.
     **/
    public boolean posHasGround(Level world, BlockPos pos) {
        if (pos == null || pos.getY() == 0)
            return false;
        BlockState possibleGroundBlock = world.getBlockState(pos.below());
        try {
            if (possibleGroundBlock.canOcclude())
                return true;
        } catch (Exception e) {
        }
        try {
			/*if (possibleGroundBlock.isSideSolid(world, pos.down(), EnumFacing.UP))
				return true;
			if (possibleGroundBlock.isSideSolid(world, pos.down(), EnumFacing.DOWN))
				return true;*/
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * Returns the height of valid blocks from the starting position checking upwards until the position no longer has a valid block or maxY is reached.
     **/
    public int getValidBlockHeight(Level world, BlockPos startPos, int maxY) {
        int y;
        for (y = startPos.getY(); y <= maxY; y++) {
            BlockPos checkPos = new BlockPos(startPos.getX(), y, startPos.getZ());
            if (!this.isValidBlock(world, checkPos)) {
                break;
            }
        }
        return y - startPos.getY();
    }
}
