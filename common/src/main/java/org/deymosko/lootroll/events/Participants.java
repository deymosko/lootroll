package org.deymosko.lootroll.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.deymosko.lootroll.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    /**
     * Players eligible for a mob's loot, applying the vote config.
     * @param source the mob, used for the line-of-sight check
     * @param killer UUID of whoever got the kill, or null
     */
    public static List<ServerPlayer> forLoot(ServerLevel level, Vec3 pos, Entity source, UUID killer) {
        List<ServerPlayer> result = new ArrayList<>(around(level, pos, Config.VOTE_RADIUS.get()));

        if (Config.REQUIRE_LINE_OF_SIGHT.get() && source != null && !source.isRemoved()) {
            result.removeIf(p -> !p.hasLineOfSight(source));
        }

        if (Config.INCLUDE_KILLER.get() && killer != null
                && level.getPlayerByUUID(killer) instanceof ServerPlayer player
                && !player.isSpectator() && !result.contains(player)) {
            result.add(player);
        }
        return result;
    }
}
