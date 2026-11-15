package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.data.info.gui.Beastiary;
import com.lycanitesmobs.core.data.info.creature.CreatureKnowledge;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public class MessageBeastiary implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessageBeastiary> TYPE =
            new CustomPacketPayload.Type<>(com.lycanitesmobs.core.util.helpers.AssetHelper.modResource("message_beastiary"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageBeastiary> STREAM_CODEC =
            StreamCodec.of((buf, message) -> encode(message, buf), MessageBeastiary::decode);

    @Override
    public CustomPacketPayload.Type<MessageBeastiary> type() {
        return TYPE;
    }

    public int entryAmount = 0;
    public String[] creatureNames;
    public int[] ranks;
    public int[] experience;

    public MessageBeastiary() {
    }

    public MessageBeastiary(Beastiary beastiary) {
        this.entryAmount = Math.min(201, beastiary.getCreatureKnowledgeCount());
        if (this.entryAmount > 0) {
            this.creatureNames = new String[this.entryAmount];
            this.ranks = new int[this.entryAmount];
            this.experience = new int[this.entryAmount];
            int i = 0;
            for (CreatureKnowledge creatureKnowledge : beastiary.getCreatureKnowledgeValues()) {
                this.creatureNames[i] = creatureKnowledge.getCreatureName();
                this.ranks[i] = creatureKnowledge.getRank();
                this.experience[i] = creatureKnowledge.getExperience();
                i++;
            }
        }
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageBeastiary message, IPayloadContext ctx) {
        if (ctx.flow() != PacketFlow.CLIENTBOUND)
            return;

        ctx.enqueueWork(() ->
                LycanitesMobs.PROXY.applyBeastiaryEntries(
                        message.creatureNames,
                        message.ranks,
                        message.experience,
                        message.entryAmount));
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageBeastiary decode(FriendlyByteBuf packet) {
        MessageBeastiary message = new MessageBeastiary();
        message.entryAmount = Math.min(300, packet.readInt());
        if (message.entryAmount == 300) {
            LMHelperClass.logWarningMessage("Received 300 or more creature entries, something went wrong with the Beastiary packet! Addition entries will be skipped to prevent OOM!");
        }
        if (message.entryAmount > 0) {
            message.creatureNames = new String[message.entryAmount];
            message.ranks = new int[message.entryAmount];
            message.experience = new int[message.entryAmount];
            for (int i = 0; i < message.entryAmount; i++) {
                message.creatureNames[i] = packet.readUtf(32767);
                message.ranks[i] = packet.readInt();
                message.experience[i] = packet.readInt();
            }
        }
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageBeastiary message, FriendlyByteBuf packet) {
        packet.writeInt(message.entryAmount);
        if (message.entryAmount > 0) {
            for (int i = 0; i < message.entryAmount; i++) {
                packet.writeUtf(message.creatureNames[i]);
                packet.writeInt(message.ranks[i]);
                packet.writeInt(message.experience[i]);
            }
        }
    }
}
