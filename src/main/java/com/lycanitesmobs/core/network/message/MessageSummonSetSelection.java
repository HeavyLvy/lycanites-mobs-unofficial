package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public class MessageSummonSetSelection implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessageSummonSetSelection> TYPE =
            new CustomPacketPayload.Type<>(com.lycanitesmobs.core.util.helpers.AssetHelper.modResource("message_summon_set_selection"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageSummonSetSelection> STREAM_CODEC =
            StreamCodec.of((buf, message) -> encode(message, buf), MessageSummonSetSelection::decode);

    @Override
    public CustomPacketPayload.Type<MessageSummonSetSelection> type() {
        return TYPE;
    }

    public byte summonSetID;

    public MessageSummonSetSelection() {
    }

    public MessageSummonSetSelection(ExtendedPlayer playerExt) {
        this.summonSetID = (byte) playerExt.getSelectedSummonSetId();
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageSummonSetSelection message, IPayloadContext ctx) {
        // Server Side:
        if (ctx.flow() == PacketFlow.SERVERBOUND) {
            ctx.enqueueWork(() -> {
                Player player = ctx.player();
                ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
                playerExt.setSelectedSummonSet(message.summonSetID);
            });
            return;
        }

        // Client Side:
        ctx.enqueueWork(() -> LycanitesMobs.PROXY.applySelectedSummonSet(message.summonSetID));
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageSummonSetSelection decode(FriendlyByteBuf packet) {
        MessageSummonSetSelection message = new MessageSummonSetSelection();
        message.summonSetID = packet.readByte();
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageSummonSetSelection message, FriendlyByteBuf packet) {
        packet.writeByte(message.summonSetID);
    }

}
