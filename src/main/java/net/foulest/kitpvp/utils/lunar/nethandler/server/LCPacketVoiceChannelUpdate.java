package net.foulest.kitpvp.utils.lunar.nethandler.server;

import net.foulest.kitpvp.utils.lunar.nethandler.ByteBufWrapper;
import net.foulest.kitpvp.utils.lunar.nethandler.LCPacket;
import net.foulest.kitpvp.utils.lunar.nethandler.client.LCNetHandlerClient;
import net.foulest.kitpvp.utils.lunar.nethandler.shared.LCNetHandler;

import java.beans.ConstructorProperties;
import java.util.UUID;

public class LCPacketVoiceChannelUpdate extends LCPacket {

    public int status;
    private UUID channelUuid;
    private UUID uuid;
    private String name;

    @ConstructorProperties({"status", "channelUuid", "uuid", "name"})
    public LCPacketVoiceChannelUpdate(int status, UUID channelUuid, UUID uuid, String name) {
        this.status = status;
        this.channelUuid = channelUuid;
        this.uuid = uuid;
        this.name = name;
    }

    public LCPacketVoiceChannelUpdate() {
    }

    @Override
    public void write(ByteBufWrapper buf) {
        buf.writeVarInt(this.status);
        buf.writeUUID(this.channelUuid);
        buf.writeUUID(this.uuid);
        buf.writeString(this.name);
    }

    @Override
    public void read(ByteBufWrapper buf) {
        status = buf.readVarInt();
        channelUuid = buf.readUUID();
        uuid = buf.readUUID();
        name = buf.readString();
    }

    @Override
    public void process(LCNetHandler handler) {
        ((LCNetHandlerClient) handler).handleVoiceChannelUpdate(this);
    }

    public int getStatus() {
        return this.status;
    }

    public UUID getChannelUuid() {
        return this.channelUuid;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public String getName() {
        return this.name;
    }
}
