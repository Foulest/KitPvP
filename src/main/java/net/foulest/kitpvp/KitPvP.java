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
package net.foulest.kitpvp;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
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
@ToString
@NoArgsConstructor
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

        // Sets up the database instance.
        MessageUtil.log(Level.INFO, "Loading Database...");
        DatabaseUtil.loadDatabase();

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
        KitManager.getKits().clear();

        // Saves online players' data.
        MessageUtil.log(Level.INFO, "Saving Player Data...");
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (PlayerDataManager.hasPlayerData(player)) {
                PlayerData playerData = PlayerDataManager.getPlayerData(player);
                playerData.saveAll();
            }
        }

        // Closes the DBCP connection.
        MessageUtil.log(Level.INFO, "Saving Database...");
        DatabaseUtil.closeDbcp();

        MessageUtil.log(Level.INFO, "Shut down successfully.");
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
    private static void loadKits(Kit... kits) {
        Collections.addAll(KitManager.getKits(), kits);
    }
}
