package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public class MessagePlayerStats implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessagePlayerStats> TYPE =
            new CustomPacketPayload.Type<>(com.lycanitesmobs.core.util.helpers.AssetHelper.modResource("message_player_stats"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessagePlayerStats> STREAM_CODEC =
            StreamCodec.of((buf, message) -> encode(message, buf), MessagePlayerStats::decode);

    @Override
    public CustomPacketPayload.Type<MessagePlayerStats> type() {
        return TYPE;
    }

    public int spirit;
    public int summonFocus;
    public int creatureStudyCooldown;

    public MessagePlayerStats() {
    }

    public MessagePlayerStats(ExtendedPlayer playerExt) {
        this.spirit = playerExt.getSpirit();
        this.summonFocus = playerExt.getSummonFocus();
        this.creatureStudyCooldown = playerExt.getCreatureStudyCooldown();
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessagePlayerStats message, IPayloadContext ctx) {
        if (ctx.flow() != PacketFlow.CLIENTBOUND)
            return;

        ctx.enqueueWork(() ->
                LycanitesMobs.PROXY.applyPlayerStats(
                        message.spirit,
                        message.summonFocus,
                        message.creatureStudyCooldown));
    }

    /**
     * Reads the message from bytes.
     */
    public static MessagePlayerStats decode(FriendlyByteBuf packet) {
        MessagePlayerStats message = new MessagePlayerStats();
        message.spirit = packet.readInt();
        message.summonFocus = packet.readInt();
        message.creatureStudyCooldown = packet.readInt();
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessagePlayerStats message, FriendlyByteBuf packet) {
        packet.writeInt(message.spirit);
        packet.writeInt(message.summonFocus);
        packet.writeInt(message.creatureStudyCooldown);
    }

}
