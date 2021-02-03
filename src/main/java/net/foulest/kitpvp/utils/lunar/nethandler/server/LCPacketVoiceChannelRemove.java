package net.foulest.kitpvp.utils.lunar.nethandler.server;

import net.foulest.kitpvp.utils.lunar.nethandler.ByteBufWrapper;
import net.foulest.kitpvp.utils.lunar.nethandler.LCPacket;
import net.foulest.kitpvp.utils.lunar.nethandler.client.LCNetHandlerClient;
import net.foulest.kitpvp.utils.lunar.nethandler.shared.LCNetHandler;

import java.beans.ConstructorProperties;
import java.util.UUID;

public class LCPacketVoiceChannelRemove extends LCPacket {

    private UUID uuid;

    @ConstructorProperties({"uuid"})
    public LCPacketVoiceChannelRemove(UUID uuid) {
        this.uuid = uuid;
    }

    public LCPacketVoiceChannelRemove() {
    }

    @Override
    public void write(ByteBufWrapper buf) {
        buf.writeUUID(uuid);
    }

    @Override
    public void read(ByteBufWrapper buf) {
        uuid = buf.readUUID();
    }

    @Override
    public void process(LCNetHandler handler) {
        ((LCNetHandlerClient) handler).handleVoiceChannelDelete(this);
    }

    public UUID getUuid() {
        return uuid;
    }
}
