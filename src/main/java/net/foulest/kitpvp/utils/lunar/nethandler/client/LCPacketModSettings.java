package net.foulest.kitpvp.utils.lunar.nethandler.client;


import net.foulest.kitpvp.utils.lunar.nethandler.ByteBufWrapper;
import net.foulest.kitpvp.utils.lunar.nethandler.LCPacket;
import net.foulest.kitpvp.utils.lunar.nethandler.obj.ModSettings;
import net.foulest.kitpvp.utils.lunar.nethandler.shared.LCNetHandler;

import java.io.IOException;

public class LCPacketModSettings extends LCPacket {

    private ModSettings settings;

    public LCPacketModSettings() {
    }

    public LCPacketModSettings(ModSettings modSettings) {
        settings = modSettings;
    }

    @Override
    public void write(ByteBufWrapper buf) throws IOException {
        buf.writeString(ModSettings.GSON.toJson(settings));
    }

    @Override
    public void read(ByteBufWrapper buf) throws IOException {
        settings = ModSettings.GSON.fromJson(buf.readString(), ModSettings.class);
    }

    @Override
    public void process(LCNetHandler handler) {
        ((LCNetHandlerClient) handler).handleModSettings(this);
    }

    public ModSettings getSettings() {
        return settings;
    }
}
