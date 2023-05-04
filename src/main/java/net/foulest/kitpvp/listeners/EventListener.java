package net.foulest.kitpvp.listeners;

import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.menus.KitEnchanter;
import net.foulest.kitpvp.menus.KitSelector;
import net.foulest.kitpvp.menus.KitShop;
import net.foulest.kitpvp.region.Regions;
import net.foulest.kitpvp.region.Spawn;
import net.foulest.kitpvp.util.DatabaseUtil;
import net.foulest.kitpvp.util.ItemBuilder;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.Settings;
import net.foulest.kitpvp.util.kits.Kit;
import net.foulest.kitpvp.util.kits.KitManager;
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
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Foulest
 * @project KitPvP
 * <p>
 * Handles all server events
 */
public class EventListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public static void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!KitPvP.loaded) {
            player.kickPlayer("Disconnected");
            return;
        }

        PlayerData playerData = PlayerData.getInstance(player);

        if (playerData == null) {
            player.kickPlayer("Disconnected");
            return;
        }

        if (!playerData.load()) {
            player.kickPlayer("Disconnected");
            return;
        }

        player.setHealth(20);
        player.setGameMode(GameMode.ADVENTURE);
        player.getInventory().setHeldItemSlot(0);

        for (Kit kit : KitManager.kits) {
            if (kit.getCost() == 0 && !playerData.getOwnedKits().contains(kit)) {
                playerData.getOwnedKits().add(kit);
            }
        }

        Spawn.teleport(player);
    }

    @EventHandler
    public static void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        if (playerData == null) {
            player.kickPlayer("Disconnected");
            return;
        }

        if (playerData.isNoFall()) {
            playerData.setNoFall(false);
            playerData.setPendingNoFallRemoval(false);
        }

        if (CombatLog.isInCombat(player)) {
            DeathListener.handleDeath(player, true);
        }

        playerData.clearCooldowns();
        playerData.setKillstreak(0);
        playerData.saveAll();
        playerData.unload();
    }

    @EventHandler
    public static void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        event.setKeepInventory(true);
        event.getDrops().clear();
        event.setDroppedExp(0);
        event.setDeathMessage("");

        DeathListener.handleDeath(player, false);
    }

    @EventHandler(ignoreCancelled = true)
    public static void onDropItem(PlayerDropItemEvent event) {
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
    public static void onLadderBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (player == null) {
            return;
        }

        if (event.getBlock().getType() == Material.LADDER
            && player.getGameMode() == GameMode.CREATIVE
            && player.hasPermission("kitpvp.modify")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public static void onBowShoot(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (Regions.isInSafezone(player.getLocation())) {
                event.setCancelled(true);
                player.updateInventory();
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public static void onArrowShoot(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();

            if (arrow.getShooter() instanceof Player && event.getEntity() instanceof Player) {
                Player damager = (Player) arrow.getShooter();
                Player receiver = (Player) event.getEntity();

                if (receiver == damager) {
                    event.setCancelled(true);
                    return;
                }

                CombatLog.markForCombat(damager, receiver);

                MessageUtil.messagePlayer(damager, "&c" + receiver.getName() + " &eis on &6"
                                                   + String.format("%.01f", Math.max(receiver.getHealth() - event.getFinalDamage(), 0.0)) + "❤&e.");

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        ((CraftPlayer) receiver).getHandle().getDataWatcher().watch(9, (byte) 0);
                    }
                }.runTaskLater(KitPvP.instance, 100L);
            }
        }
    }

    @EventHandler
    public static void onExplode(EntityExplodeEvent event) {
        event.blockList().clear();
    }

    @EventHandler
    public static void onEntityDeath(EntityDeathEvent event) {
        event.setDroppedExp(0);
        event.getDrops().clear();
    }

    @EventHandler
    public static void onPlayerCraft(CraftItemEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (!(player.getGameMode() == GameMode.CREATIVE
              && player.hasPermission("kitpvp.modify"))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public static void onEntityDamageEntity(EntityDamageByEntityEvent event) {
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

                CombatLog.markForCombat(damager, receiver);
            }
            return;
        }

        // Combat tags players for Wolf damage.
        if (event.getDamager() instanceof Wolf) {
            Wolf wolf = (Wolf) event.getDamager();

            if (wolf.getOwner() instanceof Player && event.getEntity() instanceof Player) {
                Player damager = (Player) wolf.getOwner();
                Player receiver = (Player) event.getEntity();

                CombatLog.markForCombat(damager, receiver);
            }
            return;
        }

        // Combat tags players for Iron Golem damage.
        if (event.getDamager() instanceof IronGolem && event.getEntity() instanceof Player) {
            IronGolem golem = (IronGolem) event.getDamager();
            Player receiver = (Player) event.getEntity();

            for (Player damager : Bukkit.getOnlinePlayers()) {
                if (golem.hasMetadata(damager.getName())) {
                    CombatLog.markForCombat(damager, receiver);
                    return;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public static void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            PlayerData playerData = PlayerData.getInstance(player);

            if (playerData == null) {
                event.setCancelled(true);
                player.kickPlayer("Disconnected");
                return;
            }

            if (playerData.getTeleportingToSpawn() != null) {
                playerData.getTeleportingToSpawn().cancel();
                MessageUtil.messagePlayer(player, MessageUtil.colorize("&cTeleportation cancelled, you took damage."));
                playerData.setTeleportingToSpawn(null);
            }
        }
    }

    @EventHandler
    public static void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        PlayerData playerData = PlayerData.getInstance(player);

        if (playerData == null) {
            event.setCancelled(true);
            player.kickPlayer("Disconnected");
            return;
        }

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

            Kit kit = KitManager.getKit(itemName);

            if (kit != null) {
                if (playerData.getCoins() - kit.getCost() < 0) {
                    MessageUtil.messagePlayer(player, "&cYou do not have enough coins to purchase " + kit.getName() + ".");
                    return;
                }

                if (!player.hasPermission("kitpvp.bypasslimit") && Settings.premiumEnabled
                    && playerData.getOwnedKits().size() == Settings.nonPremiumKitLimit) {
                    MessageUtil.messagePlayer(player, "");
                    MessageUtil.messagePlayer(player, " &cYou have reached your kit limit.");
                    MessageUtil.messagePlayer(player, "");
                    MessageUtil.messagePlayer(player, " &ePurchase &6" + Settings.premiumRankName + " &eto bypass this limit.");
                    MessageUtil.messagePlayer(player, " &eStore: &6" + Settings.premiumStoreLink);
                    MessageUtil.messagePlayer(player, "");
                    return;
                }

                playerData.getOwnedKits().add(kit);
                playerData.removeCoins(kit.getCost());
                DatabaseUtil.update("INSERT INTO PlayerKits (uuid, kitName) VALUES ('" + player.getUniqueId().toString() + "', '" + kit.getName() + "')");
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
                    Kit kit = KitManager.getKit(itemName);

                    if (kit != null) {
                        kit.apply(player);
                        MessageUtil.messagePlayer(player, "&aYou equipped the " + kit.getName() + " kit.");
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
                    int ffCost = 50;

                    if (!Regions.isInSafezone(player.getLocation())) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou need to be in spawn to do this.");
                        event.setCancelled(true);
                        return;
                    }

                    if (playerData.getCoins() - ffCost < 0) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou do not have enough coins.");
                        event.setCancelled(true);
                        return;
                    }

                    if (playerData.isFeatherFallingEnchant()) {
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

                    playerData.removeCoins(ffCost);
                    playerData.setFeatherFallingEnchant(true);

                    if (playerData.getKit() != null) {
                        playerData.getKit().apply(player);
                    }
                    break;

                case "Thorns":
                    int thCost = 100;

                    if (!Regions.isInSafezone(player.getLocation())) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou need to be in spawn to do this.");
                        event.setCancelled(true);
                        return;
                    }

                    if (playerData.getCoins() - thCost < 0) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou do not have enough coins.");
                        event.setCancelled(true);
                        return;
                    }

                    if (playerData.isThornsEnchant()) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou already have the Thorns enchantment.");
                        event.setCancelled(true);
                        return;
                    }

                    player.playSound(player.getLocation(), Sound.ANVIL_USE, 1.0F, 1.0F);
                    MessageUtil.messagePlayer(player, "");
                    MessageUtil.messagePlayer(player, "&eThe &aThorns &eenchantment has been purchased.");
                    MessageUtil.messagePlayer(player, "&eThis enchantment only lasts one life.");
                    MessageUtil.messagePlayer(player, "");

                    playerData.removeCoins(thCost);
                    playerData.setThornsEnchant(true);

                    if (playerData.getKit() != null) {
                        playerData.getKit().apply(player);
                    }
                    break;

                case "Protection":
                    int ptCost = 150;

                    if (!Regions.isInSafezone(player.getLocation())) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou need to be in spawn to do this.");
                        event.setCancelled(true);
                        return;
                    }

                    if (playerData.getCoins() - ptCost < 0) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou do not have enough coins.");
                        event.setCancelled(true);
                        return;
                    }

                    if (playerData.isProtectionEnchant()) {
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

                    playerData.removeCoins(ptCost);
                    playerData.setProtectionEnchant(true);

                    if (playerData.getKit() != null) {
                        playerData.getKit().apply(player);
                    }
                    break;

                case "Knockback":
                    int kbCost = 100;

                    if (!Regions.isInSafezone(player.getLocation())) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou need to be in spawn to do this.");
                        event.setCancelled(true);
                        return;
                    }

                    if (playerData.getCoins() - kbCost < 0) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou do not have enough coins.");
                        event.setCancelled(true);
                        return;
                    }

                    if (playerData.isKnockbackEnchant()) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou already have the Knockback enchantment.");
                        event.setCancelled(true);
                        return;
                    }

                    player.playSound(player.getLocation(), Sound.ANVIL_USE, 1.0F, 1.0F);
                    MessageUtil.messagePlayer(player, "");
                    MessageUtil.messagePlayer(player, "&eThe &aKnockback &eenchantment has been purchased.");
                    MessageUtil.messagePlayer(player, "&eThis enchantment only lasts one life.");
                    MessageUtil.messagePlayer(player, "");

                    playerData.removeCoins(kbCost);
                    playerData.setKnockbackEnchant(true);

                    if (playerData.getKit() != null) {
                        playerData.getKit().apply(player);
                    }
                    break;

                case "Sharpness":
                    int shCost = 200;

                    if (!Regions.isInSafezone(player.getLocation())) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou need to be in spawn to do this.");
                        event.setCancelled(true);
                        return;
                    }

                    if (playerData.getCoins() - shCost < 0) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou do not have enough coins.");
                        event.setCancelled(true);
                        return;
                    }

                    if (playerData.isSharpnessEnchant()) {
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

                    playerData.removeCoins(shCost);
                    playerData.setSharpnessEnchant(true);

                    if (playerData.getKit() != null) {
                        playerData.getKit().apply(player);
                    }
                    break;

                case "Punch":
                    int pnCost = 100;

                    if (!Regions.isInSafezone(player.getLocation())) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou need to be in spawn to do this.");
                        event.setCancelled(true);
                        return;
                    }

                    if (playerData.getCoins() - pnCost < 0) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou do not have enough coins.");
                        event.setCancelled(true);
                        return;
                    }

                    if (playerData.isPunchEnchant()) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou already have the Punch enchantment.");
                        event.setCancelled(true);
                        return;
                    }

                    player.playSound(player.getLocation(), Sound.ANVIL_USE, 1.0F, 1.0F);
                    MessageUtil.messagePlayer(player, "");
                    MessageUtil.messagePlayer(player, "&eThe &aPunch &eenchantment has been purchased.");
                    MessageUtil.messagePlayer(player, "&eThis enchantment only lasts one life.");
                    MessageUtil.messagePlayer(player, "");

                    playerData.removeCoins(pnCost);
                    playerData.setPunchEnchant(true);

                    if (playerData.getKit() != null) {
                        playerData.getKit().apply(player);
                    }
                    break;

                case "Power":
                    int pwCost = 200;

                    if (!Regions.isInSafezone(player.getLocation())) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou need to be in spawn to do this.");
                        event.setCancelled(true);
                        return;
                    }

                    if (playerData.getCoins() - pwCost < 0) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "&cYou do not have enough coins.");
                        event.setCancelled(true);
                        return;
                    }

                    if (playerData.isPowerEnchant()) {
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

                    playerData.removeCoins(pwCost);
                    playerData.setPowerEnchant(true);

                    if (playerData.getKit() != null) {
                        playerData.getKit().apply(player);
                    }
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
    public static void onFoodChange(FoodLevelChangeEvent event) {
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
    public static void onMobSpawn(CreatureSpawnEvent event) {
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
    public static void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Block block = event.getClickedBlock();
        PlayerData playerData = PlayerData.getInstance(player);

        if (playerData == null) {
            event.setCancelled(true);
            player.kickPlayer("Disconnected");
            return;
        }

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
                    if (Regions.isInSafezone(player.getLocation())) {
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

                    if (Regions.isInSafezone(player.getLocation())) {
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

                    if (Regions.isInSafezone(player.getLocation())) {
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
                        player.performCommand("stats");
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
    public static void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        if (playerData == null) {
            event.setCancelled(true);
            player.kickPlayer("Disconnected");
        }
    }

    /**
     * Handles player move events.
     *
     * @param event PlayerMoveEvent
     */
    @EventHandler
    public static void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);
        double deltaY = event.getTo().getY() - event.getFrom().getY();
        double deltaXZ = Math.hypot(event.getTo().getX() - event.getFrom().getX(), event.getTo().getZ() - event.getFrom().getZ());
        boolean playerMoved = (deltaXZ > 0.05 || Math.abs(deltaY) > 0.05);

        if (playerData == null) {
            event.setCancelled(true);
            player.kickPlayer("Disconnected");
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
            DeathListener.handleDeath(player, false);
            return;
        }

        // Equips the player's previously used kit when they leave spawn without a kit equipped.
        if (playerData.getKit() == null && !player.isDead() && !player.getAllowFlight()
            && !Regions.isInSafezone(event.getFrom())) {
            player.closeInventory();
            playerData.getPreviousKit().apply(player);
            MessageUtil.messagePlayer(player, "&cYour previous kit has been automatically applied.");
            return;
        }

        // Denies entry into spawn while combat tagged.
        // Also heals the player whilst in a safe zone.
        if (Regions.isInSafezone(event.getTo())) {
            if (CombatLog.isInCombat(player)) {
                player.teleport(event.getFrom());
                MessageUtil.messagePlayer(player, "&cYou can't enter spawn while combat tagged.");

            } else {
                player.setHealth(20);
                player.setFireTicks(0);

                if (!playerData.isNoFall()) {
                    playerData.setNoFall(true);
                }
            }
        }

        // Removes the player's no fall.
        if (playerData.isNoFall() && !playerData.isPendingNoFallRemoval() && !Regions.isInSafezone(player.getLocation())) {
            playerData.setPendingNoFallRemoval(true);

            new BukkitRunnable() {
                @Override
                public void run() {
                    playerData.setPendingNoFallRemoval(false);

                    if (playerData.isNoFall() && !Regions.isInSafezone(player.getLocation())) {
                        playerData.setNoFall(false);
                    }
                }
            }.runTaskLater(KitPvP.instance, 30L);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public static void onFallDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            PlayerData playerData = PlayerData.getInstance(player);

            if (playerData == null) {
                event.setCancelled(true);
                player.kickPlayer("Disconnected");
                return;
            }

            if (event.getCause() == EntityDamageEvent.DamageCause.FALL && playerData.isNoFall()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public static void onWeatherChange(WeatherChangeEvent event) {
        if (event.toWeatherState()) {
            event.setCancelled(true);
            event.getWorld().setStorm(false);
            event.getWorld().setThundering(false);
        }
    }

    @EventHandler
    public static void onThunderChange(ThunderChangeEvent event) {
        if (event.toThunderState()) {
            event.setCancelled(true);
            event.getWorld().setStorm(false);
            event.getWorld().setThundering(false);
        }
    }

    @EventHandler
    public static void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (!(player.getGameMode() == GameMode.CREATIVE
              && player.hasPermission("kitpvp.modify"))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public static void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (!(player.getGameMode() == GameMode.CREATIVE
              && player.hasPermission("kitpvp.modify"))) {
            event.setCancelled(true);
            player.updateInventory();
        }
    }

    @EventHandler
    public static void onBlockIgnite(BlockIgniteEvent event) {
        Player player = event.getPlayer();

        if (!(event.getCause() == BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL
              && player.getGameMode() == GameMode.CREATIVE
              && player.hasPermission("kitpvp.modify"))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public static void onBlockBurn(BlockBurnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public static void onBlockDecay(LeavesDecayEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public static void onBedEnter(PlayerBedEnterEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public static void onExpChange(PlayerExpChangeEvent event) {
        event.setAmount(0);
    }

    @EventHandler
    public static void onItemPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();

        if (!(player.getGameMode() == GameMode.CREATIVE
              && player.hasPermission("kitpvp.modify"))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public static void onItemDamage(PlayerItemDamageEvent event) {
        Player player = event.getPlayer();

        if (!(player.getGameMode() == GameMode.CREATIVE
              && player.hasPermission("kitpvp.modify"))) {
            event.setCancelled(true);
            player.updateInventory();
        }
    }

    @EventHandler
    public static void onCropTrample(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() == Action.PHYSICAL && event.hasBlock()) {
            Block block = player.getWorld().getBlockAt(event.getClickedBlock().getLocation());

            if (block.getType() == Material.SOIL) {
                event.setCancelled(true);
            }
        }
    }
}
