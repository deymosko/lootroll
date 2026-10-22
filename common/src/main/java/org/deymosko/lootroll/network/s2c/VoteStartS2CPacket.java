package org.deymosko.lootroll.network.s2c;

import dev.architectury.networking.NetworkManager;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.deymosko.lootroll.Lootroll;
import org.deymosko.lootroll.client.LootrollClient;

import java.util.List;
import java.util.UUID;

// Durations are sent instead of a timestamp, so client and server clocks don't have to agree.
public record VoteStartS2CPacket(UUID voteId, List<ItemStack> items, int durationTicks, int remainingTicks, boolean allowGreed)
        implements CustomPacketPayload {

    public static final Type<VoteStartS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Lootroll.MODID, "vote_start"));

    public static final StreamCodec<RegistryFriendlyByteBuf, VoteStartS2CPacket> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, VoteStartS2CPacket::voteId,
            ItemStack.LIST_STREAM_CODEC, VoteStartS2CPacket::items,
            ByteBufCodecs.VAR_INT, VoteStartS2CPacket::durationTicks,
            ByteBufCodecs.VAR_INT, VoteStartS2CPacket::remainingTicks,
            ByteBufCodecs.BOOL, VoteStartS2CPacket::allowGreed,
            VoteStartS2CPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(VoteStartS2CPacket msg, NetworkManager.PacketContext ctx) {
        ctx.queue(() -> LootrollClient.onVoteStart(msg.voteId, msg.items, msg.durationTicks, msg.remainingTicks, msg.allowGreed));
    }
}
