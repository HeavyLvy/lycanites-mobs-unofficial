package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.LycanitesMobs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public class MessageWorldEvent implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessageWorldEvent> TYPE =
            new CustomPacketPayload.Type<>(com.lycanitesmobs.core.util.helpers.AssetHelper.modResource("message_world_event"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageWorldEvent> STREAM_CODEC =
            StreamCodec.of((buf, message) -> encode(message, buf), MessageWorldEvent::decode);

    @Override
    public CustomPacketPayload.Type<MessageWorldEvent> type() {
        return TYPE;
    }

    public String mobEventName;
    public BlockPos pos;
    public int level = 1;
    public int subspecies = 1;

    public MessageWorldEvent() {
    }

    public MessageWorldEvent(String mobEventName, BlockPos pos, int level, int subspecies) {
        this.mobEventName = mobEventName;
        this.pos = pos;
        this.level = level;
        this.subspecies = subspecies;
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageWorldEvent message, IPayloadContext ctx) {
        if (ctx.flow() != PacketFlow.CLIENTBOUND)
            return;

        ctx.enqueueWork(() ->
                LycanitesMobs.PROXY.applyWorldEvent(
                        message.mobEventName,
                        message.pos,
                        message.level,
                        message.subspecies));
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageWorldEvent decode(FriendlyByteBuf packet) {
        MessageWorldEvent message = new MessageWorldEvent();
        message.mobEventName = packet.readUtf(256);
        message.pos = packet.readBlockPos();
        message.level = packet.readInt();
        message.subspecies = packet.readInt();
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageWorldEvent message, FriendlyByteBuf packet) {
        packet.writeUtf(message.mobEventName);
        packet.writeBlockPos(message.pos);
        packet.writeInt(message.level);
        packet.writeInt(message.subspecies);
    }

}
