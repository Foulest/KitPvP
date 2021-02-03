package net.foulest.kitpvp.utils.lunar.nethandler.client;

import net.foulest.kitpvp.utils.lunar.nethandler.ByteBufWrapper;
import net.foulest.kitpvp.utils.lunar.nethandler.LCPacket;
import net.foulest.kitpvp.utils.lunar.nethandler.shared.LCNetHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LCPacketHologramUpdate extends LCPacket {

    private UUID uuid;
    private List<String> lines;

    public LCPacketHologramUpdate() {
    }

    public LCPacketHologramUpdate(UUID uuid, List<String> lines) {
        this.uuid = uuid;
        this.lines = lines;
    }

    @Override
    public void write(ByteBufWrapper buf) throws IOException {
        buf.writeUUID(uuid);
        buf.writeVarInt(lines.size());

        for (String s : lines) {
            buf.writeString(s);
        }
    }

    @Override
    public void read(ByteBufWrapper buf) throws IOException {
        uuid = buf.readUUID();
        int linesSize = buf.readVarInt();
        lines = new ArrayList<>(linesSize);

        for (int i = 0; i < linesSize; ++i) {
            lines.add(buf.readString());
        }
    }

    @Override
    public void process(LCNetHandler handler) {
        ((LCNetHandlerClient) handler).handleUpdateHologram(this);
    }

    public UUID getUuid() {
        return uuid;
    }

    public List<String> getLines() {
        return lines;
    }
}