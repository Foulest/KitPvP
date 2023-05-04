package net.foulest.kitpvp.data;

import lombok.Getter;
import lombok.Setter;
import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.util.DatabaseUtil;
import net.foulest.kitpvp.util.ItemBuilder;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.SkullCreatorUtil;
import net.foulest.kitpvp.util.kits.Kit;
import net.foulest.kitpvp.util.kits.KitManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;

/**
 * @author Foulest
 * @project KitPvP
 * <p>
 * Main class for storing player data.
 */
@Getter
@Setter
public final class PlayerData {

    private static final Set<PlayerData> INSTANCES = new HashSet<>();
    private final Player player;
    private final List<Kit> ownedKits = new ArrayList<>();
    private Map<Kit, Long> cooldowns = new HashMap<>();
    private BukkitTask abilityCooldownNotifier;
    private BukkitTask teleportingToSpawn;
    private Kit kit;
    private Kit previousKit = KitManager.getKit("Knight");
    private int coins;
    private int kills;
    private int experience;
    private int level;
    private int deaths;
    private int killstreak;
    private int topKillstreak;
    private int bounty;
    private UUID benefactor;
    private boolean noFall;
    private boolean usingSoup;
    private boolean featherFallingEnchant;
    private boolean thornsEnchant;
    private boolean protectionEnchant;
    private boolean knockbackEnchant;
    private boolean sharpnessEnchant;
    private boolean punchEnchant;
    private boolean powerEnchant;
    private boolean pendingNoFallRemoval;

    private PlayerData(Player player) {
        this.player = player;
        INSTANCES.add(this);
    }

    /**
     * Returns the player's PlayerData.
     */
    public static PlayerData getInstance(Player player) {
        if (INSTANCES.isEmpty()) {
            new PlayerData(player);
        }

        for (PlayerData playerData : INSTANCES) {
            if (playerData == null || playerData.getPlayer() == null
                || playerData.getPlayer().getUniqueId() == null
                || player == null || player.getUniqueId() == null) {
                MessageUtil.log(Level.WARNING, "Player data for player '" + player + "' is null");
                return null;
            }

            if (playerData.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                return playerData;
            }
        }

        return new PlayerData(player);
    }

    /**
     * Checks if a player has a cooldown for the kit they have equipped.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasCooldown(boolean sendMessage) {
        long cooldown = cooldowns.containsKey(kit) ? (cooldowns.get(kit) - System.currentTimeMillis()) : 0L;

        if (cooldown > 0) {
            if (sendMessage) {
                MessageUtil.messagePlayer(player, "&cYou are still on cooldown for %time% seconds.".replace("%time%",
                        String.valueOf(BigDecimal.valueOf((double) cooldown / 1000)
                                .setScale(1, RoundingMode.HALF_UP).doubleValue())));
            }
            return true;
        }
        return false;
    }

    /**
     * Clears a player's cooldowns for their kit.
     */
    public void clearCooldowns() {
        cooldowns.clear();

        if (abilityCooldownNotifier != null) {
            abilityCooldownNotifier.cancel();
            abilityCooldownNotifier = null;
        }
    }

    public void setCooldown(Kit kit, int cooldownTime, boolean notify) {
        cooldowns.put(kit, System.currentTimeMillis() + cooldownTime * 1000L);

        if (notify) {
            abilityCooldownNotifier = new BukkitRunnable() {
                @Override
                public void run() {
                    MessageUtil.messagePlayer(player, MessageUtil.colorize("&aYour ability cooldown has expired."));
                    cooldowns.remove(kit);
                }
            }.runTaskLater(KitPvP.instance, cooldownTime * 20L);
        }
    }

    public boolean load() {
        // Inserts default values into PlayerStats.
        if (!DatabaseUtil.exists("*", "PlayerStats", "uuid", "=", player.getUniqueId().toString())) {
            MessageUtil.log(Level.INFO, "Player doesn't exist, inserting into database.");

            DatabaseUtil.update("INSERT INTO PlayerStats (uuid, coins, experience, kills, deaths, killstreak, topKillstreak, usingSoup, previousKit)"
                                + " VALUES ('" + player.getUniqueId().toString() + "', " + 500 + ", " + 0 + ", " + 0 + ", " + 0 + ", " + 0 + ", " + 0 + ", " + true + ", 'Knight')");

            if (!DatabaseUtil.exists("*", "PlayerStats", "uuid", "=", player.getUniqueId().toString())) {
                MessageUtil.log(Level.WARNING, "Player data '" + player.getName() + "' failed to load.");
                return false;
            }
        }

        // Loads values from PlayerStats.
        try {
            setCoins((Integer) DatabaseUtil.get("coins", "*", "PlayerStats", "uuid", "=", player.getUniqueId().toString()));
            setExperience((Integer) DatabaseUtil.get("experience", "*", "PlayerStats", "uuid", "=", player.getUniqueId().toString()));
            setKills((Integer) DatabaseUtil.get("kills", "*", "PlayerStats", "uuid", "=", player.getUniqueId().toString()));
            setDeaths((Integer) DatabaseUtil.get("deaths", "*", "PlayerStats", "uuid", "=", player.getUniqueId().toString()));
            setKillstreak((Integer) DatabaseUtil.get("killstreak", "*", "PlayerStats", "uuid", "=", player.getUniqueId().toString()));
            setTopKillstreak((Integer) DatabaseUtil.get("topKillstreak", "*", "PlayerStats", "uuid", "=", player.getUniqueId().toString()));
            setUsingSoup((Boolean) DatabaseUtil.get("usingSoup", "*", "PlayerStats", "uuid", "=", player.getUniqueId().toString()));
            setPreviousKit(KitManager.getKit((String) DatabaseUtil.get("previousKit", "*", "PlayerStats", "uuid", "=", player.getUniqueId().toString())));

        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }

        // Inserts default values into PlayerKits.
        if (!DatabaseUtil.exists("*", "PlayerKits", "uuid", "=", player.getUniqueId().toString())) {
            DatabaseUtil.update("INSERT INTO PlayerKits (uuid, kitName)" +
                                " VALUES ('" + player.getUniqueId().toString() + "', 'Knight')");
        } else {
            // Loads values from PlayerKits.
            try {
                ResultSet playerKits = DatabaseUtil.query("*", "PlayerKits", "uuid", "=", player.getUniqueId().toString());

                while (playerKits.next()) {
                    ownedKits.add(KitManager.getKit(playerKits.getString("kitName")));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        // Loads values from Bounties.
        try {
            if (DatabaseUtil.exists("*", "Bounties", "uuid", "=", player.getUniqueId().toString())) {
                setBounty((Integer) DatabaseUtil.get("bounty", "*", "Bounties", "uuid", "=", player.getUniqueId().toString()));
                setBenefactor(UUID.fromString((String) DatabaseUtil.get("benefactor", "*", "Bounties", "uuid", "=", player.getUniqueId().toString())));
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }

        // Inserts default values into Enchants.
        if (!DatabaseUtil.exists("*", "Enchants", "uuid", "=", player.getUniqueId().toString())) {
            DatabaseUtil.update("INSERT INTO Enchants (uuid, featherFalling, thorns, protection, knockback, sharpness, punch, power)"
                                + " VALUES ('" + player.getUniqueId().toString() + "', " + false + ", " + false + ", " + false + ", " + false + ", " + false + ", " + false + ", " + false + ")");

            setFeatherFallingEnchant(false);
            setThornsEnchant(false);
            setProtectionEnchant(false);
            setKnockbackEnchant(false);
            setSharpnessEnchant(false);
            setPunchEnchant(false);
            setPowerEnchant(false);

        } else {
            // Loads values from Enchants.
            try {
                if (DatabaseUtil.exists("featherFalling", "Enchants", "uuid", "=", player.getUniqueId().toString())) {
                    setFeatherFallingEnchant((Boolean) DatabaseUtil.get("featherFalling", "*", "Enchants", "uuid", "=", player.getUniqueId().toString()));
                }

                if (DatabaseUtil.exists("thorns", "Enchants", "uuid", "=", player.getUniqueId().toString())) {
                    setThornsEnchant((Boolean) DatabaseUtil.get("thorns", "*", "Enchants", "uuid", "=", player.getUniqueId().toString()));
                }

                if (DatabaseUtil.exists("protection", "Enchants", "uuid", "=", player.getUniqueId().toString())) {
                    setProtectionEnchant((Boolean) DatabaseUtil.get("protection", "*", "Enchants", "uuid", "=", player.getUniqueId().toString()));
                }

                if (DatabaseUtil.exists("knockback", "Enchants", "uuid", "=", player.getUniqueId().toString())) {
                    setKnockbackEnchant((Boolean) DatabaseUtil.get("knockback", "*", "Enchants", "uuid", "=", player.getUniqueId().toString()));
                }

                if (DatabaseUtil.exists("sharpness", "Enchants", "uuid", "=", player.getUniqueId().toString())) {
                    setSharpnessEnchant((Boolean) DatabaseUtil.get("sharpness", "*", "Enchants", "uuid", "=", player.getUniqueId().toString()));
                }

                if (DatabaseUtil.exists("punch", "Enchants", "uuid", "=", player.getUniqueId().toString())) {
                    setPunchEnchant((Boolean) DatabaseUtil.get("punch", "*", "Enchants", "uuid", "=", player.getUniqueId().toString()));
                }

                if (DatabaseUtil.exists("power", "Enchants", "uuid", "=", player.getUniqueId().toString())) {
                    setPowerEnchant((Boolean) DatabaseUtil.get("power", "*", "Enchants", "uuid", "=", player.getUniqueId().toString()));
                }

            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
        }

        return true;
    }

    public void saveAll() {
        if (!ownedKits.isEmpty()) {
            DatabaseUtil.update("DELETE FROM PlayerKits WHERE uuid='" + player.getUniqueId().toString() + "'");

            for (Kit kits : ownedKits) {
                if (kits == null) {
                    continue;
                }

                DatabaseUtil.update("INSERT INTO PlayerKits (uuid, kitName)" +
                                    " VALUES ('" + player.getUniqueId().toString() + "', '" + kits.getName() + "');");
            }
        }

        saveStats();
    }

    public void saveStats() {
        if (previousKit == null) {
            previousKit = KitManager.getKit("Knight");
        }

        DatabaseUtil.update("UPDATE PlayerStats SET"
                            + " coins=" + coins
                            + ", experience=" + experience
                            + ", kills=" + kills
                            + ", deaths=" + deaths
                            + ", killstreak=" + killstreak
                            + ", topKillstreak=" + topKillstreak
                            + ", usingSoup=" + usingSoup
                            + ", previousKit='" + previousKit.getName()
                            + "' WHERE uuid='" + player.getUniqueId().toString() + "'");

        DatabaseUtil.update("UPDATE Bounties SET"
                            + " bounty=" + bounty
                            + ", benefactor='" + benefactor
                            + "' WHERE uuid='" + player.getUniqueId().toString() + "'");

        DatabaseUtil.update("UPDATE Enchants SET"
                            + " featherFalling=" + featherFallingEnchant
                            + ", thorns=" + thornsEnchant
                            + ", protection=" + protectionEnchant
                            + ", knockback=" + knockbackEnchant
                            + ", sharpness=" + sharpnessEnchant
                            + ", punch=" + punchEnchant
                            + ", power=" + powerEnchant
                            + " WHERE uuid='" + player.getUniqueId().toString() + "'");
    }

    public void addBounty(int bounty, UUID benefactor) {
        if (bounty > 0) {
            removeBounty();
        }

        this.bounty = bounty;
        this.benefactor = benefactor;

        DatabaseUtil.update("INSERT INTO Bounties (uuid, bounty, benefactor)" +
                            " VALUES ('" + player.getUniqueId().toString() + "', " + bounty + ", '" + benefactor + "');");
    }

    public void removeBounty() {
        if (bounty > 0) {
            bounty = 0;
            benefactor = null;

            DatabaseUtil.update("DELETE FROM Bounties WHERE uuid='" + player.getUniqueId().toString() + "'");
        }
    }

    public void unload() {
        ownedKits.clear();
        kit = null;
        benefactor = null;

        INSTANCES.remove(this);
    }

    public void setKillstreak(int streak) {
        killstreak = streak;
        setTopKillstreak();
    }

    public void addKillstreak() {
        killstreak += 1;
        setTopKillstreak();
    }

    public void setTopKillstreak() {
        if (killstreak > topKillstreak) {
            topKillstreak = killstreak;
            saveStats();
        }
    }

    public double getKDR() {
        return (getDeaths() == 0) ? getKills() : getKills() / (double) getDeaths();
    }

    public String getKDRText() {
        String decimalFormatStr = "####0.00";
        DecimalFormat format = new DecimalFormat(decimalFormatStr);

        return format.format(getKDR());
    }

    public void setExperience(int exp) {
        experience = exp;
        calcLevel(false);
    }

    public void addExperience(int exp) {
        if (exp == 0) {
            return;
        }

        experience += exp;
        calcLevel(true);
    }

    public float getExpDecimal() {
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

    public int getExpPercent() {
        double percent;
        int nextLevelXP = (getLevel() * 25) * 25;
        int pastLevelXP = (Math.max(1, getLevel() - 1) * 25) * 25;

        if (getLevel() == 1) {
            percent = ((double) getExperience() / nextLevelXP) * 100;
        } else {
            percent = ((double) (getExperience() - pastLevelXP) / (nextLevelXP - pastLevelXP)) * 100;
        }

        return (int) percent;
    }

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
                    setCoins(getCoins() + 250);
                }
            }
        }

        player.setLevel(level);
        player.setExp(getExpDecimal());
    }

    public void removeCoins(int coins) {
        if (coins == 0) {
            return;
        }

        this.coins = Math.max(0, this.coins - coins);
    }

    public void addCoins(int coins) {
        if (coins == 0) {
            return;
        }

        this.coins += coins;
    }

    public void giveDefaultItems() {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        ItemStack kitSelector = new ItemBuilder(Material.NETHER_STAR).name("&aKit Selector &7(Right Click)").getItem();
        player.getInventory().setItem(0, kitSelector);

        ItemStack shopSelector = new ItemBuilder(Material.ENDER_CHEST).name("&aKit Shop &7(Right Click)").getItem();
        player.getInventory().setItem(1, shopSelector);

        ItemStack previousKit = new ItemBuilder(Material.WATCH).name("&aPrevious Kit &7(Right Click)").getItem();
        player.getInventory().setItem(2, previousKit);

        ItemStack yourStats = new ItemBuilder(SkullCreatorUtil.itemFromUuid(player.getUniqueId())).name("&aYour Stats &7(Right Click)").getItem();
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
