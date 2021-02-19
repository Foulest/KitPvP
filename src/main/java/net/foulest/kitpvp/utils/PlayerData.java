package net.foulest.kitpvp.utils;

import com.lunarclient.bukkitapi.LunarClientAPI;
import com.lunarclient.bukkitapi.object.LCCooldown;
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
import java.util.concurrent.TimeUnit;

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public final class PlayerData {

    static final Set<PlayerData> INSTANCES = new HashSet<>();
    private final Player player;
    private final Map<Kit, Long> cooldowns = new HashMap<>();
    private final KitPvP kitPvP = KitPvP.getInstance();
    private final MySQL mySQL = MySQL.getInstance();
    private final LunarClientAPI lunarClientAPI = LunarClientAPI.getInstance();
    private final KitManager kitManager = KitManager.getInstance();
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
    private boolean isLoaded;
    private boolean pendingNoFallRemoval;

    private PlayerData(Player player) {
        this.player = player;
        previousKit = KitManager.getInstance().valueOf("Knight");
        kit = null;

        INSTANCES.add(this);
    }

    public static PlayerData getInstance(Player player) {
        for (PlayerData playerData : INSTANCES) {
            if (playerData != null && playerData.getPlayer() != null && playerData.getPlayer().isOnline()
                    && playerData.getPlayer().getName().equalsIgnoreCase(player.getName())) {
                return playerData;
            }
        }

        return new PlayerData(player);
    }

    public Player getPlayer() {
        return player;
    }

    public Kit getKit() {
        return kit;
    }

    public void setKit(Kit kit) {
        this.kit = kit;
    }

    public boolean hasKit() {
        return kit != null;
    }

    public Kit getPreviousKit() {
        return previousKit;
    }

    public void setPreviousKit(Kit kit) {
        previousKit = kit;
    }

    public boolean hasPreviousKit() {
        return previousKit != null;
    }

    public boolean ownsKit(Kit kit) {
        return ownedKits.contains(kit);
    }

    public void addOwnedKit(Kit kit) {
        ownedKits.add(kit);
    }

    public List<Kit> getKits() {
        return ownedKits;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public void addCoins(int coins) {
        this.coins += coins;
    }

    public void removeCoins(int coins) {
        this.coins = Math.max(this.coins - coins, 0);
    }

    public int getBounty() {
        return bounty;
    }

    public void setBounty(int bounty) {
        this.bounty = bounty;
    }

    public void removeBounty() {
        bounty = 0;
    }

    public UUID getBenefactor() {
        return benefactor;
    }

    public void setBenefactor(UUID benefactor) {
        this.benefactor = benefactor;
    }

    public void removeBenefactor() {
        benefactor = null;
    }

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

    public long getCooldown() {
        return cooldowns.containsKey(kit) ? (cooldowns.get(kit) - System.currentTimeMillis()) : 0L;
    }

    public void clearCooldowns() {
        cooldowns.clear();

        if (hasKit() && lunarClientAPI.isRunningLunarClient(player)) {
            lunarClientAPI.clearCooldown(player, new LCCooldown("Ability", 0L, TimeUnit.SECONDS,
                    getKit().getDisplayItem().getType()));
        }

        if (abilityCooldownNotifier != null) {
            abilityCooldownNotifier.cancel();
            abilityCooldownNotifier = null;
        }
    }

    public void setCooldown(Kit kit, Material icon, int cooldownTime, boolean notify) {
        cooldowns.put(kit, System.currentTimeMillis() + cooldownTime * 1000L);

        if (hasKit() && lunarClientAPI.isRunningLunarClient(player)) {
            lunarClientAPI.sendCooldown(player, new LCCooldown("Ability", cooldownTime, TimeUnit.SECONDS, icon));
        }

        if (notify) {
            abilityCooldownNotifier = new BukkitRunnable() {
                @Override
                public void run() {
                    MessageUtil.messagePlayer(player, MessageUtil.colorize("&aYour ability cooldown has expired."));
                    cooldowns.remove(kit);
                }
            }.runTaskLater(kitPvP, cooldownTime * 20L);
        }
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public void load() throws SQLException {
        if (!mySQL.exists("*", "PlayerStats", "uuid", "=", player.getUniqueId().toString())) {
            mySQL.update("INSERT INTO PlayerStats (uuid, coins, experience, kills, deaths, killstreak," +
                    " topKillstreak, usingSoup, previousKit)" + " VALUES ('" + player.getUniqueId().toString()
                    + "', " + 500 + ", " + 0 + ", " + 0 + ", " + 0 + ", " + 0 + ", " + 0 + ", " + true + ", 'Knight')");
        }

        if (!mySQL.exists("*", "PlayerKits", "uuid", "=", player.getUniqueId().toString())) {
            mySQL.update("INSERT INTO PlayerKits (uuid, kitName) VALUES ('" + player.getUniqueId().toString() + "', 'Knight')");
        } else {
            ResultSet result;

            try (Connection connection = kitPvP.getHikari().getConnection();
                 PreparedStatement select = connection.prepareStatement("SELECT * FROM PlayerKits WHERE uuid='" + player.getUniqueId().toString() + "'")) {
                result = select.executeQuery();

                while (result.next()) {
                    ownedKits.add(kitManager.valueOf(result.getString("kitName")));
                }

            } catch (SQLException e) {
                // ignored
            }
        }

        if (mySQL.exists("*", "Bounties", "uuid", "=", player.getUniqueId().toString())) {
            setBounty((Integer) mySQL.get("bounty", "*", "Bounties", "uuid", "=", player.getUniqueId().toString()));
            setBenefactor((UUID) mySQL.get("benefactor", "*", "Bounties", "uuid", "=", player.getUniqueId().toString()));
        }

        setCoins((Integer) mySQL.get("coins", "*", "PlayerStats", "uuid", "=", player.getUniqueId().toString()));
        setExperience((Integer) mySQL.get("experience", "*", "PlayerStats", "uuid", "=", player.getUniqueId().toString()));
        setKills((Integer) mySQL.get("kills", "*", "PlayerStats", "uuid", "=", player.getUniqueId().toString()));
        setDeaths((Integer) mySQL.get("deaths", "*", "PlayerStats", "uuid", "=", player.getUniqueId().toString()));
        setKillstreak((Integer) mySQL.get("killstreak", "*", "PlayerStats", "uuid", "=", player.getUniqueId().toString()));
        setTopKillstreak((Integer) mySQL.get("topKillstreak", "*", "PlayerStats", "uuid", "=", player.getUniqueId().toString()));
        setUsingSoup((Boolean) mySQL.get("usingSoup", "*", "PlayerStats", "uuid", "=", player.getUniqueId().toString()));
        setPreviousKit(KitManager.getInstance().valueOf((String) mySQL.get("previousKit", "*", "PlayerStats", "uuid", "=", player.getUniqueId().toString())));

        isLoaded = true;
    }

    public void saveAll() {
        if (!ownedKits.isEmpty()) {
            mySQL.update("DELETE FROM PlayerKits WHERE uuid='" + player.getUniqueId().toString() + "'");

            for (Kit kits : ownedKits) {
                if (kits == null) {
                    continue;
                }

                mySQL.update("INSERT INTO PlayerKits (uuid, kitName) VALUES ('" + player.getUniqueId().toString() + "', '" + kits.getName() + "');");
            }
        }

        saveStats();
    }

    public void saveStats() {
        mySQL.update("UPDATE PlayerStats SET coins=" + coins + ", experience=" + experience
                + ", kills=" + kills + ", deaths=" + deaths + ", killstreak=" + killstreak
                + ", topKillstreak=" + topKillstreak + ", usingSoup=" + usingSoup
                + ", previousKit='" + previousKit.getName() + "' WHERE uuid='" + player.getUniqueId().toString() + "'");

        if (bounty != 0 && benefactor != null) {
            mySQL.update("UPDATE Bounties SET bounty=" + bounty + ", benefactor='" + benefactor
                    + "' WHERE uuid='" + player.getUniqueId().toString() + "'");
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

    public void addDeath() {
        deaths += 1;
    }

    public void addKill() {
        kills += 1;
    }

    public int getKillstreak() {
        return killstreak;
    }

    public void setKillstreak(int streak) {
        killstreak = streak;
        setTopKillstreak();
    }

    public void addKillstreak() {
        killstreak += 1;
        setTopKillstreak();
    }

    public void resetKillStreak() {
        killstreak = 0;
    }

    public int getTopKillstreak() {
        return topKillstreak;
    }

    public void setTopKillstreak(int streak) {
        topKillstreak = streak;
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

    public boolean isPendingNoFallRemoval() {
        return pendingNoFallRemoval;
    }

    public void setPendingNoFallRemoval(boolean value) {
        pendingNoFallRemoval = value;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public int getLevel() {
        return level;
    }

    public int getExperience() {
        return experience;
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
                    player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
                    MessageUtil.messagePlayer(player, "");
                    MessageUtil.messagePlayer(player, " &b&lLevel Up");
                    MessageUtil.messagePlayer(player, " &7You leveled up to &fLevel " + level + " &7and");
                    MessageUtil.messagePlayer(player, " &7earned yourself &f250 Coins&7!");
                    MessageUtil.messagePlayer(player, "");
                    addCoins(150);
                }
            }
        }

        player.setLevel(level);
        player.setExp(getExpDecimal());
    }

    public boolean isTeleportingToSpawn() {
        return teleportingToSpawn != null;
    }

    public void setTeleportingToSpawn(BukkitTask task) {
        teleportingToSpawn = task;
    }

    public BukkitTask getTeleportingToSpawnTask() {
        return teleportingToSpawn;
    }

    public boolean isUsingSoup() {
        return usingSoup;
    }

    public void setUsingSoup(boolean status) {
        usingSoup = status;
    }
}
