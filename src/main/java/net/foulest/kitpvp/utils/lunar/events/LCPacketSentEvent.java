package net.foulest.kitpvp.utils.lunar.events;

import net.foulest.kitpvp.utils.lunar.nethandler.LCPacket;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class LCPacketSentEvent extends PlayerEvent {

    private static HandlerList handlerList;

    static {
        LCPacketSentEvent.handlerList = new HandlerList();
    }

    private final LCPacket packet;

    public LCPacketSentEvent(Player player, LCPacket packet) {
        super(player);
        this.packet = packet;
    }

    public static HandlerList getHandlerList() {
        return LCPacketSentEvent.handlerList;
    }

    public HandlerList getHandlers() {
        return LCPacketSentEvent.handlerList;
    }

    public LCPacket getPacket() {
        return this.packet;
    }
}
