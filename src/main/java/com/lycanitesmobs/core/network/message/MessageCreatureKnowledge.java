package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.data.info.creature.CreatureKnowledge;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;



public class MessageCreatureKnowledge implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessageCreatureKnowledge> TYPE =
            new CustomPacketPayload.Type<>(com.lycanitesmobs.core.util.helpers.AssetHelper.modResource("message_creature_knowledge"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageCreatureKnowledge> STREAM_CODEC =
            StreamCodec.of((buf, message) -> encode(message, buf), MessageCreatureKnowledge::decode);

    @Override
    public CustomPacketPayload.Type<MessageCreatureKnowledge> type() {
        return TYPE;
    }

    public String creatureName;
    public int rank;
    public int experience;

    public MessageCreatureKnowledge() {
    }

    public MessageCreatureKnowledge(CreatureKnowledge creatureKnowledge) {
        this.creatureName = creatureKnowledge.getCreatureName();
        this.rank = creatureKnowledge.getRank();
        this.experience = creatureKnowledge.getExperience();
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageCreatureKnowledge message, IPayloadContext ctx) {
        if (ctx.flow() != PacketFlow.CLIENTBOUND)
            return;

        ctx.enqueueWork(() ->
                LycanitesMobs.PROXY.applyCreatureKnowledge(
                        message.creatureName,
                        message.rank,
                        message.experience));
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageCreatureKnowledge decode(FriendlyByteBuf packet) {
        MessageCreatureKnowledge message = new MessageCreatureKnowledge();
        try {
            message.creatureName = packet.readUtf(256);
            message.rank = packet.readInt();
            message.experience = packet.readInt();
        } catch (Exception e) {
            LMHelperClass.logWarningMessage("There was a problem decoding the packet: " + packet + ".");
            e.printStackTrace();
        }
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageCreatureKnowledge message, FriendlyByteBuf packet) {
        packet.writeUtf(message.creatureName);
        packet.writeInt(message.rank);
        packet.writeInt(message.experience);
    }

}
