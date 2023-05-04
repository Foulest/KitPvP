package net.foulest.kitpvp.util;

import net.foulest.kitpvp.KitPvP;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class Settings {

    public static File file;
    public static FileConfiguration config;
    public static String spawnWorld;
    public static double spawnX;
    public static double spawnY;
    public static double spawnZ;
    public static float spawnYaw;
    public static float spawnPitch;
    public static int killCoinBonus;
    public static int killExpBonus;
    public static int killStreakBonus;
    public static boolean premiumEnabled;
    public static String premiumRankName;
    public static String premiumPermission;
    public static String premiumStoreLink;
    public static int nonPremiumKitLimit;
    public static boolean bountiesPremiumOnly;
    public static String host;
    public static String user;
    public static String password;
    public static String database;
    public static int port;

    public static void setupSettings() {
        file = new File(KitPvP.instance.getDataFolder(), "settings.yml");

        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException ex) {
                MessageUtil.log(Level.WARNING, "Couldn't create the config file.");
                ex.printStackTrace();
                return;
            }
        }

        config = YamlConfiguration.loadConfiguration(file);

        config.addDefault("spawn.world", "world");
        config.addDefault("spawn.x", 0.5);
        config.addDefault("spawn.y", 64.0);
        config.addDefault("spawn.z", 0.5);
        config.addDefault("spawn.yaw", 90.0);
        config.addDefault("spawn.pitch", 0.0);
        config.addDefault("kill.coin-bonus", 10);
        config.addDefault("kill.exp-bonus", 15);
        config.addDefault("kill.streak-bonus", 5);
        config.addDefault("premium.enabled", true);
        config.addDefault("premium.rank-name", "Premium");
        config.addDefault("premium.permission", "kitpvp.premium");
        config.addDefault("premium.store-link", "store.kitpvp.io");
        config.addDefault("premium.non-premium-kit-limit", 10);
        config.addDefault("premium.bounties-premium-only", true);
        config.addDefault("storage.host", "host");
        config.addDefault("storage.user", "user");
        config.addDefault("storage.password", "password");
        config.addDefault("storage.database", "database");
        config.addDefault("storage.port", 3306);
        config.options().copyDefaults(true);

        try {
            config.save(file);
        } catch (IOException ex) {
            MessageUtil.log(Level.WARNING, "Couldn't save the config file.");
        }
    }

    public static void loadSettings() {
        config = YamlConfiguration.loadConfiguration(file);

        spawnWorld = config.getString("spawn.world");
        spawnX = config.getDouble("spawn.x");
        spawnY = config.getDouble("spawn.y");
        spawnZ = config.getDouble("spawn.z");
        spawnYaw = (float) config.getDouble("spawn.yaw");
        spawnPitch = (float) config.getDouble("spawn.pitch");

        killCoinBonus = config.getInt("kill.coin-bonus");
        killExpBonus = config.getInt("kill.exp-bonus");
        killStreakBonus = config.getInt("kill.streak-bonus");

        premiumEnabled = config.getBoolean("premium.enabled");
        premiumRankName = config.getString("premium.rank-name");
        premiumPermission = config.getString("premium.permission");
        premiumStoreLink = config.getString("premium.store-link");
        nonPremiumKitLimit = config.getInt("premium.non-premium-kit-limit");
        bountiesPremiumOnly = config.getBoolean("premium.bounties-premium-only");

        host = config.getString("storage.host");
        user = config.getString("storage.user");
        password = config.getString("storage.password");
        database = config.getString("storage.database");
        port = config.getInt("storage.port");
    }

    public static void saveSettings() {
        config.set("spawn.world", spawnWorld);
        config.set("spawn.x", spawnX);
        config.set("spawn.y", spawnY);
        config.set("spawn.z", spawnZ);
        config.set("spawn.yaw", spawnYaw);
        config.set("spawn.pitch", spawnPitch);

        config.set("kill.coin-bonus", killCoinBonus);
        config.set("kill.exp-bonus", killExpBonus);
        config.set("kill.streak-bonus", killStreakBonus);

        config.set("premium.enabled", premiumEnabled);
        config.set("premium.rank-name", premiumRankName);
        config.set("premium.permission", premiumPermission);
        config.set("premium.store-link", premiumStoreLink);
        config.set("premium.non-premium-kit-limit", nonPremiumKitLimit);
        config.set("premium.bounties-premium-only", bountiesPremiumOnly);

        config.set("storage.host", host);
        config.set("storage.user", user);
        config.set("storage.password", password);
        config.set("storage.database", database);
        config.set("storage.port", port);

        try {
            config.save(file);
        } catch (IOException ex) {
            MessageUtil.log(Level.WARNING, "Couldn't save the config file.");
        }
    }
}
