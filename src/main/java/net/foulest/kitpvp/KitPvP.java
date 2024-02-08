package net.foulest.kitpvp;

import lombok.Getter;
import lombok.SneakyThrows;
import net.foulest.kitpvp.cmds.*;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
import net.foulest.kitpvp.kits.Kit;
import net.foulest.kitpvp.kits.KitManager;
import net.foulest.kitpvp.kits.type.*;
import net.foulest.kitpvp.listeners.DeathListener;
import net.foulest.kitpvp.listeners.EventListener;
import net.foulest.kitpvp.listeners.KitListener;
import net.foulest.kitpvp.region.Spawn;
import net.foulest.kitpvp.util.DatabaseUtil;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.PlaceholderUtil;
import net.foulest.kitpvp.util.Settings;
import net.foulest.kitpvp.util.command.CommandFramework;
import org.apache.commons.dbcp2.BasicDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Main class for KitPvP.
 *
 * @author Foulest
 * @project KitPvP
 */
@Getter
public class KitPvP extends JavaPlugin {

    @Getter
    public static KitPvP instance;
    private CommandFramework framework;

    @Override
    public void onLoad() {
        // Sets the instance.
        instance = this;
    }

    @Override
    @SneakyThrows
    public void onEnable() {
        // Kicks all online players.
        Bukkit.getOnlinePlayers().forEach(player -> player.kickPlayer("Disconnected"));

        // Initializes the Command Framework.
        MessageUtil.log(Level.INFO, "Initializing Command Framework...");
        framework = new CommandFramework(this);

        // Registers placeholders with PlaceholderAPI.
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            MessageUtil.log(Level.INFO, "Integrating with PlaceholderAPI...");
            new PlaceholderUtil().register();
        }

        // Creates the default settings config.
        MessageUtil.log(Level.INFO, "Loading Settings...");
        Settings.loadSettings();

        // Sets up the Hikari instance.
        MessageUtil.log(Level.INFO, "Loading Database...");
        loadDatabase();

        // Loads the plugin's listeners.
        MessageUtil.log(Level.INFO, "Loading Listeners...");
        loadListeners(new DeathListener(), new EventListener(), new KitListener());

        // Loads the plugin's commands.
        MessageUtil.log(Level.INFO, "Loading Commands...");
        loadCommands(new BalanceCmd(), new BountyCmd(), new ClearKitCmd(), new CombatTagCmd(), new EcoCmd(),
                new KitsCmd(), new PayCmd(), new SetSpawnCmd(), new SpawnCmd(), new StatsCmd(),
                new KitShopCmd(), new ArmorColorCmd(), new KitEnchanterCmd(), new SoupCmd(),
                new PotionsCmd(), new KitPvPCmd());

        // Loads the plugin's kits.
        MessageUtil.log(Level.INFO, "Loading Kits...");
        loadKits(new Archer(), new Burrower(), new Cactus(), new Dragon(), new Fisherman(), new Ghost(), new Tamer(),
                new Hulk(), new Imprisoner(), new Kangaroo(), new Knight(), new Mage(), new Monk(), new Ninja(),
                new Pyro(), new Spiderman(), new Summoner(), new Tank(), new Thor(), new Timelord(), new Vampire(),
                new Zen());

        // Loads the spawn.
        MessageUtil.log(Level.INFO, "Loading Spawn...");
        Spawn.load();

        // Checks if the world difficulty is set to Peaceful.
        if (Spawn.getLocation().getWorld().getDifficulty() == Difficulty.PEACEFUL) {
            MessageUtil.log(Level.WARNING, "The world difficulty is set to Peaceful."
                    + " This will cause issues with hostile mobs in certain kits.");
        }

        // Loads online players' user data.
        MessageUtil.log(Level.INFO, "Loading Player Data...");
        for (Player player : Bukkit.getOnlinePlayers()) {
            Objects.requireNonNull(PlayerDataManager.getPlayerData(player)).load();
            Spawn.teleport(player);
            player.getInventory().setHeldItemSlot(0);
        }

        MessageUtil.log(Level.INFO, "Loaded successfully.");
    }

    @Override
    public void onDisable() {
        // Unloads the kits saved in the Kit Manager.
        MessageUtil.log(Level.INFO, "Unloading Kits...");
        KitManager.kits.clear();

        // Saves online players' data.
        MessageUtil.log(Level.INFO, "Saving Player Data...");
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData playerData = PlayerDataManager.getPlayerData(player);
            playerData.saveAll();
        }

        // Closes the DBCP connection.
        MessageUtil.log(Level.INFO, "Saving Database...");
        DatabaseUtil.closeDbcp();

        MessageUtil.log(Level.INFO, "Shut down successfully.");
    }

    /**
     * Loads the plugin's databases.
     */
    private void loadDatabase() {
        // Initializes the DBCP instance.
        DatabaseUtil.initialize(new BasicDataSource());

        if (Settings.usingFlatFile) {
            // Sets up the DBCP instance for SQLite.
            DatabaseUtil.setupDbcp("jdbc:sqlite:" + Settings.flatFilePath, "org.sqlite.JDBC",
                    null, null, null, false, null);

        } else {
            // Sets up the DBCP instance for MariaDB.
            DatabaseUtil.setupDbcp("jdbc:mariadb://" + Settings.host + ":" + Settings.port + "/" + Settings.database,
                    "org.mariadb.jdbc.Driver", Settings.user, Settings.password,
                    "utf8", true, "SELECT 1;");
        }

        // Creates the PlayerStats table if it doesn't exist.
        DatabaseUtil.createTableIfNotExists(
                "PlayerStats",
                "uuid VARCHAR(255) NOT NULL, "
                        + "coins INT, "
                        + "experience INT, "
                        + "kills INT, "
                        + "deaths INT, "
                        + "killstreak INT, "
                        + "topKillstreak INT, "
                        + "usingSoup INT, "
                        + "previousKit VARCHAR(255), "
                        + "PRIMARY KEY (uuid)"
        );

        // Creates the PlayerKits table if it doesn't exist.
        DatabaseUtil.createTableIfNotExists(
                "PlayerKits",
                "uuid VARCHAR(255) NOT NULL, "
                        + "kitName VARCHAR(255)"
        );

        // Creates the Bounties table if it doesn't exist.
        DatabaseUtil.createTableIfNotExists(
                "Bounties",
                "uuid VARCHAR(255) NOT NULL, "
                        + "bounty INT, "
                        + "benefactor VARCHAR(255), "
                        + "PRIMARY KEY (uuid)"
        );

        // Creates the Enchants table if it doesn't exist.
        DatabaseUtil.createTableIfNotExists(
                "Enchants",
                "uuid VARCHAR(255) NOT NULL, "
                        + "featherFalling INT, "
                        + "thorns INT, "
                        + "protection INT, "
                        + "knockback INT, "
                        + "sharpness INT, "
                        + "punch INT, "
                        + "power INT, "
                        + "PRIMARY KEY (uuid)"
        );
    }

    /**
     * Loads the plugin's listeners.
     *
     * @param listeners Listener to load.
     */
    private void loadListeners(Listener @NotNull ... listeners) {
        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, this);
        }
    }

    /**
     * Loads the plugin's commands.
     *
     * @param commands Command to load.
     */
    private void loadCommands(Object @NotNull ... commands) {
        for (Object command : commands) {
            framework.registerCommands(command);
        }
    }

    /**
     * Loads the plugin's kits.
     *
     * @param kits Kit to load.
     */
    private void loadKits(Kit... kits) {
        Collections.addAll(KitManager.kits, kits);
    }
}
