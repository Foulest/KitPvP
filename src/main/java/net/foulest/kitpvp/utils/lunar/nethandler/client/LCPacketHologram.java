package net.foulest.kitpvp.utils.lunar.nethandler.client;

import net.foulest.kitpvp.utils.lunar.nethandler.ByteBufWrapper;
import net.foulest.kitpvp.utils.lunar.nethandler.LCPacket;
import net.foulest.kitpvp.utils.lunar.nethandler.shared.LCNetHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LCPacketHologram extends LCPacket {

    private UUID uuid;
    private double x;
    private double y;
    private double z;
    private List<String> lines;

    public LCPacketHologram() {
    }

    public LCPacketHologram(UUID uuid, double x, double y, double z, List<String> lines) {
        this.uuid = uuid;
        this.x = x;
        this.y = y;
        this.z = z;
        this.lines = lines;
    }

    @Override
    public void write(ByteBufWrapper buf) throws IOException {
        buf.writeUUID(uuid);
        buf.buf().writeDouble(x);
        buf.buf().writeDouble(y);
        buf.buf().writeDouble(z);
        buf.writeVarInt(lines.size());

        for (String s : lines) {
            buf.writeString(s);
        }
    }

    @Override
    public void read(ByteBufWrapper buf) throws IOException {
        uuid = buf.readUUID();
        x = buf.buf().readDouble();
        y = buf.buf().readDouble();
        z = buf.buf().readDouble();
        int linesSize = buf.readVarInt();
        lines = new ArrayList<>(linesSize);

        for (int i = 0; i < linesSize; ++i) {
            lines.add(buf.readString());
        }
    }

    @Override
    public void process(LCNetHandler handler) {
        ((LCNetHandlerClient) handler).handleAddHologram(this);
    }

    public UUID getUuid() {
        return uuid;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public List<String> getLines() {
        return lines;
    }
}
