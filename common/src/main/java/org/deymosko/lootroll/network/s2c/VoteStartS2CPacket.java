package org.deymosko.lootroll.network.s2c;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.deymosko.lootroll.ClientVoteCache;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;



public class VoteStartS2CPacket {
    private final UUID voteId;
    private final List<ItemStack> items;
    private final long endTime;

    public VoteStartS2CPacket(UUID voteId, List<ItemStack> items, long endTime) {
        this.voteId = voteId;
        this.items = items;
        this.endTime = endTime;
    }

    public static void encode(VoteStartS2CPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.voteId);
        buf.writeInt(msg.items.size());
        for (ItemStack stack : msg.items) {
            buf.writeItem(stack);
        }
        buf.writeLong(msg.endTime);
    }

    public static VoteStartS2CPacket decode(FriendlyByteBuf buf) {
        UUID voteId = buf.readUUID();
        int size = buf.readInt();
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            items.add(buf.readItem());
        }
        long endTime = buf.readLong();
        return new VoteStartS2CPacket(voteId, items, endTime);
    }

    public static void handle(FriendlyByteBuf buf, NetworkManager.PacketContext ctx) {
        VoteStartS2CPacket msg = decode(buf);
        ctx.queue(() -> ClientVoteCache.add(msg.voteId, msg.items, msg.endTime));
    }
}
