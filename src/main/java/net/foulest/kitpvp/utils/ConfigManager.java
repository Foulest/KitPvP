package net.foulest.kitpvp.utils;

import net.foulest.kitpvp.KitPvP;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
@SuppressWarnings({"ResultOfMethodCallIgnored"})
public final class ConfigManager {

    private static final KitPvP KITPVP = KitPvP.getInstance();
    private static File file;
    private static FileConfiguration config;

    private ConfigManager() {
    }

    public static void setup() {
        file = new File(Bukkit.getServer().getPluginManager().getPlugin(KITPVP.getName()).getDataFolder(), "settings.yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ignored) {
                MessageUtil.log("&c[KitPvP] Couldn't create the config file.");
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    public static FileConfiguration get() {
        return config;
    }

    public static void save() {
        try {
            config.save(file);
        } catch (IOException exception) {
            MessageUtil.log("&c[KitPvP] Couldn't save the config file.");
        }
    }

    public static void reload() {
        config = YamlConfiguration.loadConfiguration(file);
    }
}
