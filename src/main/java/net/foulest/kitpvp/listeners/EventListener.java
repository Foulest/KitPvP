package net.foulest.kitpvp.listeners;

import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.utils.*;
import net.foulest.kitpvp.utils.kits.Kit;
import net.foulest.kitpvp.utils.kits.KitManager;
import net.foulest.kitpvp.utils.menus.KitEnchanter;
import net.foulest.kitpvp.utils.menus.KitSelector;
import net.foulest.kitpvp.utils.menus.KitShop;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
public class EventListener implements Listener {

    private static final EventListener INSTANCE = new EventListener();
    private static final Spawn SPAWN = Spawn.getInstance();
    private static final KitPvP KITPVP = KitPvP.getInstance();
    private static final MySQL MYSQL = MySQL.getInstance();
    private static final KitManager KIT_MANAGER = KitManager.getInstance();
    private static final CombatLog COMBAT_LOG = CombatLog.getInstance();
    private static final Regions REGIONS = Regions.getInstance();
    private static final DeathListener DEATH_LISTENER = DeathListener.getInstance();

    public static EventListener getInstance() {
        return INSTANCE;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        player.setHealth(20);
        player.setGameMode(GameMode.ADVENTURE);
        player.getInventory().clear();
        player.getInventory().setHeldItemSlot(0);
        player.getInventory().setArmorContents(null);

        try {
            playerData.load();
        } catch (SQLException ex) {
            // ignored
        }

        for (Kit kit : KIT_MANAGER.getKits()) {
            if (kit.getCost() == 0 && !playerData.getOwnedKits().contains(kit)) {
                playerData.getOwnedKits().add(kit);
            }
        }

        SPAWN.teleport(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        if (player.hasMetadata("noFall")) {
            player.removeMetadata("noFall", KITPVP);
            playerData.setPendingNoFallRemoval(false);
        }

        if (COMBAT_LOG.isInCombat(player)) {
            DEATH_LISTENER.handleDeath(player, true);
        }

        playerData.clearCooldowns();
        playerData.setKillstreak(0);
        playerData.saveAll();
        playerData.unload();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        event.getDrops().clear();
        event.setDroppedExp(0);

        DEATH_LISTENER.handleDeath(player, false);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (!(player.getGameMode() == GameMode.CREATIVE
                && player.hasPermission("kitpvp.modify"))) {
            if (event.getItemDrop().getItemStack().getType() == Material.BOWL) {
                event.getItemDrop().remove();
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onLadderBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (player == null) {
            return;
        }

        if (event.getBlock().getType() == Material.LADDER && player.getGameMode() == GameMode.CREATIVE
                && player.hasPermission("kitpvp.modify")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBowShoot(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (REGIONS.isInSafezone(player)) {
                event.setCancelled(true);
                player.updateInventory();
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onArrowShoot(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();

            if (arrow.getShooter() instanceof Player && event.getEntity() instanceof Player) {
                Player damager = (Player) arrow.getShooter();
                Player receiver = (Player) event.getEntity();

                if (receiver == damager) {
                    event.setCancelled(true);
                    return;
                }

                COMBAT_LOG.markForCombat(damager, receiver);

                MessageUtil.messagePlayer(damager, "&c" + receiver.getName() + " &eis on &6"
                        + String.format("%.01f", Math.max(receiver.getHealth() - event.getFinalDamage(), 0.0)) + " ❤&e.");

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        ((CraftPlayer) receiver).getHandle().getDataWatcher().watch(9, (byte) 0);
                    }
                }.runTaskLater(KITPVP, 100L);
            }
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        event.blockList().clear();
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        event.setDroppedExp(0);
        event.getDrops().clear();
    }

    @EventHandler
    public void onPlayerCraft(CraftItemEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (!(player.getGameMode() == GameMode.CREATIVE
                && player.hasPermission("kitpvp.modify"))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();

            // Prevents players from hitting their own Wolves.
            if (event.getEntity() instanceof Wolf) {
                Wolf wolf = (Wolf) event.getEntity();

                if (wolf.getOwner() == damager) {
                    event.setCancelled(true);
                    return;
                }
            }

            // Prevents players from hitting their own Iron Golems.
            if (event.getEntity() instanceof IronGolem && event.getEntity().hasMetadata(damager.getName())) {
                event.setCancelled(true);
                return;
            }

            // Combat tags players for Player on Player damage.
            if (event.getEntity() instanceof Player) {
                Player receiver = (Player) event.getEntity();

                COMBAT_LOG.markForCombat(damager, receiver);
            }
        }

        // Combat tags players for Wolf damage.
        if (event.getDamager() instanceof Wolf) {
            Wolf wolf = (Wolf) event.getDamager();

            if (wolf.getOwner() instanceof Player && event.getEntity() instanceof Player) {
                Player damager = (Player) wolf.getOwner();
                Player receiver = (Player) event.getEntity();

                COMBAT_LOG.markForCombat(damager, receiver);
            }
        }

        // Combat tags players for Iron Golem damage.
        if (event.getDamager() instanceof IronGolem && event.getEntity() instanceof Player) {
            IronGolem golem = (IronGolem) event.getDamager();
            Player receiver = (Player) event.getEntity();

            for (Player damager : Bukkit.getOnlinePlayers()) {
                if (golem.hasMetadata(damager.getName())) {
                    COMBAT_LOG.markForCombat(damager, receiver);
                    return;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            PlayerData playerData = PlayerData.getInstance(player);

            if (playerData.getTeleportingToSpawn() != null) {
                playerData.getTeleportingToSpawn().cancel();
                MessageUtil.messagePlayer(player, MessageUtil.colorize("&cTeleportation cancelled, you took damage."));
                playerData.setTeleportingToSpawn(null);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        PlayerData playerData = PlayerData.getInstance(player);

        // Fixes the weird hotbar swap bug.
        if (event.getAction() == InventoryAction.HOTBAR_SWAP
                && !event.getClickedInventory().getName().equals("container.inventory")) {
            event.setCancelled(true);
            player.updateInventory();
            return;
        }

        // Null/meta check.
        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()
                || event.getCurrentItem().getItemMeta() == null) {
            return;
        }

        // Prevents players in kits from moving their armor.
        if (playerData.getKit() != null && event.getSlotType() == InventoryType.SlotType.ARMOR) {
            event.setCancelled(true);
            player.updateInventory();
            return;
        }

        if (event.getView().getTitle().contains("Kit Shop")) {
            event.setCancelled(true);
            player.updateInventory();

            if (event.getCurrentItem().getItemMeta() == null) {
                return;
            }

            String itemName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

            if (itemName == null || itemName.trim().isEmpty()) {
                return;
            }

            if (KIT_MANAGER.valueOf(itemName) != null) {
                Kit kit = KIT_MANAGER.valueOf(itemName);

                if (playerData.getCoins() - kit.getCost() < 0) {
                    MessageUtil.messagePlayer(player, "&cYou do not have enough coins to purchase " + kit.getName() + ".");
                    return;
                }

                if (!player.hasPermission("kitpvp.bypasslimit") && playerData.getOwnedKits().size() == 10) {
                    MessageUtil.messagePlayer(player, "");
                    MessageUtil.messagePlayer(player, " &cYou have reached your kit limit.");
                    MessageUtil.messagePlayer(player, "");
                    MessageUtil.messagePlayer(player, " &ePurchase &6Premium &eto bypass this limit.");
                    MessageUtil.messagePlayer(player, " &eStore: &6store.kitpvp.io");
                    MessageUtil.messagePlayer(player, "");
                    return;
                }

                playerData.getOwnedKits().add(kit);
                playerData.removeCoins(kit.getCost());
                MYSQL.update("INSERT INTO PlayerKits (uuid, kitName) VALUES ('" + player.getUniqueId().toString() + "', '" + kit.getName() + "')");
                MessageUtil.messagePlayer(player, "&aYou purchased the " + kit.getName() + " kit for " + kit.getCost() + " coins.");
                player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
                player.closeInventory();
            }

        } else if (event.getView().getTitle().contains("Kit Selector")) {
            event.setCancelled(true);
            player.updateInventory();

            String itemName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

            if (itemName == null || itemName.trim().isEmpty()) {
                return;
            }

            switch (itemName) {
                case "-->":
                    new KitSelector(player, KitSelector.getPage(player) + 1);
                    break;

                case "<--":
                    new KitSelector(player, KitSelector.getPage(player) - 1);
                    break;

                case "Kit Shop":
                    new KitShop(player);
                    break;

                default:
                    Kit kit = KIT_MANAGER.valueOf(itemName);

                    if (kit != null) {
                        kit.apply(player);
                        MessageUtil.messagePlayer(player, "&aYou equipped the " + KIT_MANAGER.valueOf(itemName).getName() + " kit.");
                        player.playSound(player.getLocation(), Sound.SLIME_WALK, 1, 1);
                        player.updateInventory();
                        player.closeInventory();
                    }
                    break;
            }

        } else if (event.getView().getTitle().contains("Kit Enchanter")) {
            event.setCancelled(true);
            player.updateInventory();

            if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()
                    || event.getCurrentItem().getItemMeta() == null) {
                return;
            }

            String itemName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

            if (itemName == null || itemName.trim().isEmpty()) {
                return;
            }

            switch (itemName) {
                case "Feather Falling":
                    if (!REGIONS.isInSafezone(player)) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou need to be in spawn to do this.");
                        event.setCancelled(true);
                        return;
                    }

                    if (playerData.getCoins() - 50 < 0) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou do not have enough coins.");
                        event.setCancelled(true);
                        return;
                    }

                    if (playerData.getKit() == null) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou do not have a kit equipped.");
                        event.setCancelled(true);
                        return;
                    }

                    if (player.hasMetadata("featherFalling")) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou already have the Feather Falling enchantment.");
                        event.setCancelled(true);
                        return;
                    }

                    player.playSound(player.getLocation(), Sound.ANVIL_USE, 1.0F, 1.0F);
                    MessageUtil.messagePlayer(player, "");
                    MessageUtil.messagePlayer(player, "&eThe &aFeather Falling &eenchantment has been purchased.");
                    MessageUtil.messagePlayer(player, "&eThis enchantment only lasts one life.");
                    MessageUtil.messagePlayer(player, "");

                    playerData.removeCoins(50);
                    player.setMetadata("featherFalling", new FixedMetadataValue(KITPVP, true));
                    playerData.getKit().apply(player);
                    break;

                case "Knockback":
                    if (!REGIONS.isInSafezone(player)) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou need to be in spawn to do this.");
                        event.setCancelled(true);
                        return;
                    }

                    if (playerData.getCoins() - 100 < 0) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou do not have enough coins.");
                        event.setCancelled(true);
                        return;
                    }

                    if (playerData.getKit() == null) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou do not have a kit equipped.");
                        event.setCancelled(true);
                        return;
                    }

                    if (player.hasMetadata("featherFalling")) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou already have the Feather Falling enchantment.");
                        event.setCancelled(true);
                        return;
                    }

                    player.playSound(player.getLocation(), Sound.ANVIL_USE, 1.0F, 1.0F);
                    MessageUtil.messagePlayer(player, "");
                    MessageUtil.messagePlayer(player, "&eThe &aKnockback &eenchantment has been purchased.");
                    MessageUtil.messagePlayer(player, "&eThis enchantment only lasts one life.");
                    MessageUtil.messagePlayer(player, "");

                    playerData.removeCoins(100);
                    player.setMetadata("knockback", new FixedMetadataValue(KITPVP, true));
                    playerData.getKit().apply(player);
                    break;

                case "Protection":
                    if (!REGIONS.isInSafezone(player)) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou need to be in spawn to do this.");
                        event.setCancelled(true);
                        return;
                    }

                    if (playerData.getCoins() - 150 < 0) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou do not have enough coins.");
                        event.setCancelled(true);
                        return;
                    }

                    if (playerData.getKit() == null) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou do not have a kit equipped.");
                        event.setCancelled(true);
                        return;
                    }

                    if (player.hasMetadata("protection")) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou already have the Protection enchantment.");
                        event.setCancelled(true);
                        return;
                    }

                    player.playSound(player.getLocation(), Sound.ANVIL_USE, 1.0F, 1.0F);
                    MessageUtil.messagePlayer(player, "");
                    MessageUtil.messagePlayer(player, "&eThe &aProtection &eenchantment has been purchased.");
                    MessageUtil.messagePlayer(player, "&eThis enchantment only lasts one life.");
                    MessageUtil.messagePlayer(player, "");

                    playerData.removeCoins(150);
                    player.setMetadata("protection", new FixedMetadataValue(KITPVP, true));
                    playerData.getKit().apply(player);
                    break;

                case "Power":
                    if (!REGIONS.isInSafezone(player)) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou need to be in spawn to do this.");
                        event.setCancelled(true);
                        return;
                    }

                    if (playerData.getCoins() - 200 < 0) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou do not have enough coins.");
                        event.setCancelled(true);
                        return;
                    }

                    if (playerData.getKit() == null) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou do not have a kit equipped.");
                        event.setCancelled(true);
                        return;
                    }

                    if (player.hasMetadata("power")) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou already have the Power enchantment.");
                        event.setCancelled(true);
                        return;
                    }

                    player.playSound(player.getLocation(), Sound.ANVIL_USE, 1.0F, 1.0F);
                    MessageUtil.messagePlayer(player, "");
                    MessageUtil.messagePlayer(player, "&eThe &aPower &eenchantment has been purchased.");
                    MessageUtil.messagePlayer(player, "&eThis enchantment only lasts one life.");
                    MessageUtil.messagePlayer(player, "");

                    playerData.removeCoins(200);
                    player.setMetadata("power", new FixedMetadataValue(KITPVP, true));
                    playerData.getKit().apply(player);
                    break;

                case "Sharpness":
                    if (!REGIONS.isInSafezone(player)) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou need to be in spawn to do this.");
                        event.setCancelled(true);
                        return;
                    }

                    if (playerData.getCoins() - 250 < 0) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou do not have enough coins.");
                        event.setCancelled(true);
                        return;
                    }

                    if (playerData.getKit() == null) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou do not have a kit equipped.");
                        event.setCancelled(true);
                        return;
                    }

                    if (player.hasMetadata("sharpness")) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou already have the Sharpness enchantment.");
                        event.setCancelled(true);
                        return;
                    }

                    player.playSound(player.getLocation(), Sound.ANVIL_USE, 1.0F, 1.0F);
                    MessageUtil.messagePlayer(player, "");
                    MessageUtil.messagePlayer(player, "&eThe &aSharpness &eenchantment has been purchased.");
                    MessageUtil.messagePlayer(player, "&eThis enchantment only lasts one life.");
                    MessageUtil.messagePlayer(player, "");

                    playerData.removeCoins(250);
                    player.setMetadata("sharpness", new FixedMetadataValue(KITPVP, true));
                    playerData.getKit().apply(player);
                    break;

                default:
                    break;
            }

        } else if (playerData.getKit() == null) {
            event.setCancelled(true);
            player.updateInventory();
        }
    }

    /**
     * Prevents players from getting hungry.
     *
     * @param event FoodLevelChangeEvent
     */
    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        Player player = (Player) event.getEntity();

        event.setFoodLevel(20);
        player.setSaturation(20);
    }

    /**
     * Prevents mobs from naturally spawning.
     *
     * @param event CreatureSpawnEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMobSpawn(CreatureSpawnEvent event) {
        CreatureSpawnEvent.SpawnReason spawnEvent = event.getSpawnReason();

        if (spawnEvent != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG
                && spawnEvent != CreatureSpawnEvent.SpawnReason.SPAWNER
                && spawnEvent != CreatureSpawnEvent.SpawnReason.CUSTOM) {
            event.setCancelled(true);
        }
    }

    /**
     * Huge event that handles right clicks.
     *
     * @param event PlayerInteractEvent
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Block block = event.getClickedBlock();
        PlayerData playerData = PlayerData.getInstance(player);

        if (event.getAction().toString().contains("RIGHT") && block != null
                && block.getState() instanceof InventoryHolder) {
            event.setCancelled(true);
            return;
        }

        if (event.getAction().toString().contains("RIGHT") && item != null) {
            switch (item.getType()) {
                case WEB:
                case BLAZE_ROD:
                case IRON_BLOCK:
                case SLIME_BALL:
                case DISPENSER:
                    break;

                case FISHING_ROD:
                    if (REGIONS.isInSafezone(player)) {
                        event.setCancelled(true);
                    }
                    break;

                case POTION:
                    if (item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Using Potions")) {
                        event.setCancelled(true);
                        player.updateInventory();

                        playerData.setUsingSoup(true);
                        playerData.saveStats();
                        MessageUtil.messagePlayer(player, "&aYou are now using Soup.");

                        ItemStack healingItem = new ItemBuilder(Material.MUSHROOM_SOUP).name("&aUsing Soup &7(Right Click)").getItem();
                        player.getInventory().setItem(6, healingItem);
                        break;
                    }

                    if (REGIONS.isInSafezone(player)) {
                        event.setCancelled(true);
                        player.updateInventory();
                    }
                    break;

                case MUSHROOM_SOUP:
                    if (item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Using Soup")) {
                        event.setCancelled(true);
                        player.updateInventory();

                        playerData.setUsingSoup(false);
                        playerData.saveStats();
                        MessageUtil.messagePlayer(player, "&aYou are now using Potions.");

                        ItemStack healingItem = new ItemBuilder(Material.POTION).hideInfo().durability(16421).name("&aUsing Potions &7(Right Click)").getItem();
                        player.getInventory().setItem(6, healingItem);
                        break;
                    }

                    if (REGIONS.isInSafezone(player)) {
                        event.setCancelled(true);
                        break;
                    }

                    if (player.getHealth() < player.getMaxHealth()) {
                        event.setCancelled(true);
                        player.setHealth(Math.min(player.getHealth() + 7, player.getMaxHealth()));
                        player.setItemInHand(new ItemBuilder(Material.BOWL).name("&fBowl").getItem());
                    }
                    break;

                case WATCH:
                    if (item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Previous Kit")
                            && playerData.getPreviousKit() != null && playerData.getKit() == null) {
                        event.setCancelled(true);
                        playerData.getPreviousKit().apply(player);
                        MessageUtil.messagePlayer(player, "&aYou equipped the " + playerData.getPreviousKit().getName() + " kit.");
                        player.playSound(player.getLocation(), Sound.SLIME_WALK, 1, 1);
                        player.updateInventory();
                        player.closeInventory();
                    }
                    break;

                case SKULL_ITEM:
                    if (item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Your Stats")) {
                        event.setCancelled(true);
                        player.updateInventory();
                        MessageUtil.messagePlayer(player, "");
                        MessageUtil.messagePlayer(player, " &a&lYour Stats");
                        MessageUtil.messagePlayer(player, " &fKills: &e" + playerData.getKills());
                        MessageUtil.messagePlayer(player, " &fDeaths: &e" + playerData.getDeaths());
                        MessageUtil.messagePlayer(player, " &fK/D Ratio: &e" + playerData.getKDRText());
                        MessageUtil.messagePlayer(player, "");
                        MessageUtil.messagePlayer(player, " &fStreak: &e" + playerData.getKillstreak());
                        MessageUtil.messagePlayer(player, " &fHighest Streak: &e" + playerData.getTopKillstreak());
                        MessageUtil.messagePlayer(player, "");
                        MessageUtil.messagePlayer(player, " &fLevel: &e" + playerData.getLevel() + " &7(" + playerData.getExpPercent() + "%)");
                        MessageUtil.messagePlayer(player, " &fCoins: &6" + playerData.getCoins());
                        MessageUtil.messagePlayer(player, " &fBounty: &6" + playerData.getBounty());
                        MessageUtil.messagePlayer(player, "");
                    }
                    break;

                case NETHER_STAR:
                    if (item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Kit Selector")) {
                        event.setCancelled(true);
                        player.updateInventory();
                        new KitSelector(player);
                    }
                    break;

                case ENCHANTED_BOOK:
                    if (item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Kit Enchanter")) {
                        event.setCancelled(true);
                        player.updateInventory();
                        new KitEnchanter(player);
                    }
                    break;

                case ENDER_CHEST:
                    if (item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Kit Shop")) {
                        event.setCancelled(true);
                        player.updateInventory();
                        new KitShop(player);
                    }
                    break;

                default:
                    if (item.hasItemMeta() && item.getItemMeta().getDisplayName() != null
                            && item.getItemMeta().getDisplayName().toLowerCase().contains("right click")) {
                        event.setCancelled(true);
                        player.updateInventory();
                    }
                    break;
            }
        }
    }

    /**
     * Prevents players from chatting while not loaded.
     * This fixes an issue that almost never happens.
     *
     * @param event -
     */
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        if (!playerData.isLoaded()) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles player move events.
     *
     * @param event PlayerMoveEvent
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);
        double deltaY = event.getTo().getY() - event.getFrom().getY();
        double deltaXZ = Math.hypot(event.getTo().getX() - event.getFrom().getX(), event.getTo().getZ() - event.getFrom().getZ());
        boolean playerMoved = (deltaXZ > 0.05 || Math.abs(deltaY) > 0.05);

        // Locks the player in place if they aren't loaded.
        if (!playerData.isLoaded()) {
            event.setTo(event.getFrom());
            return;
        }

        // Ignores rotation updates.
        if (!playerMoved) {
            return;
        }

        // Cancels pending teleportation when moving.
        if (playerData.getTeleportingToSpawn() != null) {
            MessageUtil.messagePlayer(player, MessageUtil.colorize("&cTeleportation cancelled, you moved."));
            playerData.getTeleportingToSpawn().cancel();
            playerData.setTeleportingToSpawn(null);
        }

        // Kills the player if they leave the map/fall into the void.
        if (player.getLocation().getY() < 0 && !player.getAllowFlight()) {
            DEATH_LISTENER.handleDeath(player, false);
            return;
        }

        // Equips the player's previously used kit when they leave spawn without a kit equipped.
        if (playerData.getKit() == null && !player.isDead() && !player.getAllowFlight()
                && !REGIONS.isInSafezone(event.getFrom())) {
            player.closeInventory();
            playerData.getPreviousKit().apply(player);
            MessageUtil.messagePlayer(player, "&cYour previous kit has been automatically applied.");
            return;
        }

        // Denies entry into spawn while combat tagged.
        // Also heals the player whilst in a safe zone.
        if (REGIONS.isInSafezone(player)) {
            if (COMBAT_LOG.isInCombat(player)) {
                event.setTo(event.getFrom());
                MessageUtil.messagePlayer(player, "&cYou can't enter spawn while combat tagged.");
            } else {
                player.setHealth(20);
                player.setFireTicks(0);

                if (!player.hasMetadata("noFall")) {
                    player.setMetadata("noFall", new FixedMetadataValue(KITPVP, true));
                }
            }
        }

        // Removes the player's noFall metadata.
        if (player.hasMetadata("noFall")
                && !playerData.isPendingNoFallRemoval()
                && !REGIONS.isInSafezone(player)) {
            playerData.setPendingNoFallRemoval(true);

            new BukkitRunnable() {
                @Override
                public void run() {
                    playerData.setPendingNoFallRemoval(false);

                    if (player.hasMetadata("noFall")
                            && !REGIONS.isInSafezone(player)) {
                        player.removeMetadata("noFall", KITPVP);
                    }
                }
            }.runTaskLater(KITPVP, 30L);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player
                && event.getCause() == EntityDamageEvent.DamageCause.FALL
                && event.getEntity().hasMetadata("noFall")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVoidDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player
                && event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            Player player = (Player) event.getEntity();

            DEATH_LISTENER.handleDeath(player, false);
        }
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (event.toWeatherState()) {
            event.setCancelled(true);
            event.getWorld().setStorm(false);
            event.getWorld().setThundering(false);
        }
    }

    @EventHandler
    public void onThunderChange(ThunderChangeEvent event) {
        if (event.toThunderState()) {
            event.setCancelled(true);
            event.getWorld().setStorm(false);
            event.getWorld().setThundering(false);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (!(player.getGameMode() == GameMode.CREATIVE
                && player.hasPermission("kitpvp.modify"))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (!(player.getGameMode() == GameMode.CREATIVE
                && player.hasPermission("kitpvp.modify"))) {
            event.setCancelled(true);
            player.updateInventory();
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        Player player = event.getPlayer();

        if (!(event.getCause() == BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL
                && player.getGameMode() == GameMode.CREATIVE
                && player.hasPermission("kitpvp.modify"))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockDecay(LeavesDecayEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onExpChange(PlayerExpChangeEvent event) {
        event.setAmount(0);
    }

    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();

        if (!(player.getGameMode() == GameMode.CREATIVE
                && player.hasPermission("kitpvp.modify"))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        Player player = event.getPlayer();

        if (!(player.getGameMode() == GameMode.CREATIVE
                && player.hasPermission("kitpvp.modify"))) {
            event.setCancelled(true);
            player.updateInventory();
        }
    }

    @EventHandler
    public void onCropTrample(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() == Action.PHYSICAL && event.hasBlock()) {
            Block block = player.getWorld().getBlockAt(event.getClickedBlock().getLocation());

            if (block.getType() == Material.SOIL) {
                event.setCancelled(true);
            }
        }
    }
}
