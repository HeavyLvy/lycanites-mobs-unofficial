package com.lycanitesmobs.core.event;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.capabilities.level.ExtendedWorld;
import com.lycanitesmobs.core.data.config.ConfigDungeons;
import com.lycanitesmobs.core.entity.spawner.condition.WorldSpawnCondition;
import com.lycanitesmobs.core.manager.DungeonManager;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.lycanitesmobs.core.worldgen.dungeon.definition.DungeonSchematic;
import com.lycanitesmobs.core.worldgen.dungeon.instance.DungeonBuildPlan;
import com.lycanitesmobs.core.worldgen.dungeon.instance.DungeonInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

//@Mod.EventBusSubscriber(modid = LycanitesMobs.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class StructureSpawnEvents {
    private static final boolean DEBUG_DUNGEON_PLACEMENT = true;
    private static final boolean DEBUG_DUNGEON_DRY_RUN = true;

    private static final java.util.Set<String> scheduledChecks =
            java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());

    private static String makeCheckKey(ServerLevel level, BlockPos pos, DungeonSchematic schematic) {
        ChunkPos chunkPos = new ChunkPos(pos);
        return level.dimension().location() + "|" + schematic.name + "|" + chunkPos.x + "|" + chunkPos.z;
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        ChunkPos chunkPos = event.getChunk().getPos();
        ExtendedWorld extendedWorld = ExtendedWorld.getForWorld(level);
        if (extendedWorld == null) return;

        long worldSeed = extendedWorld.getWorldSeed();
        RandomSource random = RandomSource.create(chunkPos.toLong() ^ worldSeed);

        java.util.List<DungeonInstance> nearby = extendedWorld.getNearbyDungeonInstances(chunkPos, 0);
        for (DungeonInstance instance : nearby) {
            if (!instance.complete && instance.isChunkPosWithin(chunkPos, 0)) {
                instance.buildChunk(level, level, chunkPos, random);
            }
        }
    }


    @SubscribeEvent
    public static void onChunkInit(ChunkEvent.Load event) {
        if (!event.isNewChunk()) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        ChunkPos chunkPos = event.getChunk().getPos();
        BlockPos chunkWorldPos = chunkPos.getWorldPosition();

        ExtendedWorld extendedWorld = ExtendedWorld.getForWorld(level);
        if (extendedWorld == null) return;

        long worldSeed = level.getSeed();

        for (DungeonSchematic schematic : DungeonManager.getInstance().getSchematics()) {
            if (schematic == null) continue;

            WorldSpawnCondition worldCondition = schematic.getWorldSpawnCondition();
            if (worldCondition == null) continue;

            if (!shouldGenerateDungeon(worldSeed, chunkPos, worldCondition)) {
                continue;
            }

            performIfLoaded(level, chunkWorldPos, () -> {
                BlockPos origin = computeDungeonOrigin(level, chunkPos, schematic);
                if (origin == null) return;

                if (!worldCondition.isMet(level, null, origin)) {
                    java.util.List<String> reasons = new java.util.ArrayList<>();
                    worldCondition.collectDebugFailures(level, null, origin, reasons);
                    LMHelperClass.logDebug(
                            "Dungeon",
                            "performIfLoaded: worldCondition failed for " + schematic.name +
                                    " at " + origin + " reasons=" + reasons
                    );
                    return;
                }

                if (!schematic.canBuild(level, origin)) {
                    LMHelperClass.logDebug(
                            "Dungeon",
                            "performIfLoaded: canBuild=false for " + schematic.name + " at " + origin
                    );
                    return;
                }

                spawnDungeonInstance(level, extendedWorld, schematic, origin);
            }, schematic);
        }
    }

    @SubscribeEvent
    public static void onChunkLoadForDungeons(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        ExtendedWorld extendedWorld = ExtendedWorld.getForWorld(level);
        if (extendedWorld == null) return;

        ChunkPos chunkPos = event.getChunk().getPos();
        List<DungeonInstance> nearby = extendedWorld.getNearbyDungeonInstances(chunkPos, 0);
        if (nearby == null || nearby.isEmpty()) return;

        RandomSource random = RandomSource.create(chunkPos.toLong() ^ extendedWorld.getWorldSeed());
        for (DungeonInstance instance : nearby) {
            if (instance == null || instance.complete) continue;
            instance.buildChunk(level, level, chunkPos, random);
        }
    }

    public static boolean place(ServerLevel level, BlockPos origin) {
        boolean enabled = ConfigDungeons.INSTANCE.dungeonsEnabled.get();
        int dungeonDistance = ConfigDungeons.INSTANCE.dungeonDistance.get();

        ExtendedWorld extendedWorld = ExtendedWorld.getForWorld(level);
        if (!enabled || extendedWorld == null) {
            return false;
        }

        RandomSource random = level.getRandom();

        try {
            int dungeonSizeMax = dungeonDistance;
            ChunkPos chunkPos = new ChunkPos(origin);
            List<DungeonInstance> nearbyDungeons = extendedWorld.getNearbyDungeonInstances(chunkPos, dungeonSizeMax * 2);

            if (nearbyDungeons.isEmpty()) {
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && z == 0) {
                            continue;
                        }
                        if (x != 0 && z != 0) {
                            continue;
                        }

                        DungeonInstance dungeonInstance = new DungeonInstance();
                        int yPos = level.getSeaLevel();
                        if (yPos < 64) {
                            yPos = 64;
                        }

                        ChunkPos center = new ChunkPos(
                                chunkPos.x + (dungeonSizeMax * x),
                                chunkPos.z + (dungeonSizeMax * z)
                        );
                        BlockPos dungeonPos = center.getWorldPosition().offset(7, yPos, 7);

                        dungeonInstance.setOrigin(dungeonPos);
                        if (dungeonInstance.init(level)) {
                            extendedWorld.addDungeonInstance(
                                    dungeonInstance,
                                    new UUID(random.nextLong(), random.nextLong())
                            );
                        }
                    }
                }
            }

        } catch (Exception e) {
            LMHelperClass.logErrorMessageOnceCatchable("Dungeon Error: ", e);
        }

        return true;
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (true) {
            //place(event.getEntity().getServer().getLevel(event.getLevel().dimension()), event.getPos());
            return;
        }
        if (event.getLevel().isClientSide()) {
            LMHelperClass.logDebug("Dungeon", "RightClick: ClientSide -> abort");
            return;
        }

        if (event.getHand() != InteractionHand.MAIN_HAND) {
            LMHelperClass.logDebug("Dungeon", "RightClick: Not main hand -> abort");
            return;
        }

        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            LMHelperClass.logDebug("Dungeon", "RightClick: Not a ServerPlayer -> abort");
            return;
        }
        if (!serverPlayer.isCreative()) {
            LMHelperClass.logDebug("Dungeon", "RightClick: Player not creative -> abort");
            return;
        }

        ServerLevel level = (ServerLevel) event.getLevel();
        BlockPos clickPos = event.getPos();
        ChunkPos chunkPos = new ChunkPos(clickPos);

        LMHelperClass.logDebug("Dungeon", "RightClick: Debug spawn on chunk " + chunkPos + " at " + clickPos);

        ExtendedWorld extendedWorld = ExtendedWorld.getForWorld(level);
        if (extendedWorld == null) {
            LMHelperClass.logDebug("Dungeon", "RightClick: ExtendedWorld null -> abort");
            return;
        }

        boolean spawned = false;

        for (DungeonSchematic schematic : DungeonManager.getInstance().getSchematics()) {
            if (schematic == null || schematic.name == null) {
                continue;
            }

            LMHelperClass.logDebug("Dungeon", "RightClick: Trying schematic: " + schematic.name);

            WorldSpawnCondition worldCondition = schematic.getWorldSpawnCondition();
            if (worldCondition == null) {
                LMHelperClass.logDebug("Dungeon", "RightClick: Schematic " + schematic.name + " has no WorldSpawnCondition, skipping");
                continue;
            }

            int minY = worldCondition.getMinY();
            int maxY = worldCondition.getMaxY();
            if (maxY < minY) {
                int t = minY;
                minY = maxY;
                maxY = t;
            }

            int clampedY = Mth.clamp(clickPos.getY(), minY, maxY);
            BlockPos origin = new BlockPos(clickPos.getX(), clampedY, clickPos.getZ());

            LMHelperClass.logDebug("Dungeon", "RightClick: " + schematic.name + " origin candidate at " + origin);

            if (!worldCondition.isMet(level, serverPlayer, origin)) {
                LMHelperClass.logDebug("Dungeon", "RightClick: WorldSpawnCondition not met for " + schematic.name + " at " + origin);
                continue;
            }

            if (!schematic.canBuild(level, origin)) {
                LMHelperClass.logDebug("Dungeon", "RightClick: canBuild=false for " + schematic.name + " at " + origin);
                continue;
            }

            DungeonSchematic usedSchematic = schematic;
            ExtendedWorld usedExtended = extendedWorld;
            BlockPos usedOrigin = origin.immutable();

            performIfLoaded(level, usedOrigin, () -> {
                debugSpawnDungeonInstance(level, usedExtended, usedSchematic, usedOrigin);
            }, usedSchematic);

            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal(
                            "Queued \"" + schematic.name + "\" at " +
                                    origin.getX() + ", " + origin.getY() + ", " + origin.getZ()
                    )
            );

            spawned = true;
            break;
        }

        if (!spawned) {
            LMHelperClass.logDebug("Dungeon", "RightClick: No suitable schematic found at " + clickPos);
        }

        event.setCanceled(true);
    }


    public static BlockPos findNearestTheoreticalDungeon(ServerLevel level, DungeonSchematic schematic, BlockPos origin, int maxRegionRadius) {
        WorldSpawnCondition condition = schematic.getWorldSpawnCondition();
        if (condition == null) return null;

        long worldSeed = level.getSeed();
        int spacing = Math.max(1, condition.getSpacing());
        long salt = condition.getSalt();

        ChunkPos originChunk = new ChunkPos(origin);
        int originRegionX = originChunk.x < 0 ? (originChunk.x - spacing + 1) / spacing : originChunk.x / spacing;
        int originRegionZ = originChunk.z < 0 ? (originChunk.z - spacing + 1) / spacing : originChunk.z / spacing;

        BlockPos bestPos = null;
        double bestDistSq = Double.MAX_VALUE;

        for (int radius = 0; radius <= maxRegionRadius; radius++) {
            boolean foundInThisRadius = false;

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (radius > 0 && dx != -radius && dx != radius && dz != -radius && dz != radius) continue;

                    int regionX = originRegionX + dx;
                    int regionZ = originRegionZ + dz;

                    long structureSeed = computeStructureSeed(worldSeed, salt, regionX, regionZ);
                    RandomSource random = RandomSource.create(structureSeed);

                    int originChunkX = regionX * spacing + random.nextInt(spacing);
                    int originChunkZ = regionZ * spacing + random.nextInt(spacing);

                    int centerX = originChunkX * 16 + 8;
                    int centerZ = originChunkZ * 16 + 8;

                    int minY = condition.getMinY();
                    int maxY = condition.getMaxY();
                    if (maxY < minY) {
                        int t = minY;
                        minY = maxY;
                        maxY = t;
                    }
                    int yRange = Math.max(1, maxY - minY + 1);

                    RandomSource yRandom = RandomSource.create(computeStructureSeed(worldSeed, salt, originChunkX, originChunkZ));
                    int y = minY + yRandom.nextInt(yRange);

                    BlockPos candidate = new BlockPos(centerX, y, centerZ);

                    if (!condition.isMet(level, null, candidate)) continue;
                    if (!schematic.canBuild(level, candidate)) continue;

                    double distSq = candidate.distSqr(origin);
                    if (distSq < bestDistSq) {
                        bestDistSq = distSq;
                        bestPos = candidate;
                        foundInThisRadius = true;
                    }
                }
            }

            if (foundInThisRadius && bestPos != null) {
                break;
            }
        }

        return bestPos;
    }

    private static boolean hasExistingDungeonAtSalt(ServerLevel level, DungeonSchematic schematic, ChunkPos saltChunkPos) {
        ExtendedWorld worldExt = ExtendedWorld.getForWorld(level);
        if (worldExt == null) return false;

        List<DungeonInstance> nearby = worldExt.getNearbyDungeonInstances(saltChunkPos, 0);
        if (nearby == null || nearby.isEmpty()) return false;

        for (DungeonInstance instance : nearby) {
            if (instance == null || instance.schematic == null || instance.schematic.name == null) continue;
            if (!instance.schematic.name.equals(schematic.name)) continue;
            BlockPos origin = instance.getOrigin();
            if (origin == null) continue;
            ChunkPos originChunk = new ChunkPos(origin);
            if (originChunk.x == saltChunkPos.x && originChunk.z == saltChunkPos.z) {
                return true;
            }
        }

        return false;
    }

    private static void debugSpawnDungeonInstance(ServerLevel level, ExtendedWorld extendedWorld, DungeonSchematic schematic, BlockPos origin) {
        DungeonInstance instance = new DungeonInstance();
        instance.schematic = schematic;
        instance.setOrigin(origin);
        instance.init(level);

        long xorSeed = level.getSeed() ^ origin.asLong();
        RandomSource idRandom = RandomSource.create(xorSeed);
        UUID id = new UUID(idRandom.nextLong(), idRandom.nextLong());
        instance.uuid = id;

        extendedWorld.addDungeonInstance(instance, id);

        if (DEBUG_DUNGEON_DRY_RUN) {
            LMHelperClass.logDebug("DungeonDebug", "Dry-run placement for " + instance);
            extendedWorld.debugDryRunDungeonPlacement(instance);
        } else {
            extendedWorld.buildDungeonInLoadedChunks(instance);
        }

        LMHelperClass.logDebug("Dungeon",
                "Debug dungeon " + schematic.name + " spawned at " + origin +
                        " in dimension " + level.dimension().location());
    }

    private static void performIfLoaded(ServerLevel level, BlockPos pos, Runnable runnable, DungeonSchematic schematic) {
        MinecraftServer server = level.getServer();
        if (server == null) return;
        if (schematic == null) return;

        WorldSpawnCondition worldCondition = schematic.getWorldSpawnCondition();
        if (worldCondition == null) return;

        String key = makeCheckKey(level, pos, schematic);
        if (!scheduledChecks.add(key)) {
            return;
        }

        BlockPos checkPos = pos.immutable();
        ChunkPos chunkPos = new ChunkPos(checkPos);
        long worldSeed = level.getSeed();
        long salt = worldCondition.getSalt();

        CompletableFuture
                .supplyAsync(() -> {
                    int minY = worldCondition.getMinY();
                    int maxY = worldCondition.getMaxY();
                    if (maxY < minY) {
                        int t = minY;
                        minY = maxY;
                        maxY = t;
                    }
                    int yRange = Math.max(1, maxY - minY + 1);

                    long structureSeed = computeStructureSeed(worldSeed, salt, chunkPos.x, chunkPos.z);
                    RandomSource random = RandomSource.create(structureSeed);
                    int y = minY + random.nextInt(yRange);

                    int centerX = chunkPos.getMinBlockX() + 8;
                    int centerZ = chunkPos.getMinBlockZ() + 8;
                    BlockPos origin = new BlockPos(centerX, y, centerZ);

                    int radius = worldCondition.getMaxDistanceFromCenter();
                    int height = worldCondition.getEstimatedHeight();
                    BlockPos furthestPos = origin.offset(radius, height, radius);

                    return level.isLoaded(furthestPos);
                })
                .thenAcceptAsync(loaded -> {
                    if (!loaded) {
                        return;
                    }

                    server.execute(() -> {
                        try {
                            runnable.run();
                        } finally {
                            scheduledChecks.remove(key);
                        }
                    });
                }, server);
    }

    private static boolean shouldSpawnDungeonHere(ServerLevel level, BlockPos chunkWorldPos, DungeonSchematic schematic) {
        WorldSpawnCondition worldCondition = schematic.getWorldSpawnCondition();
        if (worldCondition == null) return false;

        ChunkPos chunkPos = new ChunkPos(chunkWorldPos);
        long worldSeed = level.getSeed();

        if (!shouldGenerateDungeon(worldSeed, chunkPos, worldCondition)) {
            return false;
        }

        if (hasExistingDungeonAtSalt(level, schematic, chunkPos)) {
            LMHelperClass.logDebug(
                    "Dungeon",
                    "shouldSpawnDungeonHere: existing instance for " + schematic.name + " at salt chunk " + chunkPos
            );
            return false;
        }

        long structureSeed = computeStructureSeed(
                worldSeed,
                worldCondition.getSalt(),
                chunkPos.x,
                chunkPos.z
        );
        RandomSource random = RandomSource.create(structureSeed);

        int minY = worldCondition.getMinY();
        int maxY = worldCondition.getMaxY();
        if (maxY < minY) {
            int t = minY;
            minY = maxY;
            maxY = t;
        }
        int yRange = Math.max(1, maxY - minY + 1);
        int y = minY + random.nextInt(yRange);

        int centerX = chunkPos.getMinBlockX() + 8;
        int centerZ = chunkPos.getMinBlockZ() + 8;
        BlockPos origin = new BlockPos(centerX, y, centerZ);

        if (!worldCondition.isMet(level, null, origin)) {
            java.util.List<String> reasons = new java.util.ArrayList<>();
            worldCondition.collectDebugFailures(level, null, origin, reasons);
            LMHelperClass.logDebug(
                    "Dungeon",
                    "shouldSpawnDungeonHere: worldCondition failed for " + schematic.name +
                            " at " + origin + " reasons=" + reasons
            );
            return false;
        }

        return true;
    }


    private static BlockPos computeDungeonOrigin(ServerLevel level, ChunkPos chunkPos, DungeonSchematic schematic) {
        WorldSpawnCondition worldCondition = schematic.getWorldSpawnCondition();
        if (worldCondition == null) return null;

        long worldSeed = level.getSeed();
        long seed = computeStructureSeed(worldSeed, worldCondition.getSalt(), chunkPos.x, chunkPos.z);
        RandomSource random = RandomSource.create(seed);

        int minY = worldCondition.getMinY();
        int maxY = worldCondition.getMaxY();
        if (maxY < minY) {
            int t = minY;
            minY = maxY;
            maxY = t;
        }
        int yRange = Math.max(1, maxY - minY + 1);
        int y = minY + random.nextInt(yRange);

        int centerX = chunkPos.getMinBlockX() + 8;
        int centerZ = chunkPos.getMinBlockZ() + 8;

        return new BlockPos(centerX, y, centerZ);
    }

    private static boolean shouldGenerateDungeon(long worldSeed, ChunkPos chunkPos, WorldSpawnCondition condition) {
        int spacing = condition.getSpacing();
        int separation = condition.getSeparation();
        if (spacing <= 0) {
            return false;
        }

        int regionX = chunkPos.x < 0 ? (chunkPos.x - spacing + 1) / spacing : chunkPos.x / spacing;
        int regionZ = chunkPos.z < 0 ? (chunkPos.z - spacing + 1) / spacing : chunkPos.z / spacing;

        long structureSeed = computeStructureSeed(worldSeed, condition.getSalt(), regionX, regionZ);
        RandomSource random = RandomSource.create(structureSeed);

        int originX = regionX * spacing + random.nextInt(spacing);
        int originZ = regionZ * spacing + random.nextInt(spacing);

        boolean isOrigin = chunkPos.x == originX && chunkPos.z == originZ;
        return isOrigin;
    }


    private static long computeStructureSeed(long worldSeed, long salt, int chunkX, int chunkZ) {
        return worldSeed + salt + chunkX * 2345803L + chunkZ * 9236449L + (long) chunkX * chunkZ * 223L;
    }

    private static void spawnDungeonInstance(ServerLevel level, ExtendedWorld extendedWorld, DungeonSchematic schematic, BlockPos origin) {
        DungeonInstance instance = new DungeonInstance();
        instance.schematic = schematic;
        instance.setOrigin(origin);
        instance.init(level);

        long xorSeed = level.getSeed() ^ origin.asLong();
        RandomSource idRandom = RandomSource.create(xorSeed);
        UUID id = new UUID(idRandom.nextLong(), idRandom.nextLong());
        instance.uuid = id;

        extendedWorld.addDungeonInstance(instance, id);

        LMHelperClass.logDebug("Dungeon", "Natural dungeon " + schematic.name + " spawned at " + origin + " in dimension " + level.dimension().location());
    }


}
