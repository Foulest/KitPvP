package net.foulest.kitpvp.utils.lunar.nethandler.client;

import net.foulest.kitpvp.utils.lunar.nethandler.server.LCPacketVoice;
import net.foulest.kitpvp.utils.lunar.nethandler.server.LCPacketVoiceChannel;
import net.foulest.kitpvp.utils.lunar.nethandler.server.LCPacketVoiceChannelRemove;
import net.foulest.kitpvp.utils.lunar.nethandler.server.LCPacketVoiceChannelUpdate;
import net.foulest.kitpvp.utils.lunar.nethandler.shared.LCNetHandler;

public interface LCNetHandlerClient extends LCNetHandler {

    void handleCooldown(LCPacketCooldown p0);

    void handleGhost(LCPacketGhost p0);

    void handleAddHologram(LCPacketHologram p0);

    void handleRemoveHologram(LCPacketHologramRemove p0);

    void handleUpdateHologram(LCPacketHologramUpdate p0);

    void handleNotification(LCPacketNotification p0);

    void handleServerRule(LCPacketServerRule p0);

    void handleServerUpdate(LCPacketServerUpdate p0);

    void handleTeammates(LCPacketTeammates p0);

    void handleUpdateWorld(LCPacketUpdateWorld p0);

    void handleVoice(LCPacketVoice p0);

    void handleVoiceChannels(LCPacketVoiceChannel p0);

    void handleVoiceChannelUpdate(LCPacketVoiceChannelUpdate p0);

    void handleVoiceChannelDelete(LCPacketVoiceChannelRemove p0);

    void handleModSettings(LCPacketModSettings p0);
}
