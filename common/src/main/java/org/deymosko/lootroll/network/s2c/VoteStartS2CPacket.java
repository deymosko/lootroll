package org.deymosko.lootroll.network.s2c;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.deymosko.lootroll.ClientVoteCache;
import org.deymosko.lootroll.network.Packets;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;



public class VoteStartS2CPacket {
    private final UUID voteId;
    private final List<ItemStack> items;
    private final int durationTicks;
    private final int remainingTicks;
    private final boolean allowGreed;

    // Durations are sent instead of a timestamp, so client and server clocks don't have to agree.
    public VoteStartS2CPacket(UUID voteId, List<ItemStack> items, int durationTicks, int remainingTicks, boolean allowGreed) {
        this.voteId = voteId;
        this.items = items;
        this.durationTicks = durationTicks;
        this.remainingTicks = remainingTicks;
        this.allowGreed = allowGreed;
    }

    public static void encode(VoteStartS2CPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(Packets.PROTOCOL_VERSION);
        buf.writeUUID(msg.voteId);
        buf.writeVarInt(msg.items.size());
        for (ItemStack stack : msg.items) {
            buf.writeItem(stack);
        }
        buf.writeVarInt(msg.durationTicks);
        buf.writeVarInt(msg.remainingTicks);
        buf.writeBoolean(msg.allowGreed);
    }

    public static VoteStartS2CPacket decode(FriendlyByteBuf buf) {
        UUID voteId = buf.readUUID();
        int size = buf.readVarInt();
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            items.add(buf.readItem());
        }
        int durationTicks = buf.readVarInt();
        int remainingTicks = buf.readVarInt();
        boolean allowGreed = buf.readBoolean();
        return new VoteStartS2CPacket(voteId, items, durationTicks, remainingTicks, allowGreed);
    }

    public static void handle(FriendlyByteBuf buf, NetworkManager.PacketContext ctx) {
        if (!Packets.checkProtocol(buf, Packets.VOTE_START)) return;
        VoteStartS2CPacket msg = decode(buf);
        ctx.queue(() -> ClientVoteCache.add(msg.voteId, msg.items, msg.durationTicks, msg.remainingTicks, msg.allowGreed));
    }
}
