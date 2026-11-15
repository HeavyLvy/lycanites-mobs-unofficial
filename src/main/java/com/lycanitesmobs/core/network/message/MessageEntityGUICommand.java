package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public class MessageEntityGUICommand implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessageEntityGUICommand> TYPE =
            new CustomPacketPayload.Type<>(com.lycanitesmobs.core.util.helpers.AssetHelper.modResource("message_entity_g_u_i_command"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageEntityGUICommand> STREAM_CODEC =
            StreamCodec.of((buf, message) -> encode(message, buf), MessageEntityGUICommand::decode);

    @Override
    public CustomPacketPayload.Type<MessageEntityGUICommand> type() {
        return TYPE;
    }

    int entityID;
    public int guiCommandID;

    public MessageEntityGUICommand() {
    }

    public MessageEntityGUICommand(int guiCommandID, Entity entity) {
        this.entityID = entity.getId();
        this.guiCommandID = guiCommandID;
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageEntityGUICommand message, IPayloadContext ctx) {
        if (ctx.flow() != PacketFlow.SERVERBOUND)
            return;

        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            Level world = player.getCommandSenderWorld();
            Entity entity = world.getEntity(message.entityID);
            if (entity instanceof TameableCreatureEntity) {
                TameableCreatureEntity pet = (TameableCreatureEntity) entity;
                pet.performGUICommand(player, message.guiCommandID);
            }
        });
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageEntityGUICommand decode(FriendlyByteBuf packet) {
        MessageEntityGUICommand message = new MessageEntityGUICommand();
        message.entityID = packet.readInt();
        message.guiCommandID = packet.readInt();
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageEntityGUICommand message, FriendlyByteBuf packet) {
        packet.writeInt(message.entityID);
        packet.writeInt(message.guiCommandID);
    }

}
