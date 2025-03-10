/*
 * KitPvP - a fully-featured core plugin for the KitPvP gamemode.
 * Copyright (C) 2024 Foulest (https://github.com/Foulest)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package net.foulest.kitpvp.util;

import lombok.Cleanup;
import lombok.Data;
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
 */
@Data
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

    // Flask settings
    public static boolean flaskEnabled;
    public static int flaskAmount;
    public static int flaskCooldown;
    public static int flaskOnKill;

    // Combat tag settings
    public static boolean combatTagEnabled;
    public static int combatTagDuration;
    public static boolean combatTagCancelTeleport;
    public static boolean combatTagPunishLogout;
    public static boolean combatTagDenyEnteringSpawn;

    // Bounties settings
    public static boolean bountiesEnabled;
    public static int bountiesCooldown;
    public static int bountiesMinAmount;
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

    // Archer kit settings
    public static int archerKitCooldown;
    public static int archerKitDuration;

    // Fisherman kit settings
    public static int fishermanKitCooldown;
    public static int fishermanKitRodCooldown;
    public static int fishermanKitDuration;

    // Jester kit settings
    public static int jesterKitCooldown;
    public static int jesterKitDuration;
    public static double jesterKitDamage;

    // Kangaroo kit settings
    public static int kangarooKitCooldown;

    // Mage kit settings
    public static int mageKitCooldown;
    public static int mageKitDuration;

    // Ninja kit settings
    public static int ninjaKitCooldown;
    public static int ninjaKitDuration;

    // Pyro kit settings
    public static int pyroKitCooldown;
    public static int pyroKitDuration;
    public static double pyroKitDamage;

    // Tank kit settings
    public static int tankKitCooldown;
    public static int tankKitDuration;

    // Vampire kit settings
    public static int vampireKitCooldown;
    public static int vampireKitDuration;

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
    @SuppressWarnings({"WeakerAccess", "OverlyBroadCatchBlock"})
    public static void loadConfigFile() {
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
            ex.printStackTrace();
        }
    }

    /**
     * Saves the configuration file.
     */
    public static void saveConfig() {
        try {
            config.save(file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Loads configuration values into the relevant static fields.
     */
    @SuppressWarnings("WeakerAccess")
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
        bountiesMinAmount = config.getInt("kitpvp.bounties.min-amount");
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

        // Flask settings
        flaskEnabled = config.getBoolean("kitpvp.flasks.enabled");
        flaskAmount = config.getInt("kitpvp.flasks.amount");
        flaskCooldown = config.getInt("kitpvp.flasks.cooldown");
        flaskOnKill = config.getInt("kitpvp.flasks.on-kill");

        // Archer kit settings
        archerKitCooldown = config.getInt("kitpvp.kits.archer.ability.cooldown");
        archerKitDuration = config.getInt("kitpvp.kits.archer.ability.duration");

        // Fisherman kit settings
        fishermanKitRodCooldown = config.getInt("kitpvp.kits.fisherman.rod-cooldown");
        fishermanKitCooldown = config.getInt("kitpvp.kits.fisherman.ability.cooldown");
        fishermanKitDuration = config.getInt("kitpvp.kits.fisherman.ability.duration");

        // Jester kit settings
        jesterKitCooldown = config.getInt("kitpvp.kits.jester.ability.cooldown");
        jesterKitDuration = config.getInt("kitpvp.kits.jester.ability.duration");
        jesterKitDamage = config.getDouble("kitpvp.kits.jester.ability.damage");

        // Kangaroo kit settings
        kangarooKitCooldown = config.getInt("kitpvp.kits.kangaroo.ability.cooldown");

        // Mage kit settings
        mageKitCooldown = config.getInt("kitpvp.kits.mage.ability.cooldown");
        mageKitDuration = config.getInt("kitpvp.kits.mage.ability.duration");

        // Ninja kit settings
        ninjaKitCooldown = config.getInt("kitpvp.kits.ninja.ability.cooldown");
        ninjaKitDuration = config.getInt("kitpvp.kits.ninja.ability.duration");

        // Pyro kit settings
        pyroKitCooldown = config.getInt("kitpvp.kits.pyro.ability.cooldown");
        pyroKitDuration = config.getInt("kitpvp.kits.pyro.ability.duration");
        pyroKitDamage = config.getDouble("kitpvp.kits.pyro.ability.damage");

        // Tank kit settings
        tankKitCooldown = config.getInt("kitpvp.kits.tank.ability.cooldown");
        tankKitDuration = config.getInt("kitpvp.kits.tank.ability.duration");

        // Vampire kit settings
        vampireKitCooldown = config.getInt("kitpvp.kits.vampire.ability.cooldown");
        vampireKitDuration = config.getInt("kitpvp.kits.vampire.ability.duration");

        // Database settings
        usingFlatFile = config.getString("kitpvp.storage.type").trim().equalsIgnoreCase("sqlite");
        flatFilePath = KitPvP.getInstance().getDataFolder() + File.separator + config.getString("kitpvp.storage.sqlite.file");
        host = config.getString("kitpvp.storage.mariadb.host");
        port = config.getInt("kitpvp.storage.mariadb.port");
        database = config.getString("kitpvp.storage.mariadb.database");
        user = config.getString("kitpvp.storage.mariadb.user");
        password = config.getString("kitpvp.storage.mariadb.password");
    }
}
