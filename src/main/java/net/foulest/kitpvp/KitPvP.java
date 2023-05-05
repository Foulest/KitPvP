package net.foulest.kitpvp;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.SneakyThrows;
import net.foulest.kitpvp.cmds.*;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.kits.*;
import net.foulest.kitpvp.listeners.CombatLog;
import net.foulest.kitpvp.listeners.DeathListener;
import net.foulest.kitpvp.listeners.EventListener;
import net.foulest.kitpvp.listeners.KitListener;
import net.foulest.kitpvp.region.Spawn;
import net.foulest.kitpvp.util.DatabaseUtil;
import net.foulest.kitpvp.util.PlaceholderUtil;
import net.foulest.kitpvp.util.Settings;
import net.foulest.kitpvp.util.command.CommandFramework;
import net.foulest.kitpvp.util.kits.Kit;
import net.foulest.kitpvp.util.kits.KitManager;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.Objects;

/**
 * @author Foulest
 * @project KitPvP
 */
@Getter
public class KitPvP extends JavaPlugin {

    // TODO: Add admin command to remove a bounty and refund coins.

    public static KitPvP instance;
    public static String pluginName = "KitPvP";
    public static boolean loaded = false;
    private CommandFramework framework;

    @Override
    @SneakyThrows
    public void onEnable() {
        instance = this;
        framework = new CommandFramework(this);

        // Registers placeholders with PlaceholderAPI.
        Bukkit.getLogger().info("[" + pluginName + " - Loading Placeholders...");
        new PlaceholderUtil().register();

        // Creates the default settings config.
        Bukkit.getLogger().info("[" + pluginName + " - Loading Settings...");
        Settings.setupSettings();
        Settings.loadSettings();

        // Sets up the Hikari instance.
        Bukkit.getLogger().info("[" + pluginName + " - Loading Database...");
        loadDatabase();

        // Loads the plugin's listeners.
        Bukkit.getLogger().info("[" + pluginName + " - Loading Listeners...");
        loadListeners(new DeathListener(), new EventListener(), new KitListener());

        // Loads the plugin's commands.
        Bukkit.getLogger().info("[" + pluginName + " - Loading Commands...");
        loadCommands(new BalanceCmd(), new BountyCmd(), new ClearKitCmd(), new CombatTagCmd(), new EcoGiveCmd(),
                new EcoSetCmd(), new KitsCmd(), new PayCmd(), new SetSpawnCmd(), new SpawnCmd(), new StatsCmd(),
                new KitShopCmd(), new EcoTakeCmd(), new ArmorColorCmd(), new KitEnchanterCmd(), new SoupCmd(),
                new PotionsCmd(), new PunishCmd(), new ReloadCfgCmd());

        // Loads the plugin's kits.
        Bukkit.getLogger().info("[" + pluginName + " - Loading Kits...");
        loadKits(new Archer(), new Burrower(), new Cactus(), new Dragon(), new Fisherman(), new Ghost(), new Tamer(),
                new Hulk(), new Imprisoner(), new Kangaroo(), new Knight(), new Mage(), new Monk(), new Ninja(),
                new Pyro(), new Spiderman(), new Summoner(), new Tank(), new Thor(), new Timelord(), new Vampire(),
                new Zen());

        // Loads the spawn.
        Bukkit.getLogger().info("[" + pluginName + " - Loading Spawn...");
        Spawn.load();

        if (Spawn.getLocation().getWorld().getDifficulty() == Difficulty.PEACEFUL) {
            Bukkit.getLogger().warning("[" + pluginName + " - The world difficulty is set to Peaceful. This will cause issues with hostile mobs in certain kits.");
        }

        // Loads online players' user data.
        Bukkit.getLogger().info("[" + pluginName + " - Loading Player Data...");
        for (Player player : Bukkit.getOnlinePlayers()) {
            Objects.requireNonNull(PlayerData.getInstance(player)).load();
            Spawn.teleport(player);
            player.getInventory().setHeldItemSlot(0);
        }

        Bukkit.getLogger().info("[" + pluginName + " Loaded successfully.");
        loaded = true;
    }

    @Override
    public void onDisable() {
        // Unloads the kits saved in the Kit Manager.
        Bukkit.getLogger().info("[" + pluginName + " - Unloading Kits...");
        KitManager.kits.clear();

        // Saves the settings.
        Bukkit.getLogger().info("[" + pluginName + " - Saving Settings...");
        Settings.saveSettings();

        // Saves online players' data.
        Bukkit.getLogger().info("[" + pluginName + " - Saving Player Data...");
        for (Player player : Bukkit.getOnlinePlayers()) {
            Objects.requireNonNull(PlayerData.getInstance(player)).saveAll();

            if (CombatLog.isInCombat(player)) {
                CombatLog.remove(player);
            }
        }

        // Closes the MySQL connection.
        Bukkit.getLogger().info("[" + pluginName + " - Saving Database...");
        if (DatabaseUtil.hikari != null) {
            DatabaseUtil.hikari.close();
        }

        Bukkit.getLogger().info("[" + pluginName + " Shut down successfully.");
    }

    /**
     * Loads the plugin's databases.
     */
    private void loadDatabase() {
        DatabaseUtil.hikari = new HikariDataSource();

        // Sets up the MariaDB database.
        DatabaseUtil.hikari.setPoolName("MariaDBConnectionPool");
        DatabaseUtil.hikari.setJdbcUrl("jdbc:mariadb://" + Settings.host + ":" + Settings.port + "/" + Settings.database);
        DatabaseUtil.hikari.setDriverClassName("org.mariadb.jdbc.Driver");
        DatabaseUtil.hikari.addDataSourceProperty("user", Settings.user);
        DatabaseUtil.hikari.addDataSourceProperty("password", Settings.password);
        DatabaseUtil.hikari.addDataSourceProperty("characterEncoding", "utf8");
        DatabaseUtil.hikari.addDataSourceProperty("useUnicode", "true");

        // Sets up other connection flags.
        DatabaseUtil.hikari.setConnectionTestQuery("SELECT 1;");

        // Creates missing tables in the database.
        DatabaseUtil.update("CREATE TABLE IF NOT EXISTS PlayerStats (uuid VARCHAR(36), coins INT, " +
                            "experience INT, kills INT, deaths INT, killstreak INT, topKillstreak INT, usingSoup BOOLEAN, " +
                            "previousKit VARCHAR(36))");
        DatabaseUtil.update("CREATE TABLE IF NOT EXISTS PlayerKits (uuid VARCHAR(36), kitName VARCHAR(36))");
        DatabaseUtil.update("CREATE TABLE IF NOT EXISTS Bounties (uuid VARCHAR(36), bounty INT, benefactor VARCHAR(36))");
        DatabaseUtil.update("CREATE TABLE IF NOT EXISTS Enchants (uuid VARCHAR(36), featherFalling BOOLEAN," +
                            " thorns BOOLEAN, protection BOOLEAN, knockback BOOLEAN, sharpness BOOLEAN, punch BOOLEAN, power BOOLEAN)");
    }

    /**
     * Loads the plugin's listeners.
     *
     * @param listeners Listener to load.
     */
    private void loadListeners(Listener... listeners) {
        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, this);
        }
    }

    /**
     * Loads the plugin's commands.
     *
     * @param commands Command to load.
     */
    private void loadCommands(Object... commands) {
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
