package net.foulest.kitpvp.data;

import lombok.Getter;
import lombok.Setter;
import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.enchants.Enchants;
import net.foulest.kitpvp.kits.Kit;
import net.foulest.kitpvp.kits.KitManager;
import net.foulest.kitpvp.util.DatabaseUtil;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.item.ItemBuilder;
import net.foulest.kitpvp.util.item.SkullBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;

import static net.foulest.kitpvp.util.Settings.startingCoins;

/**
 * Main class for storing player data.
 *
 * @author Foulest
 * @project KitPvP
 */
@Getter
@Setter
public final class PlayerData {

    // Player data
    private UUID uniqueId;
    private String uuid; // Needed for SQL queries
    private Player player;

    // Kit data
    private Set<Kit> ownedKits = new HashSet<>();
    private Kit activeKit;
    private Kit previousKit = KitManager.getKit("Knight");

    // Player stats
    private int coins = startingCoins;
    private int kills;
    private int experience;
    private int level;
    private int deaths;
    private int killstreak;
    private int topKillstreak;

    // Bounty data
    private int bounty;
    private UUID benefactor;

    // Cooldowns and timers
    private final Map<Kit, Long> cooldowns = new HashMap<>();
    private BukkitTask abilityCooldownNotifier;
    private BukkitTask teleportToSpawnTask;

    // Enchant data
    private Set<Enchants> enchants = new HashSet<>();

    // Other data
    @Setter
    private boolean usingSoup;
    private boolean noFall;
    private boolean pendingNoFallRemoval;

    public PlayerData(@NotNull UUID uniqueId, Player player) {
        this.uniqueId = uniqueId;
        this.player = player;
        uuid = uniqueId.toString();
    }

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
        DatabaseUtil.addDefaultDataToTable("PlayerStats", new HashMap<String, Object>() {{
            put("uuid", uuid);
            put("coins", 500);
            put("experience", 0);
            put("kills", 0);
            put("deaths", 0);
            put("killstreak", 0);
            put("topKillstreak", 0);
            put("usingSoup", 0);
            put("previousKit", "Knight");
        }});

        // Loads values from PlayerStats.
        try {
            List<HashMap<String, Object>> data = DatabaseUtil.loadDataFromTable("PlayerStats", "uuid = ?", Collections.singletonList(uuid));

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
            MessageUtil.printException(ex);
        }

        // Inserts default values into PlayerKits.
        DatabaseUtil.addDefaultDataToTable("PlayerKits", new HashMap<String, Object>() {{
            put("uuid", uuid);
            put("kitName", "Knight");
        }});

        // Loads values from PlayerKits.
        try {
            List<HashMap<String, Object>> data = DatabaseUtil.loadDataFromTable("PlayerKits", "uuid = ?", Collections.singletonList(uuid));

            if (!data.isEmpty()) {
                for (HashMap<String, Object> row : data) {
                    ownedKits.add(KitManager.getKit((String) row.get("kitName")));
                }
            }
        } catch (SQLException ex) {
            MessageUtil.printException(ex);
        }

        // Loads values from Bounties.
        try {
            List<HashMap<String, Object>> data = DatabaseUtil.loadDataFromTable("Bounties", "uuid = ?", Collections.singletonList(uuid));

            if (!data.isEmpty()) {
                HashMap<String, Object> playerData = data.get(0);
                bounty = (Integer) playerData.get("bounty");

                if (!playerData.get("benefactor").equals("")) {
                    benefactor = UUID.fromString((String) playerData.get("benefactor"));
                }
            }
        } catch (SQLException ex) {
            MessageUtil.printException(ex);
        }

        // Inserts default values into Enchants.
        DatabaseUtil.addDefaultDataToTable("Enchants", new HashMap<String, Object>() {{
            put("uuid", uuid);
            put("featherFalling", 0);
            put("thorns", 0);
            put("protection", 0);
            put("knockback", 0);
            put("sharpness", 0);
            put("punch", 0);
            put("power", 0);
        }});

        // Loads values from Enchants.
        try {
            List<HashMap<String, Object>> data = DatabaseUtil.loadDataFromTable("Enchants", "uuid = ?", Collections.singletonList(uuid));

            if (!data.isEmpty()) {
                HashMap<String, Object> playerData = data.get(0);

                for (Enchants enchant : Enchants.values()) {
                    String key = enchant.getDatabaseName();
                    Object value = playerData.get(key);

                    if (Integer.valueOf(1).equals(value)) {
                        enchants.add(enchant);
                    }
                }
            }
        } catch (SQLException ex) {
            MessageUtil.printException(ex);
        }
        return true;
    }

    public void saveAll() {
        if (previousKit == null) {
            previousKit = KitManager.getKit("Knight");
        }

        // Saves values to PlayerKits.
        updatePlayerKitsTable();

        // Saves values to PlayerStats.
        updatePlayerStatsTable();

        // Saves values to Bounties.
        if (bounty > 0 && benefactor != null) {
            updateBountiesTable();
        }

        // Saves values to Enchants.
        updateEnchantsTable();
    }

    public void addBounty(int bounty, UUID benefactor) {
        if (bounty > 0) {
            removeBounty();
        }

        this.bounty = bounty;
        this.benefactor = benefactor;
        updateBountiesTable();
    }

    public void removeBounty() {
        if (bounty > 0) {
            bounty = 0;
            benefactor = null;

            DatabaseUtil.deleteDataFromTable("Bounties", "uuid = ?",
                    Collections.singletonList(player.getUniqueId().toString()));
        }
    }

    public void setKillstreak(int streak) {
        killstreak = streak;

        if (killstreak > topKillstreak) {
            topKillstreak = killstreak;
        }

        updatePlayerStatsTable();
    }

    public void addKillstreak() {
        killstreak += 1;

        if (killstreak > topKillstreak) {
            topKillstreak = killstreak;
        }

        updatePlayerStatsTable();
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
        updatePlayerStatsTable();
    }

    public void addExperience(int exp) {
        if (exp == 0) {
            return;
        }

        experience += exp;
        calcLevel(true);
        updatePlayerStatsTable();
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

    public void addCoins(int value) {
        if (value == 0) {
            return;
        }

        this.coins += value;
        updatePlayerStatsTable();
    }

    public void removeCoins(int value) {
        if (value == 0) {
            return;
        }

        this.coins = Math.max(0, this.coins - value);
        updatePlayerStatsTable();
    }

    public void setCoins(int value) {
        this.coins = Math.max(0, value);
        updatePlayerStatsTable();
    }

    public void setPreviousKit(Kit kit) {
        previousKit = kit;
        updatePlayerStatsTable();
    }

    public void setDeaths(int value) {
        deaths = value;
        updatePlayerStatsTable();
    }

    public void setKills(int value) {
        kills = value;
        updatePlayerStatsTable();
    }

    public void addEnchant(Enchants enchant) {
        enchants.add(enchant);
        updateEnchantsTable();
    }

    public void removeEnchant(Enchants enchant) {
        enchants.remove(enchant);
        updateEnchantsTable();
    }

    public void removeAllEnchants() {
        enchants.clear();
        updateEnchantsTable();
    }

    public void updatePlayerStatsTable() {
        DatabaseUtil.addDataToTable("PlayerStats", new HashMap<String, Object>() {{
            put("uuid", uuid);
            put("coins", coins);
            put("experience", experience);
            put("kills", kills);
            put("deaths", deaths);
            put("killstreak", killstreak);
            put("topKillstreak", topKillstreak);
            put("usingSoup", (usingSoup ? 1 : 0));
            put("previousKit", previousKit.getName());
        }});
    }

    public void updatePlayerKitsTable() {
        if (!ownedKits.isEmpty()) {
            DatabaseUtil.deleteDataFromTable("PlayerKits", "uuid = ?", Collections.singletonList(player.getUniqueId().toString()));

            for (Kit kits : ownedKits) {
                if (kits == null) {
                    continue;
                }

                DatabaseUtil.addDataToTable("PlayerKits", new HashMap<String, Object>() {{
                    put("uuid", uuid);
                    put("kitName", kits.getName());
                }});
            }
        }
    }

    public void updateEnchantsTable() {
        DatabaseUtil.addDataToTable("Enchants", new HashMap<String, Object>() {{
            put("uuid", uuid);
            put("featherFalling", enchants.contains(Enchants.FEATHER_FALLING) ? 1 : 0);
            put("thorns", enchants.contains(Enchants.THORNS) ? 1 : 0);
            put("protection", enchants.contains(Enchants.PROTECTION) ? 1 : 0);
            put("knockback", enchants.contains(Enchants.KNOCKBACK) ? 1 : 0);
            put("sharpness", enchants.contains(Enchants.SHARPNESS) ? 1 : 0);
            put("punch", enchants.contains(Enchants.PUNCH) ? 1 : 0);
            put("power", enchants.contains(Enchants.POWER) ? 1 : 0);
        }});
    }

    public void updateBountiesTable() {
        DatabaseUtil.addDataToTable("Bounties", new HashMap<String, Object>() {{
            put("uuid", uuid);
            put("bounty", bounty);
            put("benefactor", (benefactor == null ? "" : benefactor.toString()));
        }});
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
