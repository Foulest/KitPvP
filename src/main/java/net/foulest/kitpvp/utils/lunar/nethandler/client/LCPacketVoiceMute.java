package net.foulest.kitpvp.utils.lunar.nethandler.client;

import net.foulest.kitpvp.utils.lunar.nethandler.ByteBufWrapper;
import net.foulest.kitpvp.utils.lunar.nethandler.LCPacket;
import net.foulest.kitpvp.utils.lunar.nethandler.server.LCNetHandlerServer;
import net.foulest.kitpvp.utils.lunar.nethandler.shared.LCNetHandler;

import java.io.IOException;
import java.util.UUID;

public class LCPacketVoiceMute extends LCPacket {

    private UUID muting;
    private int volume;

    public LCPacketVoiceMute() {
    }

    public LCPacketVoiceMute(UUID muting) {
        this(muting, 0);
    }

    public LCPacketVoiceMute(UUID muting, int volume) {
        this.muting = muting;
        this.volume = volume;
    }

    @Override
    public void write(ByteBufWrapper buf) throws IOException {
        buf.writeUUID(muting);
        buf.writeVarInt(volume);
    }

    @Override
    public void read(ByteBufWrapper b) throws IOException {
        muting = b.readUUID();
        volume = b.readVarInt();
    }

    @Override
    public void process(LCNetHandler handler) {
        ((LCNetHandlerServer) handler).handleVoiceMute(this);
    }

    public UUID getMuting() {
        return muting;
    }

    public int getVolume() {
        return volume;
    }
}
