package org.deymosko.lootroll.network.s2c;

import dev.architectury.networking.NetworkManager;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.deymosko.lootroll.Lootroll;
import org.deymosko.lootroll.client.LootrollClient;

import java.util.List;
import java.util.UUID;

/**
 * Tells the client a vote is over for it (time ran out, everyone voted, or it voted via chat)
 * so it can drop it from the UI. Carries the items when this player won them.
 */
public record VoteEndS2CPacket(UUID voteId, List<ItemStack> wonItems) implements CustomPacketPayload {

    public static final Type<VoteEndS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Lootroll.MODID, "vote_end"));

    public static final StreamCodec<RegistryFriendlyByteBuf, VoteEndS2CPacket> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, VoteEndS2CPacket::voteId,
            ItemStack.LIST_STREAM_CODEC, VoteEndS2CPacket::wonItems,
            VoteEndS2CPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(VoteEndS2CPacket msg, NetworkManager.PacketContext ctx) {
        ctx.queue(() -> LootrollClient.onVoteEnd(msg.voteId, !msg.wonItems.isEmpty(), msg.wonItems));
    }
}
