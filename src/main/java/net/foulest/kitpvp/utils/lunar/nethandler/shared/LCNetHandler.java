package net.foulest.kitpvp.utils.lunar.nethandler.shared;

public interface LCNetHandler {

    void handleAddWaypoint(LCPacketWaypointAdd p0);

    void handleRemoveWaypoint(LCPacketWaypointRemove p0);

    void handleEmote(LCPacketEmoteBroadcast p0);
}
