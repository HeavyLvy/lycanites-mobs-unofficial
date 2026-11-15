package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.pets.SummonSet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public class MessageSummonSet implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessageSummonSet> TYPE =
            new CustomPacketPayload.Type<>(com.lycanitesmobs.core.util.helpers.AssetHelper.modResource("message_summon_set"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageSummonSet> STREAM_CODEC =
            StreamCodec.of((buf, message) -> encode(message, buf), MessageSummonSet::decode);

    @Override
    public CustomPacketPayload.Type<MessageSummonSet> type() {
        return TYPE;
    }

    public byte summonSetID;
    public int subpsecies;
    public int variant;
    public String summonType;
    public byte behaviour;

    public MessageSummonSet() {
    }

    public MessageSummonSet(ExtendedPlayer playerExt, byte summonSetID) {
        this.summonSetID = summonSetID;
        this.summonType = playerExt.getSummonSet(summonSetID).getSummonType();
        this.subpsecies = playerExt.getSummonSet(summonSetID).getSubspecies();
        this.variant = playerExt.getSummonSet(summonSetID).getVariant();
        this.behaviour = playerExt.getSummonSet(summonSetID).getBehaviourByte();
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageSummonSet message, IPayloadContext ctx) {
        // Server Side:
        if (ctx.flow() == PacketFlow.SERVERBOUND) {
            ctx.enqueueWork(() -> {
                Player player = ctx.player();
                ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);

                SummonSet summonSet = playerExt.getSummonSet(message.summonSetID);
                summonSet.readFromPacket(message.summonType, message.subpsecies, message.variant, message.behaviour);
            });
            return;
        }

        // Client Side:
        ctx.enqueueWork(() -> LycanitesMobs.PROXY.applySummonSet(message));
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageSummonSet decode(FriendlyByteBuf packet) {
        MessageSummonSet message = new MessageSummonSet();
        message.summonSetID = packet.readByte();
        message.summonType = packet.readUtf(256);
        message.subpsecies = packet.readInt();
        message.variant = packet.readInt();
        message.behaviour = packet.readByte();
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageSummonSet message, FriendlyByteBuf packet) {
        packet.writeByte(message.summonSetID);
        packet.writeUtf(message.summonType);
        packet.writeInt(message.subpsecies);
        packet.writeInt(message.variant);
        packet.writeByte(message.behaviour);
    }

}
