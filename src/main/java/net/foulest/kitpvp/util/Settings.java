package net.foulest.kitpvp.util;

import lombok.Cleanup;
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

    // Bounties settings
    public static boolean bountiesEnabled;
    public static int bountiesCooldown;
    public static int bountiesMaxAmount;

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

    // Knight kit settings
    public static boolean knightKitEnabled;
    public static int knightKitCost;

    // Archer kit settings
    public static boolean archerKitEnabled;
    public static int archerKitCost;

    // Burrower kit settings
    public static boolean burrowerKitEnabled;
    public static int burrowerKitCost;
    public static int burrowerKitCooldown;
    public static int burrowerKitDuration;

    // Cactus kit settings
    public static boolean cactusKitEnabled;
    public static int cactusKitCost;
    public static int cactusKitPassiveDuration;

    // Dragon kit settings
    public static boolean dragonKitEnabled;
    public static int dragonKitCost;
    public static int dragonKitCooldown;
    public static int dragonKitDuration;
    public static int dragonKitRange;
    public static int dragonKitDamage;

    // Fisherman kit settings
    public static boolean fishermanKitEnabled;
    public static int fishermanKitCost;
    public static int fishermanKitCooldown;

    // Ghost kit settings
    public static boolean ghostKitEnabled;
    public static int ghostKitCost;
    public static int ghostKitRange;

    // Hulk kit settings
    public static boolean hulkKitEnabled;
    public static int hulkKitCost;
    public static int hulkKitCooldown;
    public static int hulkKitRange;
    public static int hulkKitDamage;
    public static double hulkKitMultiplier;

    // Imprisoner kit settings
    public static boolean imprisonerKitEnabled;
    public static int imprisonerKitCost;
    public static int imprisonerKitCooldown;
    public static int imprisonerKitDuration;
    public static int imprisonerKitDamage;
    public static int imprisonerKitHeight;

    // Kangaroo kit settings
    public static boolean kangarooKitEnabled;
    public static int kangarooKitCost;
    public static int kangarooKitCooldown;
    public static double kangarooKitSneakingHeight;
    public static double kangarooKitSneakingMultiplier;
    public static double kangarooKitNormalHeight;
    public static double kangarooKitNormalMultiplier;

    // Mage kit settings
    public static boolean mageKitEnabled;
    public static int mageKitCost;
    public static int mageKitCooldown;

    // Monk kit settings
    public static boolean monkKitEnabled;
    public static int monkKitCost;
    public static int monkKitCooldown;

    // Ninja kit settings
    public static boolean ninjaKitEnabled;
    public static int ninjaKitCost;

    // Pyro kit settings
    public static boolean pyroKitEnabled;
    public static int pyroKitCost;

    // Spiderman kit settings
    public static boolean spidermanKitEnabled;
    public static int spidermanKitCost;
    public static int spidermanKitCooldown;
    public static int spidermanKitDuration;

    // Summoner kit settings
    public static boolean summonerKitEnabled;
    public static int summonerKitCost;
    public static int summonerKitCooldown;
    public static int summonerKitDuration;
    public static int summonerKitRange;

    // Tamer kit settings
    public static boolean tamerKitEnabled;
    public static int tamerKitCost;
    public static int tamerKitCooldown;
    public static int tamerKitDuration;
    public static int tamerKitAmount;

    // Tank kit settings
    public static boolean tankKitEnabled;
    public static int tankKitCost;

    // Thor kit settings
    public static boolean thorKitEnabled;
    public static int thorKitCost;
    public static int thorKitCooldown;
    public static int thorKitDuration;
    public static int thorKitRange;
    public static int thorKitDamage;

    // Timelord kit settings
    public static boolean timelordKitEnabled;
    public static int timelordKitCost;
    public static int timelordKitCooldown;
    public static int timelordKitDuration;
    public static int timelordKitRange;

    // Vampire kit settings
    public static boolean vampireKitEnabled;
    public static int vampireKitCost;
    public static int vampireKitPassiveDuration;
    public static int vampireKitCooldown;
    public static int vampireKitDuration;
    public static int vampireKitRange;

    // Zen kit settings
    public static boolean zenKitEnabled;
    public static int zenKitCost;
    public static int zenKitCooldown;
    public static int zenKitDuration;
    public static int zenKitRange;

    // Database settings
    public static boolean usingFlatFile;
    public static long autoSaveInterval;
    public static String flatFilePath;
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
        try {
            // First, attempt to load the default configuration as a stream to check if it exists in the plugin JAR
            @Cleanup InputStream defConfigStream = KitPvP.getInstance().getResource(fileName);

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
            @Cleanup InputStreamReader reader = new InputStreamReader(defConfigStream, StandardCharsets.UTF_8);
            CustomYamlConfiguration defConfig = CustomYamlConfiguration.loadConfiguration(reader);

            // Ensure defaults are applied
            config.setDefaults(defConfig);
            config.options().copyDefaults(true);
            saveConfig(); // Save the config with defaults applied
        } catch (Exception ex) {
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

        // Bounties settings
        bountiesEnabled = config.getBoolean("kitpvp.bounties.enabled");
        bountiesCooldown = config.getInt("kitpvp.bounties.cooldown");
        bountiesMaxAmount = config.getInt("kitpvp.bounties.max-amount");

        // Economy settings
        startingCoins = config.getInt("kitpvp.economy.starting-coins");
        coinsOnKill = config.getInt("kitpvp.economy.coins-on-kill");
        expOnKill = config.getInt("kitpvp.economy.exp-on-kill");

        // Killstreak settings
        killStreaksEnabled = config.getBoolean("kitpvp.killstreaks.enabled");
        killStreaksCoinsBonus = config.getInt("kitpvp.killstreaks.coins-bonus");
        killStreaksExpBonus = config.getInt("kitpvp.killstreaks.exp-bonus");

        // Kit enchanter settings
        kitEnchanterEnabled = config.getBoolean("kitpvp.kit-enchanter.enabled");
        featherFallingEnabled = config.getBoolean("kitpvp.kit-enchanter.feather-falling.enabled");
        featherFallingCost = config.getInt("kitpvp.kit-enchanter.feather-falling.cost");
        thornsEnabled = config.getBoolean("kitpvp.kit-enchanter.thorns.enabled");
        thornsCost = config.getInt("kitpvp.kit-enchanter.thorns.cost");
        protectionEnabled = config.getBoolean("kitpvp.kit-enchanter.protection.enabled");
        protectionCost = config.getInt("kitpvp.kit-enchanter.protection.cost");
        knockbackEnabled = config.getBoolean("kitpvp.kit-enchanter.knockback.enabled");
        knockbackCost = config.getInt("kitpvp.kit-enchanter.knockback.cost");
        sharpnessEnabled = config.getBoolean("kitpvp.kit-enchanter.sharpness.enabled");
        sharpnessCost = config.getInt("kitpvp.kit-enchanter.sharpness.cost");
        punchEnabled = config.getBoolean("kitpvp.kit-enchanter.punch.enabled");
        punchCost = config.getInt("kitpvp.kit-enchanter.punch.cost");
        powerEnabled = config.getBoolean("kitpvp.kit-enchanter.power.enabled");
        powerCost = config.getInt("kitpvp.kit-enchanter.power.cost");

        // Knight kit settings
        knightKitEnabled = config.getBoolean("kitpvp.kits.knight.enabled");
        knightKitCost = config.getInt("kitpvp.kits.knight.cost");

        // Archer kit settings
        archerKitEnabled = config.getBoolean("kitpvp.kits.archer.enabled");
        archerKitCost = config.getInt("kitpvp.kits.archer.cost");

        // Burrower kit settings
        burrowerKitEnabled = config.getBoolean("kitpvp.kits.burrower.enabled");
        burrowerKitCost = config.getInt("kitpvp.kits.burrower.cost");
        burrowerKitCooldown = config.getInt("kitpvp.kits.burrower.ability.cooldown");
        burrowerKitDuration = config.getInt("kitpvp.kits.burrower.ability.duration");

        // Cactus kit settings
        cactusKitEnabled = config.getBoolean("kitpvp.kits.cactus.enabled");
        cactusKitCost = config.getInt("kitpvp.kits.cactus.cost");
        cactusKitPassiveDuration = config.getInt("kitpvp.kits.cactus.passive.duration");

        // Dragon kit settings
        dragonKitEnabled = config.getBoolean("kitpvp.kits.dragon.enabled");
        dragonKitCost = config.getInt("kitpvp.kits.dragon.cost");
        dragonKitCooldown = config.getInt("kitpvp.kits.dragon.ability.cooldown");
        dragonKitDuration = config.getInt("kitpvp.kits.dragon.ability.duration");
        dragonKitRange = config.getInt("kitpvp.kits.dragon.ability.range");
        dragonKitDamage = config.getInt("kitpvp.kits.dragon.ability.damage");

        // Fisherman kit settings
        fishermanKitEnabled = config.getBoolean("kitpvp.kits.fisherman.enabled");
        fishermanKitCost = config.getInt("kitpvp.kits.fisherman.cost");
        fishermanKitCooldown = config.getInt("kitpvp.kits.fisherman.ability.cooldown");

        // Ghost kit settings
        ghostKitEnabled = config.getBoolean("kitpvp.kits.ghost.enabled");
        ghostKitCost = config.getInt("kitpvp.kits.ghost.cost");

        // Hulk kit settings
        hulkKitEnabled = config.getBoolean("kitpvp.kits.hulk.enabled");
        hulkKitCost = config.getInt("kitpvp.kits.hulk.cost");
        hulkKitCooldown = config.getInt("kitpvp.kits.hulk.ability.cooldown");
        hulkKitRange = config.getInt("kitpvp.kits.hulk.ability.range");
        hulkKitDamage = config.getInt("kitpvp.kits.hulk.ability.damage");
        hulkKitMultiplier = config.getDouble("kitpvp.kits.hulk.ability.multiplier");

        // Imprisoner kit settings
        imprisonerKitEnabled = config.getBoolean("kitpvp.kits.imprisoner.enabled");
        imprisonerKitCost = config.getInt("kitpvp.kits.imprisoner.cost");
        imprisonerKitCooldown = config.getInt("kitpvp.kits.imprisoner.ability.cooldown");
        imprisonerKitDuration = config.getInt("kitpvp.kits.imprisoner.ability.duration");
        imprisonerKitDamage = config.getInt("kitpvp.kits.imprisoner.ability.damage");
        imprisonerKitHeight = config.getInt("kitpvp.kits.imprisoner.ability.height");

        // Kangaroo kit settings
        kangarooKitEnabled = config.getBoolean("kitpvp.kits.kangaroo.enabled");
        kangarooKitCost = config.getInt("kitpvp.kits.kangaroo.cost");
        kangarooKitCooldown = config.getInt("kitpvp.kits.kangaroo.ability.cooldown");
        kangarooKitSneakingHeight = config.getDouble("kitpvp.kits.kangaroo.ability.sneaking.height");
        kangarooKitSneakingMultiplier = config.getDouble("kitpvp.kits.kangaroo.ability.sneaking.multiplier");
        kangarooKitNormalHeight = config.getDouble("kitpvp.kits.kangaroo.ability.normal.height");
        kangarooKitNormalMultiplier = config.getDouble("kitpvp.kits.kangaroo.ability.normal.multiplier");

        // Mage kit settings
        mageKitEnabled = config.getBoolean("kitpvp.kits.mage.enabled");
        mageKitCost = config.getInt("kitpvp.kits.mage.cost");
        mageKitCooldown = config.getInt("kitpvp.kits.mage.ability.cooldown");

        // Monk kit settings
        monkKitEnabled = config.getBoolean("kitpvp.kits.monk.enabled");
        monkKitCost = config.getInt("kitpvp.kits.monk.cost");
        monkKitCooldown = config.getInt("kitpvp.kits.monk.ability.cooldown");

        // Ninja kit settings
        ninjaKitEnabled = config.getBoolean("kitpvp.kits.ninja.enabled");
        ninjaKitCost = config.getInt("kitpvp.kits.ninja.cost");

        // Pyro kit settings
        pyroKitEnabled = config.getBoolean("kitpvp.kits.pyro.enabled");
        pyroKitCost = config.getInt("kitpvp.kits.pyro.cost");

        // Spiderman kit settings
        spidermanKitEnabled = config.getBoolean("kitpvp.kits.spiderman.enabled");
        spidermanKitCost = config.getInt("kitpvp.kits.spiderman.cost");
        spidermanKitCooldown = config.getInt("kitpvp.kits.spiderman.ability.cooldown");
        spidermanKitDuration = config.getInt("kitpvp.kits.spiderman.ability.duration");

        // Summoner kit settings
        summonerKitEnabled = config.getBoolean("kitpvp.kits.summoner.enabled");
        summonerKitCost = config.getInt("kitpvp.kits.summoner.cost");
        summonerKitCooldown = config.getInt("kitpvp.kits.summoner.ability.cooldown");
        summonerKitDuration = config.getInt("kitpvp.kits.summoner.ability.duration");
        summonerKitRange = config.getInt("kitpvp.kits.summoner.ability.range");

        // Tamer kit settings
        tamerKitEnabled = config.getBoolean("kitpvp.kits.tamer.enabled");
        tamerKitCost = config.getInt("kitpvp.kits.tamer.cost");
        tamerKitCooldown = config.getInt("kitpvp.kits.tamer.ability.cooldown");
        tamerKitDuration = config.getInt("kitpvp.kits.tamer.ability.duration");
        tamerKitAmount = config.getInt("kitpvp.kits.tamer.ability.amount");

        // Tank kit settings
        tankKitEnabled = config.getBoolean("kitpvp.kits.tank.enabled");
        tankKitCost = config.getInt("kitpvp.kits.tank.cost");

        // Thor kit settings
        thorKitEnabled = config.getBoolean("kitpvp.kits.thor.enabled");
        thorKitCost = config.getInt("kitpvp.kits.thor.cost");
        thorKitCooldown = config.getInt("kitpvp.kits.thor.ability.cooldown");
        thorKitDuration = config.getInt("kitpvp.kits.thor.ability.duration");
        thorKitRange = config.getInt("kitpvp.kits.thor.ability.range");
        thorKitDamage = config.getInt("kitpvp.kits.thor.ability.damage");

        // Timelord kit settings
        timelordKitEnabled = config.getBoolean("kitpvp.kits.timelord.enabled");
        timelordKitCost = config.getInt("kitpvp.kits.timelord.cost");
        timelordKitCooldown = config.getInt("kitpvp.kits.timelord.ability.cooldown");
        timelordKitDuration = config.getInt("kitpvp.kits.timelord.ability.duration");
        timelordKitRange = config.getInt("kitpvp.kits.timelord.ability.range");

        // Vampire kit settings
        vampireKitEnabled = config.getBoolean("kitpvp.kits.vampire.enabled");
        vampireKitCost = config.getInt("kitpvp.kits.vampire.cost");
        vampireKitPassiveDuration = config.getInt("kitpvp.kits.vampire.passive.duration");
        vampireKitCooldown = config.getInt("kitpvp.kits.vampire.ability.cooldown");
        vampireKitDuration = config.getInt("kitpvp.kits.vampire.ability.duration");
        vampireKitRange = config.getInt("kitpvp.kits.vampire.ability.range");

        // Zen kit settings
        zenKitEnabled = config.getBoolean("kitpvp.kits.zen.enabled");
        zenKitCost = config.getInt("kitpvp.kits.zen.cost");
        zenKitCooldown = config.getInt("kitpvp.kits.zen.ability.cooldown");
        zenKitDuration = config.getInt("kitpvp.kits.zen.ability.duration");
        zenKitRange = config.getInt("kitpvp.kits.zen.ability.range");

        // Database settings
        usingFlatFile = config.getString("kitpvp.storage.type").trim().equalsIgnoreCase("sqlite");
        flatFilePath = KitPvP.getInstance().getDataFolder() + "\\" + config.getString("kitpvp.storage.sqlite.file");
        host = config.getString("kitpvp.storage.mariadb.host");
        port = config.getInt("kitpvp.storage.mariadb.port");
        database = config.getString("kitpvp.storage.mariadb.database");
        user = config.getString("kitpvp.storage.mariadb.user");
        password = config.getString("kitpvp.storage.mariadb.password");
    }
}
