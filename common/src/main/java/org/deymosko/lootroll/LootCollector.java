package org.deymosko.lootroll;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.deymosko.lootroll.events.Participants;

import java.util.*;

/**
 * Gathers everything a mob drops into one lot before voting starts.
 * <p>
 * Drops arrive from several places: the loader death-drop hooks, the integration mixins,
 * and a "capture zone" that grabs item entities spawning around a tracked mob while it dies.
 * The zone is what makes bosses with custom death logic work (drops after an animation,
 * spawned straight into the world, one by one, ...) without a mixin per mod.
 * A lot is turned into votes once it stops receiving items.
 */
public final class LootCollector {

    /** Ticks without new items before a lot is put up for vote. */
    private static final int QUIET_TICKS = 10;
    /** Ticks the zone stays open after the mob's body is removed, for drops spawned on removal. */
    private static final int REMOVAL_GRACE_TICKS = 5;

    private static final class Lot {
        final LivingEntity entity;
        final ServerLevel level;
        final List<ItemStack> items = new ArrayList<>();
        // Decided when the mob dies: whoever was there for the kill.
        final List<ServerPlayer> participants;
        final int closesAt;
        final boolean zone;
        Vec3 pos;
        int lastAddTick;
        int removedAt = -1;

        Lot(LivingEntity entity, boolean zone, int now) {
            this.entity = entity;
            this.level = (ServerLevel) entity.level();
            this.zone = zone;
            this.pos = entity.position();
            this.lastAddTick = now;
            this.closesAt = now + Config.CAPTURE_WINDOW.get() * 20;
            Entity credit = entity.getKillCredit();
            this.participants = Participants.forLoot(level, pos, entity, credit != null ? credit.getUUID() : null);
        }

        boolean zoneOpen(int now) {
            if (!zone || now >= closesAt) return false;
            // Another mod cancelled the death (totem-like effects): stop capturing around a living mob.
            if (!entity.isRemoved() && !entity.isDeadOrDying()) return false;
            return removedAt < 0 || now - removedAt <= REMOVAL_GRACE_TICKS;
        }

        boolean readyToFlush(int now) {
            return !zoneOpen(now) && now - lastAddTick >= QUIET_TICKS;
        }
    }

    private static final Map<UUID, Lot> lots = new LinkedHashMap<>();

    private LootCollector() {}

    private static int now(Level level) {
        return level.getServer() != null ? level.getServer().getTickCount() : 0;
    }

    /** A tracked mob died: open its capture zone before its drops start appearing. */
    public static void onLivingDeath(LivingEntity entity, DamageSource source) {
        if (!Config.CAPTURE_NEARBY_DROPS.get() || !LootDropHandler.isTracked(entity)) return;
        lots.computeIfAbsent(entity.getUUID(), id -> new Lot(entity, true, now(entity.level())));
    }

    /** Adds drops to the mob's lot. */
    public static void collect(LivingEntity entity, List<ItemStack> items) {
        if (entity.level().isClientSide || items.isEmpty()) return;

        int now = now(entity.level());
        Lot lot = lots.computeIfAbsent(entity.getUUID(), id -> new Lot(entity, false, now));
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) lot.items.add(stack.copy());
        }
        lot.lastAddTick = now;
    }

    /** @return true if the item entity was captured and must not be added to the world. */
    public static boolean onItemSpawn(ItemEntity item, Level level) {
        if (lots.isEmpty() || level.isClientSide || LootDropHandler.isReleasing()) return false;
        if (item.getOwner() != null || item.getItem().isEmpty()) return false;

        int now = now(level);
        double radius = Config.CAPTURE_RADIUS.get();
        for (Lot lot : lots.values()) {
            if (lot.level != level || !lot.zoneOpen(now)) continue;

            boolean inside = lot.entity.isRemoved()
                    ? item.position().closerThan(lot.pos, radius + lot.entity.getBbWidth())
                    : lot.entity.getBoundingBox().inflate(radius).contains(item.position());
            if (inside) {
                lot.items.add(item.getItem().copy());
                lot.lastAddTick = now;
                return true;
            }
        }
        return false;
    }

    public static void tick(MinecraftServer server) {
        if (lots.isEmpty()) return;

        int now = server.getTickCount();
        List<Lot> ready = new ArrayList<>();
        for (Iterator<Lot> it = lots.values().iterator(); it.hasNext(); ) {
            Lot lot = it.next();
            if (!lot.entity.isRemoved()) {
                lot.pos = lot.entity.position();
            } else if (lot.removedAt < 0) {
                lot.removedAt = now;
            }
            if (lot.readyToFlush(now)) {
                it.remove();
                ready.add(lot);
            }
        }
        for (Lot lot : ready) {
            if (!lot.items.isEmpty()) {
                LootDropHandler.startVotes(lot.level, lot.pos, lot.participants, lot.items);
            }
        }
    }

    /** On shutdown, put everything still being collected on the ground so nothing is lost. */
    public static void onServerStopping(MinecraftServer server) {
        for (Lot lot : lots.values()) {
            for (ItemStack stack : lot.items) {
                LootDropHandler.dropInWorld(lot.level, lot.pos, stack);
            }
        }
        lots.clear();
    }
}
