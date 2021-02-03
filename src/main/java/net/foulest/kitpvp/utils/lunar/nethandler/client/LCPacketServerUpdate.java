package net.foulest.kitpvp.utils.lunar.nethandler.client;

import net.foulest.kitpvp.utils.lunar.nethandler.ByteBufWrapper;
import net.foulest.kitpvp.utils.lunar.nethandler.LCPacket;
import net.foulest.kitpvp.utils.lunar.nethandler.shared.LCNetHandler;

import java.io.IOException;

public class LCPacketServerUpdate extends LCPacket {

    private String server;

    public LCPacketServerUpdate() {
    }

    public LCPacketServerUpdate(String server) {
        this.server = server;
    }

    @Override
    public void write(ByteBufWrapper buf) throws IOException {
        buf.writeString(server);
    }

    @Override
    public void read(ByteBufWrapper buf) throws IOException {
        this.server = buf.readString();
    }

    @Override
    public void process(LCNetHandler handler) {
        ((LCNetHandlerClient) handler).handleServerUpdate(this);
    }

    public String getServer() {
        return server;
    }
}
