package net.foulest.kitpvp.utils.lunar.nethandler.client;

import net.foulest.kitpvp.utils.lunar.nethandler.ByteBufWrapper;
import net.foulest.kitpvp.utils.lunar.nethandler.LCPacket;
import net.foulest.kitpvp.utils.lunar.nethandler.shared.LCNetHandler;

import java.io.IOException;

public class LCPacketCooldown extends LCPacket {

    private String message;
    private long durationMs;
    private int iconId;

    public LCPacketCooldown() {
    }

    public LCPacketCooldown(String message, long durationMs, int iconId) {
        this.message = message;
        this.durationMs = durationMs;
        this.iconId = iconId;
    }

    @Override
    public void write(ByteBufWrapper buf) throws IOException {
        buf.writeString(message);
        buf.buf().writeLong(durationMs);
        buf.buf().writeInt(iconId);
    }

    @Override
    public void read(ByteBufWrapper buf) throws IOException {
        message = buf.readString();
        durationMs = buf.buf().readLong();
        iconId = buf.buf().readInt();
    }

    @Override
    public void process(LCNetHandler handler) {
        ((LCNetHandlerClient) handler).handleCooldown(this);
    }

    public String getMessage() {
        return message;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public int getIconId() {
        return iconId;
    }
}
