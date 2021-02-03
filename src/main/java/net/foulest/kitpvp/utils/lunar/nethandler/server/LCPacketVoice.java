package net.foulest.kitpvp.utils.lunar.nethandler.server;

import net.foulest.kitpvp.utils.lunar.nethandler.ByteBufWrapper;
import net.foulest.kitpvp.utils.lunar.nethandler.LCPacket;
import net.foulest.kitpvp.utils.lunar.nethandler.client.LCNetHandlerClient;
import net.foulest.kitpvp.utils.lunar.nethandler.shared.LCNetHandler;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LCPacketVoice extends LCPacket {

    private Set<UUID> uuids;
    private byte[] data;

    @ConstructorProperties({"uuids", "data"})
    public LCPacketVoice(Set<UUID> uuids, byte[] data) {
        this.uuids = uuids;
        this.data = data;
    }

    public LCPacketVoice() {
    }

    @Override
    public void write(ByteBufWrapper b) {
        b.writeVarInt(this.uuids.size());
        this.uuids.forEach(b::writeUUID);
        this.writeBlob(b, this.data);
    }

    @Override
    public void read(ByteBufWrapper b) {
        this.uuids = new HashSet<>();
        for (int size = b.readVarInt(), i = 0; i < size; ++i) {
            this.uuids.add(b.readUUID());
        }
        this.data = this.readBlob(b);
    }

    @Override
    public void process(LCNetHandler handler) {
        ((LCNetHandlerClient) handler).handleVoice(this);
    }

    @Deprecated
    public UUID getUuid() {
        return new ArrayList<>(this.uuids).get(0);
    }

    public Set<UUID> getUuids() {
        return this.uuids;
    }

    public byte[] getData() {
        return this.data;
    }
}
