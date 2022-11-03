package net.foulest.kitpvp.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

/**
 * @author Foulest
 * @project KitPvP
 */
public final class MessageUtil {

    public static void messagePlayer(CommandSender sender, String message) {
        sender.sendMessage(colorize(message));
    }

    public static void log(Level level, String message) {
        Bukkit.getLogger().log(level, "[KitPvP] " + message);
    }

    public static void broadcast(String message) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            messagePlayer(online, message);
        }
    }

    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
