package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.base.RideableCreatureEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public class MessagePlayerControl implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessagePlayerControl> TYPE =
            new CustomPacketPayload.Type<>(com.lycanitesmobs.core.util.helpers.AssetHelper.modResource("message_player_control"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessagePlayerControl> STREAM_CODEC =
            StreamCodec.of((buf, message) -> encode(message, buf), MessagePlayerControl::decode);

    @Override
    public CustomPacketPayload.Type<MessagePlayerControl> type() {
        return TYPE;
    }

    public byte controlStates;

    public MessagePlayerControl() {
    }

    public MessagePlayerControl(byte controlStates) {
        this.controlStates = controlStates;
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessagePlayerControl message, IPayloadContext ctx) {
        if (ctx.flow() != PacketFlow.SERVERBOUND)
            return;

        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            if (player == null) {
                return;
            }
            ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
            playerExt.updateControlStates(message.controlStates);
            Entity vehicle = player.getVehicle();
            if (vehicle instanceof RideableCreatureEntity rideableCreature
                    && rideableCreature.getControllingPassenger() == player
                    && rideableCreature.riderControl()) {
                rideableCreature.handleRiderControls(player, playerExt);
            }
        });
    }

    /**
     * Reads the message from bytes.
     */
    public static MessagePlayerControl decode(FriendlyByteBuf packet) {
        MessagePlayerControl message = new MessagePlayerControl();
        message.controlStates = packet.readByte();
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessagePlayerControl message, FriendlyByteBuf packet) {
        packet.writeByte(message.controlStates);
    }

}
