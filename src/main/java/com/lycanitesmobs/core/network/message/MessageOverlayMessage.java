package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.LycanitesMobs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public class MessageOverlayMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessageOverlayMessage> TYPE =
            new CustomPacketPayload.Type<>(com.lycanitesmobs.core.util.helpers.AssetHelper.modResource("message_overlay_message"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageOverlayMessage> STREAM_CODEC =
            StreamCodec.of((buf, message) -> encode(message, buf), MessageOverlayMessage::decode);

    @Override
    public CustomPacketPayload.Type<MessageOverlayMessage> type() {
        return TYPE;
    }

    public Component message;

    public MessageOverlayMessage() {
    }

    public MessageOverlayMessage(MutableComponent message) {
        this.message = message;
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageOverlayMessage message, IPayloadContext ctx) {
        if (ctx.flow() != PacketFlow.CLIENTBOUND)
            return;

        ctx.enqueueWork(() -> LycanitesMobs.PROXY.showOverlayMessage(message.message));
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageOverlayMessage decode(RegistryFriendlyByteBuf packet) {
        MessageOverlayMessage message = new MessageOverlayMessage();
        message.message = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(packet);
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageOverlayMessage message, RegistryFriendlyByteBuf packet) {
        ComponentSerialization.TRUSTED_STREAM_CODEC.encode(packet, message.message);
    }

}
