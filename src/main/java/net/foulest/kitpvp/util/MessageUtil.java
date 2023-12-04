package net.foulest.kitpvp.util;

import lombok.NonNull;
import net.foulest.kitpvp.KitPvP;
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

    public static void messagePlayer(@NonNull CommandSender sender, @NonNull String message) {
        sender.sendMessage(colorize(message));
    }

    public static void log(@NonNull Level level, @NonNull String message) {
        Bukkit.getLogger().log(level, "[" + KitPvP.pluginName + "] " + message);
    }

    public static void broadcast(@NonNull String message) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            messagePlayer(online, message);
        }

        messagePlayer(Bukkit.getConsoleSender(), message);
    }

    public static void broadcastWithPerm(@NonNull String message, @NonNull String permission) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission(permission)) {
                messagePlayer(online, message);
            }
        }

        messagePlayer(Bukkit.getConsoleSender(), message);
    }

    public static String colorize(@NonNull String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String stripColor(@NonNull String message) {
        return ChatColor.stripColor(message);
    }
}
