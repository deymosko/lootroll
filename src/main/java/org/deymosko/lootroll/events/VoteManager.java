package org.deymosko.lootroll.events;

import java.util.*;

public class VoteManager {
    private static final Map<UUID, VoteSession> activeVotes = new HashMap<>();

    public static void addSession(VoteSession session) {
        activeVotes.put(session.getId(), session);
    }

    public static Collection<VoteSession> getActiveSessions() {
        return activeVotes.values();
    }

    public static void tick() {
        List<UUID> finished = new ArrayList<>();
        for (VoteSession session : activeVotes.values()) {
            if (session.isFinished()) {
                finished.add(session.getId());
                // тут буде логіка видачі луту
            }
        }
        for (UUID id : finished) {
            activeVotes.remove(id);
        }
    }

    public static Optional<VoteSession> get(UUID id) {
        return Optional.ofNullable(activeVotes.get(id));
    }
}
