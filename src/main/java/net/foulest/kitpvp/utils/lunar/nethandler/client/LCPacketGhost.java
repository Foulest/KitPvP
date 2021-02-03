package net.foulest.kitpvp.utils.lunar.nethandler.client;

import net.foulest.kitpvp.utils.lunar.nethandler.ByteBufWrapper;
import net.foulest.kitpvp.utils.lunar.nethandler.LCPacket;
import net.foulest.kitpvp.utils.lunar.nethandler.shared.LCNetHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LCPacketGhost extends LCPacket {

    private List<UUID> addGhostList;
    private List<UUID> removeGhostList;

    public LCPacketGhost() {
    }

    public LCPacketGhost(List<UUID> uuidList, List<UUID> removeGhostList) {
        this.addGhostList = uuidList;
        this.removeGhostList = removeGhostList;
    }

    @Override
    public void write(ByteBufWrapper buf) throws IOException {
        buf.writeVarInt(addGhostList.size());
        for (UUID uuid : addGhostList) {
            buf.writeUUID(uuid);
        }

        buf.writeVarInt(removeGhostList.size());
        for (UUID uuid : removeGhostList) {
            buf.writeUUID(uuid);
        }
    }

    @Override
    public void read(ByteBufWrapper buf) throws IOException {
        int addListSize = buf.readVarInt();
        addGhostList = new ArrayList<>(addListSize);
        for (int i = 0; i < addListSize; ++i) {
            addGhostList.add(buf.readUUID());
        }

        int removeListSize = buf.readVarInt();
        removeGhostList = new ArrayList<>(removeListSize);
        for (int j = 0; j < removeListSize; ++j) {
            removeGhostList.add(buf.readUUID());
        }
    }

    @Override
    public void process(LCNetHandler handler) {
        ((LCNetHandlerClient) handler).handleGhost(this);
    }

    public List<UUID> getAddGhostList() {
        return addGhostList;
    }

    public List<UUID> getRemoveGhostList() {
        return removeGhostList;
    }
}