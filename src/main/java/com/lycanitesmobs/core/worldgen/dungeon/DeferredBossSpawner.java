package com.lycanitesmobs.core.worldgen.dungeon;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.spawner.MobSpawn;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Mod.EventBusSubscriber(modid = LycanitesMobs.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DeferredBossSpawner {

    private static final int STARTUP_DELAY_TICKS = 200;

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

    private static final ConcurrentLinkedQueue<BossSpawnRequest> PENDING = new ConcurrentLinkedQueue<>();
    private static final Set<Long> ENQUEUED_POSITIONS = ConcurrentHashMap.newKeySet();
    private static final AtomicInteger tickCounter = new AtomicInteger(0);

    public static void enqueue(ResourceKey<Level> dimension, BlockPos pos, MobSpawn mobSpawn, int roomRadius) {
        long posKey = pos.asLong();
        if (ENQUEUED_POSITIONS.add(posKey)) {
            PENDING.add(new BossSpawnRequest(dimension, pos, mobSpawn, roomRadius));
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        int ticks = tickCounter.incrementAndGet();
        if (ticks < STARTUP_DELAY_TICKS) {
            return;
        }

        if (PENDING.isEmpty()) {
            return;
        }

        int processed = 0;
        BossSpawnRequest request;
        while (processed < 3 && (request = PENDING.poll()) != null) {
            try {
                ENQUEUED_POSITIONS.remove(request.pos.asLong());

                ServerLevel level = event.getServer().getLevel(request.dimension);
                if (level == null) {
                    continue;
                }

                if (!level.isLoaded(request.pos)) {
                    continue;
                }

                spawnBoss(level, request);
                processed++;
            } catch (Exception e) {
                LMHelperClass.logWarning("", "[DeferredBoss] Error: " + e.getMessage());
            }
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
