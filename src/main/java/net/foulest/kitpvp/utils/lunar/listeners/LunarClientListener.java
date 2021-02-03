package net.foulest.kitpvp.utils.lunar.listeners;

import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.utils.lunar.LunarClientAPI;
import net.foulest.kitpvp.utils.lunar.events.LCPlayerRegisterEvent;
import net.foulest.kitpvp.utils.lunar.events.LCPlayerUnregisterEvent;
import net.foulest.kitpvp.utils.lunar.nethandler.client.LCPacketUpdateWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

public class LunarClientListener implements Listener {

    private final KitPvP kitPvP = KitPvP.getInstance();
    private final LunarClientAPI lunarAPI = LunarClientAPI.getInstance();

    @EventHandler
    public void onRegister(PlayerRegisterChannelEvent event) {
        if (!event.getChannel().equals(LunarClientAPI.getMessageChannel())) {
            return;
        }

        lunarAPI.playersNotRegistered.remove(event.getPlayer().getUniqueId());
        lunarAPI.playersRunningLunarClient.add(event.getPlayer().getUniqueId());

        if (lunarAPI.packetQueue.containsKey(event.getPlayer().getUniqueId())) {
            lunarAPI.packetQueue.get(event.getPlayer().getUniqueId()).forEach(p -> lunarAPI.sendPacket(event.getPlayer(), p));
            lunarAPI.packetQueue.remove(event.getPlayer().getUniqueId());
        }

        kitPvP.getServer().getPluginManager().callEvent(new LCPlayerRegisterEvent(event.getPlayer()));
        updateWorld(event.getPlayer());
    }

    @EventHandler
    public void onUnregister(PlayerUnregisterChannelEvent event) {
        if (event.getChannel().equals(LunarClientAPI.getMessageChannel())) {
            lunarAPI.playersRunningLunarClient.remove(event.getPlayer().getUniqueId());
            kitPvP.getServer().getPluginManager().callEvent(new LCPlayerUnregisterEvent(event.getPlayer()));
        }
    }

    @EventHandler
    public void onUnregister(PlayerQuitEvent event) {
        lunarAPI.playersRunningLunarClient.remove(event.getPlayer().getUniqueId());
        lunarAPI.playersNotRegistered.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        new BukkitRunnable() {
            public void run() {
                if (!lunarAPI.isRunningLunarClient(event.getPlayer())) {
                    lunarAPI.playersNotRegistered.add(event.getPlayer().getUniqueId());
                    lunarAPI.packetQueue.remove(event.getPlayer().getUniqueId());
                }
            }
        }.runTaskLater(kitPvP, 40L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        updateWorld(event.getPlayer());
    }

    private void updateWorld(Player player) {
        String worldIdentifier = lunarAPI.getWorldIdentifier(player.getWorld());
        lunarAPI.sendPacket(player, new LCPacketUpdateWorld(worldIdentifier));
    }
}
