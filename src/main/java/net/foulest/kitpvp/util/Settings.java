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

    public static int coinsOnKill;
    public static int expOnKill;

    public static boolean killStreaksEnabled;
    public static int killStreaksCoinsBonus;
    public static int killStreaksExpBonus;

    public static int startingCoins;

    public static boolean archerKitEnabled;
    public static boolean archerKitPremiumOnly;
    public static int archerKitCost;

    public static boolean burrowerKitEnabled;
    public static boolean burrowerKitPremiumOnly;
    public static int burrowerKitCost;

    public static boolean cactusKitEnabled;
    public static boolean cactusKitPremiumOnly;
    public static int cactusKitCost;

    public static boolean dragonKitEnabled;
    public static boolean dragonKitPremiumOnly;
    public static int dragonKitCost;

    public static boolean fishermanKitEnabled;
    public static boolean fishermanKitPremiumOnly;
    public static int fishermanKitCost;

    public static boolean ghostKitEnabled;
    public static boolean ghostKitPremiumOnly;
    public static int ghostKitCost;

    public static boolean hulkKitEnabled;
    public static boolean hulkKitPremiumOnly;
    public static int hulkKitCost;

    public static boolean imprisonerKitEnabled;
    public static boolean imprisonerKitPremiumOnly;
    public static int imprisonerKitCost;

    public static boolean kangarooKitEnabled;
    public static boolean kangarooKitPremiumOnly;
    public static int kangarooKitCost;

    public static boolean knightKitEnabled;
    public static boolean knightKitPremiumOnly;
    public static int knightKitCost;

    public static boolean mageKitEnabled;
    public static boolean mageKitPremiumOnly;
    public static int mageKitCost;

    public static boolean monkKitEnabled;
    public static boolean monkKitPremiumOnly;
    public static int monkKitCost;

    public static boolean ninjaKitEnabled;
    public static boolean ninjaKitPremiumOnly;
    public static int ninjaKitCost;

    public static boolean pyroKitEnabled;
    public static boolean pyroKitPremiumOnly;
    public static int pyroKitCost;

    public static boolean spidermanKitEnabled;
    public static boolean spidermanKitPremiumOnly;
    public static int spidermanKitCost;

    public static boolean summonerKitEnabled;
    public static boolean summonerKitPremiumOnly;
    public static int summonerKitCost;

    public static boolean tamerKitEnabled;
    public static boolean tamerKitPremiumOnly;
    public static int tamerKitCost;

    public static boolean tankKitEnabled;
    public static boolean tankKitPremiumOnly;
    public static int tankKitCost;

    public static boolean thorKitEnabled;
    public static boolean thorKitPremiumOnly;
    public static int thorKitCost;

    public static boolean timelordKitEnabled;
    public static boolean timelordKitPremiumOnly;
    public static int timelordKitCost;

    public static boolean vampireKitEnabled;
    public static boolean vampireKitPremiumOnly;
    public static int vampireKitCost;

    public static boolean zenKitEnabled;
    public static boolean zenKitPremiumOnly;
    public static int zenKitCost;

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
                MessageUtil.log(Level.WARNING, "Failed to create settings.yml file.");
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

        config.addDefault("kill.coins-on-kill", 10);
        config.addDefault("kill.exp-on-kill", 15);

        config.addDefault("killstreaks.enabled", true);
        config.addDefault("killstreaks.coins-bonus", 5);
        config.addDefault("killstreaks.exp-bonus", 10);

        config.addDefault("kits.starting-coins", 500);

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

        coinsOnKill = config.getInt("kill.coins-on-kill");
        expOnKill = config.getInt("kill.exp-on-kill");

        killStreaksEnabled = config.getBoolean("killstreaks.enabled");
        killStreaksCoinsBonus = config.getInt("killstreaks.coins-bonus");
        killStreaksExpBonus = config.getInt("killstreaks.exp-bonus");

        startingCoins = config.getInt("kits.starting-coins");

        archerKitEnabled = config.getBoolean("kits.archer.enabled");
        archerKitPremiumOnly = config.getBoolean("kits.archer.premium-only");
        archerKitCost = config.getInt("kits.archer.cost");

        burrowerKitEnabled = config.getBoolean("kits.burrower.enabled");
        burrowerKitPremiumOnly = config.getBoolean("kits.burrower.premium-only");
        burrowerKitCost = config.getInt("kits.burrower.cost");

        cactusKitEnabled = config.getBoolean("kits.cactus.enabled");
        cactusKitPremiumOnly = config.getBoolean("kits.cactus.premium-only");
        cactusKitCost = config.getInt("kits.cactus.cost");

        dragonKitEnabled = config.getBoolean("kits.dragon.enabled");
        dragonKitPremiumOnly = config.getBoolean("kits.dragon.premium-only");
        dragonKitCost = config.getInt("kits.dragon.cost");

        fishermanKitEnabled = config.getBoolean("kits.fisherman.enabled");
        fishermanKitPremiumOnly = config.getBoolean("kits.fisherman.premium-only");
        fishermanKitCost = config.getInt("kits.fisherman.cost");

        ghostKitEnabled = config.getBoolean("kits.ghost.enabled");
        ghostKitPremiumOnly = config.getBoolean("kits.ghost.premium-only");
        ghostKitCost = config.getInt("kits.ghost.cost");

        hulkKitEnabled = config.getBoolean("kits.hulk.enabled");
        hulkKitPremiumOnly = config.getBoolean("kits.hulk.premium-only");
        hulkKitCost = config.getInt("kits.hulk.cost");

        imprisonerKitEnabled = config.getBoolean("kits.imprisoner.enabled");
        imprisonerKitPremiumOnly = config.getBoolean("kits.imprisoner.premium-only");
        imprisonerKitCost = config.getInt("kits.imprisoner.cost");

        kangarooKitEnabled = config.getBoolean("kits.kangaroo.enabled");
        kangarooKitPremiumOnly = config.getBoolean("kits.kangaroo.premium-only");
        kangarooKitCost = config.getInt("kits.kangaroo.cost");

        knightKitEnabled = config.getBoolean("kits.knight.enabled");
        knightKitPremiumOnly = config.getBoolean("kits.knight.premium-only");
        knightKitCost = config.getInt("kits.knight.cost");

        mageKitEnabled = config.getBoolean("kits.mage.enabled");
        mageKitPremiumOnly = config.getBoolean("kits.mage.premium-only");
        mageKitCost = config.getInt("kits.mage.cost");

        monkKitEnabled = config.getBoolean("kits.monk.enabled");
        monkKitPremiumOnly = config.getBoolean("kits.monk.premium-only");
        monkKitCost = config.getInt("kits.monk.cost");

        ninjaKitEnabled = config.getBoolean("kits.ninja.enabled");
        ninjaKitPremiumOnly = config.getBoolean("kits.ninja.premium-only");
        ninjaKitCost = config.getInt("kits.ninja.cost");

        pyroKitEnabled = config.getBoolean("kits.pyro.enabled");
        pyroKitPremiumOnly = config.getBoolean("kits.pyro.premium-only");
        pyroKitCost = config.getInt("kits.pyro.cost");

        spidermanKitEnabled = config.getBoolean("kits.spiderman.enabled");
        spidermanKitPremiumOnly = config.getBoolean("kits.spiderman.premium-only");
        spidermanKitCost = config.getInt("kits.spiderman.cost");

        summonerKitEnabled = config.getBoolean("kits.summoner.enabled");
        summonerKitPremiumOnly = config.getBoolean("kits.summoner.premium-only");
        summonerKitCost = config.getInt("kits.summoner.cost");

        tamerKitEnabled = config.getBoolean("kits.tamer.enabled");
        tamerKitPremiumOnly = config.getBoolean("kits.tamer.premium-only");
        tamerKitCost = config.getInt("kits.tamer.cost");

        tankKitEnabled = config.getBoolean("kits.tank.enabled");
        tankKitPremiumOnly = config.getBoolean("kits.tank.premium-only");
        tankKitCost = config.getInt("kits.tank.cost");

        thorKitEnabled = config.getBoolean("kits.thor.enabled");
        thorKitPremiumOnly = config.getBoolean("kits.thor.premium-only");
        thorKitCost = config.getInt("kits.thor.cost");

        timelordKitEnabled = config.getBoolean("kits.timelord.enabled");
        timelordKitPremiumOnly = config.getBoolean("kits.timelord.premium-only");
        timelordKitCost = config.getInt("kits.timelord.cost");

        vampireKitEnabled = config.getBoolean("kits.vampire.enabled");
        vampireKitPremiumOnly = config.getBoolean("kits.vampire.premium-only");
        vampireKitCost = config.getInt("kits.vampire.cost");

        zenKitEnabled = config.getBoolean("kits.zen.enabled");
        zenKitPremiumOnly = config.getBoolean("kits.zen.premium-only");
        zenKitCost = config.getInt("kits.zen.cost");

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

        config.set("kill.coins-on-kill", coinsOnKill);
        config.set("kill.exp-on-kill", expOnKill);

        config.set("kill.killstreaks.enabled", killStreaksEnabled);
        config.set("kill.killstreaks.coins-bonus", killStreaksCoinsBonus);
        config.set("kill.killstreaks.exp-bonus", killStreaksExpBonus);

        config.set("kits.starting-coins", startingCoins);

        config.set("kits.archer.enabled", archerKitEnabled);
        config.set("kits.archer.premium-only", archerKitPremiumOnly);
        config.set("kits.archer.cost", archerKitCost);

        config.set("kits.burrower.enabled", burrowerKitEnabled);
        config.set("kits.burrower.premium-only", burrowerKitPremiumOnly);
        config.set("kits.burrower.cost", burrowerKitCost);

        config.set("kits.cactus.enabled", cactusKitEnabled);
        config.set("kits.cactus.premium-only", cactusKitPremiumOnly);
        config.set("kits.cactus.cost", cactusKitCost);

        config.set("kits.dragon.enabled", dragonKitEnabled);
        config.set("kits.dragon.premium-only", dragonKitPremiumOnly);
        config.set("kits.dragon.cost", dragonKitCost);

        config.set("kits.fisherman.enabled", fishermanKitEnabled);
        config.set("kits.fisherman.premium-only", fishermanKitPremiumOnly);
        config.set("kits.fisherman.cost", fishermanKitCost);

        config.set("kits.ghost.enabled", ghostKitEnabled);
        config.set("kits.ghost.premium-only", ghostKitPremiumOnly);
        config.set("kits.ghost.cost", ghostKitCost);

        config.set("kits.hulk.enabled", hulkKitEnabled);
        config.set("kits.hulk.premium-only", hulkKitPremiumOnly);
        config.set("kits.hulk.cost", hulkKitCost);

        config.set("kits.imprisoner.enabled", imprisonerKitEnabled);
        config.set("kits.imprisoner.premium-only", imprisonerKitPremiumOnly);
        config.set("kits.imprisoner.cost", imprisonerKitCost);

        config.set("kits.kangaroo.enabled", kangarooKitEnabled);
        config.set("kits.kangaroo.premium-only", kangarooKitPremiumOnly);
        config.set("kits.kangaroo.cost", kangarooKitCost);

        config.set("kits.knight.enabled", knightKitEnabled);
        config.set("kits.knight.premium-only", knightKitPremiumOnly);
        config.set("kits.knight.cost", knightKitCost);

        config.set("kits.mage.enabled", mageKitEnabled);
        config.set("kits.mage.premium-only", mageKitPremiumOnly);
        config.set("kits.mage.cost", mageKitCost);

        config.set("kits.monk.enabled", monkKitEnabled);
        config.set("kits.monk.premium-only", monkKitPremiumOnly);
        config.set("kits.monk.cost", monkKitCost);

        config.set("kits.ninja.enabled", ninjaKitEnabled);
        config.set("kits.ninja.premium-only", ninjaKitPremiumOnly);
        config.set("kits.ninja.cost", ninjaKitCost);

        config.set("kits.pyro.enabled", pyroKitEnabled);
        config.set("kits.pyro.premium-only", pyroKitPremiumOnly);
        config.set("kits.pyro.cost", pyroKitCost);

        config.set("kits.spiderman.enabled", spidermanKitEnabled);
        config.set("kits.spiderman.premium-only", spidermanKitPremiumOnly);
        config.set("kits.spiderman.cost", spidermanKitCost);

        config.set("kits.summoner.enabled", summonerKitEnabled);
        config.set("kits.summoner.premium-only", summonerKitPremiumOnly);
        config.set("kits.summoner.cost", summonerKitCost);

        config.set("kits.tamer.enabled", tamerKitEnabled);
        config.set("kits.tamer.premium-only", tamerKitPremiumOnly);
        config.set("kits.tamer.cost", tamerKitCost);

        config.set("kits.tank.enabled", tankKitEnabled);
        config.set("kits.tank.premium-only", tankKitPremiumOnly);
        config.set("kits.tank.cost", tankKitCost);

        config.set("kits.thor.enabled", thorKitEnabled);
        config.set("kits.thor.premium-only", thorKitPremiumOnly);
        config.set("kits.thor.cost", thorKitCost);

        config.set("kits.timelord.enabled", timelordKitEnabled);
        config.set("kits.timelord.premium-only", timelordKitPremiumOnly);
        config.set("kits.timelord.cost", timelordKitCost);

        config.set("kits.vampire.enabled", vampireKitEnabled);
        config.set("kits.vampire.premium-only", vampireKitPremiumOnly);
        config.set("kits.vampire.cost", vampireKitCost);

        config.set("kits.zen.enabled", zenKitEnabled);
        config.set("kits.zen.premium-only", zenKitPremiumOnly);
        config.set("kits.zen.cost", zenKitCost);

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
