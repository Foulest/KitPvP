package net.foulest.kitpvp.utils.lunar.nethandler.shared;

import net.foulest.kitpvp.utils.lunar.nethandler.ByteBufWrapper;
import net.foulest.kitpvp.utils.lunar.nethandler.LCPacket;

import java.io.IOException;
import java.util.UUID;

public class LCPacketEmoteBroadcast extends LCPacket {

    private UUID uuid;
    private int emoteId;

    public LCPacketEmoteBroadcast() {
    }

    public LCPacketEmoteBroadcast(UUID uuid, int emoteId) {
        this.uuid = uuid;
        this.emoteId = emoteId;
    }

    @Override
    public void write(ByteBufWrapper buf) throws IOException {
        buf.writeUUID(this.uuid);
        buf.buf().writeInt(this.emoteId);
    }

    @Override
    public void read(ByteBufWrapper buf) {
        this.uuid = buf.readUUID();
        this.emoteId = buf.buf().readInt();
    }

    @Override
    public void process(LCNetHandler handler) {
        handler.handleEmote(this);
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public int getEmoteId() {
        return this.emoteId;
    }
}
