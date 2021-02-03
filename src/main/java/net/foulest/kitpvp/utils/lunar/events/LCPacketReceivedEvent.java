package net.foulest.kitpvp.utils.lunar.events;

import net.foulest.kitpvp.utils.lunar.nethandler.LCPacket;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class LCPacketReceivedEvent extends PlayerEvent {

    private static HandlerList handlerList;

    static {
        LCPacketReceivedEvent.handlerList = new HandlerList();
    }

    private final LCPacket packet;

    public LCPacketReceivedEvent(Player player, LCPacket packet) {
        super(player);
        this.packet = packet;
    }

    public static HandlerList getHandlerList() {
        return LCPacketReceivedEvent.handlerList;
    }

    public HandlerList getHandlers() {
        return LCPacketReceivedEvent.handlerList;
    }

    public LCPacket getPacket() {
        return this.packet;
    }
}
