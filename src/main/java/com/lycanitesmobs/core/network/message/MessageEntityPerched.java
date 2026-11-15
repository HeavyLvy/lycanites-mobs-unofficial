package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.LycanitesMobs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public class MessageEntityPerched implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessageEntityPerched> TYPE =
            new CustomPacketPayload.Type<>(com.lycanitesmobs.core.util.helpers.AssetHelper.modResource("message_entity_perched"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageEntityPerched> STREAM_CODEC =
            StreamCodec.of((buf, message) -> encode(message, buf), MessageEntityPerched::decode);

    @Override
    public CustomPacketPayload.Type<MessageEntityPerched> type() {
        return TYPE;
    }

    public int perchedOnEntityID;
    public int perchedByEntityID;

    public MessageEntityPerched() {
    }

    public MessageEntityPerched(Entity perchedOnEntityID, Entity perchedByEntity) {
        this.perchedOnEntityID = perchedOnEntityID.getId();
        this.perchedByEntityID = perchedByEntity != null ? perchedByEntity.getId() : 0;
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageEntityPerched message, IPayloadContext ctx) {
        if (ctx.flow() != PacketFlow.CLIENTBOUND)
            return;

        ctx.enqueueWork(() ->
                LycanitesMobs.PROXY.applyEntityPerched(message.perchedOnEntityID, message.perchedByEntityID));
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageEntityPerched decode(FriendlyByteBuf packet) {
        MessageEntityPerched message = new MessageEntityPerched();
        message.perchedOnEntityID = packet.readInt();
        message.perchedByEntityID = packet.readInt();
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageEntityPerched message, FriendlyByteBuf packet) {
        packet.writeInt(message.perchedOnEntityID);
        packet.writeInt(message.perchedByEntityID);
    }

}
