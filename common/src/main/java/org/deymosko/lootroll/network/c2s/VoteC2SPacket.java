package org.deymosko.lootroll.network.c2s;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.deymosko.lootroll.Lootroll;
import org.deymosko.lootroll.enums.VoteType;
import org.deymosko.lootroll.events.VoteManager;

import java.util.UUID;

public record VoteC2SPacket(UUID voteId, VoteType voteType) implements CustomPacketPayload {

    public static final Type<VoteC2SPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Lootroll.MODID, "vote"));

    private static final StreamCodec<ByteBuf, VoteType> VOTE_TYPE_CODEC =
            ByteBufCodecs.VAR_INT.map(i -> VoteType.values()[Math.floorMod(i, VoteType.values().length)], VoteType::ordinal);

    public static final StreamCodec<RegistryFriendlyByteBuf, VoteC2SPacket> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, VoteC2SPacket::voteId,
            VOTE_TYPE_CODEC, VoteC2SPacket::voteType,
            VoteC2SPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(VoteC2SPacket msg, NetworkManager.PacketContext ctx) {
        ctx.queue(() -> {
            if (ctx.getPlayer() instanceof ServerPlayer player) {
                VoteManager.vote(msg.voteId, player, msg.voteType);
            }
        });
    }
}
