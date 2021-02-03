package net.foulest.kitpvp.utils.lunar.nethandler.shared;

import net.foulest.kitpvp.utils.lunar.nethandler.ByteBufWrapper;
import net.foulest.kitpvp.utils.lunar.nethandler.LCPacket;

import java.io.IOException;

public class LCPacketWaypointRemove extends LCPacket {

    private String name;
    private String world;

    public LCPacketWaypointRemove() {
    }

    public LCPacketWaypointRemove(String name, String world) {
        this.name = name;
        this.world = world;
    }

    @Override
    public void write(ByteBufWrapper buf) throws IOException {
        buf.writeString(name);
        buf.writeString(world);
    }

    @Override
    public void read(ByteBufWrapper buf) {
        name = buf.readString();
        world = buf.readString();
    }

    @Override
    public void process(LCNetHandler handler) {
        handler.handleRemoveWaypoint(this);
    }

    public String getName() {
        return name;
    }

    public String getWorld() {
        return world;
    }
}
