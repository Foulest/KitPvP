package net.foulest.kitpvp.utils.lunar.nethandler.client;

import net.foulest.kitpvp.utils.lunar.nethandler.ByteBufWrapper;
import net.foulest.kitpvp.utils.lunar.nethandler.LCPacket;
import net.foulest.kitpvp.utils.lunar.nethandler.server.LCNetHandlerServer;
import net.foulest.kitpvp.utils.lunar.nethandler.shared.LCNetHandler;

import java.io.IOException;
import java.util.UUID;

public class LCPacketVoiceChannelSwitch extends LCPacket {

    private UUID switchingTo;

    public LCPacketVoiceChannelSwitch() {
    }

    public LCPacketVoiceChannelSwitch(UUID switchingTo) {
        this.switchingTo = switchingTo;
    }

    @Override
    public void write(ByteBufWrapper b) throws IOException {
        b.writeUUID(switchingTo);
    }

    @Override
    public void read(ByteBufWrapper b) throws IOException {
        switchingTo = b.readUUID();
    }

    @Override
    public void process(LCNetHandler handler) {
        ((LCNetHandlerServer) handler).handleVoiceChannelSwitch(this);
    }

    public UUID getSwitchingTo() {
        return switchingTo;
    }
}
