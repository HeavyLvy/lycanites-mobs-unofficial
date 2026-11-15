package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.pets.PetEntry;
import com.lycanitesmobs.core.manager.PetManager;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class MessagePetEntryRemove implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessagePetEntryRemove> TYPE =
            new CustomPacketPayload.Type<>(com.lycanitesmobs.core.util.helpers.AssetHelper.modResource("message_pet_entry_remove"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessagePetEntryRemove> STREAM_CODEC =
            StreamCodec.of((buf, message) -> encode(message, buf), MessagePetEntryRemove::decode);

    @Override
    public CustomPacketPayload.Type<MessagePetEntryRemove> type() {
        return TYPE;
    }

    public UUID petEntryID;


    // ==================================================
    //                    Constructors
    // ==================================================
    public MessagePetEntryRemove() {
    }

    public MessagePetEntryRemove(ExtendedPlayer playerExt, PetEntry petEntry) {
        this.petEntryID = petEntry.getPetEntryID();
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessagePetEntryRemove message, IPayloadContext ctx) {
        // Server Side:
        if (ctx.flow() == PacketFlow.SERVERBOUND) {
            ctx.enqueueWork(() -> {
                Player player = ctx.player();
                ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);

                PetManager petManager = playerExt.getPetManager();
                PetEntry petEntry = petManager.getEntry(message.petEntryID);
                if (petEntry == null) {
                    LMHelperClass.logWarningMessage("Tried to remove a null PetEntry from server!");
                    return; // Nothing to remove!
                }
                petEntry.remove();
            });
            return;
        }

        // Client Side:
        ctx.enqueueWork(() -> LycanitesMobs.PROXY.removePetEntry(message.petEntryID));
    }

    /**
     * Reads the message from bytes.
     */
    public static MessagePetEntryRemove decode(FriendlyByteBuf packet) {
        MessagePetEntryRemove message = new MessagePetEntryRemove();
        try {
            message.petEntryID = packet.readUUID();
        } catch (Exception e) {
            LMHelperClass.logWarningMessage("There was a problem decoding the packet: " + packet + ".");
            e.printStackTrace();
        }
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessagePetEntryRemove message, FriendlyByteBuf packet) {
        try {
            packet.writeUUID(message.petEntryID);
        } catch (Exception e) {
            LMHelperClass.logWarningMessage("There was a problem encoding the packet: " + packet + ".");
            e.printStackTrace();
        }
    }

}
