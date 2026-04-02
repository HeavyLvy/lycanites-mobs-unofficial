package com.lycanitesmobs.core.effect;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.network.message.MessageEntityVelocity;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Handles fear motion logic for a single entity that has the fear effect.
 * Stored on ExtendedEntity — not an entity itself.
 * <p>
 * Pushes the feared entity away from the fear source (or in a random wander
 * direction if no source is known). The entity is tethered to the ground and
 * gets a step-height boost so it can traverse small obstacles despite limited
 * control.
 */
public class FearHandler {
    private static final int WANDER_CHANGE_TICKS = 40;
    private static final double STEP_HEIGHT_BOOST = 0.5;
    private static final UUID STEP_HEIGHT_UUID = UUID.fromString("a5f6a820-97d3-4be8-a05e-7c3e6e3d8f1a");

    @Nullable
    private LivingEntity fearSource;
    private double wanderAngle;
    private int wanderTimer;
    private boolean stepHeightApplied;
    public double pushStrength = 0.09;

    public FearHandler(@Nullable LivingEntity fearSource) {
        this.fearSource = fearSource;
        this.wanderAngle = Math.random() * Math.PI * 2;
        this.wanderTimer = 0;
        this.stepHeightApplied = false;
    }

    public void setFearSource(@Nullable LivingEntity source) {
        this.fearSource = source;
    }

    @Nullable
    public LivingEntity getFearSource() {
        return this.fearSource;
    }

    /**
     * Ticks the fear logic for the given entity.
     *
     * @return false if fear is no longer active and this handler should be removed.
     */
    public boolean tick(LivingEntity entity) {
        if (entity.getCommandSenderWorld().isClientSide) {
            return true;
        }

        MobEffect fear = ObjectManager.getEffect("fear");
        if (fear == null || !entity.hasEffect(fear)) {
            removeStepHeightBoost(entity);
            return false;
        }

        if (this.fearSource != null && !this.fearSource.isAlive()) {
            this.fearSource = null;
        }

        applyStepHeightBoost(entity);

        double pushX, pushZ;
        if (this.fearSource != null) {
            double dx = entity.position().x() - this.fearSource.position().x();
            double dz = entity.position().z() - this.fearSource.position().z();
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist < 0.01) {
                pushX = Math.cos(this.wanderAngle) * pushStrength;
                pushZ = Math.sin(this.wanderAngle) * pushStrength;
            } else {
                pushX = (dx / dist) * pushStrength;
                pushZ = (dz / dist) * pushStrength;
            }
        } else {
            this.wanderTimer++;
            if (this.wanderTimer >= WANDER_CHANGE_TICKS) {
                this.wanderTimer = 0;
                this.wanderAngle += (entity.getRandom().nextDouble() - 0.5) * Math.PI;
            }
            pushX = Math.cos(this.wanderAngle) * pushStrength;
            pushZ = Math.sin(this.wanderAngle) * pushStrength;
        }

        Vec3 current = entity.getDeltaMovement();
        double clampedY = Math.min(current.y, 0.0);
        entity.setDeltaMovement(current.x + pushX, clampedY, current.z + pushZ);

        if (entity instanceof ServerPlayer player) {
            try {
                player.connection.send(new ClientboundSetEntityMotionPacket(entity));
                MessageEntityVelocity msg = new MessageEntityVelocity(player, pushX, 0, pushZ);
                LycanitesMobs.PACKET_MANAGER.sendToPlayer(msg, player);
            } catch (Exception ignored) {
            }
        } else {
            entity.hurtMarked = true;
        }

        return true;
    }

    private void applyStepHeightBoost(LivingEntity entity) {
        if (this.stepHeightApplied) {
            return;
        }
        AttributeInstance attr = entity.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());
        if (attr != null && attr.getModifier(STEP_HEIGHT_UUID) == null) {
            attr.addTransientModifier(new AttributeModifier(
                    STEP_HEIGHT_UUID, "Fear step height boost", STEP_HEIGHT_BOOST, AttributeModifier.Operation.ADDITION
            ));
        }
        this.stepHeightApplied = true;
    }

    private void removeStepHeightBoost(LivingEntity entity) {
        if (!this.stepHeightApplied) {
            return;
        }
        AttributeInstance attr = entity.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());
        if (attr != null) {
            attr.removeModifier(STEP_HEIGHT_UUID);
        }
        this.stepHeightApplied = false;
    }
}
