package net.foulest.kitpvp.utils.lunar.nethandler.shared;

import net.foulest.kitpvp.utils.lunar.nethandler.ByteBufWrapper;
import net.foulest.kitpvp.utils.lunar.nethandler.LCPacket;

import java.io.IOException;

public class LCPacketWaypointAdd extends LCPacket {

    private String name;
    private String world;
    private int color;
    private int x;
    private int y;
    private int z;
    private boolean forced;
    private boolean visible;

    public LCPacketWaypointAdd() {
    }

    public LCPacketWaypointAdd(String name, String world, int color, int x, int y, int z, boolean forced, boolean visible) {
        this.name = name;
        this.world = world;
        this.color = color;
        this.x = x;
        this.y = y;
        this.z = z;
        this.forced = forced;
        this.visible = visible;
    }

    @Override
    public void write(ByteBufWrapper buf) throws IOException {
        buf.writeString(name);
        buf.writeString(world);
        buf.buf().writeInt(color);
        buf.buf().writeInt(x);
        buf.buf().writeInt(y);
        buf.buf().writeInt(z);
        buf.buf().writeBoolean(forced);
        buf.buf().writeBoolean(visible);
    }

    @Override
    public void read(ByteBufWrapper buf) {
        name = buf.readString();
        world = buf.readString();
        color = buf.buf().readInt();
        x = buf.buf().readInt();
        y = buf.buf().readInt();
        z = buf.buf().readInt();
        forced = buf.buf().readBoolean();
        visible = buf.buf().readBoolean();
    }

    @Override
    public void process(LCNetHandler handler) {
        handler.handleAddWaypoint(this);
    }

    public String getName() {
        return name;
    }

    public String getWorld() {
        return world;
    }

    public int getColor() {
        return color;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public boolean isForced() {
        return forced;
    }

    public boolean isVisible() {
        return visible;
    }
}
