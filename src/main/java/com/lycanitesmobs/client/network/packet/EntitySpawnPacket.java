package com.lycanitesmobs.client.network.packet;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.projectile.generic.CustomProjectileEntity;
import com.lycanitesmobs.core.entity.util.EntityFactory;
import com.lycanitesmobs.core.data.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.manager.ProjectileManager;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.util.UUID;

public class EntitySpawnPacket implements Packet<ClientPacketListener> {
    public String entityTypeName = "";
    public int entityId = 0;
    public UUID uuid;
    public float pitch;
    public float yaw;
    public Vec3 pos;
    public double y;
    public double z;

    public EntitySpawnPacket(Entity serverEntity) {
        if (serverEntity != null) {
            if (serverEntity instanceof BaseProjectileEntity) {
                this.entityTypeName = ((BaseProjectileEntity) serverEntity).getEntityName();
            }
            this.entityId = serverEntity.getId();
            this.uuid = serverEntity.getUUID();
            this.pitch = serverEntity.xRotO;
            this.yaw = serverEntity.yRotO;
            this.pos = serverEntity.position();
        }
    }


    public void read(FriendlyByteBuf packet) throws IOException {
        this.entityTypeName = packet.readUtf();
        this.entityId = packet.readInt();
        this.uuid = packet.readUUID();
        this.pitch = packet.readFloat();
        this.yaw = packet.readFloat();
        this.pos = new Vec3(packet.readDouble(), packet.readDouble(), packet.readDouble());
    }

    @Override
    public void write(FriendlyByteBuf packet) {
        packet.writeUtf(this.entityTypeName);
        packet.writeInt(this.entityId);
        packet.writeUUID(this.uuid);
        packet.writeFloat(this.pitch);
        packet.writeFloat(this.yaw);
        packet.writeDouble(this.pos.x());
        packet.writeDouble(this.pos.y());
        packet.writeDouble(this.pos.z());
    }

    @Override
    public void handle(ClientPacketListener handler) {
        EntityFactory entityFactory = EntityFactory.getInstance();
        if (!entityFactory.hasEntityTypeNetworkName(this.entityTypeName)) {
            LMHelperClass.logWarningMessage("Unable to find entity type from packet: " + this.entityTypeName);
            return;
        }
        EntityType entityType = entityFactory.getEntityTypeByNetworkName(this.entityTypeName);
        Entity entity = entityFactory.create(entityType, LycanitesMobs.PROXY.getWorld());
        if (entity == null) {
            LMHelperClass.logWarningMessage("Unable to create client entity from packet: " + this.entityTypeName);
            return;
        }
        entity.setPos(this.pos.x(), this.pos.y(), this.pos.z());
        entity.xRotO = this.pitch;
        entity.yRotO = this.yaw;
        entity.setId(this.entityId);
        entity.setUUID(this.uuid);

        // Projectiles:
        if (entity instanceof BaseProjectileEntity) {
            ((BaseProjectileEntity) entity).setEntityName(this.entityTypeName);
            if (entity instanceof CustomProjectileEntity) {
                ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile(this.entityTypeName);
                if (projectileInfo != null) {
                    ((CustomProjectileEntity) entity).setProjectileInfo(projectileInfo);
                }
            }
        }

        handler.getLevel().putNonPlayerEntity(this.entityId, entity);
    }
}
