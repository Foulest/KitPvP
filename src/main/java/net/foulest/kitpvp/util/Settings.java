package net.foulest.kitpvp.util;

import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.koth.KOTH;
import net.foulest.kitpvp.util.scoreboard.Scoreboard;
import net.foulest.kitpvp.util.scoreboard.ScoreboardUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
    public static String storageType;
    public static File storageFile;
    public static String storageFileName;
    public static String host;
    public static String user;
    public static String password;
    public static String database;
    public static int port;
    public static boolean usingMariaDB;
    public static boolean usingSQLite;
    public static boolean scoreboardsEnabled;
    public static Scoreboard defaultScoreboard;
    public static Scoreboard kothScoreboard;

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
        config.addDefault("storage.type", "sqlite");
        config.addDefault("storage.sqlite.file", "sqlite.db");
        config.addDefault("storage.mariadb.host", "host");
        config.addDefault("storage.mariadb.user", "user");
        config.addDefault("storage.mariadb.password", "password");
        config.addDefault("storage.mariadb.database", "database");
        config.addDefault("storage.mariadb.port", 3306);
        config.addDefault("scoreboards", Collections.<String>emptyList());
        config.addDefault("scoreboards.enabled", true);
        config.addDefault("scoreboards.default-scoreboard", "default");
        config.addDefault("scoreboards.koth-scoreboard", "koth");
        config.addDefault("scoreboards.default.title", "&e&lKitPvP");
        config.addDefault("scoreboards.default.lines", new ArrayList<String>());
        config.addDefault("scoreboards.koth.title", "&e&lKitPvP");
        config.addDefault("scoreboards.koth.lines", new ArrayList<String>());
        config.addDefault("koth", Collections.<String>emptyList());
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

        storageType = config.getString("storage.type");
        storageFileName = config.getString("storage.sqlite.file");
        storageFile = new File(KitPvP.instance.getDataFolder() + File.separator + config.getString("storage.sqlite.file"));
        host = config.getString("storage.mariadb.host");
        user = config.getString("storage.mariadb.user");
        password = config.getString("storage.mariadb.password");
        database = config.getString("storage.mariadb.database");
        port = config.getInt("storage.mariadb.port");

        scoreboardsEnabled = config.getBoolean("scoreboards.enabled");
        defaultScoreboard = ScoreboardUtil.getScoreboard(config.getString("scoreboards.default-scoreboard"));
        kothScoreboard = ScoreboardUtil.getScoreboard(config.getString("scoreboards.koth-scoreboard"));

        switch (storageType.toLowerCase()) {
            case "mariadb":
                usingMariaDB = true;
                usingSQLite = false;
                break;

            case "sqlite":
                usingMariaDB = false;
                usingSQLite = true;
                break;
        }

        KOTH.loadKoths();
        ScoreboardUtil.loadScoreboards();
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

        config.set("storage.type", storageType);
        config.set("storage.sqlite.file", storageFileName);
        config.set("storage.mariadb.host", host);
        config.set("storage.mariadb.user", user);
        config.set("storage.mariadb.password", password);
        config.set("storage.mariadb.database", database);
        config.set("storage.mariadb.port", port);

        config.set("scoreboards.enabled", scoreboardsEnabled);

        if (defaultScoreboard != null) {
            config.set("scoreboards.default-scoreboard", defaultScoreboard.getName());
        }

        if (kothScoreboard != null) {
            config.set("scoreboards.koth-scoreboard", kothScoreboard.getName());
        }

        try {
            config.save(file);
        } catch (IOException ex) {
            MessageUtil.log(Level.WARNING, "Couldn't save the config file.");
        }
    }
}
