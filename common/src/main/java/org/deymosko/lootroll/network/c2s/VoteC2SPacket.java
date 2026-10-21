package org.deymosko.lootroll.network.c2s;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.deymosko.lootroll.events.VoteManager;
import org.deymosko.lootroll.enums.VoteType;
import org.deymosko.lootroll.network.Packets;

import java.util.UUID;

public class VoteC2SPacket {
    private final UUID voteId;
    private final VoteType type;

    public VoteC2SPacket(UUID voteId, VoteType type) {
        this.voteId = voteId;
        this.type = type;
    }

    public static void encode(VoteC2SPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(Packets.PROTOCOL_VERSION);
        buf.writeUUID(msg.voteId);
        buf.writeEnum(msg.type);
    }

    public static VoteC2SPacket decode(FriendlyByteBuf buf) {
        return new VoteC2SPacket(buf.readUUID(), buf.readEnum(VoteType.class));
    }

    public static void handle(FriendlyByteBuf buf, NetworkManager.PacketContext ctx) {
        if (!Packets.checkProtocol(buf, Packets.VOTE)) return;
        VoteC2SPacket msg = decode(buf);
        ctx.queue(() -> {
            if (ctx.getPlayer() instanceof ServerPlayer player) {
                VoteManager.vote(msg.voteId, player, msg.type);
            }
        });
    }
}
