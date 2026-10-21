package org.deymosko.lootroll.network.s2c;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.deymosko.lootroll.client.LootrollClient;
import org.deymosko.lootroll.network.Packets;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Tells the client a vote is over for it (time ran out, everyone voted, or it voted via chat)
 * so it can drop it from the UI. Carries the items when this player won them.
 */
public class VoteEndS2CPacket {
    private final UUID voteId;
    private final List<ItemStack> wonItems;

    public VoteEndS2CPacket(UUID voteId, List<ItemStack> wonItems) {
        this.voteId = voteId;
        this.wonItems = wonItems;
    }

    public static void encode(VoteEndS2CPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(Packets.PROTOCOL_VERSION);
        buf.writeUUID(msg.voteId);
        buf.writeVarInt(msg.wonItems.size());
        for (ItemStack stack : msg.wonItems) {
            buf.writeItem(stack);
        }
    }

    public static void handle(FriendlyByteBuf buf, NetworkManager.PacketContext ctx) {
        if (!Packets.checkProtocol(buf, Packets.VOTE_END)) return;
        UUID voteId = buf.readUUID();
        int size = buf.readVarInt();
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            items.add(buf.readItem());
        }
        ctx.queue(() -> LootrollClient.onVoteEnd(voteId, !items.isEmpty(), items));
    }
}
