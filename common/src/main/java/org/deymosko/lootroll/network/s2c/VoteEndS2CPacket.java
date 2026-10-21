package org.deymosko.lootroll.network.s2c;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import org.deymosko.lootroll.ClientVoteCache;
import org.deymosko.lootroll.network.Packets;

import java.util.UUID;

/** Tells the client a vote is over (time ran out or everyone voted) so it can drop it from the UI. */
public class VoteEndS2CPacket {
    private final UUID voteId;

    public VoteEndS2CPacket(UUID voteId) {
        this.voteId = voteId;
    }

    public static void encode(VoteEndS2CPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(Packets.PROTOCOL_VERSION);
        buf.writeUUID(msg.voteId);
    }

    public static void handle(FriendlyByteBuf buf, NetworkManager.PacketContext ctx) {
        if (!Packets.checkProtocol(buf, Packets.VOTE_END)) return;
        UUID voteId = buf.readUUID();
        ctx.queue(() -> ClientVoteCache.remove(voteId));
    }
}
