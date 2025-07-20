package org.deymosko.lootroll.network.c2s;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.deymosko.lootroll.events.VoteManager;
import org.deymosko.lootroll.enums.VoteType;

import java.util.UUID;
import java.util.function.Supplier;

public class VoteC2SPacket {
    private final UUID voteId;
    private final VoteType type;

    public VoteC2SPacket(UUID voteId, VoteType type) {
        this.voteId = voteId;
        this.type = type;
    }

    public static void encode(VoteC2SPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.voteId);
        buf.writeEnum(msg.type);
    }

    public static VoteC2SPacket decode(FriendlyByteBuf buf) {
        return new VoteC2SPacket(buf.readUUID(), buf.readEnum(VoteType.class));
    }

    public static void handle(VoteC2SPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                VoteManager.vote(msg.voteId, player, msg.type);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
