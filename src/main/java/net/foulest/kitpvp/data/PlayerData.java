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
package net.foulest.kitpvp.data;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.enchants.Enchants;
import net.foulest.kitpvp.kits.Kit;
import net.foulest.kitpvp.kits.KitManager;
import net.foulest.kitpvp.util.DatabaseUtil;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.Settings;
import net.foulest.kitpvp.util.item.ItemBuilder;
import net.foulest.kitpvp.util.item.SkullBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Main class for storing player data.
 *
 * @author Foulest
 */
@Getter
@Setter
@ToString
@SuppressWarnings("unused")
public final class PlayerData {

    // Player data
    private final UUID uniqueId;
    private final Player player;

    // Kit data
    private final Set<Kit> ownedKits = new HashSet<>();
    private Kit activeKit;
    private Kit previousKit = KitManager.getKit("Knight");

    // Player stats
    private int coins = Settings.startingCoins;
    private int kills;
    private int experience;
    private int level;
    private int deaths;
    private int killstreak;
    private int topKillstreak;
    private boolean usingSoup;

    // Bounty data
    private int bounty;
    private @Nullable UUID benefactor;

    // Enchant data
    private final Set<Enchants> enchants = EnumSet.noneOf(Enchants.class);

    // No-fall data
    private boolean noFall;
    private boolean pendingNoFallRemoval;

    // Cooldowns and timers
    private final Map<Kit, Long> cooldowns = new HashMap<>();
    private @Nullable BukkitTask abilityCooldownNotifier;
    private BukkitTask teleportToSpawnTask;

    /**
     * Creates a new player data object.
     *
     * @param uniqueId The player's UUID.
     * @param player  The player object.
     */
    public PlayerData(@NotNull UUID uniqueId, Player player) {
        this.uniqueId = uniqueId;
        this.player = player;
    }

    /**
     * Checks if the player has a cooldown for their active kit.
     *
     * @param sendMessage Whether to send a message to the player.
     * @return Whether the player has a cooldown.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasCooldown(boolean sendMessage) {
        long cooldown = cooldowns.containsKey(activeKit) ? (cooldowns.get(activeKit) - System.currentTimeMillis()) : 0L;

        if (cooldown > 0) {
            if (sendMessage) {
                MessageUtil.messagePlayer(player, "&cYou are still on cooldown for %time% seconds."
                        .replace("%time%", String.valueOf(BigDecimal.valueOf((double) cooldown / 1000)
                                .setScale(1, RoundingMode.HALF_UP).doubleValue())));
            }
            return true;
        }
        return false;
    }

    /**
     * Clears all cooldowns.
     */
    public void clearCooldowns() {
        cooldowns.clear();

        if (abilityCooldownNotifier != null) {
            abilityCooldownNotifier.cancel();
            abilityCooldownNotifier = null;
        }
    }

    /**
     * Sets a cooldown for a specific kit.
     *
     * @param kit          The kit to set the cooldown for.
     * @param cooldownTime The time in seconds for the cooldown.
     * @param notify       Whether to notify the player when the cooldown expires.
     */
    public void setCooldown(Kit kit, int cooldownTime, boolean notify) {
        cooldowns.put(kit, System.currentTimeMillis() + cooldownTime * 1000L);

        if (notify) {
            abilityCooldownNotifier = new BukkitRunnable() {
                @Override
                public void run() {
                    MessageUtil.messagePlayer(player, "&aYour ability cooldown has expired.");
                    cooldowns.remove(kit);
                }
            }.runTaskLater(KitPvP.instance, cooldownTime * 20L);
        }
    }

    /**
     * Loads the player's data from the database.
     *
     * @return Whether the data was loaded successfully.
     */
    public boolean load() {
        // Inserts default values into PlayerStats.
        Map<String, Object> defaultStats = new HashMap<>();
        defaultStats.put("uuid", uniqueId.toString());
        defaultStats.put("coins", 500);
        defaultStats.put("experience", 0);
        defaultStats.put("kills", 0);
        defaultStats.put("deaths", 0);
        defaultStats.put("killstreak", 0);
        defaultStats.put("topKillstreak", 0);
        defaultStats.put("usingSoup", 0);
        defaultStats.put("previousKit", "Knight");
        DatabaseUtil.addDefaultDataToTable("PlayerStats", defaultStats);

        // Loads values from PlayerStats.
        try {
            List<HashMap<String, Object>> data = DatabaseUtil.loadDataFromTable("PlayerStats",
                    "uuid = ?", Collections.singletonList(uniqueId.toString()));

            if (!data.isEmpty()) {
                HashMap<String, Object> playerData = data.get(0);
                coins = (Integer) playerData.get("coins");
                experience = (Integer) playerData.get("experience");
                kills = (Integer) playerData.get("kills");
                deaths = (Integer) playerData.get("deaths");
                killstreak = (Integer) playerData.get("killstreak");
                topKillstreak = (Integer) playerData.get("topKillstreak");
                usingSoup = (Integer) playerData.get("usingSoup") == 1;
                previousKit = KitManager.getKit((String) playerData.get("previousKit"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }

        // Inserts default values into PlayerKits.
        Map<String, Object> defaultKits = new HashMap<>();
        defaultKits.put("uuid", uniqueId.toString());
        defaultKits.put("kitName", "Knight");
        DatabaseUtil.addDefaultDataToTable("PlayerKits", defaultKits);

        // Loads values from PlayerKits.
        try {
            List<HashMap<String, Object>> data = DatabaseUtil.loadDataFromTable("PlayerKits",
                    "uuid = ?", Collections.singletonList(uniqueId.toString()));

            if (!data.isEmpty()) {
                for (HashMap<String, Object> row : data) {
                    ownedKits.add(KitManager.getKit((String) row.get("kitName")));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }

        // Loads values from Bounties.
        try {
            List<HashMap<String, Object>> data = DatabaseUtil.loadDataFromTable("Bounties",
                    "uuid = ?", Collections.singletonList(uniqueId.toString()));

            if (!data.isEmpty()) {
                HashMap<String, Object> playerData = data.get(0);
                bounty = (Integer) playerData.get("bounty");

                if (!playerData.get("benefactor").equals("")) {
                    benefactor = UUID.fromString((String) playerData.get("benefactor"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }

        // Loads values from Enchants.
        try {
            List<HashMap<String, Object>> data = DatabaseUtil.loadDataFromTable("Enchants",
                    "uuid = ?", Collections.singletonList(uniqueId.toString()));

            if (!data.isEmpty()) {
                Map<String, Object> playerData = data.get(0);

                for (Enchants enchant : Enchants.values()) {
                    String key = enchant.getDatabaseName();
                    Object value = playerData.get(key);

                    if (Integer.valueOf(1).equals(value)) {
                        enchants.add(enchant);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Saves the player's data to the database.
     */
    public void saveAll() {
        if (previousKit == null) {
            previousKit = KitManager.getKit("Knight");
        }

        // Updates values in every table.
        DatabaseUtil.updatePlayerKitsTable(this);
        DatabaseUtil.updatePlayerStatsTable(this);
        DatabaseUtil.updateBountiesTable(this);
        DatabaseUtil.updateEnchantsTable(this);
    }

    /**
     * Sets the player's previous kit.
     *
     * @param kit The previous kit.
     */
    public void setPreviousKit(Kit kit) {
        previousKit = kit;

        // Updates values in the PlayerStats table.
        DatabaseUtil.updatePlayerStatsTable(this);
    }

    /**
     * Adds a bounty to the player.
     *
     * @param bounty     The amount of the bounty.
     * @param benefactor The UUID of the benefactor.
     */
    public void addBounty(int bounty, UUID benefactor) {
        if (bounty > 0) {
            removeBounty();
        }

        this.bounty = bounty;
        this.benefactor = benefactor;

        // Updates values in the Bounties table.
        DatabaseUtil.updateBountiesTable(this);
    }

    /**
     * Removes the player's bounty.
     */
    public void removeBounty() {
        if (bounty > 0) {
            bounty = 0;
            benefactor = null;

            // Updates values in the Bounties table.
            DatabaseUtil.updateBountiesTable(this);
        }
    }

    /**
     * Sets the player's kills to a specific amount.
     *
     * @param kills The amount of kills to set.
     */
    public void setKills(int kills) {
        this.kills = kills;

        // Updates values in the PlayerStats table.
        DatabaseUtil.updatePlayerStatsTable(this);
    }

    /**
     * Sets the player's deaths to a specific amount.
     *
     * @param deaths The amount of deaths to set.
     */
    public void setDeaths(int deaths) {
        this.deaths = deaths;

        // Updates values in the PlayerStats table.
        DatabaseUtil.updatePlayerStatsTable(this);
    }

    /**
     * Sets the player's killstreak to a specific amount.
     *
     * @param streak The killstreak to set.
     */
    public void setKillstreak(int streak) {
        killstreak = streak;

        if (killstreak > topKillstreak) {
            topKillstreak = killstreak;
        }

        // Updates values in the PlayerStats table.
        DatabaseUtil.updatePlayerStatsTable(this);
    }

    /**
     * Adds a kill to the player's current killstreak.
     */
    public void addKillstreak() {
        killstreak += 1;

        if (killstreak > topKillstreak) {
            topKillstreak = killstreak;
        }

        // Updates values in the PlayerStats table.
        DatabaseUtil.updatePlayerStatsTable(this);
    }

    /**
     * Sets the player's experience to a specific amount.
     *
     * @param value The experience to set.
     */
    public void setExperience(int value) {
        experience = value;
        calcLevel(false);

        // Updates values in the PlayerStats table.
        DatabaseUtil.updatePlayerStatsTable(this);
    }

    /**
     * Adds experience to the player's current experience.
     *
     * @param value The experience to add.
     */
    public void addExperience(int value) {
        experience += value;
        calcLevel(true);

        // Updates values in the PlayerStats table.
        DatabaseUtil.updatePlayerStatsTable(this);
    }

    /**
     * Removes experience from the player's current experience.
     *
     * @param value The experience to remove.
     */
    public void removeExperience(int value) {
        experience = Math.max(0, experience - value);
        calcLevel(false);

        // Updates values in the PlayerStats table.
        DatabaseUtil.updatePlayerStatsTable(this);
    }

    /**
     * Adds coins to the player's current amount.
     *
     * @param value The amount of coins to add.
     */
    public void addCoins(int value) {
        coins = Math.max(0, coins + value);

        // Updates values in the PlayerStats table.
        DatabaseUtil.updatePlayerStatsTable(this);
    }

    /**
     * Removes coins from the player's current amount.
     *
     * @param value The amount of coins to remove.
     */
    public void removeCoins(int value) {
        coins = Math.max(0, coins - value);

        // Updates values in the PlayerStats table.
        DatabaseUtil.updatePlayerStatsTable(this);
    }

    /**
     * Sets the player's coins to a specific amount.
     *
     * @param value The amount of coins to set.
     */
    public void setCoins(int value) {
        coins = Math.max(0, value);

        // Updates values in the PlayerStats table.
        DatabaseUtil.updatePlayerStatsTable(this);
    }

    /**
     * Gets the player's experience as a decimal.
     *
     * @return The player's experience as a decimal.
     */
    private float getExpDecimal() {
        float decimal;
        String decimalFormatStr = "#####.0#";
        DecimalFormat format = new DecimalFormat(decimalFormatStr);
        int nextLevelXP = (level * 25) * 25;
        int pastLevelXP = (Math.max(1, level - 1) * 25) * 25;

        if (level == 1) {
            decimal = ((float) experience / nextLevelXP);
        } else {
            decimal = ((float) (experience - pastLevelXP) / (nextLevelXP - pastLevelXP));
        }
        return Float.parseFloat(format.format(decimal));
    }

    /**
     * Gets the player's experience as a percent.
     *
     * @return The player's experience as a percent.
     */
    public int getExpPercent() {
        double percent;
        int nextLevelXP = (level * 25) * 25;
        int pastLevelXP = (Math.max(1, level - 1) * 25) * 25;

        if (level == 1) {
            percent = ((double) experience / nextLevelXP) * 100;
        } else {
            percent = ((double) (experience - pastLevelXP) / (nextLevelXP - pastLevelXP)) * 100;
        }
        return (int) percent;
    }

    /**
     * Gets the player's KDR.
     *
     * @return The player's KDR.
     */
    private double getKDR() {
        return (deaths == 0) ? kills : kills / (double) deaths;
    }

    /**
     * Gets the player's KDR as a string.
     *
     * @return The player's KDR as a string.
     */
    public String getKDRText() {
        String decimalFormatStr = "####0.00";
        DecimalFormat format = new DecimalFormat(decimalFormatStr);
        return format.format(getKDR());
    }

    /**
     * Calculates the player's level and sets it to their experience bar.
     *
     * @param afterKill Whether the level up is after a kill.
     */
    public void calcLevel(boolean afterKill) {
        if (experience == 0) {
            level = 1;
        } else {
            while ((double) experience / ((level * 25) * 25) >= 1.0) {
                level += 1;

                if (afterKill) {
                    player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0F, 1.0F);
                    MessageUtil.messagePlayer(player, "");
                    MessageUtil.messagePlayer(player, " &b&lLevel Up");
                    MessageUtil.messagePlayer(player, " &7You leveled up to &fLevel " + level + " &7and");
                    MessageUtil.messagePlayer(player, " &7earned yourself &f250 Coins&7!");
                    setCoins(coins + 250);
                }
            }
        }

        player.setLevel(level);
        player.setExp(getExpDecimal());
    }

    /**
     * Gives the player their default items.
     */
    @SuppressWarnings("LocalVariableHidesMemberVariable")
    public void giveDefaultItems() {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        ItemStack kitSelector = new ItemBuilder(Material.NETHER_STAR).name("&aKit Selector &7(Right Click)").getItem();
        player.getInventory().setItem(0, kitSelector);

        ItemStack shopSelector = new ItemBuilder(Material.ENDER_CHEST).name("&aKit Shop &7(Right Click)").getItem();
        player.getInventory().setItem(1, shopSelector);

        ItemStack previousKit = new ItemBuilder(Material.WATCH).name("&aPrevious Kit &7(Right Click)").getItem();
        player.getInventory().setItem(2, previousKit);

        ItemStack yourStats = new ItemBuilder(SkullBuilder.itemFromUuid(player.getUniqueId())).name("&aYour Stats &7(Right Click)").getItem();
        player.getInventory().setItem(4, yourStats);

        ItemStack healingItem;
        if (usingSoup) {
            healingItem = new ItemBuilder(Material.MUSHROOM_SOUP).name("&aUsing Soup &7(Right Click)").getItem();
        } else {
            healingItem = new ItemBuilder(Material.POTION).hideInfo().durability(16421).name("&aUsing Potions &7(Right Click)").getItem();
        }
        player.getInventory().setItem(6, healingItem);

        ItemStack kitEnchanter = new ItemBuilder(Material.ENCHANTED_BOOK).name("&aKit Enchanter &7(Right Click)").getItem();
        player.getInventory().setItem(7, kitEnchanter);

        player.updateInventory();
    }
}
