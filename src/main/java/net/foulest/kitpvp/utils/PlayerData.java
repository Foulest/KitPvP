package net.foulest.kitpvp.utils;

import com.lunarclient.bukkitapi.LunarClientAPI;
import com.lunarclient.bukkitapi.nethandler.client.LCPacketCooldown;
import lombok.Getter;
import lombok.Setter;
import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.utils.kits.Kit;
import net.foulest.kitpvp.utils.kits.KitManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
@Getter
@Setter
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public final class PlayerData {

    private static final Set<PlayerData> INSTANCES = new HashSet<>();
    private static final Map<Kit, Long> COOLDOWNS = new HashMap<>();
    private static final KitPvP KITPVP = KitPvP.getInstance();
    private static final MySQL MYSQL = MySQL.getInstance();
    private static final LunarClientAPI LUNAR_API = LunarClientAPI.getInstance();
    private static final KitManager KIT_MANAGER = KitManager.getInstance();
    private final Player player;
    private final List<Kit> ownedKits = new ArrayList<>();
    private BukkitTask abilityCooldownNotifier;
    private BukkitTask teleportingToSpawn;
    private Kit kit;
    private Kit previousKit;
    private int coins;
    private int kills;
    private int experience;
    private int level;
    private int deaths;
    private int killstreak;
    private int topKillstreak;
    private int bounty;
    private UUID benefactor;
    private boolean usingSoup;
    private boolean featherFallingEnchant;
    private boolean protectionEnchant;
    private boolean sharpnessEnchant;
    private boolean powerEnchant;
    private boolean isLoaded;
    private boolean pendingNoFallRemoval;

    private PlayerData(Player player) {
        this.player = player;
        previousKit = KIT_MANAGER.valueOf("Knight");
        kit = null;

        INSTANCES.add(this);
    }

    /**
     * Returns the player's PlayerData.
     */
    public static PlayerData getInstance(Player player) {
        for (PlayerData playerData : INSTANCES) {
            if (playerData != null && playerData.getPlayer() != null && playerData.getPlayer().isOnline()
                    && playerData.getPlayer().getName().equalsIgnoreCase(player.getName())) {
                return playerData;
            }
        }

        return new PlayerData(player);
    }

    /**
     * Checks if a player has a cooldown for
     * the kit they have equipped.
     */
    public boolean hasCooldown(boolean sendMessage) {
        long cooldown = COOLDOWNS.containsKey(kit) ? (COOLDOWNS.get(kit) - System.currentTimeMillis()) : 0L;

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
        COOLDOWNS.clear();

        if (getKit() != null && LUNAR_API.isRunningLunarClient(player)) {
            LUNAR_API.sendPacket(player, new LCPacketCooldown("Ability", 0L, getKit().getDisplayItem().getType().getId()));
        }

        if (abilityCooldownNotifier != null) {
            abilityCooldownNotifier.cancel();
            abilityCooldownNotifier = null;
        }
    }

    public void setCooldown(Kit kit, Material icon, int cooldownTime, boolean notify) {
        COOLDOWNS.put(kit, System.currentTimeMillis() + cooldownTime * 1000L);

        if (getKit() != null && LUNAR_API.isRunningLunarClient(player)) {
            LUNAR_API.sendPacket(player, new LCPacketCooldown("Ability", cooldownTime * 1000L, icon.getId()));
        }

        if (notify) {
            abilityCooldownNotifier = new BukkitRunnable() {
                @Override
                public void run() {
                    MessageUtil.messagePlayer(player, MessageUtil.colorize("&aYour ability cooldown has expired."));
                    COOLDOWNS.remove(kit);
                }
            }.runTaskLater(KITPVP, cooldownTime * 20L);
        }
    }

    public void load() throws SQLException {
        if (!MYSQL.exists("*", "PlayerStats", "uuid", "=", player.getUniqueId().toString())) {
            MYSQL.update("INSERT INTO PlayerStats (uuid, coins, experience, kills, deaths, killstreak," +
                    " topKillstreak, usingSoup, previousKit)" + " VALUES ('" + player.getUniqueId().toString()
                    + "', " + 500 + ", " + 0 + ", " + 0 + ", " + 0 + ", " + 0 + ", " + 0 + ", " + true + ", 'Knight')");
        }

        if (!MYSQL.exists("*", "PlayerKits", "uuid", "=", player.getUniqueId().toString())) {
            MYSQL.update("INSERT INTO PlayerKits (uuid, kitName) VALUES ('" + player.getUniqueId().toString() + "', 'Knight')");
        } else {
            ResultSet result;

            try (Connection connection = KITPVP.getHikari().getConnection();
                 PreparedStatement select = connection.prepareStatement("SELECT * FROM PlayerKits WHERE uuid='" + player.getUniqueId().toString() + "'")) {
                result = select.executeQuery();

                while (result.next()) {
                    ownedKits.add(KIT_MANAGER.valueOf(result.getString("kitName")));
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        if (MYSQL.exists("*", "Bounties", "uuid", "=", player.getUniqueId().toString())) {
            setBounty((Integer) MYSQL.get("bounty", "*", "Bounties", "uuid", "=", player.getUniqueId().toString()));
            setBenefactor(UUID.fromString((String) MYSQL.get("benefactor", "*", "Bounties", "uuid", "=", player.getUniqueId().toString())));
        }

        setCoins((Integer) MYSQL.get("coins", "*", "PlayerStats", "uuid", "=", player.getUniqueId().toString()));
        setExperience((Integer) MYSQL.get("experience", "*", "PlayerStats", "uuid", "=", player.getUniqueId().toString()));
        setKills((Integer) MYSQL.get("kills", "*", "PlayerStats", "uuid", "=", player.getUniqueId().toString()));
        setDeaths((Integer) MYSQL.get("deaths", "*", "PlayerStats", "uuid", "=", player.getUniqueId().toString()));
        setKillstreak((Integer) MYSQL.get("killstreak", "*", "PlayerStats", "uuid", "=", player.getUniqueId().toString()));
        setTopKillstreak((Integer) MYSQL.get("topKillstreak", "*", "PlayerStats", "uuid", "=", player.getUniqueId().toString()));
        setUsingSoup((Boolean) MYSQL.get("usingSoup", "*", "PlayerStats", "uuid", "=", player.getUniqueId().toString()));
        setPreviousKit(KIT_MANAGER.valueOf((String) MYSQL.get("previousKit", "*", "PlayerStats", "uuid", "=", player.getUniqueId().toString())));

        isLoaded = true;
    }

    public void saveAll() {
        if (!ownedKits.isEmpty()) {
            MYSQL.update("DELETE FROM PlayerKits WHERE uuid='" + player.getUniqueId().toString() + "'");

            for (Kit kits : ownedKits) {
                if (kits == null) {
                    continue;
                }

                MYSQL.update("INSERT INTO PlayerKits (uuid, kitName) VALUES ('" + player.getUniqueId().toString() + "', '" + kits.getName() + "');");
            }
        }

        saveStats();
    }

    public void saveStats() {
        MYSQL.update("UPDATE PlayerStats SET coins=" + coins + ", experience=" + experience
                + ", kills=" + kills + ", deaths=" + deaths + ", killstreak=" + killstreak
                + ", topKillstreak=" + topKillstreak + ", usingSoup=" + usingSoup
                + ", previousKit='" + previousKit.getName() + "' WHERE uuid='" + player.getUniqueId().toString() + "'");

        MYSQL.update("UPDATE Bounties SET bounty=" + bounty + ", benefactor='" + benefactor
                + "' WHERE uuid='" + player.getUniqueId().toString() + "'");
    }

    public void addBounty(int bounty, UUID benefactor) {
        if (bounty > 0) {
            removeBounty();
        }

        this.bounty = bounty;
        this.benefactor = benefactor;

        MYSQL.update("INSERT INTO Bounties (uuid, bounty, benefactor) VALUES ('" + player.getUniqueId().toString() + "', " + bounty + ", '" + benefactor + "');");
    }

    public void removeBounty() {
        if (bounty > 0) {
            bounty = 0;
            benefactor = null;

            MYSQL.update("DELETE FROM Bounties WHERE uuid='" + player.getUniqueId().toString() + "'");
        }
    }

    public void unload() {
        isLoaded = false;
        ownedKits.clear();
        kit = null;
        benefactor = null;
        teleportingToSpawn = null;
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
        return (getDeaths() == 0) ? getKills() : (double) getKills() / (double) getDeaths();
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
                    MessageUtil.messagePlayer(player, "");
                    setCoins(getCoins() + 150);
                }
            }
        }

        player.setLevel(level);
        player.setExp(getExpDecimal());
    }

    public void removeCoins(int coins) {
        this.coins = Math.max(0, this.coins - coins);
    }
}
