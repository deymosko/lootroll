package org.deymosko.lootroll.network;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.deymosko.lootroll.Lootroll;
import org.deymosko.lootroll.network.c2s.VoteC2SPacket;
import org.deymosko.lootroll.network.s2c.VoteEndS2CPacket;
import org.deymosko.lootroll.network.s2c.VoteStartS2CPacket;

import java.util.function.Predicate;

public class Packets
{
    /** Bump whenever a packet layout changes; mismatched packets are ignored instead of crashing. */
    public static final int PROTOCOL_VERSION = 4;

    public static final ResourceLocation VOTE_START = new ResourceLocation(Lootroll.MODID, "vote_start");
    public static final ResourceLocation VOTE_END = new ResourceLocation(Lootroll.MODID, "vote_end");
    public static final ResourceLocation VOTE = new ResourceLocation(Lootroll.MODID, "vote");

    // Architectury's own check only becomes true after its channel-sync handshake, which isn't
    // reliable on Forge; loaders can replace it with a native check.
    private static Predicate<ServerPlayer> modPresenceCheck = player -> NetworkManager.canPlayerReceive(player, VOTE_START);

    public static void setModPresenceCheck(Predicate<ServerPlayer> check)
    {
        modPresenceCheck = check;
    }

    public static void register()
    {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, VOTE, VoteC2SPacket::handle);
    }

    /** Client-bound receivers; only registered on the physical client. */
    public static void registerClient()
    {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, VOTE_START, VoteStartS2CPacket::handle);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, VOTE_END, VoteEndS2CPacket::handle);
    }

    public static void sendToServer(VoteC2SPacket message)
    {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        VoteC2SPacket.encode(message, buf);
        NetworkManager.sendToServer(VOTE, buf);
    }

    /** Whether the player's client has LootRoll installed (and can show the vote GUI). */
    public static boolean hasMod(ServerPlayer player)
    {
        return modPresenceCheck.test(player);
    }

    public static void sendToClient(VoteStartS2CPacket message, ServerPlayer player)
    {
        // Players without the mod installed simply don't get the GUI.
        if (!hasMod(player)) return;

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        VoteStartS2CPacket.encode(message, buf);
        NetworkManager.sendToPlayer(player, VOTE_START, buf);
    }

    public static void sendToClient(VoteEndS2CPacket message, ServerPlayer player)
    {
        if (!hasMod(player)) return;

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        VoteEndS2CPacket.encode(message, buf);
        NetworkManager.sendToPlayer(player, VOTE_END, buf);
    }

    /** Reads and checks the protocol version written first in every packet. */
    public static boolean checkProtocol(FriendlyByteBuf buf, ResourceLocation id)
    {
        int version = buf.readVarInt();
        if (version != PROTOCOL_VERSION) {
            Lootroll.LOGGER.warn("Ignoring {} packet with protocol {} (expected {}); is LootRoll the same version on client and server?",
                    id, version, PROTOCOL_VERSION);
            return false;
        }
        return true;
    }
}
