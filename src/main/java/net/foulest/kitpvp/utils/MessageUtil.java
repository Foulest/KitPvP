package net.foulest.kitpvp.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Random;

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
public final class MessageUtil {

    public static final Random RANDOM = new Random();

    private MessageUtil() {
    }

    public static void messagePlayer(CommandSender sender, String message) {
        sender.sendMessage(colorize(message));
    }

    public static void log(String message) {
        Bukkit.getConsoleSender().sendMessage(colorize(message));
    }

    public static void broadcastMessage(String message) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.sendMessage(colorize(message));
        }
    }

    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
