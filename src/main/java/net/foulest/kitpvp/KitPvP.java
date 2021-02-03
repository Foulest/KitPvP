package net.foulest.kitpvp;

import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import net.foulest.kitpvp.cmds.*;
import net.foulest.kitpvp.kits.*;
import net.foulest.kitpvp.listeners.CombatLog;
import net.foulest.kitpvp.listeners.DeathListener;
import net.foulest.kitpvp.listeners.EventListener;
import net.foulest.kitpvp.listeners.KitListener;
import net.foulest.kitpvp.utils.*;
import net.foulest.kitpvp.utils.command.CommandFramework;
import net.foulest.kitpvp.utils.kits.Kit;
import net.foulest.kitpvp.utils.kits.KitManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class KitPvP extends JavaPlugin {

    private static KitPvP instance;
    private CommandFramework framework;
    private HikariDataSource hikari;

    public static KitPvP getInstance() {
        return instance;
    }

    @Override
    @SneakyThrows
    public void onEnable() {
        instance = this;
        framework = new CommandFramework(this);

        // Registers placeholders with PlaceholderAPI.
        new Placeholders().register();

        // Creates the default config.
        ConfigManager.setup();
        ConfigManager.get().addDefault("spawn.world", "world");
        ConfigManager.get().addDefault("spawn.x", 0.5);
        ConfigManager.get().addDefault("spawn.y", 64.0);
        ConfigManager.get().addDefault("spawn.z", 0.5);
        ConfigManager.get().addDefault("spawn.yaw", 90.0);
        ConfigManager.get().addDefault("spawn.pitch", 0.0);
        ConfigManager.get().addDefault("kill.coins-bonus", 10);
        ConfigManager.get().addDefault("kill.experience-bonus", 25);
        ConfigManager.get().addDefault("kill.killstreak-bonus", 5);
        ConfigManager.get().addDefault("mysql.host", "host");
        ConfigManager.get().addDefault("mysql.user", "user");
        ConfigManager.get().addDefault("mysql.password", "password");
        ConfigManager.get().addDefault("mysql.database", "database");
        ConfigManager.get().addDefault("mysql.port", "port");
        ConfigManager.get().options().copyDefaults(true);
        ConfigManager.save();

        // Sets up the MySQL database.
        hikari = new HikariDataSource();
        hikari.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        hikari.addDataSourceProperty("serverName", ConfigManager.get().getString("mysql.host"));
        hikari.addDataSourceProperty("port", ConfigManager.get().getString("mysql.port"));
        hikari.addDataSourceProperty("databaseName", ConfigManager.get().getString("mysql.database"));
        hikari.addDataSourceProperty("user", ConfigManager.get().getString("mysql.user"));
        hikari.addDataSourceProperty("password", ConfigManager.get().getString("mysql.password"));
        hikari.addDataSourceProperty("characterEncoding", "utf8");
        hikari.addDataSourceProperty("useUnicode", "true");

        // Creates missing tables in the MySQL database.
        try (Connection connection = hikari.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS PlayerStats (uuid VARCHAR(36), coins INT, " +
                    "experience INT, kills INT, deaths INT, killstreak INT, topKillstreak INT, usingSoup BOOLEAN, " +
                    "previousKit VARCHAR(36))");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS PlayerKits (uuid VARCHAR(36), kitName VARCHAR(36))");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Loads the plugin's listeners.
        loadListeners(new DeathListener(), new EventListener(), new KitListener());

        // Loads the plugin's commands.
        loadCommands(new BalanceCmd(), new ClearKitCmd(), new CombatLogCmd(), new EcoGiveCmd(), new EcoSetCmd(),
                new KitsCmd(), new PayCmd(), new SetSpawnCmd(), new SpawnCmd(), new StatsCmd(), new KitShopCmd(),
                new StaffCmd(), new EcoTakeCmd(), new ArmorColorCmd(), new KitEnchanterCmd(), new SoupCmd(),
                new PotionsCmd());

        // Loads the plugin's kits.
        loadKits(new Archer(), new Burrower(), new Cactus(), new Dragon(), new Eskimo(), new Fisherman(), new Ghost(),
                new Tamer(), new Hulk(), new Imprisoner(), new Kangaroo(), new Knight(), new Mage(), new Monk(),
                new Ninja(), new Pyro(), new Spiderman(), new Summoner(), new Tank(), new Thor(), new Timelord(),
                new Vampire(), new Zen());

        // Loads the spawn.
        Spawn.getInstance().load();
    }

    @Override
    public void onDisable() {
        // Unloads the kits saved in the Kit Manager.
        KitManager.getInstance().unloadKits();

        // Saves the spawn.
        Spawn.getInstance().save();

        // Saves online players' data.
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData.getInstance(player).saveAll();

            if (CombatLog.getInstance().isInCombat(player)) {
                CombatLog.getInstance().remove(player);
            }
        }

        // Closes the MySQL connection.
        if (hikari != null) {
            hikari.close();
        }
    }

    public HikariDataSource getHikari() {
        return hikari;
    }

    public void giveDefaultItems(Player player) {
        PlayerData playerData = PlayerData.getInstance(player);

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        ItemStack kitSelector = new ItemBuilder(Material.NETHER_STAR).name("&aKit Selector &7(Right Click)").build();
        player.getInventory().setItem(0, kitSelector);

        ItemStack shopSelector = new ItemBuilder(Material.ENDER_CHEST).name("&aKit Shop &7(Right Click)").build();
        player.getInventory().setItem(1, shopSelector);

        ItemStack previousKit = new ItemBuilder(Material.WATCH).name("&aPrevious Kit &7(Right Click)").build();
        player.getInventory().setItem(2, previousKit);

        ItemStack yourStats = new ItemBuilder(SkullCreator.itemFromUuid(player.getUniqueId())).name("&aYour Stats &7(Right Click)").build();
        player.getInventory().setItem(4, yourStats);

        ItemStack healingItem;
        if (playerData.isUsingSoup()) {
            healingItem = new ItemBuilder(Material.POTION).durability(16421).name("&aUse Potions &7(Right Click)").build();
        } else {
            healingItem = new ItemBuilder(Material.MUSHROOM_SOUP).name("&aUse Soup &7(Right Click)").build();
        }
        player.getInventory().setItem(6, healingItem);

        if (player.hasPermission("kitpvp.staff")) {
            ItemStack staffMode = new ItemBuilder(Material.EYE_OF_ENDER).name("&aStaff Mode &7(Right Click)").build();
            player.getInventory().setItem(8, staffMode);
        }
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
        for (Kit kit : kits) {
            KitManager.getInstance().registerKit(kit);
        }
    }


}
