package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.LycanitesMobs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public class MessageEntityPickedUp implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessageEntityPickedUp> TYPE =
            new CustomPacketPayload.Type<>(com.lycanitesmobs.core.util.helpers.AssetHelper.modResource("message_entity_picked_up"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageEntityPickedUp> STREAM_CODEC =
            StreamCodec.of((buf, message) -> encode(message, buf), MessageEntityPickedUp::decode);

    @Override
    public CustomPacketPayload.Type<MessageEntityPickedUp> type() {
        return TYPE;
    }

    public int pickedUpEntityID;
    public int pickedUpByEntityID;

    public MessageEntityPickedUp() {
    }

    public MessageEntityPickedUp(Entity pickedUpEntity, Entity pickedUpByEntity) {
        this.pickedUpEntityID = pickedUpEntity.getId();
        this.pickedUpByEntityID = pickedUpByEntity != null ? pickedUpByEntity.getId() : 0;
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageEntityPickedUp message, IPayloadContext ctx) {
        if (ctx.flow() != PacketFlow.CLIENTBOUND)
            return;

        ctx.enqueueWork(() ->
                LycanitesMobs.PROXY.applyEntityPickedUp(message.pickedUpEntityID, message.pickedUpByEntityID));
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageEntityPickedUp decode(FriendlyByteBuf packet) {
        MessageEntityPickedUp message = new MessageEntityPickedUp();
        message.pickedUpEntityID = packet.readInt();
        message.pickedUpByEntityID = packet.readInt();
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageEntityPickedUp message, FriendlyByteBuf packet) {
        packet.writeInt(message.pickedUpEntityID);
        packet.writeInt(message.pickedUpByEntityID);
    }

}
