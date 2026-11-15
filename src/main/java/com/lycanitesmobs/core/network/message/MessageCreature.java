package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public class MessageCreature implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessageCreature> TYPE =
            new CustomPacketPayload.Type<>(com.lycanitesmobs.core.util.helpers.AssetHelper.modResource("message_creature"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageCreature> STREAM_CODEC =
            StreamCodec.of((buf, message) -> encode(message, buf), MessageCreature::decode);

    @Override
    public CustomPacketPayload.Type<MessageCreature> type() {
        return TYPE;
    }

    int entityID;
    int playerReputation = 0;

    public MessageCreature() {
    }

    public MessageCreature(BaseCreatureEntity creatureEntity, int playerReputation) {
        this.entityID = creatureEntity.getId();
        this.playerReputation = playerReputation;
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageCreature message, IPayloadContext ctx) {
        if (ctx.flow() != PacketFlow.CLIENTBOUND)
            return;

        ctx.enqueueWork(() ->
                LycanitesMobs.PROXY.applyCreatureReputation(message.entityID, message.playerReputation));
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageCreature decode(FriendlyByteBuf packet) {
        MessageCreature message = new MessageCreature();
        try {
            message.entityID = packet.readInt();
            message.playerReputation = packet.readInt();
        } catch (Exception e) {
            LMHelperClass.logWarningMessage("There was a problem decoding the packet: " + packet + ".");
            e.printStackTrace();
        }
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageCreature message, FriendlyByteBuf packet) {
        packet.writeInt(message.entityID);
        packet.writeInt(message.playerReputation);
    }

}
