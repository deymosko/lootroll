package org.deymosko.lootroll.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class Participants {

    private Participants() {}

    /**
     * Living, non-spectator players in this level within a sphere of the given radius.
     * Unlike getNearbyPlayers with a mob as the source, this doesn't require line of sight,
     * so players behind a wall or pillar still take part.
     */
    public static List<ServerPlayer> around(ServerLevel level, Vec3 center, double radius) {
        double radiusSq = radius * radius;
        return level.players().stream()
                .filter(p -> p.isAlive() && !p.isSpectator())
                .filter(p -> p.distanceToSqr(center) <= radiusSq)
                .toList();
    }
}
