package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.block.blockentity.TileEntitySummoningPedestal;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public class MessageSummoningPedestalStats implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessageSummoningPedestalStats> TYPE =
            new CustomPacketPayload.Type<>(com.lycanitesmobs.core.util.helpers.AssetHelper.modResource("message_summoning_pedestal_stats"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageSummoningPedestalStats> STREAM_CODEC =
            StreamCodec.of((buf, message) -> encode(message, buf), MessageSummoningPedestalStats::decode);

    @Override
    public CustomPacketPayload.Type<MessageSummoningPedestalStats> type() {
        return TYPE;
    }

    public int capacity;
    public int progress;
    public int fuel;
    public int fuelMax;
    public int x;
    public int y;
    public int z;

    public MessageSummoningPedestalStats() {
    }

    public MessageSummoningPedestalStats(int capacity, int progress, int fuel, int fuelMax, int x, int y, int z) {
        this.capacity = capacity;
        this.progress = progress;
        this.fuel = fuel;
        this.fuelMax = fuelMax;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public MessageSummoningPedestalStats(TileEntitySummoningPedestal.SummoningPedestalStats stats, BlockPos blockPos) {
        this(stats.capacity(), stats.summonProgress(), stats.summoningFuel(), stats.summoningFuelMax(),
                blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageSummoningPedestalStats message, IPayloadContext ctx) {
        if (ctx.flow() != PacketFlow.CLIENTBOUND)
            return;

        ctx.enqueueWork(() -> LycanitesMobs.PROXY.applySummoningPedestalStats(message));
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageSummoningPedestalStats decode(FriendlyByteBuf packet) {
        MessageSummoningPedestalStats message = new MessageSummoningPedestalStats();
        message.x = packet.readInt();
        message.y = packet.readInt();
        message.z = packet.readInt();
        message.capacity = packet.readInt();
        message.progress = packet.readInt();
        message.fuel = packet.readInt();
        message.fuelMax = packet.readInt();
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageSummoningPedestalStats message, FriendlyByteBuf packet) {
        packet.writeInt(message.x);
        packet.writeInt(message.y);
        packet.writeInt(message.z);
        packet.writeInt(message.capacity);
        packet.writeInt(message.progress);
        packet.writeInt(message.fuel);
        packet.writeInt(message.fuelMax);
    }

}
