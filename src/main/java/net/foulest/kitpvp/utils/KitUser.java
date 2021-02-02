package net.foulest.kitpvp.utils;

import com.lunarclient.bukkitapi.LunarClientAPI;
import com.lunarclient.bukkitapi.object.LCCooldown;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.utils.kits.Kit;
import net.foulest.kitpvp.utils.kits.KitManager;
import org.bukkit.Location;
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

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class KitUser {

    static final Set<KitUser> instances = new HashSet<>();
    private final Player player;
    private final Map<String, Long> cooldowns = new HashMap<>();
    private final KitPvP kitPvP = KitPvP.getInstance();
    private final MySQL mySQL = MySQL.getInstance();
    private final LunarClientAPI lunarAPI = LunarClientAPI.getInstance();
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
    private boolean inStaffMode;
    private boolean isLoaded;
    private boolean pendingNoFallRemoval;
    private String clientBrand;

    private KitUser(Player player) {
        this.player = player;
        this.kit = null;

        instances.add(this);
    }

    public static KitUser getInstance(Player player) {
        for (KitUser users : instances) {
            if (users != null && users.getPlayer() != null && users.getPlayer().isOnline()
                    && users.getPlayer().getName().equalsIgnoreCase(player.getName())) {
                return users;
            }
        }

        return new KitUser(player);
    }

    public boolean isInRegion() {
        WorldGuardPlugin worldGuard = WorldGuardPlugin.inst();
        RegionManager regionManager = worldGuard.getRegionManager(player.getLocation().getWorld());
        ApplicableRegionSet set = regionManager.getApplicableRegions(player.getLocation());

        for (ProtectedRegion region : set) {
            return region != null;
        }

        return false;
    }

    public boolean isInRegion(Location loc) {
        WorldGuardPlugin worldGuard = WorldGuardPlugin.inst();
        RegionManager regionManager = worldGuard.getRegionManager(loc.getWorld());
        ApplicableRegionSet set = regionManager.getApplicableRegions(loc);

        for (ProtectedRegion region : set) {
            return region != null;
        }

        return false;
    }

    public boolean isInSafezone() {
        WorldGuardPlugin worldGuard = WorldGuardPlugin.inst();
        RegionManager regionManager = worldGuard.getRegionManager(player.getLocation().getWorld());
        ApplicableRegionSet set = regionManager.getApplicableRegions(player.getLocation());

        if (isInRegion()) {
            for (ProtectedRegion region : set) {
                return region.getFlag(DefaultFlag.PVP) == StateFlag.State.DENY;
            }
        }

        return false;
    }

    public boolean isInSafezone(Location loc) {
        WorldGuardPlugin worldGuard = WorldGuardPlugin.inst();
        RegionManager regionManager = worldGuard.getRegionManager(loc.getWorld());
        ApplicableRegionSet set = regionManager.getApplicableRegions(loc);

        if (isInRegion()) {
            for (ProtectedRegion region : set) {
                return region.getFlag(DefaultFlag.PVP) == StateFlag.State.DENY;
            }
        }

        return false;
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
        this.previousKit = kit;
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

    public boolean hasCooldown(Player player, String kit) {
        long cooldown = cooldowns.containsKey(kit) ? (cooldowns.get(kit) - System.currentTimeMillis()) : 0L;

        if (cooldown > 0) {
            MiscUtils.messagePlayer(player, "&cYou are still on cooldown for %time% seconds.".replace("%time%",
                    String.valueOf(BigDecimal.valueOf((double) cooldown / 1000)
                            .setScale(1, RoundingMode.HALF_UP).doubleValue())));
            return true;
        }

        return false;
    }

    public long getCooldown(String kit) {
        return cooldowns.containsKey(kit) ? (cooldowns.get(kit) - System.currentTimeMillis()) : 0L;
    }

    public void clearCooldowns() {
        cooldowns.clear();

        if (hasKit() && isOnLunar()) {
            lunarAPI.clearCooldown(player, new LCCooldown("Ability", 0L, TimeUnit.SECONDS,
                    getKit().getDisplayItem().getType()));
        }

        if (abilityCooldownNotifier != null) {
            abilityCooldownNotifier.cancel();
            abilityCooldownNotifier = null;
        }
    }

    public void setCooldown(String kitName, Material icon, int cooldownTime, boolean notify) {
        cooldowns.put(kitName, System.currentTimeMillis() + cooldownTime * 1000L);

        if (hasKit() && isOnLunar()) {
            lunarAPI.sendCooldown(player, new LCCooldown("Ability", cooldownTime, TimeUnit.SECONDS, icon));
        }

        if (notify) {
            abilityCooldownNotifier = new BukkitRunnable() {
                public void run() {
                    if (player != null) {
                        MiscUtils.messagePlayer(player, MiscUtils.colorize("&aYour ability cooldown has expired."));
                    }
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
        isLoaded = true;

        if (!mySQL.exists("*", "PlayerStats", "uuid", "=", player.getUniqueId().toString())) {
            mySQL.update("INSERT INTO PlayerStats (uuid, coins, experience, kills, deaths, killstreak, topKillstreak)" +
                    " VALUES ('" + player.getUniqueId().toString() + "', " + 500 + ", " + 0 + ", " + 0 + ", " + 0 + ", " + 0 + ", " + 0 + ")");
        }

        if (!mySQL.exists("*", "PlayerKits", "uuid", "=", player.getUniqueId().toString())) {
            mySQL.update("INSERT INTO PlayerKits (uuid, kitId) VALUES ('" + player.getUniqueId().toString() + "', " + 10 + ")");
        } else {
            ResultSet result;

            try (Connection connection = kitPvP.getHikari().getConnection();
                 PreparedStatement select = connection.prepareStatement("SELECT * FROM PlayerKits WHERE uuid='" + player.getUniqueId().toString() + "'")) {
                result = select.executeQuery();

                while (result.next()) {
                    ownedKits.add(kitManager.valueOfId(result.getInt("kitId")));
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        setCoins((Integer) mySQL.get("coins", "*", "PlayerStats", "uuid", "=", player.getUniqueId().toString()));
        setExperience((Integer) mySQL.get("experience", "*", "PlayerStats", "uuid", "=", player.getUniqueId().toString()));
        setKills((Integer) mySQL.get("kills", "*", "PlayerStats", "uuid", "=", player.getUniqueId().toString()));
        setDeaths((Integer) mySQL.get("deaths", "*", "PlayerStats", "uuid", "=", player.getUniqueId().toString()));
        setKillstreak((Integer) mySQL.get("killstreak", "*", "PlayerStats", "uuid", "=", player.getUniqueId().toString()));
        setTopKillstreak((Integer) mySQL.get("topKillstreak", "*", "PlayerStats", "uuid", "=", player.getUniqueId().toString()));
    }

    public void saveAll() {
        if (!ownedKits.isEmpty()) {
            mySQL.update("DELETE FROM PlayerKits WHERE uuid='" + player.getUniqueId().toString() + "'");

            for (Kit kits : ownedKits) {
                if (kits == null) {
                    continue;
                }

                mySQL.update("INSERT INTO PlayerKits (uuid, kitId) VALUES ('" + player.getUniqueId().toString() + "', " + kits.getId() + ");");
            }
        }

        saveStats();
    }

    public void saveStats() {
        mySQL.update("UPDATE PlayerStats SET coins=" + getCoins() + ", experience=" + getExperience()
                + ", kills=" + getKills() + ", deaths=" + getDeaths() + ", killstreak=" + getKillstreak()
                + ", topKillstreak=" + getTopKillstreak() + " WHERE uuid='" + player.getUniqueId().toString() + "'");
    }

    public void unload() {
        isLoaded = false;
        ownedKits.clear();
        kit = null;
        previousKit = null;
        teleportingToSpawn = null;
        instances.remove(this);
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

    public boolean isInStaffMode() {
        return inStaffMode;
    }

    public void setStaffMode(boolean value) {
        inStaffMode = value;
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

    public String getClientBrand() {
        return clientBrand;
    }

    public void setClientBrand(String brand) {
        clientBrand = brand;
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
                    MiscUtils.messagePlayer(player, "");
                    MiscUtils.messagePlayer(player, " &b&lLevel Up");
                    MiscUtils.messagePlayer(player, " &7You leveled up to &fLevel " + level + " &7and");
                    MiscUtils.messagePlayer(player, " &7earned yourself &f250 Coins&7!");
                    MiscUtils.messagePlayer(player, "");
                    addCoins(150);
                }
            }
        }

        player.setLevel(level);
        player.setExp(getExpDecimal());
    }

    public boolean isOnLunar() {
        return getClientBrand().contains("lunarclient:") && lunarAPI.isRunningLunarClient(player);
    }

    public boolean isTeleportingToSpawn() {
        return teleportingToSpawn != null;
    }

    public BukkitTask getTeleportingToSpawnTask() {
        return teleportingToSpawn;
    }

    public void setTeleportingToSpawn(BukkitTask task) {
        teleportingToSpawn = task;
    }
}
