package net.foulest.kitpvp.utils.lunar.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class LCPlayerUnregisterEvent extends PlayerEvent {

    private static HandlerList handlerList;

    static {
        LCPlayerUnregisterEvent.handlerList = new HandlerList();
    }

    public LCPlayerUnregisterEvent(Player player) {
        super(player);
    }

    public static HandlerList getHandlerList() {
        return LCPlayerUnregisterEvent.handlerList;
    }

    public HandlerList getHandlers() {
        return LCPlayerUnregisterEvent.handlerList;
    }
}
