package com.lycanitesmobs.core.network.packet;

import com.lycanitesmobs.LycanitesMobs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public class MessageScreenRequest implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessageScreenRequest> TYPE =
            new CustomPacketPayload.Type<>(com.lycanitesmobs.core.util.helpers.AssetHelper.modResource("message_screen_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageScreenRequest> STREAM_CODEC =
            StreamCodec.of((buf, message) -> encode(message, buf), MessageScreenRequest::decode);

    @Override
    public CustomPacketPayload.Type<MessageScreenRequest> type() {
        return TYPE;
    }

	public enum GuiRequest {
		BEASTIARY((byte)0), SUMMONING((byte)1);
		public byte id;
		GuiRequest(byte i) { id = i; }
	}

	public byte screenId;
	
	public MessageScreenRequest() {}
	public MessageScreenRequest(GuiRequest guiRequest) {
		this.screenId = guiRequest.id;
	}
	
	/**
	 * Called when this message is received.
	 */
	public static void handle(MessageScreenRequest message, IPayloadContext ctx) {
		if(ctx.flow() != PacketFlow.CLIENTBOUND)
			return;

		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			LycanitesMobs.PROXY.openScreen(message.screenId, player);
		});
	}
	
	/**
	 * Reads the message from bytes.
	 */
	public static MessageScreenRequest decode(FriendlyByteBuf packet) {
		MessageScreenRequest message = new MessageScreenRequest();
		message.screenId = packet.readByte();
		return message;
	}
	
	/**
	 * Writes the message into bytes.
	 */
	public static void encode(MessageScreenRequest message, FriendlyByteBuf packet) {
		packet.writeByte(message.screenId);
	}
	
}
