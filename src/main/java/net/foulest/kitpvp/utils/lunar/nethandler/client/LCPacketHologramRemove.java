package net.foulest.kitpvp.utils.lunar.nethandler.client;

import net.foulest.kitpvp.utils.lunar.nethandler.ByteBufWrapper;
import net.foulest.kitpvp.utils.lunar.nethandler.LCPacket;
import net.foulest.kitpvp.utils.lunar.nethandler.shared.LCNetHandler;

import java.io.IOException;
import java.util.UUID;

public class LCPacketHologramRemove extends LCPacket {

    private UUID uuid;

    public LCPacketHologramRemove() {
    }

    public LCPacketHologramRemove(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void write(ByteBufWrapper buf) throws IOException {
        buf.writeUUID(uuid);
    }

    @Override
    public void read(ByteBufWrapper buf) throws IOException {
        uuid = buf.readUUID();
    }

    @Override
    public void process(LCNetHandler handler) {
        ((LCNetHandlerClient) handler).handleRemoveHologram(this);
    }

    public UUID getUuid() {
        return uuid;
    }
}
