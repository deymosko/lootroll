package org.deymosko.lootroll.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.deymosko.lootroll.Lootroll;
import org.deymosko.lootroll.network.c2s.VoteC2SPacket;
import org.deymosko.lootroll.network.s2c.VoteStartS2CPacket;

public class Packets
{
    private static final String PROTOCOL_VERSION = "1.0";
    private static SimpleChannel INSTANCE;
    private static int packetID = 0;



    private static int id()
    {
        return packetID++;
    }
    public static void register()
    {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(Lootroll.MODID, "messages"))
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();
        INSTANCE = net;

        net.messageBuilder(VoteStartS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(VoteStartS2CPacket::encode)
                .decoder(VoteStartS2CPacket::decode)
                .consumerMainThread(VoteStartS2CPacket::handle)
                .add();

        net.messageBuilder(VoteC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(VoteC2SPacket::encode)
                .decoder(VoteC2SPacket::decode)
                .consumerMainThread(VoteC2SPacket::handle)
                .add();

    }
    public static <MSG> void sendToServer(MSG message) {INSTANCE.sendToServer(message);}

    public static <MSG> void sendToClient(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), message);
    }
}
