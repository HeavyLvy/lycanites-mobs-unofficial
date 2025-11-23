package com.lycanitesmobs.core.event;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.block.base.BlockFireBase;
import com.lycanitesmobs.core.capabilities.entity.CapabilityProviderEntity;
import com.lycanitesmobs.core.capabilities.entity.CapabilityProviderPlayer;
import com.lycanitesmobs.core.capabilities.entity.ExtendedEntity;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.data.config.ConfigExtra;
import com.lycanitesmobs.core.capabilities.level.ExtendedWorld;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.base.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.item.CustomItemEntity;
import com.lycanitesmobs.core.data.info.item.ItemConfig;
import com.lycanitesmobs.core.item.equipment.ItemEquipment;
import com.lycanitesmobs.core.network.message.MessagePlayerLeftClick;

import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LycanitesMobs.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GameEventListener {

    // ==================================================
    //                     Constructor
    // ==================================================
    public GameEventListener() {
    }


    // ==================================================
    //                    World Load
    // ==================================================
    @SubscribeEvent
    public void onWorldLoading(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof Level))
            return;

        // ========== Extended World ==========
        ExtendedWorld.getForWorld((Level) event.getLevel());
    }

    @SubscribeEvent
    public void onWorldUnloading(LevelEvent.Unload event) {
        ExtendedWorld.loadedExtWorlds.remove(event.getLevel());
    }


    // ==================================================
    //                    Player Clone
    // ==================================================
    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(event.getOriginal());
        if (extendedPlayer != null) {
            extendedPlayer.backupPlayer();
        }
    }

    // ==================================================
    //                Attach Capabilities
    // ==================================================
    @SubscribeEvent
    public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(new ResourceLocation(LycanitesMobs.MODID, "player"), new CapabilityProviderPlayer());
        } else {
            if (!event.getObject().getCapability(CapabilityProviderEntity.EXTENDED_ENTITY).isPresent()) {
                event.addCapability(new ResourceLocation(LycanitesMobs.MODID, "entity"), new CapabilityProviderEntity());
            }
        }
    }

    // ==================================================
    //                Register Capabilities
    // ==================================================
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(ExtendedEntity.class);
        event.register(ExtendedPlayer.class);
    }

    // ==================================================
    //                Entity Constructing
    // ==================================================
    @SubscribeEvent
    public void onEntityConstructing(EntityConstructing event) {
        if (event.getEntity() == null || event.getEntity().getCommandSenderWorld() == null || event.getEntity().getCommandSenderWorld().isClientSide)
            return;

        // ========== Force Remove Entity ==========
        if (!(event.getEntity() instanceof LivingEntity)) {
            if (ExtendedEntity.FORCE_REMOVE_ENTITY_IDS != null && !ExtendedEntity.FORCE_REMOVE_ENTITY_IDS.isEmpty()) {
                LMHelperClass.logDebug("ForceRemoveEntity", "Forced entity removal, checking: " + event.getEntity().getName());
                for (String forceRemoveID : ExtendedEntity.FORCE_REMOVE_ENTITY_IDS) {
                    if (forceRemoveID.equalsIgnoreCase(LMHelperClass.convertToResourceLocation(event.getEntity()).toString())) {
                        event.getEntity().remove(Entity.RemovalReason.DISCARDED);
                        break;
                    }
                }
            }
        }
    }


    // ==================================================
    //                Entity Leave World
    // ==================================================
    @SubscribeEvent
    public void onEntityLeaveWorld(EntityLeaveLevelEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        ExtendedEntity extendedEntity = ExtendedEntity.getForEntity((LivingEntity) event.getEntity());
        if (extendedEntity != null) {
            extendedEntity.onEntityRemoved();
        }
        if (event.getEntity() instanceof Player) {
            ExtendedPlayer.clientExtendedPlayers.remove((Player) event.getEntity());
        }
    }


    // ==================================================
    //                 Living Death Event
    // ==================================================
    @SubscribeEvent
    public void onLivingDeathEvent(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity == null) return;

        // ========== Extended Entity ==========
        ExtendedEntity extendedEntity = ExtendedEntity.getForEntity(entity);
        if (extendedEntity != null)
            extendedEntity.onDeath();

        // ========== Extended Player ==========
        if (entity instanceof Player) {
            Player player = (Player) entity;
            ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(player);
            if (extendedPlayer != null)
                extendedPlayer.onDeath();
        }
    }


    // ==================================================
    //                   Entity Update
    // ==================================================
    @SubscribeEvent
    public void onEntityUpdate(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity == null) return;

        // ========== Extended Entity ==========
        ExtendedEntity extendedEntity = ExtendedEntity.getForEntity(entity);
        if (extendedEntity != null)
            extendedEntity.onUpdate();

        // ========== Extended Player ==========
        if (entity instanceof Player) {
            Player player = (Player) entity;
            ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
            if (playerExt != null)
                playerExt.onUpdate();
        }
    }


    // ==================================================
    //                    Player Click
    // ==================================================
    @SubscribeEvent
    public void onPlayerLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        Player player = event.getEntity();
        if (player == null)
            return;

        ItemStack itemStack = player.getItemInHand(event.getHand());
        Item item = itemStack.getItem();
        if (item instanceof ItemEquipment) {
            MessagePlayerLeftClick message = new MessagePlayerLeftClick();
            LycanitesMobs.PACKET_MANAGER.sendToServer(message);
        }
    }

    @SubscribeEvent
    public void onPlayerLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (player == null || event.getSide().isClient())
            return;

        ItemStack itemStack = player.getItemInHand(event.getHand());
        Item item = itemStack.getItem();
        if (item instanceof ItemEquipment) {
            ((ItemEquipment) item).onItemLeftClick(event.getLevel(), player, event.getHand());
        }
    }


    // ==================================================
    //               Entity Interact Event
    // ==================================================
    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        Entity entity = event.getTarget();
        if (player == null || !(entity instanceof LivingEntity))
            return;

		/*ItemStack itemStack = player.getHeldItem(event.getHand());
		Item item = itemStack.getItem();
		if (item instanceof ItemBase) {
			if (item.itemInteractionForEntity(itemStack, player, (LivingEntity)entity, event.getHand())) {
				if (event.isCancelable())
					event.setCanceled(true);
			}
		}*/
    }


    // ==================================================
    //                 Attack Target Event
    // ==================================================
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onAttackTarget(LivingChangeTargetEvent event) {
        Entity targetEntity = event.getNewTarget();
        if (event.getEntity() == null || targetEntity == null) {
            return;
        }

        // Better Invisibility:
        if (!event.getEntity().hasEffect(MobEffects.INVISIBILITY)) {
            if (targetEntity.isInvisible()) {
                if (event.isCancelable())
                    event.setCanceled(true);
                //event.getEntity().setRevengeTarget(null);
                return;
            }
        }

        // Can Be Targeted:
        if (event.getEntity() instanceof Mob && targetEntity instanceof BaseCreatureEntity) {
            if (!((BaseCreatureEntity) targetEntity).canBeTargetedBy(event.getEntity())) {
                //event.getEntity().setRevengeTarget(null);
                if (event.isCancelable())
                    event.setCanceled(true);
                //((MobEntity)event.getEntity()).setAttackTarget(null);
            }
        }
    }


    // ==================================================
    //                 Living Hurt Event
    // ==================================================
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLivingHurt(LivingHurtEvent event) {
        if (event.isCanceled())
            return;

        if (event.getSource() == null || event.getEntity() == null)
            return;

        LivingEntity damagedEntity = event.getEntity();
        ExtendedEntity damagedEntityExt = ExtendedEntity.getForEntity(damagedEntity);

        // True Source Extended Entity:
        if (event.getSource().getEntity() != null &&
                event.getSource().getEntity() instanceof LivingEntity entity) {
            ExtendedEntity attackerExtendedEntity = ExtendedEntity.getForEntity(entity);
            if (attackerExtendedEntity != null) {
                attackerExtendedEntity.setLastAttackedEntity(damagedEntity);
            }
        }

        // ========== Mounted Protection ==========
        if (damagedEntity.getVehicle() != null) {
            if (damagedEntity.getVehicle() instanceof RideableCreatureEntity) {
                RideableCreatureEntity creatureRideable = (RideableCreatureEntity) event.getEntity().getVehicle();

                // Shielding:
                if (creatureRideable.isBlocking()) {
                    event.setAmount(0);
                    event.setCanceled(true);
                    return;
                }

                // Prevent Mounted Entities from Suffocating:
                if ("inWall".equals(event.getSource().getMsgId())) {
                    event.setAmount(0);
                    event.setCanceled(true);
                    return;
                }

                // Copy Mount Immunities to Rider:
                if (!creatureRideable.isVulnerableTo(event.getSource().getMsgId(), event.getSource(), event.getAmount())) {
                    event.setAmount(0);
                    event.setCanceled(true);
                    return;
                }
            }
        }

        // ========== Picked Up/Feared Protection ==========
        if (damagedEntityExt != null && damagedEntityExt.isPickedUp()) {
            // Prevent Picked Up and Feared Entities from Suffocating:
            if ("inWall".equals(event.getSource().getMsgId())) {
                event.setAmount(0);
                event.setCanceled(true);
                return;
            }
        }
    }


    // ==================================================
    //                 Living Drops Event
    // ==================================================
    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        Level world = event.getEntity().getCommandSenderWorld();

        // Seasonal Items:
        if (ItemConfig.seasonalItemDropChance > 0
                && (LMHelperClass.isHalloween() || LMHelperClass.isYuletide() || LMHelperClass.isNewYear())) {
            boolean noSeaonalDrop = false;
            if (event.getEntity() instanceof BaseCreatureEntity) {
                if (((BaseCreatureEntity) event.getEntity()).isMinion())
                    noSeaonalDrop = true;
            }

            Item seasonalItem = null;
            if (LMHelperClass.isHalloween())
                seasonalItem = ObjectManager.getItem("halloweentreat");
            if (LMHelperClass.isYuletide()) {
                seasonalItem = ObjectManager.getItem("wintergift");
                if (LMHelperClass.isYuletidePeak() && world.random.nextBoolean())
                    seasonalItem = ObjectManager.getItem("wintergiftlarge");
            }

            if (seasonalItem != null && !noSeaonalDrop && event.getEntity().getRandom().nextFloat() < ItemConfig.seasonalItemDropChance) {
                ItemStack dropStack = new ItemStack(seasonalItem, 1);
                CustomItemEntity entityItem = new CustomItemEntity(world, event.getEntity().position().x(), event.getEntity().position().y(), event.getEntity().position().z(), dropStack);
                entityItem.setPickUpDelay(10);
                world.addFreshEntity(entityItem);
            }
        }
    }


    // ==================================================
    //                 Bucket Fill Event
    // ==================================================
    @SubscribeEvent
    public void onBucketFill(FillBucketEvent event) {
        Level world = event.getLevel();
        HitResult target = event.getTarget();
        if (target == null || !(target instanceof BlockHitResult))
            return;
        BlockPos pos = ((BlockHitResult) target).getBlockPos();
        Block block = world.getBlockState(pos).getBlock();
        Item bucket = ObjectManager.buckets.get(block);
        if (bucket != null && world.getFluidState(pos).getAmount() == 0) {
            world.removeBlock(pos, true);
        }

        if (bucket == null)
            return;

        event.setFilledBucket(new ItemStack(bucket));
        event.setResult(Event.Result.ALLOW);
    }


    // ==================================================
    //                 Break Block Event
    // ==================================================
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getState() == null || event.getLevel() == null || event.getLevel().isClientSide() || event.isCanceled()) {
            return;
        }

        if (event.getPlayer() != null && !event.getPlayer().isCreative()) {
            if (event.getLevel() instanceof Level) {
                ExtendedWorld extendedWorld = ExtendedWorld.getForWorld((Level) event.getLevel());
                if (!(event.getState().getBlock() instanceof BlockFireBase) && extendedWorld.isBossNearby(Vec3.atLowerCornerOf(event.getPos()))) {
                    event.setCanceled(true);
                    event.setResult(Event.Result.DENY);
                    event.getPlayer().displayClientMessage(Component.translatable("boss.block.protection.break"), true);
                    return;
                }
            }
        }

        if (event.getPlayer() != null) {
            ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(event.getPlayer());
            if (extendedPlayer == null) {
                return;
            }
            extendedPlayer.setJustBrokenBlock(event.getState());
        }
    }


    // ==================================================
    //                 Block Place Event
    // ==================================================

    /**
     * This uses the block place events to update Block Spawn Triggers.
     **/
    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getState() == null || event.getLevel() == null || event.getLevel().isClientSide() || event.isCanceled()) {
            return;
        }

        if (event.getEntity() instanceof Player && !((Player) event.getEntity()).isCreative()) {
            if (event.getLevel() instanceof Level) {
                ExtendedWorld extendedWorld = ExtendedWorld.getForWorld((Level) event.getLevel());
                if (extendedWorld.isBossNearby(Vec3.atLowerCornerOf(event.getPos()))) {
                    event.setCanceled(true);
                    event.setResult(Event.Result.DENY);
                    ((Player) event.getEntity()).displayClientMessage(Component.translatable("boss.block.protection.place"), true);
                    return;
                }
            }
        }
    }


    // ==================================================
    //                   Check Spawn
    // ==================================================
    @SubscribeEvent
    public void onCheckSpawn(MobSpawnEvent.PositionCheck event) {
        if (event.getSpawnType() == MobSpawnType.SPAWNER) {
            LivingEntity entity = event.getEntity();
            if (entity instanceof BaseCreatureEntity baseCreatureEntity && event.getLevel() instanceof Level) {
                if (!baseCreatureEntity.checkSpawnGroupLimit((Level) event.getLevel(), event.getSpawner().getSpawnerEntity().blockPosition(), 16)) {
                    event.setResult(Event.Result.DENY);
                }
            }
        }
    }


    // ==================================================
    //               Mounting / Dismounting
    // ==================================================
    @SubscribeEvent
    public void onEntityMount(EntityMountEvent event) {
        if (!ConfigExtra.INSTANCE.disableSneakDismount.get() || true) { // Disabled for now as cancelling this event doesn't work correctly for players atm.
            return;
        }
        if (!(event.getEntityMounting() instanceof Player)) {
            return;
        }

        // Override Sneak to Dismount for Lycanites Mobs:
        if (event.isDismounting() && event.getEntityBeingMounted() instanceof RideableCreatureEntity) {
            ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer((Player) event.getEntityMounting());
            if (extendedPlayer == null) {
                return;
            }
            event.setCanceled(event.getEntityMounting().isShiftKeyDown() && !extendedPlayer.isControlActive(ExtendedPlayer.CONTROL_ID.MOUNT_DISMOUNT));
        }
    }


    // ==================================================
    //                 Projectile Impact
    // ==================================================
    @SubscribeEvent
    public void onProjectileImpact(ProjectileImpactEvent event) {
        Entity shooter = null;
        if (!(event.getRayTraceResult() instanceof EntityHitResult entityRayTraceResult)) {
            return;
        }
        Entity target = entityRayTraceResult.getEntity();
        if (!(target instanceof BaseCreatureEntity)) {
            return;
        }
        BaseCreatureEntity targetCreature = (BaseCreatureEntity) target;

        if (event.getEntity() instanceof Projectile projectileEntity) {
            shooter = projectileEntity.getOwner();
        }
        if (event.getEntity() instanceof ThrowableItemProjectile projectileItemEntity) {
            shooter = projectileItemEntity.getOwner();
        }

        if (shooter != null && !targetCreature.isVulnerableTo(shooter)) {
            event.setImpactResult(ProjectileImpactEvent.ImpactResult.STOP_AT_CURRENT_NO_DAMAGE);
        }
    }
}
