package com.lycanitesmobs.core.event;

import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import com.lycanitesmobs.core.capabilities.level.ExtendedWorld;
import com.lycanitesmobs.core.data.config.ConfigExtra;
import com.lycanitesmobs.core.entity.creature.aberration.EntityFear;
import com.lycanitesmobs.core.entity.effect.EffectBase;
import com.lycanitesmobs.core.entity.IGroupBoss;
import com.lycanitesmobs.core.event.mobevent.MobEventSchedule;
import com.lycanitesmobs.core.event.mobevent.trigger.AltarMobEventTrigger;
import com.lycanitesmobs.core.event.mobevent.trigger.MobEventTrigger;
import com.lycanitesmobs.core.event.mobevent.trigger.RandomMobEventTrigger;
import com.lycanitesmobs.core.event.mobevent.trigger.TickMobEventTrigger;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.manager.EffectManager;
import com.lycanitesmobs.core.manager.MobEventManager;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.network.message.MessageEntityVelocity;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.lycanitesmobs.core.util.math.SchismMath;
import com.lycanitesmobs.LycanitesMobs;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class MobEventListener {
    private static MobEventListener INSTANCE;

    private final List<RandomMobEventTrigger> randomMobEventTriggers = new ArrayList<>();
    private final List<TickMobEventTrigger> tickMobEventTriggers = new ArrayList<>();


    /**
     * Returns the main Mob Event Listener instance.
     **/
    public static MobEventListener getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MobEventListener();
        }
        return INSTANCE;
    }


    /**
     * Adds a new Mob Event Trigger.
     *
     * @return True on success, false if it failed to add (could happen if the Trigger type has no matching list created yet).
     */
    public boolean addTrigger(MobEventTrigger mobEventTrigger) {
        if (mobEventTrigger instanceof RandomMobEventTrigger randomMobEventTrigger) {
            return this.addTrigger(this.randomMobEventTriggers, randomMobEventTrigger);
        }
        if (mobEventTrigger instanceof TickMobEventTrigger tickMobEventTrigger) {
            return this.addTrigger(this.tickMobEventTriggers, tickMobEventTrigger);
        }
        return false;
    }

    private <T extends MobEventTrigger> boolean addTrigger(List<T> triggers, T mobEventTrigger) {
        if (triggers.contains(mobEventTrigger)) {
            return false;
        }
        triggers.add(mobEventTrigger);
        return true;
    }

    /**
     * Removes a Mob Event Trigger.
     */
    public void removeTrigger(MobEventTrigger mobEventTrigger) {
        this.removeTrigger(this.randomMobEventTriggers, mobEventTrigger);
        this.removeTrigger(this.tickMobEventTriggers, mobEventTrigger);
        if (mobEventTrigger instanceof AltarMobEventTrigger altarMobEventTrigger) {
            altarMobEventTrigger.onRemove();
        }
    }

    private void removeTrigger(List<? extends MobEventTrigger> triggers, MobEventTrigger mobEventTrigger) {
        triggers.remove(mobEventTrigger);
    }


    /**
     * Called every tick in a world and counts down to the next event then fires it! The countdown is paused during an event.
     **/
    @SubscribeEvent
    public void onWorldUpdate(LevelTickEvent.Post event) {
        Level world = event.getLevel();
        if (world.isClientSide) {
            return;
        }
        ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);
        if (worldExt == null) {
            return;
        }

        if (!MobEventManager.getInstance().areMobEventsEnabled() || world.getDifficulty() == Difficulty.PEACEFUL) {
            if (worldExt.hasServerWorldEventPlayer()) {
                worldExt.stopWorldEvent();
            }
            return;
        }

        if (!worldExt.markEventScheduleTickIfFresh(world.getGameTime())) {
            return;
        }
        long eventScheduleTick = worldExt.getLastEventScheduleTime();

        for (MobEventSchedule mobEventSchedule : MobEventManager.getInstance().getMobEventSchedules()) {
            if (mobEventSchedule.canStart(world)) {
                mobEventSchedule.start(worldExt);
            }
        }

        for (TickMobEventTrigger mobEventTrigger : this.tickMobEventTriggers) {
            mobEventTrigger.onTick(world, eventScheduleTick);
        }

        if (MobEventManager.getInstance().areRandomMobEventsEnabled()) {
            if (MobEventManager.getInstance().getMinEventsRandomDay() > 0
                    && Math.floor(worldExt.getConfiguredDayBaseTime(world) / 24000D) < MobEventManager.getInstance().getMinEventsRandomDay()) {
                return;
            }
            if (worldExt.getWorldEventStartTargetTime() <= 0
                    || worldExt.getWorldEventStartTargetTime() > world.getGameTime() + MobEventManager.getInstance().getMaxTicksUntilEvent()) {
                worldExt.setWorldEventStartTargetTime(world.getGameTime() + worldExt.getRandomEventDelay(world.random));
            }
            if (world.getGameTime() == worldExt.getWorldEventStartTargetTime()) {
                this.triggerRandomMobEvent(world, worldExt, 1);
            } else if (world.getGameTime() > worldExt.getWorldEventStartTargetTime()) {
                worldExt.setWorldEventStartTargetTime(0);
            }
        }
    }


    /**
     * Triggers a Random Mob Event Trigger if one is available.
     **/
    public void triggerRandomMobEvent(Level world, ExtendedWorld worldExt, int level) {
        List<RandomMobEventTrigger> validTriggers = new ArrayList<>();
        int totalWeights = 0;
        int highestPriority = 0;
        for (RandomMobEventTrigger mobEventTrigger : this.randomMobEventTriggers) {
            if (mobEventTrigger.getPriority() >= highestPriority && mobEventTrigger.canTrigger(world, null)) {
                if (mobEventTrigger.getPriority() > highestPriority) {
                    totalWeights = 0;
                    validTriggers.clear();
                }
                totalWeights += mobEventTrigger.getWeight();
                highestPriority = mobEventTrigger.getPriority();
                validTriggers.add(mobEventTrigger);
            }
        }
        if (totalWeights <= 0) {
            return;
        }

        int randomWeight = 1;
        if (totalWeights > 1) {
            randomWeight = world.random.nextInt(totalWeights - 1) + 1;
        }
        int searchWeight = 0;
        for (RandomMobEventTrigger mobEventTrigger : validTriggers) {
            if (mobEventTrigger.getWeight() + searchWeight > randomWeight) {
                mobEventTrigger.trigger(world, null, new BlockPos(0, 0, 0), level, -1);
                return;
            }
            searchWeight += mobEventTrigger.getWeight();
        }
    }


    private static final UUID SWIFTSWIMMING_MOVE_BOOST_UUID = UUID.fromString("6d4fe17f-06eb-4ebc-a573-364b79faed5e");
    private static final AttributeModifier SWIFTSWIMMING_MOVE_BOOST = new AttributeModifier(SWIFTSWIMMING_MOVE_BOOST_UUID, "Swiftswimming Speed Boost", 1D, AttributeModifier.Operation.ADD_VALUE);
    private static final UUID SWIFTSWIMMING_MOVE_BOOST_UUID_2 = UUID.fromString("6d4fe17f-06eb-4ebc-a573-364b79faed5d");
    private static final AttributeModifier SWIFTSWIMMING_MOVE_BOOST_2 = new AttributeModifier(SWIFTSWIMMING_MOVE_BOOST_UUID_2, "Swiftswimming Speed Boost 2", 2D, AttributeModifier.Operation.ADD_VALUE);
    private static final UUID SWIFTSWIMMING_MOVE_BOOST_UUID_3 = UUID.fromString("6d4fe17f-06eb-4ebc-a573-364b79faed5c");
    private static final AttributeModifier SWIFTSWIMMING_MOVE_BOOST_3 = new AttributeModifier(SWIFTSWIMMING_MOVE_BOOST_UUID_3, "Swiftswimming Speed Boost 3", 3D, AttributeModifier.Operation.ADD_VALUE);
    private static final UUID SWIFTSWIMMING_MOVE_BOOST_UUID_4 = UUID.fromString("6d4fe17f-06eb-4ebc-a573-364b79faed5b");
    private static final AttributeModifier SWIFTSWIMMING_MOVE_BOOST_4 = new AttributeModifier(SWIFTSWIMMING_MOVE_BOOST_UUID_4, "Swiftswimming Speed Boost 4", 4D, AttributeModifier.Operation.ADD_VALUE);


    @SubscribeEvent
    public void onEntityUpdate(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity == null) {
            return;
        }

        this.clearNullEffects(entity);
        this.clearBlindnessForNightVision(entity);
        this.removeDisabledNausea(event, entity);

        boolean invulnerable = this.isInvulnerable(entity);

        this.handleParalysis(entity, invulnerable);
        this.handleWeight(entity, invulnerable);
        this.handleFear(entity, invulnerable);
        this.handleInstability(entity, invulnerable);
        this.handlePlague(entity, invulnerable);
        this.handleSmited(entity, invulnerable);
        this.handleBleed(event, entity, invulnerable);
        this.handleSmouldering(entity, invulnerable);

        this.handleSwiftswimming(entity);
        this.handleImmunization(entity);
        this.handleCleansed(entity);
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player == null || player.getCommandSenderWorld().isClientSide) {
            return;
        }

        for (EntityFear fearEntity : player.getCommandSenderWorld().getEntitiesOfClass(
                EntityFear.class,
                player.getBoundingBox().inflate(128.0D),
                fear -> player.equals(fear.getHauntTarget())
        )) {
            fearEntity.discard();
        }
    }

    @SubscribeEvent
    public void onEntityJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity == null || this.isInvulnerable(entity)) {
            return;
        }

        EffectBase paralysis = ObjectManager.getEffect("paralysis");
        if (paralysis != null && entity.hasEffect(paralysis) && event.isCancelable()) {
            event.setCanceled(true);
        }

        EffectBase weight = ObjectManager.getEffect("weight");
        if (weight != null && entity.hasEffect(weight) && event.isCancelable()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (event.isCancelable() && event.isCanceled()) {
            return;
        }

        LivingEntity target = event.getEntity();
        if (target == null) {
            return;
        }

        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity livingAttacker ? livingAttacker : null;
        if (attacker == null) {
            return;
        }

        EffectBase lifeleak = ObjectManager.getEffect("lifeleak");
        if (lifeleak != null && !target.getCommandSenderWorld().isClientSide && attacker.hasEffect(lifeleak)) {
            if (event.isCancelable()) {
                event.setCanceled(true);
            }
            target.heal(event.getAmount());
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (event.isCancelable() && event.isCanceled()) {
            return;
        }

        LivingEntity target = event.getEntity();
        if (target == null) {
            return;
        }

        Entity attacker = event.getSource().getEntity();

        this.handleFallResistance(event, target);
        this.handlePenetration(event, target);
        this.handleFearWallCollision(event, target);
        this.handleLeech(event);
        this.handleRepulsion(target, attacker);
    }

    @SubscribeEvent
    public void onEntityHeal(LivingHealEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity == null) {
            return;
        }

        EffectBase rejuvenation = ObjectManager.getEffect("rejuvenation");
        if (rejuvenation != null && entity.hasEffect(rejuvenation)) {
            event.setAmount((float) Math.ceil(event.getAmount() * (2 * (1 + entity.getEffect(rejuvenation).getAmplifier()))));
        }

        EffectBase decay = ObjectManager.getEffect("decay");
        if (decay != null && entity.hasEffect(decay)) {
            event.setAmount((float) Math.floor(event.getAmount() / (2 * (1 + entity.getEffect(decay).getAmplifier()))));
        }
    }

    @SubscribeEvent
    public void onSleep(PlayerSleepInBedEvent event) {
        Player player = event.getEntity();
        if (player == null || player.getCommandSenderWorld().isClientSide || event.isCanceled()) {
            return;
        }

        EffectBase insomnia = ObjectManager.getEffect("insomnia");
        if (insomnia != null && player.hasEffect(insomnia)) {
            event.setResult(Player.BedSleepingProblem.NOT_SAFE);
        }
    }

    @SubscribeEvent
    public void onLivingUseItem(LivingEntityUseItemEvent event) {
        if (event.isCancelable() && event.isCanceled()) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity == null) {
            return;
        }

        EffectBase aphagia = ObjectManager.getEffect("aphagia");
        if (aphagia != null && !entity.getCommandSenderWorld().isClientSide && entity.hasEffect(aphagia) && event.isCancelable()) {
            event.setCanceled(true);
        }
    }

    private void clearNullEffects(LivingEntity entity) {
        for (Object effectObj : entity.getActiveEffects()) {
            if (effectObj == null) {
                entity.removeAllEffects();
                LMHelperClass.logWarning("EffectsSetup", "Found a null potion effect on entity: " + entity + " all effects have been removed from this entity.");
            }
        }
    }

    private void clearBlindnessForNightVision(LivingEntity entity) {
        if (entity.hasEffect(MobEffects.BLINDNESS) && entity.hasEffect(MobEffects.NIGHT_VISION)) {
            entity.removeEffect(MobEffects.BLINDNESS);
        }
    }

    private void removeDisabledNausea(EntityTickEvent.Pre event, LivingEntity entity) {
        EffectManager effectManager = EffectManager.getInstance();
        effectManager.setNauseaDisabled(ConfigExtra.INSTANCE.disableNausea.get());
        if (effectManager.isNauseaDisabled() && event.getEntity() instanceof Player && entity.hasEffect(MobEffects.CONFUSION)) {
            entity.removeEffect(MobEffects.CONFUSION);
        }
    }

    private boolean isInvulnerable(LivingEntity entity) {
        if (entity instanceof Player player) {
            return player.isCreative() || player.isSpectator();
        }
        return false;
    }

    private void handleParalysis(LivingEntity entity, boolean invulnerable) {
        EffectBase paralysis = ObjectManager.getEffect("paralysis");
        if (paralysis != null && !invulnerable && entity.hasEffect(paralysis)) {
            entity.setDeltaMovement(0, entity.getDeltaMovement().y() > 0 ? 0 : entity.getDeltaMovement().y(), 0);
            entity.setOnGround(false);
        }
    }

    private void handleWeight(LivingEntity entity, boolean invulnerable) {
        EffectBase weight = ObjectManager.getEffect("weight");
        if (weight != null && !invulnerable && entity.hasEffect(weight) && !entity.hasEffect(MobEffects.DAMAGE_BOOST)) {
            if (entity.getDeltaMovement().y() > -0.2D) {
                entity.setDeltaMovement(entity.getDeltaMovement().add(0, -0.2D, 0));
            }
        }
    }

    private void handleFear(LivingEntity entity, boolean invulnerable) {
        EffectBase fear = ObjectManager.getEffect("fear");
        if (fear != null && !entity.getCommandSenderWorld().isClientSide && !invulnerable && entity.hasEffect(fear) && entity instanceof Player player) {
            EntityFear.spawnForPlayer(player, null);
        }
    }

    private void handleInstability(LivingEntity entity, boolean invulnerable) {
        EffectBase instability = ObjectManager.getEffect("instability");
        if (instability == null || entity.getCommandSenderWorld().isClientSide || entity instanceof IGroupBoss) {
            return;
        }
        if (invulnerable || !entity.hasEffect(instability) || entity.getCommandSenderWorld().random.nextDouble() > 0.1D) {
            return;
        }

        double strength = 1 + entity.getEffect(instability).getAmplifier();
        double motionX = strength * (entity.getCommandSenderWorld().random.nextDouble() - 0.5D);
        double motionY = strength * (entity.getCommandSenderWorld().random.nextDouble() - 0.5D);
        double motionZ = strength * (entity.getCommandSenderWorld().random.nextDouble() - 0.5D);
        entity.setDeltaMovement(entity.getDeltaMovement().add(motionX, motionY, motionZ));
        try {
            if (entity instanceof ServerPlayer player) {
                player.connection.send(new ClientboundSetEntityMotionPacket(entity));
                MessageEntityVelocity messageEntityVelocity = new MessageEntityVelocity(
                        player,
                        strength * (entity.getCommandSenderWorld().random.nextDouble() - 0.5D),
                        strength * (entity.getCommandSenderWorld().random.nextDouble() - 0.5D),
                        strength * (entity.getCommandSenderWorld().random.nextDouble() - 0.5D)
                );
                LycanitesMobs.PACKET_MANAGER.sendToPlayer(messageEntityVelocity, player);
            }
        } catch (Exception e) {
            LMHelperClass.logWarningMessage("Failed to create and send a network packet for instability velocity!");
            e.printStackTrace();
        }
    }

    private void handlePlague(LivingEntity entity, boolean invulnerable) {
        EffectBase plague = ObjectManager.getEffect("plague");
        if (plague == null || entity.getCommandSenderWorld().isClientSide || invulnerable || !entity.hasEffect(plague)) {
            return;
        }

        int poisonAmplifier = entity.getEffect(plague).getAmplifier();
        int poisonDuration = entity.getEffect(plague).getDuration();
        if (entity.hasEffect(MobEffects.POISON)) {
            poisonAmplifier = Math.max(poisonAmplifier, entity.getEffect(MobEffects.POISON).getAmplifier());
            poisonDuration = Math.max(poisonDuration, entity.getEffect(MobEffects.POISON).getDuration());
        }
        entity.addEffect(new MobEffectInstance(MobEffects.POISON, poisonDuration, poisonAmplifier));

        if (entity.getCommandSenderWorld().getGameTime() % 20 != 0) {
            return;
        }

        List aoeTargets = EffectManager.getInstance().getNearbyEntities(entity, LivingEntity.class, null, 2);
        for (Object entityObj : aoeTargets) {
            LivingEntity target = (LivingEntity) entityObj;
            if (target == entity || entity.isAlliedTo(target)) {
                continue;
            }
            if (target instanceof Player && !entity.hasLineOfSight(target)) {
                continue;
            }

            int amplifier = entity.getEffect(plague).getAmplifier();
            int duration = entity.getEffect(plague).getDuration();
            if (amplifier > 0) {
                target.addEffect(new MobEffectInstance(plague, duration, amplifier - 1));
            } else {
                target.addEffect(new MobEffectInstance(MobEffects.POISON, duration, amplifier));
            }
        }
    }

    private void handleSmited(LivingEntity entity, boolean invulnerable) {
        EffectBase smited = ObjectManager.getEffect("smited");
        if (smited != null && !entity.getCommandSenderWorld().isClientSide && !invulnerable && entity.hasEffect(smited) && entity.getCommandSenderWorld().getGameTime() % 20 == 0) {
            float brightness = LMHelperClass.getBrightness(entity);
            if (brightness > 0.5F && entity.getCommandSenderWorld().canSeeSkyFromBelowWater(entity.blockPosition())) {
                entity.igniteForSeconds(4);
            }
        }
    }

    private void handleBleed(EntityTickEvent.Pre event, LivingEntity entity, boolean invulnerable) {
        EffectBase bleed = ObjectManager.getEffect("bleed");
        if (bleed != null && !entity.getCommandSenderWorld().isClientSide && !invulnerable && entity.hasEffect(bleed) && entity.getCommandSenderWorld().getGameTime() % 20 == 0 && entity.getVehicle() == null) {
            if (entity.walkDistO != entity.walkDist) {
                entity.hurt(event.getEntity().level().damageSources().magic(), entity.getEffect(bleed).getAmplifier() + 1);
            }
        }
    }

    private void handleSmouldering(LivingEntity entity, boolean invulnerable) {
        EffectBase smouldering = ObjectManager.getEffect("smouldering");
        if (smouldering != null && !entity.getCommandSenderWorld().isClientSide && !invulnerable && entity.hasEffect(smouldering) && entity.getCommandSenderWorld().getGameTime() % 20 == 0) {
            entity.igniteForSeconds(4 + (4 * entity.getEffect(smouldering).getAmplifier()));
        }
    }

    private void handleSwiftswimming(LivingEntity entity) {
        if (!(entity instanceof Player)) {
            return;
        }

        EffectBase swiftswimming = ObjectManager.getEffect("swiftswimming");
        if (swiftswimming == null) {
            return;
        }

        AttributeInstance movement = entity.getAttribute(NeoForgeMod.SWIM_SPEED.get());
        if (movement == null) {
            return;
        }

        int amplifier = -1;
        if (entity.hasEffect(swiftswimming)) {
            amplifier = entity.getEffect(swiftswimming).getAmplifier();
        }

        this.syncSwiftswimmingModifier(movement, amplifier == 0, SWIFTSWIMMING_MOVE_BOOST_UUID, SWIFTSWIMMING_MOVE_BOOST);
        this.syncSwiftswimmingModifier(movement, amplifier == 1, SWIFTSWIMMING_MOVE_BOOST_UUID_2, SWIFTSWIMMING_MOVE_BOOST_2);
        this.syncSwiftswimmingModifier(movement, amplifier == 2, SWIFTSWIMMING_MOVE_BOOST_UUID_3, SWIFTSWIMMING_MOVE_BOOST_3);
        this.syncSwiftswimmingModifier(movement, amplifier >= 3, SWIFTSWIMMING_MOVE_BOOST_UUID_4, SWIFTSWIMMING_MOVE_BOOST_4);
    }

    private void syncSwiftswimmingModifier(AttributeInstance movement, boolean shouldApply, UUID modifierId, AttributeModifier modifier) {
        if (shouldApply && movement.getModifier(modifierId) == null) {
            movement.addPermanentModifier(modifier);
        } else if (!shouldApply && movement.getModifier(modifierId) != null) {
            movement.removeModifier(modifier);
        }
    }

    private void handleImmunization(LivingEntity entity) {
        EffectBase immunization = ObjectManager.getEffect("immunization");
        if (immunization == null || entity.getCommandSenderWorld().isClientSide || !entity.hasEffect(immunization)) {
            return;
        }

        if (entity.hasEffect(MobEffects.POISON)) {
            entity.removeEffect(MobEffects.POISON);
        }
        if (entity.hasEffect(MobEffects.HUNGER)) {
            entity.removeEffect(MobEffects.HUNGER);
        }
        if (entity.hasEffect(MobEffects.WEAKNESS)) {
            entity.removeEffect(MobEffects.WEAKNESS);
        }
        if (entity.hasEffect(MobEffects.CONFUSION)) {
            entity.removeEffect(MobEffects.CONFUSION);
        }

        EffectBase paralysis = ObjectManager.getEffect("paralysis");
        if (paralysis != null && entity.hasEffect(paralysis)) {
            entity.removeEffect(paralysis);
        }
    }

    private void handleCleansed(LivingEntity entity) {
        EffectBase cleansed = ObjectManager.getEffect("cleansed");
        if (cleansed == null || entity.getCommandSenderWorld().isClientSide || !entity.hasEffect(cleansed)) {
            return;
        }

        if (entity.hasEffect(MobEffects.WITHER)) {
            entity.removeEffect(MobEffects.WITHER);
        }
        if (entity.hasEffect(MobEffects.UNLUCK)) {
            entity.removeEffect(MobEffects.UNLUCK);
        }

        EffectBase fear = ObjectManager.getEffect("fear");
        if (fear != null && entity.hasEffect(fear)) {
            entity.removeEffect(fear);
        }

        EffectBase insomnia = ObjectManager.getEffect("insomnia");
        if (insomnia != null && entity.hasEffect(insomnia)) {
            entity.removeEffect(insomnia);
        }
    }

    private void handleFallResistance(LivingHurtEvent event, LivingEntity target) {
        EffectBase fallresist = ObjectManager.getEffect("fallresist");
        if (fallresist != null && target.hasEffect(fallresist) && "fall".equals(event.getSource().getMsgId())) {
            event.setAmount(0);
            event.setCanceled(true);
        }
    }

    private void handlePenetration(LivingHurtEvent event, LivingEntity target) {
        EffectBase penetration = ObjectManager.getEffect("penetration");
        if (penetration != null && target.hasEffect(penetration)) {
            float damage = event.getAmount();
            float multiplier = 0.25F * (target.getEffect(penetration).getAmplifier() + 1);
            event.setAmount(damage + (damage * multiplier));
        }
    }

    private void handleFearWallCollision(LivingHurtEvent event, LivingEntity target) {
        EffectBase fear = ObjectManager.getEffect("fear");
        if (fear != null && target.hasEffect(fear) && "inWall".equals(event.getSource().getMsgId())) {
            event.setAmount(0);
            event.setCanceled(true);
        }
    }

    private void handleLeech(LivingHurtEvent event) {
        EffectBase leech = ObjectManager.getEffect("leech");
        if (leech == null || event.getSource().getEntity() == null) {
            return;
        }

        LivingEntity leechingEntity = null;
        if (event.getSource().getDirectEntity() instanceof LivingEntity directLiving) {
            leechingEntity = directLiving;
        } else if (event.getSource().getEntity() instanceof LivingEntity sourceLiving) {
            leechingEntity = sourceLiving;
        }

        if (leechingEntity != null && leechingEntity.hasEffect(leech)) {
            int leeching = leechingEntity.getEffect(leech).getAmplifier() + 1;
            leechingEntity.heal(Math.max(leeching, 1));
        }
    }

    private void handleRepulsion(LivingEntity target, Entity attacker) {
        EffectBase repulsion = ObjectManager.getEffect("repulsion");
        if (repulsion == null) {
            return;
        }

        boolean attackerIsBoss = attacker instanceof IGroupBoss;
        if (!attackerIsBoss && CreatureManager.getInstance().getCreatureGroup("boss") != null) {
            attackerIsBoss = CreatureManager.getInstance().getCreatureGroup("boss").hasEntity(attacker);
        }
        if (attacker != null && !attackerIsBoss && target.hasEffect(repulsion)) {
            double knockback = target.getEffect(repulsion).getAmplifier() + 2;
            double xDist = attacker.position().x() - target.position().x();
            double zDist = attacker.position().z() - target.position().z();
            double xzDist = SchismMath.horizontalDistanceAtLeast(xDist, zDist, 0.01D);
            double motionCap = 10;
            double xVel = xDist / xzDist * knockback;
            double zVel = zDist / xzDist * knockback;
            if (attacker.getDeltaMovement().x() < motionCap && attacker.getDeltaMovement().x() > -motionCap && attacker.getDeltaMovement().z() < motionCap && attacker.getDeltaMovement().z() > -motionCap) {
                attacker.push(xVel, 0, zVel);
            }
        }
    }


    public static void logBiomeAt(Level world, BlockPos pos) {
        ResourceLocation biomeId = world
                .getBiome(pos)
                .unwrapKey()
                .map(key -> key.location())
                .orElse(null);

        LMHelperClass.logInfo(
                "Dungeon",
                "[BiomeDebug] World biome at " + pos + " is " + biomeId
        );
    }
}
