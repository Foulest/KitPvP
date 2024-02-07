package net.foulest.kitpvp.util;

import lombok.Getter;
import lombok.Setter;
import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.util.yaml.CustomYamlConfiguration;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

/**
 * Utility class for settings.
 *
 * @author Foulest
 * @project KitPvP
 */
@Getter
@Setter
public class Settings {

    // File settings
    public static File file;
    public static FileConfiguration config;
    public static String fileName = "config.yml";

    // Spawn settings
    public static String spawnWorld;
    public static double spawnX;
    public static double spawnY;
    public static double spawnZ;
    public static float spawnYaw;
    public static float spawnPitch;

    // Combat tag settings
    public static boolean combatTagEnabled;
    public static int combatTagDuration;
    public static boolean combatTagCancelTeleport;
    public static boolean combatTagPunishLogout;
    public static boolean combatTagDenyEnteringSpawn;

    // Economy settings
    public static int startingCoins;
    public static int coinsOnKill;
    public static int expOnKill;

    // Killstreak settings
    public static boolean killStreaksEnabled;
    public static int killStreaksCoinsBonus;
    public static int killStreaksExpBonus;

    // Kit enchanter settings
    public static boolean kitEnchanterEnabled;
    public static boolean featherFallingEnabled;
    public static int featherFallingCost;
    public static boolean thornsEnabled;
    public static int thornsCost;
    public static boolean protectionEnabled;
    public static int protectionCost;
    public static boolean knockbackEnabled;
    public static int knockbackCost;
    public static boolean sharpnessEnabled;
    public static int sharpnessCost;
    public static boolean punchEnabled;
    public static int punchCost;
    public static boolean powerEnabled;
    public static int powerCost;

    // Kit enabled settings
    public static boolean archerKitEnabled;
    public static boolean burrowerKitEnabled;
    public static boolean cactusKitEnabled;
    public static boolean dragonKitEnabled;
    public static boolean fishermanKitEnabled;
    public static boolean ghostKitEnabled;
    public static boolean hulkKitEnabled;
    public static boolean imprisonerKitEnabled;
    public static boolean kangarooKitEnabled;
    public static boolean knightKitEnabled;
    public static boolean mageKitEnabled;
    public static boolean monkKitEnabled;
    public static boolean ninjaKitEnabled;
    public static boolean pyroKitEnabled;
    public static boolean spidermanKitEnabled;
    public static boolean summonerKitEnabled;
    public static boolean tamerKitEnabled;
    public static boolean tankKitEnabled;
    public static boolean thorKitEnabled;
    public static boolean timelordKitEnabled;
    public static boolean vampireKitEnabled;
    public static boolean zenKitEnabled;

    // Kit cost settings
    public static int archerKitCost;
    public static int burrowerKitCost;
    public static int cactusKitCost;
    public static int dragonKitCost;
    public static int fishermanKitCost;
    public static int ghostKitCost;
    public static int hulkKitCost;
    public static int imprisonerKitCost;
    public static int kangarooKitCost;
    public static int knightKitCost;
    public static int mageKitCost;
    public static int monkKitCost;
    public static int ninjaKitCost;
    public static int pyroKitCost;
    public static int spidermanKitCost;
    public static int summonerKitCost;
    public static int tamerKitCost;
    public static int tankKitCost;
    public static int thorKitCost;
    public static int timelordKitCost;
    public static int vampireKitCost;
    public static int zenKitCost;

    // Database settings
    public static String host;
    public static int port;
    public static String database;
    public static String user;
    public static String password;

    /**
     * Loads the configuration file and values.
     */
    public static void loadSettings() {
        loadConfigFile();
        loadConfigValues();
    }

    /**
     * Initializes the configuration file and loads defaults.
     */
    private static void loadConfigFile() {
        // First, attempt to load the default configuration as a stream to check if it exists in the plugin JAR
        InputStream defConfigStream = KitPvP.getInstance().getResource(fileName);

        if (defConfigStream == null) {
            // Log a warning if the default configuration cannot be found within the JAR
            MessageUtil.log(Level.WARNING, "Could not find " + fileName + " in the plugin JAR.");
            return;
        }

        // Proceed to check if the config file exists in the plugin's data folder
        // and save the default config from the JAR if not
        File dataFolder = KitPvP.getInstance().getDataFolder();
        file = new File(dataFolder, fileName);
        if (!file.exists()) {
            KitPvP.getInstance().saveResource(fileName, false);
        }

        // Now that we've ensured the file exists (either it already did, or we've just created it),
        // we can safely load it into our CustomYamlConfiguration object
        config = CustomYamlConfiguration.loadConfiguration(file);
        CustomYamlConfiguration defConfig = CustomYamlConfiguration
                .loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8));

        // Ensure defaults are applied
        config.setDefaults(defConfig);
        config.options().copyDefaults(true);
        saveConfig(); // Save the config with defaults applied

        // Close the defConfigStream properly to avoid resource leaks
        try {
            defConfigStream.close();
        } catch (IOException ex) {
            MessageUtil.printException(ex);
        }
    }

    /**
     * Saves the configuration file.
     */
    public static void saveConfig() {
        try {
            config.save(file);
        } catch (IOException ex) {
            MessageUtil.printException(ex);
        }
    }

    /**
     * Loads configuration values into the relevant static fields.
     */
    public static void loadConfigValues() {
        // Spawn settings
        spawnWorld = config.getString("kitpvp.spawn.world");
        spawnX = config.getDouble("kitpvp.spawn.x");
        spawnY = config.getDouble("kitpvp.spawn.y");
        spawnZ = config.getDouble("kitpvp.spawn.z");
        spawnYaw = (float) config.getDouble("kitpvp.spawn.yaw");
        spawnPitch = (float) config.getDouble("kitpvp.spawn.pitch");

        // Combat tag settings
        combatTagEnabled = config.getBoolean("kitpvp.combat-tag.enabled");
        combatTagDuration = config.getInt("kitpvp.combat-tag.duration");
        combatTagCancelTeleport = config.getBoolean("kitpvp.combat-tag.cancel-teleport");
        combatTagPunishLogout = config.getBoolean("kitpvp.combat-tag.punish-logout");
        combatTagDenyEnteringSpawn = config.getBoolean("kitpvp.combat-tag.deny-entering-spawn");

        // Economy settings
        startingCoins = config.getInt("kitpvp.economy.starting-coins");
        coinsOnKill = config.getInt("kitpvp.economy.coins-on-kill");
        expOnKill = config.getInt("kitpvp.economy.exp-on-kill");

        // Killstreak settings
        killStreaksEnabled = config.getBoolean("kitpvp.killstreaks.enabled");
        killStreaksCoinsBonus = config.getInt("kitpvp.killstreaks.coins-bonus");
        killStreaksExpBonus = config.getInt("kitpvp.killstreaks.exp-bonus");

        // Kit enchanter settings


        // Kit enabled settings
        archerKitEnabled = config.getBoolean("kits.archer.enabled");
        burrowerKitEnabled = config.getBoolean("kits.burrower.enabled");
        cactusKitEnabled = config.getBoolean("kits.cactus.enabled");
        dragonKitEnabled = config.getBoolean("kits.dragon.enabled");
        fishermanKitEnabled = config.getBoolean("kits.fisherman.enabled");
        ghostKitEnabled = config.getBoolean("kits.ghost.enabled");
        hulkKitEnabled = config.getBoolean("kits.hulk.enabled");
        imprisonerKitEnabled = config.getBoolean("kits.imprisoner.enabled");
        kangarooKitEnabled = config.getBoolean("kits.kangaroo.enabled");
        knightKitEnabled = config.getBoolean("kits.knight.enabled");
        mageKitEnabled = config.getBoolean("kits.mage.enabled");
        monkKitEnabled = config.getBoolean("kits.monk.enabled");
        ninjaKitEnabled = config.getBoolean("kits.ninja.enabled");
        pyroKitEnabled = config.getBoolean("kits.pyro.enabled");
        spidermanKitEnabled = config.getBoolean("kits.spiderman.enabled");
        summonerKitEnabled = config.getBoolean("kits.summoner.enabled");
        tamerKitEnabled = config.getBoolean("kits.tamer.enabled");
        tankKitEnabled = config.getBoolean("kits.tank.enabled");
        thorKitEnabled = config.getBoolean("kits.thor.enabled");
        timelordKitEnabled = config.getBoolean("kits.timelord.enabled");
        vampireKitEnabled = config.getBoolean("kits.vampire.enabled");
        zenKitEnabled = config.getBoolean("kits.zen.enabled");

        // Kit cost settings
        archerKitCost = config.getInt("kits.archer.cost");
        burrowerKitCost = config.getInt("kits.burrower.cost");
        cactusKitCost = config.getInt("kits.cactus.cost");
        dragonKitCost = config.getInt("kits.dragon.cost");
        fishermanKitCost = config.getInt("kits.fisherman.cost");
        ghostKitCost = config.getInt("kits.ghost.cost");
        hulkKitCost = config.getInt("kits.hulk.cost");
        imprisonerKitCost = config.getInt("kits.imprisoner.cost");
        kangarooKitCost = config.getInt("kits.kangaroo.cost");
        knightKitCost = config.getInt("kits.knight.cost");
        mageKitCost = config.getInt("kits.mage.cost");
        monkKitCost = config.getInt("kits.monk.cost");
        ninjaKitCost = config.getInt("kits.ninja.cost");
        pyroKitCost = config.getInt("kits.pyro.cost");
        spidermanKitCost = config.getInt("kits.spiderman.cost");
        summonerKitCost = config.getInt("kits.summoner.cost");
        tamerKitCost = config.getInt("kits.tamer.cost");
        tankKitCost = config.getInt("kits.tank.cost");
        thorKitCost = config.getInt("kits.thor.cost");
        timelordKitCost = config.getInt("kits.timelord.cost");
        vampireKitCost = config.getInt("kits.vampire.cost");
        zenKitCost = config.getInt("kits.zen.cost");

        // Database settings
        host = config.getString("storage.host");
        port = config.getInt("storage.port");
        database = config.getString("storage.database");
        user = config.getString("storage.user");
        password = config.getString("storage.password");
    }

    /**
     * Sets the default values for the configuration file.
     */
    private static void setDefaultConfigValues() {
        config.addDefault("kits.archer.enabled", true);
        config.addDefault("kits.archer.premium-only", false);
        config.addDefault("kits.archer.cost", 250);

        config.addDefault("kits.burrower.enabled", true);
        config.addDefault("kits.burrower.premium-only", false);
        config.addDefault("kits.burrower.cost", 250);

        config.addDefault("kits.cactus.enabled", true);
        config.addDefault("kits.cactus.premium-only", false);
        config.addDefault("kits.cactus.cost", 250);

        config.addDefault("kits.dragon.enabled", true);
        config.addDefault("kits.dragon.premium-only", false);
        config.addDefault("kits.dragon.cost", 250);

        config.addDefault("kits.fisherman.enabled", true);
        config.addDefault("kits.fisherman.premium-only", false);
        config.addDefault("kits.fisherman.cost", 250);

        config.addDefault("kits.ghost.enabled", true);
        config.addDefault("kits.ghost.premium-only", false);
        config.addDefault("kits.ghost.cost", 250);

        config.addDefault("kits.hulk.enabled", true);
        config.addDefault("kits.hulk.premium-only", false);
        config.addDefault("kits.hulk.cost", 250);

        config.addDefault("kits.imprisoner.enabled", true);
        config.addDefault("kits.imprisoner.premium-only", false);
        config.addDefault("kits.imprisoner.cost", 250);

        config.addDefault("kits.kangaroo.enabled", true);
        config.addDefault("kits.kangaroo.premium-only", false);
        config.addDefault("kits.kangaroo.cost", 250);

        config.addDefault("kits.knight.enabled", true);
        config.addDefault("kits.knight.premium-only", false);
        config.addDefault("kits.knight.cost", 0);

        config.addDefault("kits.mage.enabled", true);
        config.addDefault("kits.mage.premium-only", false);
        config.addDefault("kits.mage.cost", 250);

        config.addDefault("kits.monk.enabled", true);
        config.addDefault("kits.monk.premium-only", false);
        config.addDefault("kits.monk.cost", 250);

        config.addDefault("kits.ninja.enabled", true);
        config.addDefault("kits.ninja.premium-only", false);
        config.addDefault("kits.ninja.cost", 250);

        config.addDefault("kits.pyro.enabled", true);
        config.addDefault("kits.pyro.premium-only", false);
        config.addDefault("kits.pyro.cost", 250);

        config.addDefault("kits.spiderman.enabled", true);
        config.addDefault("kits.spiderman.premium-only", false);
        config.addDefault("kits.spiderman.cost", 250);

        config.addDefault("kits.summoner.enabled", true);
        config.addDefault("kits.summoner.premium-only", false);
        config.addDefault("kits.summoner.cost", 250);

        config.addDefault("kits.tamer.enabled", true);
        config.addDefault("kits.tamer.premium-only", false);
        config.addDefault("kits.tamer.cost", 250);

        config.addDefault("kits.tank.enabled", true);
        config.addDefault("kits.tank.premium-only", false);
        config.addDefault("kits.tank.cost", 250);

        config.addDefault("kits.thor.enabled", true);
        config.addDefault("kits.thor.premium-only", false);
        config.addDefault("kits.thor.cost", 250);

        config.addDefault("kits.timelord.enabled", true);
        config.addDefault("kits.timelord.premium-only", false);
        config.addDefault("kits.timelord.cost", 250);

        config.addDefault("kits.vampire.enabled", true);
        config.addDefault("kits.vampire.premium-only", false);
        config.addDefault("kits.vampire.cost", 250);

        config.addDefault("kits.zen.enabled", true);
        config.addDefault("kits.zen.premium-only", false);
        config.addDefault("kits.zen.cost", 250);

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
    }
}
