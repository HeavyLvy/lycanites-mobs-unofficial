package com.lycanitesmobs.core.entity.spawner;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lycanitesmobs.core.capabilities.level.ExtendedWorld;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.event.SpawnerEventListener;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.manager.SpawnerManager;
import com.lycanitesmobs.core.event.mobevent.MobEvent;
import com.lycanitesmobs.core.event.mobevent.MobEventPlayerServer;
import com.lycanitesmobs.core.entity.spawner.condition.SpawnCondition;
import com.lycanitesmobs.core.entity.spawner.location.SpawnLocation;
import com.lycanitesmobs.core.entity.spawner.trigger.ChunkSpawnTrigger;
import com.lycanitesmobs.core.entity.spawner.trigger.SpawnTrigger;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import java.util.*;

public class Spawner {
    /** Spawners are loaded from JSON and operate around their Triggers, Conditions and Locations. **/

    /**
     * A list of all Spawn Conditions which determine if the Triggers should be active or not.
     **/
    public List<SpawnCondition> conditions = new ArrayList<>();

    /**
     * A list of all Spawn Triggers which listen to events and ticks and attempt a spawn, if conditions are met.
     **/
    public List<SpawnTrigger> triggers = new ArrayList<>();

    /**
     * A list of all Spawn Locations which are used to determine where this Spawner can actually spawn the mob, some Triggers require a specific spawn location other provide an area.
     **/
    public List<SpawnLocation> locations = new ArrayList<>();

    /**
     * A list of all Mobs that can be spawned by this spawner.
     **/
    public List<MobSpawn> mobSpawns = new ArrayList<>();

    /**
     * The name of this spawner, must be unique, used by creatures/groups when entering spawners.
     **/
    public String name;

    /**
     * The name of this spawner when, used by creatures/groups when entering spawners. By default this copies the name but can be changed and doesn't have to be unique.
     **/
    public String sharedName;

    /**
     * If set to true and this spawner is a default spawner, it will reset when loading. This must be set to false or removed from the json if it is a customised spawner.
     **/
    public boolean loadDefault = true;

    /**
     * Can be set to false to completely disable this Spawner.
     **/
    public boolean enabled = true;

    /**
     * Determines how many Conditions must be met. If 0 or less all are required.
     **/
    public int conditionsRequired = 0;

    /**
     * Sets how many times any Trigger must activate (per player) before a wave is spawned. Only supported by player based triggers
     **/
    public int triggersRequired = 1;

    /**
     * A list of messages to send to the player whenever the associated count is reached.
     **/
    public Map<Integer, String> triggerCountMessages = new HashMap<>();

    /**
     * Determines how a Location is chosen if there are multiple. Can be: order, random or combine.
     **/
    public String multipleLocations = "combine";

    /**
     * The minimum amount of mobs to spawn each wave.
     **/
    public int mobCountMin = 1;

    /**
     * The maximum amount of mobs to spawn each wave.
     **/
    public int mobCountMax = 1;

    /**
     * If true, this Spawner will divide the amount of mobs to spawn by the amount of players near the trigger player (does not affect non-player triggers).
     **/
    public boolean mobCountPlayerScaling = true;

    /**
     * How far to search around a player for finding nearby other player for mob spawn scaling.
     **/
    public int mobCountPlayerRange = 64;

    /**
     * If true, this Spawner will ignore all mob dimension checks (not Spawn Condition checks), this bypasses the checks in MobSpawns and SpawnInfos.
     **/
    public boolean ignoreDimensions = false;

    /**
     * If true, this Spawner will ignore all biome checks, this bypasses the checks in MobSpawns and SpawnInfos.
     **/
    public boolean ignoreBiomes = false;

    /**
     * If true, this Spawner will ignore collision checks, this may cause mobs to become stuck and suffocate.
     **/
    public boolean ignoreCollision = false;

    /**
     * If true, this Spawner will ignore light level checks, this bypasses the checks in MobSpawns and SpawnInfos.
     **/
    public boolean ignoreLightLevel = false;

    /**
     * If true, this Spawner will ignore group limit checks, this bypasses the checks in MobSpawns and SpawnInfos.
     **/
    public boolean ignoreGroupLimit = false;

    /**
     * The range of how far this Spawner should check for surrounding mobs of the same type.
     **/
    protected double groupLimitRange = 32;

    /**
     * If set to true, the Forge Can Spawn Event is fired but its result is ignored, use this to prevent other mods from stopping the spawn via the event.
     **/
    protected boolean ignoreForgeCanSpawnEvent = false;

    /**
     * If true, mobs spawned by this Spawner will not naturally despawn.
     **/
    public boolean forceNoDespawn = false;

    /**
     * If true, actions caused by this spawner such as block destruction can trigger additional spawn triggers creating a spawner chain.
     **/
    public boolean chainSpawning = true;

    /**
     * If true, this Spawner will act as enabled even if it can't find any mobs to spawn, useful if you just want to use trigger messages, etc.
     **/
    public boolean enableWithoutMobs = false;

    /**
     * If set, this is the name of the mob event that must be active, otherwise this Spawner is always active.
     **/
    public String eventName = "";

    /**
     * If set, when a mob is spawned, blocks in the radius around the spawned mob will be destroyed.
     **/
    public int blockBreakRadius = -1;

    /**
     * If true, spawned mobs will not drop any items until a player has damaged them in some way.
     **/
    protected boolean dropsRequirePlayerDamage = false;


    /**
     * Stores how many times each player has triggered any of this spawners Spawn Triggers, the count is reset when a wave is spawned.
     **/
    protected Map<Player, Integer> triggerCounts = new HashMap<>();


    /**
     * Loads this Spawner from the provided JSON data.
     **/
    public void loadFromJSON(JsonObject json) {
        // Spawner Properties:
        this.name = json.get("name").getAsString();
        this.sharedName = this.name;

        if (json.has("sharedName"))
            this.sharedName = json.get("sharedName").getAsString();

        if (json.has("loadDefault"))
            this.loadDefault = json.get("loadDefault").getAsBoolean();

        if (json.has("enabled"))
            this.enabled = json.get("enabled").getAsBoolean();

        if (json.has("conditionsRequired"))
            this.conditionsRequired = json.get("conditionsRequired").getAsInt();

        if (json.has("triggersRequired"))
            this.triggersRequired = json.get("triggersRequired").getAsInt();

        if (json.has("triggerCountMessages")) {
            JsonArray jsonArray = json.get("triggerCountMessages").getAsJsonArray();
            Iterator<JsonElement> jsonIterator = jsonArray.iterator();
            while (jsonIterator.hasNext()) {
                JsonObject tcmJson = jsonIterator.next().getAsJsonObject();
                this.triggerCountMessages.put(tcmJson.get("count").getAsInt(), tcmJson.get("message").getAsString());
            }
        }

        if (json.has("multipleLocations"))
            this.multipleLocations = json.get("multipleLocations").getAsString();

        if (json.has("mobCountMin"))
            this.mobCountMin = json.get("mobCountMin").getAsInt();

        if (json.has("mobCountMax"))
            this.mobCountMax = json.get("mobCountMax").getAsInt();

        if (json.has("mobCountPlayerScaling"))
            this.mobCountPlayerScaling = json.get("mobCountPlayerScaling").getAsBoolean();

        if (json.has("mobCountPlayerRange"))
            this.mobCountPlayerRange = json.get("mobCountPlayerRange").getAsInt();

        if (json.has("ignoreDimensions"))
            this.ignoreDimensions = json.get("ignoreDimensions").getAsBoolean();

        if (json.has("ignoreBiomes"))
            this.ignoreBiomes = json.get("ignoreBiomes").getAsBoolean();

        if (json.has("ignoreCollision"))
            this.ignoreCollision = json.get("ignoreCollision").getAsBoolean();

        if (json.has("ignoreLightLevel"))
            this.ignoreLightLevel = json.get("ignoreLightLevel").getAsBoolean();

        if (json.has("ignoreGroupLimit"))
            this.ignoreGroupLimit = json.get("ignoreGroupLimit").getAsBoolean();

        if (json.has("groupLimitRange"))
            this.groupLimitRange = json.get("groupLimitRange").getAsDouble();

        if (json.has("ignoreForgeCanSpawnEvent"))
            this.ignoreForgeCanSpawnEvent = json.get("ignoreForgeCanSpawnEvent").getAsBoolean();

        if (json.has("forceNoDespawn"))
            this.forceNoDespawn = json.get("forceNoDespawn").getAsBoolean();

        if (json.has("chainSpawning"))
            this.chainSpawning = json.get("chainSpawning").getAsBoolean();

        if (json.has("enableWithoutMobs"))
            this.enableWithoutMobs = json.get("enableWithoutMobs").getAsBoolean();

        if (json.has("eventName"))
            this.eventName = json.get("eventName").getAsString();

        if (json.has("blockBreakRadius"))
            this.blockBreakRadius = json.get("blockBreakRadius").getAsInt();

        if (json.has("dropsRequirePlayerDamage"))
            this.dropsRequirePlayerDamage = json.get("dropsRequirePlayerDamage").getAsBoolean();

        // Conditions:
        if (json.has("conditions")) {
            JsonArray jsonArray = json.get("conditions").getAsJsonArray();
            Iterator<JsonElement> jsonIterator = jsonArray.iterator();
            while (jsonIterator.hasNext()) {
                JsonObject conditionJson = jsonIterator.next().getAsJsonObject();
                SpawnCondition spawnCondition = SpawnCondition.createFromJSON(conditionJson);
                this.conditions.add(spawnCondition);
            }
        }

        // Triggers:
        if (json.has("triggers")) {
            JsonArray jsonArray = json.get("triggers").getAsJsonArray();
            Iterator<JsonElement> jsonIterator = jsonArray.iterator();
            while (jsonIterator.hasNext()) {
                JsonObject triggerJson = jsonIterator.next().getAsJsonObject();
                SpawnTrigger spawnTrigger = SpawnTrigger.createFromJSON(triggerJson, this);
                this.triggers.add(spawnTrigger);
                if (this.enabled) {
                    SpawnerEventListener.getInstance().addTrigger(spawnTrigger);
                }
            }
        }

        // Locations:
        if (json.has("locations")) {
            JsonArray jsonArray = json.get("locations").getAsJsonArray();
            Iterator<JsonElement> jsonIterator = jsonArray.iterator();
            while (jsonIterator.hasNext()) {
                JsonObject locationJson = jsonIterator.next().getAsJsonObject();
                SpawnLocation spawnLocation = SpawnLocation.createFromJSON(locationJson);
                this.locations.add(spawnLocation);
            }
        }

        // Mob Spawns:
        if (json.has("mobSpawns")) {
            JsonArray jsonArray = json.get("mobSpawns").getAsJsonArray();
            Iterator<JsonElement> jsonIterator = jsonArray.iterator();
            while (jsonIterator.hasNext()) {
                JsonObject mobSpawnJson = jsonIterator.next().getAsJsonObject();
                MobSpawn mobSpawn = MobSpawn.createFromJSON(mobSpawnJson);
                if (mobSpawn != null) {
                    this.mobSpawns.add(mobSpawn);
                }
            }
        }
    }


    /**
     * Remove this Spawner. This removes all Triggers from the Event Listener and does other cleanup.
     **/
    public void destroy() {
        for (SpawnTrigger spawnTrigger : this.triggers) {
            SpawnerEventListener.getInstance().removeTrigger(spawnTrigger);
        }
        SpawnerManager.getInstance().removeSpawner(this);
    }


    /**
     * Returns true if this Spawner is considered enabled, this is checked first before major logging and more in depth checks are done in canSpawn().
     **/
    public boolean isEnabled(Level world, Player player) {
        if (!this.enabled || CreatureManager.getInstance().spawnConfig.disableAllSpawning || !world.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
            return false;
        }

        // Event Spawner Check:
        if (!"".equals(this.eventName)) {
            ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);
            if (worldExt == null) {
                return false;
            }
            if (worldExt.getMobEventPlayerServer(this.eventName) == null) {
                return false;
            }
        }

        if (!this.enableWithoutMobs && this.mobSpawns.isEmpty() && SpawnerMobRegistry.getMobSpawns(this.sharedName) == null) {
            return false;
        }

        return true;
    }


    /**
     * Returns true if Triggers are allowed to operate for this Spawner.
     **/
    public boolean canSpawn(Level world, Player player, BlockPos triggerPos) {
        if (!SpawnerManager.getInstance().globalSpawnConditions.isEmpty()) {
            LMHelperClass.logDebug("JSONSpawner", "Global Conditions Required: " + SpawnerManager.getInstance().globalSpawnConditions.size());
            for (SpawnCondition condition : SpawnerManager.getInstance().globalSpawnConditions) {
                if (!condition.isMet(world, player, triggerPos)) {
                    LMHelperClass.logDebug("JSONSpawner", "Global Condition: " + condition + "Failed");
                    return false;
                }
            }
        }

        if (this.conditions.isEmpty()) {
            LMHelperClass.logDebug("JSONSpawner", "No Conditions");
            return true;
        }

        LMHelperClass.logDebug("JSONSpawner", "Conditions Required: " + (this.conditionsRequired > 0 ? this.conditionsRequired : "All"));
        int conditionsMet = 0;
        int conditionsRequired = this.conditionsRequired > 0 ? this.conditionsRequired : this.conditions.size();
        for (SpawnCondition condition : this.conditions) {
            boolean met = condition.isMet(world, player, triggerPos);
            LMHelperClass.logDebug("JSONSpawner", "Condition: " + condition + " " + (met ? "Passed" : "Failed"));
            if (met) {
                if (++conditionsMet >= conditionsRequired) {
                    LMHelperClass.logDebug("JSONSpawner", "Sufficient Conditions Met");
                    return true;
                }
            }
        }

        LMHelperClass.logDebug("JSONSpawner", "Insufficient Conditions Met: " + conditionsMet + "/" + conditionsRequired);
        return false;
    }


    /**
     * Starts a new spawn, usually called by Triggers.
     *
     * @param world        The World to spawn in.
     * @param player       The player that is being spawned around. If null all player based checks and features are ignored.
     * @param spawnTrigger The spawn trigger that has triggered this spawn.
     * @param triggerPos   The location that the spawn was triggered, usually used as the center for spawning around or on.
     * @param level        The level of the spawn trigger, higher levels are from rarer spawn conditions and can result in tougher mobs being spawned.
     * @param countAmount  How much this trigger affects the trigger count by.
     * @param chain        How far along a spawner chain this trigger is, this increases whenever the actions of one spawner trigger another spawner and is used to prevent loops, etc.
     * @return True on a successful spawn and false on failure.
     **/
    public boolean trigger(Level world, Player player, SpawnTrigger spawnTrigger, BlockPos triggerPos, int level, int countAmount, int chain) {
        if (!this.isEnabled(world, player)) {
            return false;
        }

        LMHelperClass.logDebug("JSONSpawner", "~O==================== Spawner Triggered: " + this.name + " ====================O~");
        if (!this.canSpawn(world, player, triggerPos)) {
            LMHelperClass.logDebug("JSONSpawner", "This Spawner Cannot Spawn");
            return false;
        }

        // Only One Trigger Required:
        if (this.triggersRequired <= 1 || player == null) {
            LMHelperClass.logDebug("JSONSpawner", "Only one trigger required.");
            return this.doSpawn(world, player, spawnTrigger, triggerPos, level, chain);
        }

        // Get Current Count:
        if (!this.triggerCounts.containsKey(player)) {
            this.triggerCounts.put(player, Math.max(countAmount, 0));
        }
        int currentCount = this.triggerCounts.get(player);
        int lastCount = currentCount;

        // Change Count:
        if (countAmount == 0) {
            currentCount = 0;
        } else {
            currentCount += countAmount;
        }
        if (currentCount != lastCount) {
            if (this.triggerCountMessages.containsKey(currentCount)) {
                MutableComponent message = Component.translatable(this.triggerCountMessages.get(currentCount));
                ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(player);
                if (extendedPlayer != null) {
                    extendedPlayer.sendOverlayMessage(message);
                }
            }
        }

        // Required Count Met:
        LMHelperClass.logDebug("JSONSpawner", "Trigger " + currentCount + "/" + this.triggersRequired);
        if (currentCount >= this.triggersRequired) {
            this.triggerCounts.put(player, 0);
            return this.doSpawn(world, player, spawnTrigger, triggerPos, level, chain);
        }

        this.triggerCounts.put(player, currentCount);
        return false;
    }


    /**
     * Starts a new spawn, usually called from trigger() when the count is sufficient.
     *
     * @param world        The World to spawn in.
     * @param player       The player that is being spawned around.
     * @param spawnTrigger The spawn trigger that has triggered this spawn.
     * @param triggerPos   The location that the spawn was triggered, usually used as the center for spawning around or on.
     * @param level        The level of the spawn trigger, higher levels are from rarer spawn conditions and can result in tougher mobs being spawned.
     * @param chain        How far along a spawner chain this trigger is, this increases whenever the actions of one spawner trigger another spawner and is used to prevent loops, etc.
     * @return True on a successful spawn and false on failure.
     **/
    public boolean doSpawn(Level world, Player player, SpawnTrigger spawnTrigger, BlockPos triggerPos, int level, int chain) {
        // Get Amount of Mobs To Spawn:
        int mobCount = this.mobCountMin;
        if (this.mobCountMax > this.mobCountMin) {
            mobCount = world.random.nextInt(this.mobCountMax - this.mobCountMin) + this.mobCountMin;
        }
        if (player != null && this.mobCountPlayerScaling) {
            int nearbyPlayers = 0;
            List<? extends Player> allPlayers = world.players();
            for (Player targetPlayer : allPlayers) {
                if (targetPlayer.isCreative() || targetPlayer.isSpectator()) {
                    if (!SpawnerEventListener.testOnCreative)
                        break;
                }
                if (targetPlayer.distanceTo(player) > this.mobCountPlayerRange) {
                    break;
                }
                nearbyPlayers++;
            }
            LMHelperClass.logDebug("JSONSpawner", "Nearby Players: " + nearbyPlayers);
            if (nearbyPlayers > 0) {
                mobCount = (int) Math.ceil((double) mobCount / nearbyPlayers);
            }
        }

        ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);
        LMHelperClass.logDebug("JSONSpawner", "Spawning Wave: " + mobCount + " Mob(s)");
        LMHelperClass.logDebug("JSONSpawner", "Trigger World: " + world);
        LMHelperClass.logDebug("JSONSpawner", "Trigger Dimension: " + world.dimension().location());
        LMHelperClass.logDebug("JSONSpawner", "Trigger Player: " + player);
        LMHelperClass.logDebug("JSONSpawner", "Trigger Position: " + triggerPos);

        List<MobSpawn> allRegistered = new ArrayList<>(this.mobSpawns);
        Collection<MobSpawn> globalRegistered = SpawnerMobRegistry.getMobSpawns(this.sharedName);
        if (globalRegistered != null) allRegistered.addAll(0, globalRegistered);
        LMHelperClass.logDebug("JSONSpawner", "Registered mob spawns (" + allRegistered.size() + "): " + allRegistered);

        // Get Positions:
        List<BlockPos> spawnPositions = this.getSpawnPos(world, player, triggerPos);
        // Restrict To Chunk On Chunk Spawn Trigger:
        if (spawnTrigger instanceof ChunkSpawnTrigger) {
            for (BlockPos spawnPos : spawnPositions.toArray(new BlockPos[spawnPositions.size()])) {
                if (spawnPos.getX() < triggerPos.getX() - 8 || spawnPos.getX() > triggerPos.getX() + 8) {
                    LMHelperClass.logDebug("JSONSpawner", "A spawn position exceeds chunk x boundaries and will not be used for worldgen triggered spawning. Trigger Pos: " + triggerPos + " Spawn Pos: " + spawnPos);
                    spawnPositions.remove(spawnPos);
                    continue;
                }
                if (spawnPos.getZ() < triggerPos.getZ() - 8 || spawnPos.getZ() > triggerPos.getZ() + 8) {
                    LMHelperClass.logDebug("JSONSpawner", "A spawn position exceeds chunk z boundaries and will not be used for worldgen triggered spawning. Trigger Pos: " + triggerPos + " Spawn Pos: " + spawnPos);
                    spawnPositions.remove(spawnPos);
                }
            }
        }
        if (spawnPositions.isEmpty()) {
            LMHelperClass.logDebug("JSONSpawner", "No Spawn Positions Found From Spawn Location");
            return false;
        }
        Collections.shuffle(spawnPositions);
        LMHelperClass.logDebug("JSONSpawner", "Positions Found: " + spawnPositions.size());

        // Get Biomes:
        List<Biome> biomes = null;
        if (!this.ignoreBiomes) {
            biomes = new ArrayList<>();
            for (BlockPos spawnPos : spawnPositions) {
                Biome biome = world.getBiomeManager().getBiome(spawnPos).get(); //getBiomeManager().getBiome()
                if (!biomes.contains(biome))
                    biomes.add(biome);
            }
            LMHelperClass.logDebug("JSONSpawner", "Biomes Found: " + biomes.size());
            if (biomes != null) {
                for (Biome b : biomes) {
                    Object bRL = LMHelperClass.convertBiomeToResourceLocation(world, b);
                    LMHelperClass.logDebug("JSONSpawner", "  Biome: " + bRL + " (identity=" + System.identityHashCode(b) + ")");
                }
            }
        } else {
            LMHelperClass.logDebug("JSONSpawner", "All biome checks are ignored by this Spawner.");
        }

        // Get Mobs:
        Map<Biome, List<MobSpawn>> mobSpawns = this.getMobSpawns(world, player, spawnPositions.size(), biomes);
        LMHelperClass.logDebug("JSONSpawner", "Spawnable Mobs Found: " + mobSpawns.size());
        if (mobSpawns.isEmpty()) {
            LMHelperClass.logDebug("JSONSpawner", "No spawnable mobs for spawner '" + this.name + "' | biomes checked: " + biomes + " | positions: " + spawnPositions.size() + " | dimension: " + world.dimension().location());
            return false;
        }

        int mobsSpawned = 0;
        for (BlockPos spawnPos : spawnPositions) {
            if (mobsSpawned >= mobCount) {
                break;
            }
            Biome spawnBiome = world.getBiomeManager().getBiome(spawnPos).get();

            LMHelperClass.logDebug("JSONSpawner", "---------- Spawn Iteration: " + mobsSpawned + " ----------");
            LMHelperClass.logDebug("JSONSpawner", "Spawn Position: " + spawnPos);
            LMHelperClass.logDebug("JSONSpawner", "Spawn Biome: " + spawnBiome);

            // Choose Mob To Spawn:
            MobSpawn mobSpawn = null;

            BlockState feetBlock = world.getBlockState(spawnPos);
            BlockState belowBlock = world.getBlockState(spawnPos.below());
            BlockState aboveBlock = world.getBlockState(spawnPos.above());
            LMHelperClass.logDebug("JSONSpawner", "Block context at " + spawnPos
                    + " | feet=" + LMHelperClass.convertToResourceLocation(feetBlock.getBlock())
                    + " | below=" + LMHelperClass.convertToResourceLocation(belowBlock.getBlock())
                    + " | above=" + LMHelperClass.convertToResourceLocation(aboveBlock.getBlock()));

            if (mobSpawns.containsKey(null)) {
                LMHelperClass.logDebug("JSONSpawner", "Biome Ignored");
                mobSpawn = this.chooseMobToSpawn(world, mobSpawns.get(null));
            } else if (mobSpawns.containsKey(spawnBiome)) {
                List<MobSpawn> biomeMobs = mobSpawns.get(spawnBiome);
                LMHelperClass.logDebug("JSONSpawner", "Biome key found in map, mob list size=" + biomeMobs.size());
                mobSpawn = this.chooseMobToSpawn(world, biomeMobs);
            } else {
                Object spawnBiomeRL = LMHelperClass.convertBiomeToResourceLocation(world, spawnBiome);
                LMHelperClass.logDebug("JSONSpawner", "BIOME KEY MISMATCH! spawnBiome=" + spawnBiomeRL
                        + " (identity=" + System.identityHashCode(spawnBiome) + ") not found in mobSpawns map.");
                LMHelperClass.logDebug("JSONSpawner", "Map keys:");
                for (Biome key : mobSpawns.keySet()) {
                    Object keyRL = LMHelperClass.convertBiomeToResourceLocation(world, key);
                    LMHelperClass.logDebug("JSONSpawner", "  key=" + keyRL
                            + " (identity=" + System.identityHashCode(key)
                            + ") mobs=" + mobSpawns.get(key).size());
                }
            }
            if (mobSpawn == null) {
                LMHelperClass.logDebug("JSONSpawner", "No Mob Spawn Chosen");
                continue;
            }
            LMHelperClass.logDebug("JSONSpawner", "Spawn Mob: " + mobSpawn);

            // Create Entity:
            LivingEntity entityLiving = mobSpawn.createEntity(world);
            if (entityLiving == null) {
                LMHelperClass.logWarning("JSONSpawner", "Unable to instantiate an entity. MobSpawn: " + mobSpawn + " entityType: " + mobSpawn.entityType + " creatureInfo: " + (mobSpawn.creatureInfo != null ? mobSpawn.creatureInfo.getName() : "null"));
                continue;
            }
            BaseCreatureEntity entityCreature = null;
            if (entityLiving instanceof BaseCreatureEntity) {
                entityCreature = (BaseCreatureEntity) entityLiving;
                LMHelperClass.logDebug("JSONSpawner", "Mob is a Lycanites Mob");
            } else {
                LMHelperClass.logDebug("JSONSpawner", "Mob is not a Lycanites Mob");
            }

            // Attempt To Spawn LivingEntity:
            LMHelperClass.logDebug("JSONSpawner", "Attempting to spawn " + entityLiving + "...");
            entityLiving.moveTo((double) spawnPos.getX() + 0.5D, (double) spawnPos.getY(), (double) spawnPos.getZ() + 0.5D, world.random.nextFloat() * 360.0F, 0.0F);

            // Forge Can Spawn Event:
            Event.Result canSpawn = Event.Result.ALLOW;
            if (entityLiving instanceof Mob) {
                BlockPos blockPos = new BlockPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
                canSpawn = new MobSpawnEvent.SpawnPlacementCheck(entityLiving.getType(), (ServerLevelAccessor) world, MobSpawnType.NATURAL, blockPos, world.getRandom(), false).getResult();
                if (canSpawn == Event.Result.DENY && !this.ignoreForgeCanSpawnEvent && !mobSpawn.ignoreForgeCanSpawnEvent) {
                    LMHelperClass.logDebug("JSONSpawner", "Spawn Check Failed! Spawning blocked by Forge Can Spawn Event, this is caused by another mod.");
                    continue;
                }
            }

            // Mob Instance Spawn Check:
            if (!this.mobInstanceSpawnCheck(entityLiving, mobSpawn, world, player, spawnPos, level, canSpawn == Event.Result.ALLOW)) {
                LMHelperClass.logDebug("JSONSpawner", "Mob Instance Spawn Check Failed!");
                continue;
            }

            // Spawn The Mob:
            this.spawnEntity(world, worldExt, entityLiving, level, mobSpawn, player, chain);

            // Call Entity's Initial Spawn:
            if (entityLiving instanceof Mob) {
                BlockPos blockPos = new BlockPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
                if (!ForgeEventFactory.checkSpawnPlacements(entityLiving.getType(), (ServerLevelAccessor) world, MobSpawnType.NATURAL, blockPos, world.getRandom(), false)) {
                    ((Mob) entityLiving).finalizeSpawn((ServerLevelAccessor) world, world.getCurrentDifficultyAt(spawnPos), MobSpawnType.NATURAL, null, null);
                }
            }

            LMHelperClass.logDebug("JSONSpawner", "Spawn Checks Passed! Mob Spawned!");
            mobsSpawned++;
        }

        return mobsSpawned > 0;
    }


    /**
     * Gets a list of BlockPos to spawn at. Returns an empty list if all Spawn Locations fail to get a spawn position. If no Spawn Locations are set, the triggerPos is used.
     *
     * @param world      The World to spawn in.
     * @param player     The player that is being spawned around.
     * @param triggerPos The location that the spawn was triggered, usually used as the center for spawning around or on.
     * @return True on a successful spawn and false on failure.
     **/
    public List<BlockPos> getSpawnPos(Level world, Player player, BlockPos triggerPos) {
        LMHelperClass.logDebug("JSONSpawner", "Searching for Spawn Positions...");
        List<BlockPos> spawnPositions = new ArrayList<>();

        if (this.locations.isEmpty()) {
            LMHelperClass.logDebug("JSONSpawner", "No Spawn Locations, Using Trigger Position");
            spawnPositions.add(triggerPos);
            return spawnPositions;
        }
        if (this.locations.size() == 1) {
            LMHelperClass.logDebug("JSONSpawner", "Only One Spawn Location Type");
            return this.locations.get(0).getSpawnPositions(world, player, triggerPos);
        }

        LMHelperClass.logDebug("JSONSpawner", "Multiple Spawn Location Types, Mode: " + this.multipleLocations);
        if ("order".equals(this.multipleLocations)) {
            for (SpawnLocation location : this.locations) {
                spawnPositions = location.getSpawnPositions(world, player, triggerPos);
                if (!spawnPositions.isEmpty()) {
                    return spawnPositions;
                }
            }
        }

        if ("random".equals(this.multipleLocations)) {
            List<List<BlockPos>> possibleSpawnPosLists = new ArrayList<>();
            for (SpawnLocation location : this.locations) {
                spawnPositions = location.getSpawnPositions(world, player, triggerPos);
                if (!spawnPositions.isEmpty()) {
                    possibleSpawnPosLists.add(spawnPositions);
                }
            }
            if (possibleSpawnPosLists.isEmpty()) {
                return spawnPositions;
            }
            if (possibleSpawnPosLists.size() == 1) {
                return possibleSpawnPosLists.get(0);
            }
            int randomIndex = world.random.nextInt(possibleSpawnPosLists.size());
            return possibleSpawnPosLists.get(randomIndex);
        }

        if ("combine".equals(this.multipleLocations)) {
            for (SpawnLocation location : this.locations) {
                spawnPositions.addAll(location.getSpawnPositions(world, player, triggerPos));
            }
            return spawnPositions;
        }

        return spawnPositions;
    }

    /**
     * Returns all viable mobs that can be spawned within the spawn conditions. Spawn block costs, chances and biomes are checked here.
     *
     * @param world      The World to spawn in.
     * @param player     The player that is being spawned around. Can be null.
     * @param blockCount The total number of possible spawn positions found.
     * @param biomes     A list of all biomes within the spawning area. If null, biome checks will be ignored, all mob spawn will be added to a null map key.
     * @return A map of biomes and mob spawn lists with each mob spawn available to each biome.
     **/
    public Map<Biome, List<MobSpawn>> getMobSpawns(Level world, @Nullable Player player, int blockCount, @Nullable List<Biome> biomes) {
        Map<Biome, List<MobSpawn>> mobSpawns = new HashMap<>();

        if (biomes == null) {
            mobSpawns.put(null, this.getBiomeSpawns(world, player, blockCount, null));
            return mobSpawns;
        }

        for (Biome biome : biomes) {
            mobSpawns.put(biome, this.getBiomeSpawns(world, player, blockCount, biome));
        }
        return mobSpawns;
    }

    /**
     * Returns all viable mobs that can be spawned within the spawn conditions and specific biome.
     *
     * @param world      The world to spawn in.
     * @param player     The player that is being spawned around. Can be null.
     * @param blockCount The total number of possible spawn positions found.
     * @param biome      The biome to spawn in.
     * @return A list of mob spawns.
     */
    public List<MobSpawn> getBiomeSpawns(Level world, @Nullable Player player, int blockCount, @Nullable Biome biome) {
        List<MobSpawn> possibleSpawns = new ArrayList<>();

        // Global Spawns:
        Collection<MobSpawn> globalSpawns = SpawnerMobRegistry.getMobSpawns(this.sharedName);
        if (globalSpawns != null) {
            possibleSpawns.addAll(globalSpawns);
        }

        // Local Spawns:
        possibleSpawns.addAll(this.mobSpawns);

        Object biomeRL = biome != null ? LMHelperClass.convertBiomeToResourceLocation(world, biome) : null;
        LMHelperClass.logDebug("JSONSpawner", "getBiomeSpawns() spawner=" + this.name + " | biome=" + biomeRL + " | possibleMobs=" + possibleSpawns.size() + " (global=" + (globalSpawns != null ? globalSpawns.size() : 0) + ", local=" + this.mobSpawns.size() + ")");

        // Get Viable Spawns:
        List<MobSpawn> viableMobSpawns = new ArrayList<>();
        for (MobSpawn possibleMobSpawn : possibleSpawns) {
            boolean canSpawn = possibleMobSpawn.canSpawn(world, blockCount, biome, this.ignoreDimensions);
            String mobName = possibleMobSpawn.creatureInfo != null ? possibleMobSpawn.creatureInfo.getName() : String.valueOf(possibleMobSpawn.entityType);
            LMHelperClass.logDebug("JSONSpawner", "getBiomeSpawns() mob=" + mobName + " canSpawn=" + canSpawn);
            if (canSpawn) {
                viableMobSpawns.add(possibleMobSpawn);
            }
        }

        LMHelperClass.logDebug("JSONSpawner", "getBiomeSpawns() spawner=" + this.name + " | biome=" + biomeRL + " | viableMobs=" + viableMobSpawns.size() + "/" + possibleSpawns.size());
        return viableMobSpawns;
    }

    /**
     * Gets a weighted random mob to spawn.
     *
     * @param world     The World to spawn in.
     * @param mobSpawns A list of MobSpawns to choose from.
     * @return The MobSpawn of the mob to spawn or null if no mob can be spawned.
     **/
    public MobSpawn chooseMobToSpawn(Level world, List<MobSpawn> mobSpawns) {
        // Get Weights:
        int totalWeights = 0;
        for (MobSpawn mobSpawn : mobSpawns) {
            totalWeights += mobSpawn.getWeight();
        }
        if (totalWeights <= 0) {
            return null;
        }

        // Pick Random Spawn Using Weights:
        int randomWeight = 1;
        if (totalWeights > 1) {
            randomWeight = world.random.nextInt(totalWeights);
        }
        int searchWeight = 0;
        MobSpawn chosenMobSpawn = null;
        for (MobSpawn mobSpawn : mobSpawns) {
            chosenMobSpawn = mobSpawn;
            if (mobSpawn.getWeight() + searchWeight > randomWeight) {
                break;
            }
            searchWeight += mobSpawn.getWeight();
        }
        return chosenMobSpawn;
    }

    /**
     * Performs a check from the Mob Instance, this mob has not yet been spawned into the world, but has been instantiated and is able to do per mob checks.
     *
     * @param entityLiving The to be spawned mob INSTANCE.
     * @param mobSpawn     The MobSpawn that is controlling the conditions of the spawn.
     * @param world        The World to spawn in.
     * @param player       The Player that triggered the spawn. Can be null.
     * @param spawnPos     The position to spawn at.
     * @param level        The level of the spawn.
     * @param forgeForced  If true, the Forge Can Spawn Event wants to force this mob to spawn.
     * @return True if the check has passed.
     **/
    public boolean mobInstanceSpawnCheck(LivingEntity entityLiving, MobSpawn mobSpawn, Level world, Player player, BlockPos spawnPos, int level, boolean forgeForced) {
        // Lycanites Mobs:
        if (entityLiving instanceof BaseCreatureEntity) {
            BaseCreatureEntity entityCreature = (BaseCreatureEntity) entityLiving;

            LMHelperClass.logDebug("JSONSpawner", "Checking Mob Collision...");
            if (!this.ignoreCollision && !entityCreature.checkSpawnCollision(world, spawnPos)) {
                return false;
            }

            LMHelperClass.logDebug("JSONSpawner", "Checking For Nearby Boss...");
            if (!entityCreature.checkSpawnBoss(world, spawnPos)) {
                return false;
            }

            if (mobSpawn.ignoreMobInstanceConditions) {
                LMHelperClass.logDebug("JSONSpawner", "All Mob Instance Checks Ignored");
                return true;
            }

            if (!this.ignoreLightLevel && !mobSpawn.ignoreLightLevel) {
                LMHelperClass.logDebug("JSONSpawner", "Checking Light Level...");
                if (!entityCreature.checkSpawnLightLevel(world, spawnPos)) {
                    return false;
                }
            }

            if (!this.ignoreGroupLimit && !mobSpawn.ignoreGroupLimit) {
                LMHelperClass.logDebug("JSONSpawner", "Checking Group Limit...");
                if (!entityCreature.checkSpawnGroupLimit(world, spawnPos, this.groupLimitRange)) {
                    return false;
                }
            }

            return true;
        }

        // Vanilla or Other Mod Mob:
        if (!mobSpawn.ignoreMobInstanceConditions && entityLiving instanceof Mob) {
            LMHelperClass.logDebug("JSONSpawner", "None Lycanites Mobs Checks...");
            return ((Mob) entityLiving).checkSpawnRules(world, MobSpawnType.NATURAL);
        }
        LMHelperClass.logDebug("JSONSpawner", "All Mob Instance Checks Ignored");
        return true;
    }

    /**
     * Spawns an entity into the world. The mob INSTANCE should have already been positioned.
     *
     * @param world        The world to spawn in.
     * @param entityLiving The entity to spawn.
     */
    public void spawnEntity(Level world, ExtendedWorld worldExt, LivingEntity entityLiving, int level, MobSpawn mobSpawn, Player player, int chain) {
        // Before Spawn:
        entityLiving.setPortalCooldown(); // Start Portal Cooldown

        BaseCreatureEntity entityCreature;
        if (entityLiving instanceof BaseCreatureEntity) {
            entityCreature = (BaseCreatureEntity) entityLiving;
            entityCreature.forceNoDespawn = this.forceNoDespawn;
            entityCreature.spawnedRare = level > 0;
            if (this.blockBreakRadius > -1 && chain == 0) {
                BlockPos creaturePos = entityLiving.blockPosition();
                entityCreature.destroyArea(creaturePos.getX(), creaturePos.getY() - 1, creaturePos.getZ(), 100, true, this.blockBreakRadius, this.chainSpawning ? player : null, chain + 1);
            }
            entityCreature.setDropsRequirePlayerDamage(this.dropsRequirePlayerDamage);

            // Apply Mob Event:
            if (!"".equals(this.eventName) && worldExt != null) {
                MobEventPlayerServer mobEventPlayerServer = worldExt.getMobEventPlayerServer(this.eventName);
                if (mobEventPlayerServer != null) {
                    entityCreature.spawnEventType = mobEventPlayerServer.mobEvent.title;

                    // World Event Binding:
                    if (mobEventPlayerServer.mobEvent.name.equals(worldExt.getWorldEventName())) {
                        entityCreature.spawnEventCount = worldExt.getWorldEventCount();
                    }
                }
            }
        }

        // Spawn:
        LMHelperClass.logDebug("JSONSpawner", "Adding Entity To World: " + world + " - " + entityLiving);
        world.addFreshEntity(entityLiving);

        // After Spawn:
        mobSpawn.onSpawned(entityLiving, player);
        if (!"".equals(this.eventName) && worldExt != null) {
            MobEventPlayerServer mobEventPlayerServer = worldExt.getMobEventPlayerServer(this.eventName);
            if (mobEventPlayerServer != null) {
                MobEvent mobEvent = mobEventPlayerServer.mobEvent;
                mobEvent.onSpawn(entityLiving, mobEventPlayerServer.world, mobEventPlayerServer.player, mobEventPlayerServer.origin, mobEventPlayerServer.level, mobEventPlayerServer.ticks, -1);
            }
        }
    }
}
