package net.foulest.kitpvp.utils.lunar.nethandler.client;

import net.foulest.kitpvp.utils.lunar.nethandler.ByteBufWrapper;
import net.foulest.kitpvp.utils.lunar.nethandler.LCPacket;
import net.foulest.kitpvp.utils.lunar.nethandler.shared.LCNetHandler;

import java.io.IOException;

public class LCPacketNotification extends LCPacket {

    private String message;
    private long durationMs;
    private String level;

    public LCPacketNotification() {
    }

    public LCPacketNotification(String message, long durationMs, String level) {
        this.message = message;
        this.durationMs = durationMs;
        this.level = level;
    }

    @Override
    public void write(ByteBufWrapper buf) throws IOException {
        buf.writeString(message);
        buf.buf().writeLong(durationMs);
        buf.writeString(level);
    }

    @Override
    public void read(ByteBufWrapper buf) throws IOException {
        message = buf.readString();
        durationMs = buf.buf().readLong();
        level = buf.readString();
    }

    @Override
    public void process(LCNetHandler handler) {
        ((LCNetHandlerClient) handler).handleNotification(this);
    }

    public String getMessage() {
        return message;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public String getLevel() {
        return level;
    }
}
