package net.foulest.kitpvp.utils.lunar.nethandler.client;

import net.foulest.kitpvp.utils.lunar.nethandler.ByteBufWrapper;
import net.foulest.kitpvp.utils.lunar.nethandler.LCPacket;
import net.foulest.kitpvp.utils.lunar.nethandler.server.LCNetHandlerServer;
import net.foulest.kitpvp.utils.lunar.nethandler.shared.LCNetHandler;

import java.io.IOException;

public class LCPacketClientVoice extends LCPacket {

    private byte[] data;

    public LCPacketClientVoice() {
    }

    public LCPacketClientVoice(byte[] data) {
        this.data = data;
    }

    @Override
    public void write(ByteBufWrapper b) throws IOException {
        writeBlob(b, data);
    }

    @Override
    public void read(ByteBufWrapper b) throws IOException {
        data = readBlob(b);
    }

    @Override
    public void process(LCNetHandler handler) {
        ((LCNetHandlerServer) handler).handleVoice(this);
    }

    public byte[] getData() {
        return data;
    }
}
