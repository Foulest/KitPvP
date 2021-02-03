package net.foulest.kitpvp.utils.lunar.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class LCPlayerRegisterEvent extends PlayerEvent {

    private static HandlerList handlerList;

    static {
        LCPlayerRegisterEvent.handlerList = new HandlerList();
    }

    public LCPlayerRegisterEvent(Player player) {
        super(player);
    }

    public static HandlerList getHandlerList() {
        return LCPlayerRegisterEvent.handlerList;
    }

    public HandlerList getHandlers() {
        return LCPlayerRegisterEvent.handlerList;
    }
}
