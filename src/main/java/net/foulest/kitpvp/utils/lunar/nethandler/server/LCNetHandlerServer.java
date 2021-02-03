package net.foulest.kitpvp.utils.lunar.nethandler.server;

import net.foulest.kitpvp.utils.lunar.nethandler.client.LCPacketClientVoice;
import net.foulest.kitpvp.utils.lunar.nethandler.client.LCPacketVoiceChannelSwitch;
import net.foulest.kitpvp.utils.lunar.nethandler.client.LCPacketVoiceMute;
import net.foulest.kitpvp.utils.lunar.nethandler.shared.LCNetHandler;

public interface LCNetHandlerServer extends LCNetHandler {

    void handleVoice(LCPacketClientVoice p0);

    void handleVoiceMute(LCPacketVoiceMute p0);

    void handleVoiceChannelSwitch(LCPacketVoiceChannelSwitch p0);
}
