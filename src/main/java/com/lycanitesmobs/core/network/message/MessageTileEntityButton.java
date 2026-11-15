package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.core.block.blockentity.TileEntityBase;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public class MessageTileEntityButton implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessageTileEntityButton> TYPE =
            new CustomPacketPayload.Type<>(com.lycanitesmobs.core.util.helpers.AssetHelper.modResource("message_tile_entity_button"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageTileEntityButton> STREAM_CODEC =
            StreamCodec.of((buf, message) -> encode(message, buf), MessageTileEntityButton::decode);

    @Override
    public CustomPacketPayload.Type<MessageTileEntityButton> type() {
        return TYPE;
    }

    public int buttonId;
    public BlockPos tileEntityPos;

    public MessageTileEntityButton() {
    }

    public MessageTileEntityButton(int buttonId, BlockPos tileEntityPos) {
        this.buttonId = buttonId;
        this.tileEntityPos = tileEntityPos;
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageTileEntityButton message, IPayloadContext ctx) {
        if (ctx.flow() != PacketFlow.SERVERBOUND)
            return;

        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            BlockEntity tileEntity = player.getCommandSenderWorld().getBlockEntity(message.tileEntityPos);
            if (!(tileEntity instanceof TileEntityBase)) {
                return;
            }
            TileEntityBase tileEntityBase = (TileEntityBase) tileEntity;
            tileEntityBase.onGuiButton(message.buttonId);
        });
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageTileEntityButton decode(FriendlyByteBuf packet) {
        MessageTileEntityButton message = new MessageTileEntityButton();
        message.buttonId = packet.readInt();
        message.tileEntityPos = new BlockPos(packet.readInt(), packet.readInt(), packet.readInt());
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageTileEntityButton message, FriendlyByteBuf packet) {
        packet.writeInt(message.buttonId);
        packet.writeInt(message.tileEntityPos.getX());
        packet.writeInt(message.tileEntityPos.getY());
        packet.writeInt(message.tileEntityPos.getZ());
    }

}
