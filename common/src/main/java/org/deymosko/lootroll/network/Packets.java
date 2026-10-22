package org.deymosko.lootroll.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.server.level.ServerPlayer;
import org.deymosko.lootroll.network.c2s.VoteC2SPacket;
import org.deymosko.lootroll.network.s2c.VoteEndS2CPacket;
import org.deymosko.lootroll.network.s2c.VoteStartS2CPacket;

public class Packets
{
    public static void register()
    {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, VoteC2SPacket.TYPE, VoteC2SPacket.CODEC, VoteC2SPacket::handle);

        // On the client the S2C types are registered together with their receivers (registerClient);
        // a dedicated server only needs to know how to encode them.
        if (Platform.getEnvironment() == Env.SERVER) {
            NetworkManager.registerS2CPayloadType(VoteStartS2CPacket.TYPE, VoteStartS2CPacket.CODEC);
            NetworkManager.registerS2CPayloadType(VoteEndS2CPacket.TYPE, VoteEndS2CPacket.CODEC);
        }
    }

    /** Client-bound receivers; only registered on the physical client. */
    public static void registerClient()
    {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, VoteStartS2CPacket.TYPE, VoteStartS2CPacket.CODEC, VoteStartS2CPacket::handle);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, VoteEndS2CPacket.TYPE, VoteEndS2CPacket.CODEC, VoteEndS2CPacket::handle);
    }

    /** Whether the player's client has LootRoll installed (and can show the vote GUI). */
    public static boolean hasMod(ServerPlayer player)
    {
        return NetworkManager.canPlayerReceive(player, VoteStartS2CPacket.TYPE);
    }

    public static void sendToServer(VoteC2SPacket message)
    {
        NetworkManager.sendToServer(message);
    }

    public static void sendToClient(VoteStartS2CPacket message, ServerPlayer player)
    {
        // Players without the mod installed simply don't get the GUI.
        if (!hasMod(player)) return;
        NetworkManager.sendToPlayer(player, message);
    }

    public static void sendToClient(VoteEndS2CPacket message, ServerPlayer player)
    {
        if (!hasMod(player)) return;
        NetworkManager.sendToPlayer(player, message);
    }
}
