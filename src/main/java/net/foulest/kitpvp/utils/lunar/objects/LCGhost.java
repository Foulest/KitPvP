package net.foulest.kitpvp.utils.lunar.objects;

import java.util.List;
import java.util.UUID;

public final class LCGhost {

    private final List<UUID> ghostedPlayers;
    private final List<UUID> unGhostedPlayers;

    public LCGhost(List<UUID> ghostedPlayers, List<UUID> unGhostedPlayers) {
        this.ghostedPlayers = ghostedPlayers;
        this.unGhostedPlayers = unGhostedPlayers;
    }

    public List<UUID> getGhostedPlayers() {
        return ghostedPlayers;
    }

    public List<UUID> getUnGhostedPlayers() {
        return unGhostedPlayers;
    }
}
