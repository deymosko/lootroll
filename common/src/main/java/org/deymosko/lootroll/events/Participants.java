package org.deymosko.lootroll.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.deymosko.lootroll.Config;

import java.util.ArrayList;
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

    /** Players eligible for the loot of a dead entity, applying the vote config. */
    public static List<ServerPlayer> forDeath(LivingEntity entity) {
        ServerLevel level = (ServerLevel) entity.level();
        List<ServerPlayer> result = new ArrayList<>(around(level, entity.position(), Config.VOTE_RADIUS.get()));

        if (Config.REQUIRE_LINE_OF_SIGHT.get()) {
            result.removeIf(p -> !p.hasLineOfSight(entity));
        }

        if (Config.INCLUDE_KILLER.get()) {
            Entity killer = entity.getKillCredit();
            if (killer instanceof ServerPlayer player && player.level() == level && !player.isSpectator() && !result.contains(player)) {
                result.add(player);
            }
        }
        return result;
    }
}
