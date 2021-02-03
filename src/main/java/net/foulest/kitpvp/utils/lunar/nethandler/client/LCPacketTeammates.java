package net.foulest.kitpvp.utils.lunar.nethandler.client;

import net.foulest.kitpvp.utils.lunar.nethandler.ByteBufWrapper;
import net.foulest.kitpvp.utils.lunar.nethandler.LCPacket;
import net.foulest.kitpvp.utils.lunar.nethandler.shared.LCNetHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LCPacketTeammates extends LCPacket {

    private UUID leader;
    private long lastMs;
    private Map<UUID, Map<String, Double>> players;

    public LCPacketTeammates() {
    }

    public LCPacketTeammates(UUID leader, long lastMs, Map<UUID, Map<String, Double>> players) {
        this.leader = leader;
        this.lastMs = lastMs;
        this.players = players;
    }

    @Override
    public void write(ByteBufWrapper buf) throws IOException {
        buf.buf().writeBoolean(leader != null);

        if (leader != null) {
            buf.writeUUID(leader);
        }

        buf.buf().writeLong(lastMs);
        buf.writeVarInt(players.size());

        players.forEach((uuid, posMap) -> {
            buf.writeUUID(uuid);
            buf.writeVarInt(posMap.size());

            posMap.forEach((key, val) -> {
                buf.writeString(key);
                buf.buf().writeDouble(val);
            });
        });
    }

    @Override
    public void read(ByteBufWrapper buf) throws IOException {
        if (buf.buf().readBoolean()) {
            leader = buf.readUUID();
        }

        lastMs = buf.buf().readLong();
        int playersSize = buf.readVarInt();
        players = new HashMap<>();

        for (int i = 0; i < playersSize; ++i) {
            UUID uuid = buf.readUUID();
            int posMapSize = buf.readVarInt();
            Map<String, Double> posMap = new HashMap<>();

            for (int j = 0; j < posMapSize; ++j) {
                String key = buf.readString();
                double val = buf.buf().readDouble();
                posMap.put(key, val);
            }

            players.put(uuid, posMap);
        }
    }

    @Override
    public void process(LCNetHandler handler) {
        ((LCNetHandlerClient) handler).handleTeammates(this);
    }

    public UUID getLeader() {
        return leader;
    }

    public long getLastMs() {
        return lastMs;
    }

    public Map<UUID, Map<String, Double>> getPlayers() {
        return players;
    }
}
