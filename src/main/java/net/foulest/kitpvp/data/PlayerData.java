package net.foulest.kitpvp.data;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.util.*;
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
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @author Foulest
 * @project KitPvP
 * <p>
 * Main class for storing player data.
 */
@Getter
@Setter
public final class PlayerData {

    // Player data
    private UUID uniqueId;
    private String uuid; // Needed for SQL queries
    private Player player;

    // Kit data
    private List<Kit> ownedKits = new ArrayList<>();
    private Kit kit;
    private Kit previousKit = KitManager.getKit("Knight");

    // Player stats
    private int coins = Settings.startingCoins;
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
    private BukkitTask teleportingToSpawn;

    // Enchant data
    private boolean featherFallingEnchant;
    private boolean thornsEnchant;
    private boolean protectionEnchant;
    private boolean knockbackEnchant;
    private boolean sharpnessEnchant;
    private boolean punchEnchant;
    private boolean powerEnchant;

    // Other data
    private boolean usingSoup;
    private boolean noFall;
    private boolean pendingNoFallRemoval;

    public PlayerData(UUID uniqueId, Player player) {
        this.uniqueId = uniqueId;
        this.player = player;
        uuid = uniqueId.toString();
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

    public void setCooldown(@NonNull Kit kit, int cooldownTime, boolean notify) {
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
            put("usingSoup", false);
            put("previousKit", "Knight");
        }});

        // Loads values from PlayerStats.
        try {
            List<HashMap<String, Object>> data = DatabaseUtil.loadDataFromTable("PlayerStats", "uuid = ?", Collections.singletonList(uuid));
            if (!data.isEmpty()) {
                HashMap<String, Object> playerData = data.get(0);
                System.out.println(playerData.toString());
                coins = (Integer) playerData.get("coins");
                experience = (Integer) playerData.get("experience");
                kills = (Integer) playerData.get("kills");
                deaths = (Integer) playerData.get("deaths");
                killstreak = (Integer) playerData.get("killstreak");
                topKillstreak = (Integer) playerData.get("topKillstreak");
                usingSoup = (Boolean) playerData.get("usingSoup");
                previousKit = KitManager.getKit((String) playerData.get("previousKit"));
            } else {
                System.out.println("No player found with UUID " + uuid);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
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
            } else {
                System.out.println("No player found with UUID " + uuid);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
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
            ex.printStackTrace();
        }

        // Inserts default values into Enchants.
        DatabaseUtil.addDefaultDataToTable("Enchants", new HashMap<String, Object>() {{
            put("uuid", uuid);
            put("featherFalling", false);
            put("thorns", false);
            put("protection", false);
            put("knockback", false);
            put("sharpness", false);
            put("punch", false);
            put("power", false);
        }});

        // Loads values from Enchants.
        try {
            List<HashMap<String, Object>> data = DatabaseUtil.loadDataFromTable("Enchants", "uuid = ?", Collections.singletonList(uuid));
            if (!data.isEmpty()) {
                HashMap<String, Object> playerData = data.get(0);
                featherFallingEnchant = (Boolean) playerData.get("featherFalling");
                thornsEnchant = (Boolean) playerData.get("thorns");
                protectionEnchant = (Boolean) playerData.get("protection");
                knockbackEnchant = (Boolean) playerData.get("knockback");
                sharpnessEnchant = (Boolean) playerData.get("sharpness");
                punchEnchant = (Boolean) playerData.get("punch");
                powerEnchant = (Boolean) playerData.get("power");
            } else {
                System.out.println("No player found with UUID " + uuid);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return true;
    }

    public void saveAll() {
        if (previousKit == null) {
            previousKit = KitManager.getKit("Knight");
        }

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

        DatabaseUtil.addDataToTable("PlayerStats", new HashMap<String, Object>() {{
            put("uuid", uuid);
            put("coins", coins);
            put("experience", experience);
            put("kills", kills);
            put("deaths", deaths);
            put("killstreak", killstreak);
            put("topKillstreak", topKillstreak);
            put("usingSoup", usingSoup);
            put("previousKit", previousKit.getName());
        }});

        if (bounty > 0 && benefactor != null) {
            DatabaseUtil.addDataToTable("Bounties", new HashMap<String, Object>() {{
                put("uuid", uuid);
                put("bounty", bounty);
                put("benefactor", (benefactor == null ? "" : benefactor.toString()));
            }});
        }

        DatabaseUtil.addDataToTable("Enchants", new HashMap<String, Object>() {{
            put("uuid", uuid);
            put("featherFalling", featherFallingEnchant);
            put("thorns", thornsEnchant);
            put("protection", protectionEnchant);
            put("knockback", knockbackEnchant);
            put("sharpness", sharpnessEnchant);
            put("punch", punchEnchant);
            put("power", powerEnchant);
        }});
    }

    public void addBounty(int bounty, @NonNull UUID benefactor) {
        if (bounty > 0) {
            removeBounty();
        }

        this.bounty = bounty;
        this.benefactor = benefactor;

        DatabaseUtil.addDataToTable("Bounties", new HashMap<String, Object>() {{
            put("uuid", uuid);
            put("bounty", bounty);
            put("benefactor", benefactor.toString());
        }});
    }

    public void removeBounty() {
        if (bounty > 0) {
            bounty = 0;
            benefactor = null;

            DatabaseUtil.deleteDataFromTable("Bounties", "uuid = ?", Collections.singletonList(player.getUniqueId().toString()));
        }
    }

    public void setKillstreak(int streak) {
        killstreak = streak;

        if (killstreak > topKillstreak) {
            topKillstreak = killstreak;
        }

        DatabaseUtil.addDataToTable("PlayerStats", new HashMap<String, Object>() {{
            put("uuid", uuid);
            put("killstreak", killstreak);
            put("topKillstreak", topKillstreak);
        }});
    }

    public void addKillstreak() {
        killstreak += 1;

        if (killstreak > topKillstreak) {
            topKillstreak = killstreak;
        }

        DatabaseUtil.addDataToTable("PlayerStats", new HashMap<String, Object>() {{
            put("uuid", uuid);
            put("killstreak", killstreak);
            put("topKillstreak", topKillstreak);
        }});
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

        DatabaseUtil.addDataToTable("PlayerStats", new HashMap<String, Object>() {{
            put("uuid", uuid);
            put("experience", experience);
        }});
    }

    public void addExperience(int exp) {
        if (exp == 0) {
            return;
        }

        experience += exp;
        calcLevel(true);

        DatabaseUtil.addDataToTable("PlayerStats", new HashMap<String, Object>() {{
            put("uuid", uuid);
            put("experience", experience);
        }});
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

        DatabaseUtil.addDataToTable("PlayerStats", new HashMap<String, Object>() {{
            put("uuid", uuid);
            put("coins", coins);
        }});
    }

    public void removeCoins(int value) {
        if (value == 0) {
            return;
        }

        this.coins = Math.max(0, this.coins - value);

        DatabaseUtil.addDataToTable("PlayerStats", new HashMap<String, Object>() {{
            put("uuid", uuid);
            put("coins", coins);
        }});
    }

    public void setCoins(int value) {
        this.coins = Math.max(0, value);

        DatabaseUtil.addDataToTable("PlayerStats", new HashMap<String, Object>() {{
            put("uuid", uuid);
            put("coins", coins);
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

        ItemStack yourStats = new ItemBuilder(SkullUtil.itemFromUuid(player.getUniqueId())).name("&aYour Stats &7(Right Click)").getItem();
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

    public void setPreviousKit(@NonNull Kit kit) {
        previousKit = kit;

        DatabaseUtil.addDataToTable("PlayerStats", new HashMap<String, Object>() {{
            put("uuid", uuid);
            put("previousKit", previousKit.getName());
        }});
    }

    public void setUsingSoup(boolean value) {
        usingSoup = value;

        DatabaseUtil.addDataToTable("PlayerStats", new HashMap<String, Object>() {{
            put("uuid", uuid);
            put("usingSoup", usingSoup);
        }});
    }

    public void setDeaths(int value) {
        deaths = value;

        DatabaseUtil.addDataToTable("PlayerStats", new HashMap<String, Object>() {{
            put("uuid", uuid);
            put("deaths", deaths);
        }});
    }

    public void setKills(int value) {
        kills = value;

        DatabaseUtil.addDataToTable("PlayerStats", new HashMap<String, Object>() {{
            put("uuid", uuid);
            put("kills", kills);
        }});
    }

    public void setFeatherFallingEnchant(boolean value) {
        featherFallingEnchant = value;

        DatabaseUtil.addDataToTable("Enchants", new HashMap<String, Object>() {{
            put("uuid", uuid);
            put("featherFalling", featherFallingEnchant);
        }});
    }

    public void setThornsEnchant(boolean value) {
        thornsEnchant = value;

        DatabaseUtil.addDataToTable("Enchants", new HashMap<String, Object>() {{
            put("uuid", uuid);
            put("thorns", thornsEnchant);
        }});
    }

    public void setProtectionEnchant(boolean value) {
        protectionEnchant = value;

        DatabaseUtil.addDataToTable("Enchants", new HashMap<String, Object>() {{
            put("uuid", uuid);
            put("protection", protectionEnchant);
        }});
    }

    public void setKnockbackEnchant(boolean value) {
        knockbackEnchant = value;

        DatabaseUtil.addDataToTable("Enchants", new HashMap<String, Object>() {{
            put("uuid", uuid);
            put("knockback", knockbackEnchant);
        }});
    }

    public void setSharpnessEnchant(boolean value) {
        sharpnessEnchant = value;

        DatabaseUtil.addDataToTable("Enchants", new HashMap<String, Object>() {{
            put("uuid", uuid);
            put("sharpness", sharpnessEnchant);
        }});
    }

    public void setPunchEnchant(boolean value) {
        punchEnchant = value;

        DatabaseUtil.addDataToTable("Enchants", new HashMap<String, Object>() {{
            put("uuid", uuid);
            put("punch", punchEnchant);
        }});
    }

    public void setPowerEnchant(boolean value) {
        powerEnchant = value;

        DatabaseUtil.addDataToTable("Enchants", new HashMap<String, Object>() {{
            put("uuid", uuid);
            put("power", powerEnchant);
        }});
    }

    public void addOwnedKit(@NonNull Kit kit) {
        if (!ownedKits.contains(kit)) {
            ownedKits.add(kit);
        }
    }
}
