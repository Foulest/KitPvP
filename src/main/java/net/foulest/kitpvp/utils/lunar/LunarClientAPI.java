package net.foulest.kitpvp.utils.lunar;

import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.utils.lunar.events.LCPacketSentEvent;
import net.foulest.kitpvp.utils.lunar.nethandler.LCPacket;
import net.foulest.kitpvp.utils.lunar.nethandler.client.*;
import net.foulest.kitpvp.utils.lunar.nethandler.server.LCNetHandlerServer;
import net.foulest.kitpvp.utils.lunar.nethandler.shared.LCPacketEmoteBroadcast;
import net.foulest.kitpvp.utils.lunar.nethandler.shared.LCPacketWaypointAdd;
import net.foulest.kitpvp.utils.lunar.nethandler.shared.LCPacketWaypointRemove;
import net.foulest.kitpvp.utils.lunar.objects.LCCooldown;
import net.foulest.kitpvp.utils.lunar.objects.LCGhost;
import net.foulest.kitpvp.utils.lunar.objects.LCNotification;
import net.foulest.kitpvp.utils.lunar.objects.LCWaypoint;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class LunarClientAPI implements Listener {

    private static String messageChannel;
    private static LunarClientAPI instance;

    static {
        LunarClientAPI.messageChannel = null;
    }

    public final Set<UUID> playersRunningLunarClient;
    public final Set<UUID> playersNotRegistered;
    public final Map<UUID, List<LCPacket>> packetQueue;
    public final Map<UUID, Function<World, String>> worldIdentifiers;
    private final KitPvP kitPvP = KitPvP.getInstance();
    public LCNetHandlerServer netHandlerServer;

    public LunarClientAPI() {
        instance = this;
        playersRunningLunarClient = Collections.newSetFromMap(new ConcurrentHashMap<>());
        playersNotRegistered = new HashSet<>();
        packetQueue = new HashMap<>();
        worldIdentifiers = new HashMap<>();
        netHandlerServer = new LCNetHandlerServer() {
            @Override
            public void handleVoice(LCPacketClientVoice lcPacketClientVoice) {
            }

            @Override
            public void handleVoiceMute(LCPacketVoiceMute lcPacketVoiceMute) {
            }

            @Override
            public void handleVoiceChannelSwitch(LCPacketVoiceChannelSwitch lcPacketVoiceChannelSwitch) {
            }

            @Override
            public void handleAddWaypoint(LCPacketWaypointAdd lcPacketWaypointAdd) {
            }

            @Override
            public void handleRemoveWaypoint(LCPacketWaypointRemove lcPacketWaypointRemove) {
            }

            @Override
            public void handleEmote(LCPacketEmoteBroadcast lcPacketEmoteBroadcast) {
            }
        };
    }

    public static LunarClientAPI getInstance() {
        return instance;
    }

    public static String getMessageChannel() {
        if (LunarClientAPI.messageChannel == null) {
            LunarClientAPI.messageChannel = "Lunar-Client";
        }

        return LunarClientAPI.messageChannel;
    }

    public boolean isRunningLunarClient(Player player) {
        return isRunningLunarClient(player.getUniqueId());
    }

    public boolean isRunningLunarClient(UUID playerUuid) {
        return playersRunningLunarClient.contains(playerUuid);
    }

    public void sendNotification(Player player, LCNotification notification) {
        sendPacket(player, new LCPacketNotification(notification.getMessage(), notification.getDurationMs(), notification.getLevel().name()));
    }

    public String getWorldIdentifier(World world) {
        String worldIdentifier = world.getUID().toString();
        if (worldIdentifiers.containsKey(world.getUID())) {
            worldIdentifier = worldIdentifiers.get(world.getUID()).apply(world);
        }
        return worldIdentifier;
    }

    public void registerWorldIdentifier(World world, Function<World, String> identifier) {
        worldIdentifiers.put(world.getUID(), identifier);
    }

    public void sendNotificationOrFallback(Player player, LCNotification notification, Runnable fallback) {
        if (isRunningLunarClient(player)) {
            sendNotification(player, notification);
        } else {
            fallback.run();
        }
    }

    public void sendTeammates(Player player, LCPacketTeammates packet) {
        validatePlayers(player, packet);
        sendPacket(player, packet);
    }

    public void validatePlayers(Player sendingTo, LCPacketTeammates packet) {
        packet.getPlayers().entrySet().removeIf(entry -> Bukkit.getPlayer(entry.getKey()) != null
                && !Bukkit.getPlayer(entry.getKey()).getWorld().equals(sendingTo.getWorld()));
    }

    public void addHologram(Player player, UUID id, Vector position, String[] lines) {
        sendPacket(player, new LCPacketHologram(id, position.getX(), position.getY(), position.getZ(), Arrays.asList(lines)));
    }

    public void updateHologram(Player player, UUID id, String[] lines) {
        sendPacket(player, new LCPacketHologramUpdate(id, Arrays.asList(lines)));
    }

    public void removeHologram(Player player, UUID id) {
        sendPacket(player, new LCPacketHologramRemove(id));
    }

    public void sendWaypoint(Player player, LCWaypoint waypoint) {
        sendPacket(player, new LCPacketWaypointAdd(waypoint.getName(), waypoint.getWorld(), waypoint.getColor(),
                waypoint.getX(), waypoint.getY(), waypoint.getZ(), waypoint.isForced(), waypoint.isVisible()));
    }

    public void removeWaypoint(Player player, LCWaypoint waypoint) {
        sendPacket(player, new LCPacketWaypointRemove(waypoint.getName(), waypoint.getWorld()));
    }

    public void sendCooldown(Player player, LCCooldown cooldown) {
        sendPacket(player, new LCPacketCooldown(cooldown.getMessage(), cooldown.getDurationMs(),
                cooldown.getIcon().getId()));
    }

    public void sendGhost(Player player, LCGhost ghost) {
        sendPacket(player, new LCPacketGhost(ghost.getGhostedPlayers(), ghost.getUnGhostedPlayers()));
    }

    public void clearCooldown(Player player, LCCooldown cooldown) {
        sendPacket(player, new LCPacketCooldown(cooldown.getMessage(), 0L, cooldown.getIcon().getId()));
    }

    public void sendPacket(Player player, LCPacket packet) {
        if (isRunningLunarClient(player)) {
            player.sendPluginMessage(kitPvP, getMessageChannel(), LCPacket.getPacketData(packet));
            Bukkit.getPluginManager().callEvent(new LCPacketSentEvent(player, packet));
            return;
        }

        if (!playersNotRegistered.contains(player.getUniqueId())) {
            packetQueue.putIfAbsent(player.getUniqueId(), new ArrayList<>());
            packetQueue.get(player.getUniqueId()).add(packet);
        }
    }

    public void setNetHandlerServer(LCNetHandlerServer netHandlerServer) {
        this.netHandlerServer = netHandlerServer;
    }
}
