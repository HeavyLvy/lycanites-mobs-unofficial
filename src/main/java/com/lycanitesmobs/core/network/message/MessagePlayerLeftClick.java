package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.core.item.equipment.ItemEquipment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public class MessagePlayerLeftClick implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessagePlayerLeftClick> TYPE =
            new CustomPacketPayload.Type<>(com.lycanitesmobs.core.util.helpers.AssetHelper.modResource("message_player_left_click"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessagePlayerLeftClick> STREAM_CODEC =
            StreamCodec.of((buf, message) -> encode(message, buf), MessagePlayerLeftClick::decode);

    @Override
    public CustomPacketPayload.Type<MessagePlayerLeftClick> type() {
        return TYPE;
    }

    public MessagePlayerLeftClick() {
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessagePlayerLeftClick message, IPayloadContext ctx) {
        if (ctx.flow() != PacketFlow.SERVERBOUND)
            return;

        ctx.enqueueWork(() -> {
            Player player = ctx.player();

            // Equipment:
            InteractionHand activeHand = player.getUsedItemHand();
            ItemStack activeStack = player.getItemInHand(activeHand);
            if (activeStack.getItem() instanceof ItemEquipment) {
                ((ItemEquipment) activeStack.getItem()).onItemLeftClick(player.getCommandSenderWorld(), player, activeHand);
            }
        });
    }

    /**
     * Reads the message from bytes.
     */
    public static MessagePlayerLeftClick decode(FriendlyByteBuf packet) {
        MessagePlayerLeftClick message = new MessagePlayerLeftClick();
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessagePlayerLeftClick message, FriendlyByteBuf packet) {

    }

}
