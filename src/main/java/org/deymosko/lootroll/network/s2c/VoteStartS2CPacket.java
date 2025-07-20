package org.deymosko.lootroll.network.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.deymosko.lootroll.ClientVoteCache;

import java.util.UUID;
import java.util.function.Supplier;

public class VoteStartS2CPacket {
    private final UUID voteId;
    private final ItemStack item;
    private final long endTime;

    public VoteStartS2CPacket(UUID voteId, ItemStack item, long endTime) {
        this.voteId = voteId;
        this.item = item;
        this.endTime = endTime;
    }

    public static void encode(VoteStartS2CPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.voteId);
        buf.writeItem(msg.item);
        buf.writeLong(msg.endTime);
    }

    public static VoteStartS2CPacket decode(FriendlyByteBuf buf) {
        return new VoteStartS2CPacket(buf.readUUID(), buf.readItem(), buf.readLong());
    }

    public static void handle(VoteStartS2CPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientVoteCache.add(msg.voteId, msg.item, msg.endTime));
        ctx.get().setPacketHandled(true);
    }
}

