package com.lycanitesmobs.core.worldgen.dungeon;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.spawner.MobSpawn;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = LycanitesMobs.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DeferredBossSpawner {

    private static class BossSpawnRequest {
        final ResourceKey<Level> dimension;
        final BlockPos pos;
        final MobSpawn mobSpawn;
        final int roomRadius;

        BossSpawnRequest(ResourceKey<Level> dimension, BlockPos pos, MobSpawn mobSpawn, int roomRadius) {
            this.dimension = dimension;
            this.pos = pos;
            this.mobSpawn = mobSpawn;
            this.roomRadius = roomRadius;
        }
    }

    private static final Map<Long, BossSpawnRequest> PENDING = new ConcurrentHashMap<>();

    public static void enqueue(ResourceKey<Level> dimension, BlockPos pos, MobSpawn mobSpawn, int roomRadius) {
        long chunkKey = new ChunkPos(pos).toLong();
        PENDING.putIfAbsent(chunkKey, new BossSpawnRequest(dimension, pos, mobSpawn, roomRadius));
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (PENDING.isEmpty()) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        long chunkKey = event.getChunk().getPos().toLong();
        BossSpawnRequest request = PENDING.remove(chunkKey);
        if (request == null) {
            return;
        }

        if (!request.dimension.equals(level.dimension())) {
            PENDING.putIfAbsent(chunkKey, request);
            return;
        }

        try {
            spawnBoss(level, request);
        } catch (Exception e) {
            LMHelperClass.logWarning("", "[DeferredBoss] Error spawning boss: " + e.getMessage());
        }
    }

    private static void spawnBoss(ServerLevel level, BossSpawnRequest request) {
        request.mobSpawn.resolveEntityType();

        LivingEntity entity = request.mobSpawn.createEntity(level);
        if (entity == null) {
            return;
        }

        entity.setPos(request.pos.getX() + 0.5, request.pos.getY(), request.pos.getZ() + 0.5);

        if (entity instanceof BaseCreatureEntity creature) {
            creature.setHome(request.pos.getX(), request.pos.getY(), request.pos.getZ(), request.roomRadius);
            creature.spawnedAsBoss = true;
        }

        request.mobSpawn.onSpawned(entity, null);
        level.addFreshEntity(entity);
    }
}
