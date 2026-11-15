package com.lycanitesmobs.core.manager;

import com.lycanitesmobs.core.network.message.*;
import com.lycanitesmobs.core.network.packet.MessageScreenRequest;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class PacketManager {
    private static final String PROTOCOL_VERSION = "1";


    // ==================================================
    //                    Initialize
    // ==================================================

    /**
     * Registers all payloads. Subscribed to the mod event bus.
     * Handlers run on the network thread and hand work to the main thread with enqueueWork, as they did on Forge.
     */
    public void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION).executesOn(HandlerThread.NETWORK);
        registrar.playToClient(MessageBeastiary.TYPE, MessageBeastiary.STREAM_CODEC, MessageBeastiary::handle);
        registrar.playToClient(MessageCreature.TYPE, MessageCreature.STREAM_CODEC, MessageCreature::handle);
        registrar.playToClient(MessageCreatureKnowledge.TYPE, MessageCreatureKnowledge.STREAM_CODEC, MessageCreatureKnowledge::handle);
        registrar.playToClient(MessagePlayerStats.TYPE, MessagePlayerStats.STREAM_CODEC, MessagePlayerStats::handle);
        registrar.playBidirectional(MessagePetEntry.TYPE, MessagePetEntry.STREAM_CODEC, MessagePetEntry::handle);
        registrar.playBidirectional(MessagePetEntryRemove.TYPE, MessagePetEntryRemove.STREAM_CODEC, MessagePetEntryRemove::handle);
        registrar.playBidirectional(MessageSummonSet.TYPE, MessageSummonSet.STREAM_CODEC, MessageSummonSet::handle);
        registrar.playBidirectional(MessageSummonSetSelection.TYPE, MessageSummonSetSelection.STREAM_CODEC, MessageSummonSetSelection::handle);
        registrar.playToClient(MessageEntityPickedUp.TYPE, MessageEntityPickedUp.STREAM_CODEC, MessageEntityPickedUp::handle);
        registrar.playToClient(MessageEntityPerched.TYPE, MessageEntityPerched.STREAM_CODEC, MessageEntityPerched::handle);
        registrar.playToClient(MessageWorldEvent.TYPE, MessageWorldEvent.STREAM_CODEC, MessageWorldEvent::handle);
        registrar.playToClient(MessageMobEvent.TYPE, MessageMobEvent.STREAM_CODEC, MessageMobEvent::handle);
        registrar.playToClient(MessageSummoningPedestalStats.TYPE, MessageSummoningPedestalStats.STREAM_CODEC, MessageSummoningPedestalStats::handle);
        registrar.playToClient(MessageEntityVelocity.TYPE, MessageEntityVelocity.STREAM_CODEC, MessageEntityVelocity::handle);
        registrar.playToServer(MessageEntityGUICommand.TYPE, MessageEntityGUICommand.STREAM_CODEC, MessageEntityGUICommand::handle);
        registrar.playToClient(MessageScreenRequest.TYPE, MessageScreenRequest.STREAM_CODEC, MessageScreenRequest::handle);
        registrar.playToServer(MessagePlayerControl.TYPE, MessagePlayerControl.STREAM_CODEC, MessagePlayerControl::handle);
        registrar.playToServer(MessagePlayerLeftClick.TYPE, MessagePlayerLeftClick.STREAM_CODEC, MessagePlayerLeftClick::handle);
        registrar.playToServer(MessageSummoningPedestalSummonSet.TYPE, MessageSummoningPedestalSummonSet.STREAM_CODEC, MessageSummoningPedestalSummonSet::handle);
        registrar.playToServer(MessageTileEntityButton.TYPE, MessageTileEntityButton.STREAM_CODEC, MessageTileEntityButton::handle);
        registrar.playToClient(MessageSpawnEntity.TYPE, MessageSpawnEntity.STREAM_CODEC, MessageSpawnEntity::handle);
        registrar.playToClient(MessageOverlayMessage.TYPE, MessageOverlayMessage.STREAM_CODEC, MessageOverlayMessage::handle);
    }


    // ==================================================
    //                   Send To Player
    // ==================================================

    /**
     * Sends a packet from the server to the specified player.
     *
     * @param message The network packet message to send.
     * @param player  The player to send to.
     */
    public void sendToPlayer(CustomPacketPayload message, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, message);
    }


    // ==================================================
    //                 Send To All Around
    // ==================================================

    /**
     * Sends a packet from the server to all players near the specified block position within range.
     *
     * @param message The network packet message to send.
     * @param pos     The position to find players from.
     * @param range   The range to find players within.
     */
    public void sendToAllAround(CustomPacketPayload message, Level world, Vec3 pos, double range) {
        for (Player player : world.players()) {
            if (player instanceof ServerPlayer serverPlayer && player.distanceToSqr(pos) <= (range * range)) {
                this.sendToPlayer(message, serverPlayer);
            }
        }
    }


    // ==================================================
    //                 Send To Dimension
    // ==================================================

    /**
     * Sends a packet to all players within the specified world.
     *
     * @param message The network packet message to send.
     * @param world   The world to find players to send packets to.
     */
    public void sendToWorld(CustomPacketPayload message, Level world) {
        if (world instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersInDimension(serverLevel, message);
        }
    }


    // ==================================================
    //                   Send To Server
    // ==================================================

    /**
     * Sends a packet from the client player to the server.
     *
     * @param message The network packet message to send.
     */
    public void sendToServer(CustomPacketPayload message) {
        PacketDistributor.sendToServer(message);
    }
}
