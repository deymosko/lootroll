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

    public VoteStartS2CPacket(UUID voteId, ItemStack item) {
        this.voteId = voteId;
        this.item = item;
    }

    public static void encode(VoteStartS2CPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.voteId);
        buf.writeItem(msg.item);
    }

    public static VoteStartS2CPacket decode(FriendlyByteBuf buf) {
        return new VoteStartS2CPacket(buf.readUUID(), buf.readItem());
    }

    public static void handle(VoteStartS2CPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientVoteCache.add(msg.voteId, msg.item));
        ctx.get().setPacketHandled(true);
    }
}

