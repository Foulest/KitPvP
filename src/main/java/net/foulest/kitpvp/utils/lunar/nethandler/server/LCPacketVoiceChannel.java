package net.foulest.kitpvp.utils.lunar.nethandler.server;

import net.foulest.kitpvp.utils.lunar.nethandler.ByteBufWrapper;
import net.foulest.kitpvp.utils.lunar.nethandler.LCPacket;
import net.foulest.kitpvp.utils.lunar.nethandler.client.LCNetHandlerClient;
import net.foulest.kitpvp.utils.lunar.nethandler.shared.LCNetHandler;

import java.beans.ConstructorProperties;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LCPacketVoiceChannel extends LCPacket {

    private UUID uuid;
    private String name;
    private Map<UUID, String> players;
    private Map<UUID, String> listening;

    @ConstructorProperties({"uuid", "name", "players", "listening"})
    public LCPacketVoiceChannel(UUID uuid, String name, Map<UUID, String> players, Map<UUID, String> listening) {
        this.uuid = uuid;
        this.name = name;
        this.players = players;
        this.listening = listening;
    }

    public LCPacketVoiceChannel() {
    }

    @Override
    public void write(ByteBufWrapper b) {
        b.writeUUID(this.uuid);
        b.writeString(this.name);
        this.writeMap(b, this.players);
        this.writeMap(b, this.listening);
    }

    @Override
    public void read(ByteBufWrapper b) {
        this.uuid = b.readUUID();
        this.name = b.readString();
        this.players = this.readMap(b);
        this.listening = this.readMap(b);
    }

    private void writeMap(ByteBufWrapper buf, Map<UUID, String> players) {
        buf.writeVarInt(players.size());

        for (Map.Entry<UUID, String> player : players.entrySet()) {
            buf.writeUUID(player.getKey());
            buf.writeString(player.getValue());
        }
    }

    private Map<UUID, String> readMap(ByteBufWrapper buf) {
        int size = buf.readVarInt();
        Map<UUID, String> players = new HashMap<>();

        for (int i = 0; i < size; ++i) {
            UUID uuid = buf.readUUID();
            String name = buf.readString();
            players.put(uuid, name);
        }

        return players;
    }

    @Override
    public void process(LCNetHandler handler) {
        ((LCNetHandlerClient) handler).handleVoiceChannels(this);
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public String getName() {
        return this.name;
    }

    public Map<UUID, String> getPlayers() {
        return this.players;
    }

    public Map<UUID, String> getListening() {
        return this.listening;
    }
}
