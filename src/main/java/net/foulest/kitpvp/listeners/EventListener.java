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
package net.foulest.kitpvp.listeners;

import net.foulest.kitpvp.cmds.StatsCmd;
import net.foulest.kitpvp.combattag.CombatTag;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
import net.foulest.kitpvp.enchants.Enchants;
import net.foulest.kitpvp.kits.Kit;
import net.foulest.kitpvp.kits.KitManager;
import net.foulest.kitpvp.menus.KitEnchanter;
import net.foulest.kitpvp.menus.KitSelector;
import net.foulest.kitpvp.menus.KitShop;
import net.foulest.kitpvp.region.Regions;
import net.foulest.kitpvp.region.Spawn;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.Settings;
import net.foulest.kitpvp.util.TaskUtil;
import net.foulest.kitpvp.util.item.ItemBuilder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

/**
 * Handles all non-kit-related events in the plugin.
 *
 * @author Foulest
 */
public class EventListener implements Listener {

    /**
     * Handles players joining the server.
     *
     * @param event PlayerJoinEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public static void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        // Returns if the player's data is null or if it fails to load.
        if (playerData == null || !playerData.load()) {
            player.kickPlayer("Disconnected");
            return;
        }

        // Sets the player's initial settings.
        player.setHealth(20);
        player.setGameMode(GameMode.ADVENTURE);
        player.getInventory().setHeldItemSlot(0);
        playerData.calcLevel(false);

        // Adds free kits to player's owned kits list.
        for (Kit kit : KitManager.getKits()) {
            if (kit.enabled() && kit.getCost() == 0) {
                playerData.getOwnedKits().add(kit);
            }
        }

        // Teleports the player to spawn.
        Spawn.teleport(player);
    }

    /**
     * Handles players quitting the server.
     *
     * @param event PlayerQuitEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public static void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        // Kills players combat logging.
        if (Settings.combatTagPunishLogout && CombatTag.isInCombat(player)) {
            DeathListener.handleDeath(player, true);
        }

        // Saves the player's data.
        playerData.clearCooldowns();
        playerData.setKillstreak(0);
        playerData.saveAll();

        // Removes the player's data from the map.
        PlayerDataManager.removePlayerData(player);
    }

    /**
     * Handles players dying.
     *
     * @param event PlayerDeathEvent
     */
    @EventHandler
    public static void onPlayerDeath(@NotNull PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Removes the death message and drops.
        event.setKeepInventory(true);
        event.getDrops().clear();
        event.setDroppedExp(0);
        event.setDeathMessage("");

        // Handles the player's death.
        DeathListener.handleDeath(player, false);
    }

    /**
     * Handles players dropping items.
     *
     * @param event PlayerDropItemEvent
     */
    @EventHandler(ignoreCancelled = true)
    public static void onDropItem(@NotNull PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        // Ignores players in creative mode with the modify permission.
        if (player.getGameMode() == GameMode.CREATIVE
                && player.hasPermission("kitpvp.modify")) {
            return;
        }

        Item droppedItem = event.getItemDrop();

        // Removes dropped bowls and cancels other item drops.
        if (droppedItem.getItemStack().getType() == Material.BOWL) {
            droppedItem.remove();
        } else {
            event.setCancelled(true);
        }
    }

    /**
     * Handles players shooting bows.
     *
     * @param event EntityShootBowEvent
     */
    @EventHandler(ignoreCancelled = true)
    public static void onBowShoot(@NotNull EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            // Cancels the event if the player is in a safezone.
            if (Regions.isInSafezone(player.getLocation())) {
                event.setCancelled(true);
                player.updateInventory();
            }
        }
    }

    /**
     * Handles players getting hit by arrows.
     *
     * @param event EntityDamageByEntityEvent
     */
    @EventHandler(ignoreCancelled = true)
    public static void onArrowShoot(@NotNull EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();

            // Handles players shooting other players.
            if (arrow.getShooter() instanceof Player && event.getEntity() instanceof Player) {
                Player damager = (Player) arrow.getShooter();
                Player receiver = (Player) event.getEntity();

                // Cancels the event if the receiver is the damager.
                if (receiver.equals(damager)) {
                    event.setCancelled(true);
                    return;
                }

                // Marks both players for combat.
                CombatTag.markForCombat(damager, receiver);

                // Prints the Archer arrow tag message.
                MessageUtil.messagePlayer(damager, "&c" + receiver.getName() + " &eis on &6"
                        + String.format("%.01f", Math.max(receiver.getHealth() - event.getFinalDamage(), 0.0)) + "\u2764&e.");

                // Removes arrows from the receiver's body.
                TaskUtil.runTaskLater(() -> ((CraftEntity) receiver).getHandle().getDataWatcher().watch(9, (byte) 0), 100L);
            }
        }
    }

    /**
     * Handles explosions.
     *
     * @param event EntityExplodeEvent
     */
    @EventHandler
    public static void onExplode(@NotNull EntityExplodeEvent event) {
        // Clears the block list.
        event.blockList().clear();
    }

    /**
     * Handles entity deaths.
     *
     * @param event EntityDeathEvent
     */
    @EventHandler
    public static void onEntityDeath(@NotNull EntityDeathEvent event) {
        // Removes the drops and experience.
        event.setDroppedExp(0);
        event.getDrops().clear();
    }

    /**
     * Handles players crafting items.
     *
     * @param event CraftItemEvent
     */
    @EventHandler
    public static void onPlayerCraft(@NotNull CraftItemEvent event) {
        Player player = (Player) event.getWhoClicked();

        // Ignores players in creative mode with the modify permission.
        if (player.getGameMode() == GameMode.CREATIVE
                && player.hasPermission("kitpvp.modify")) {
            return;
        }

        // Cancels the event.
        event.setCancelled(true);
    }

    /**
     * Handles players damaging other entities.
     *
     * @param event EntityDamageByEntityEvent
     */
    @EventHandler(ignoreCancelled = true)
    public static void onEntityDamageEntity(@NotNull EntityDamageByEntityEvent event) {
        Entity damagerEntity = event.getDamager();
        Entity targetEntity = event.getEntity();

        // Handles players damaging other entities.
        if (damagerEntity instanceof Player) {
            Player damager = (Player) damagerEntity;

            // Prevents players from hitting their own entities.
            if ((targetEntity instanceof Wolf && ((Tameable) targetEntity).getOwner() == damager)
                    || (targetEntity instanceof IronGolem && targetEntity.hasMetadata(damager.getName()))) {
                event.setCancelled(true);
                return;
            }

            // Combat tags players for Player on Player damage.
            if (targetEntity instanceof Player) {
                CombatTag.markForCombat(damager, (Player) targetEntity);
            }
            return;
        }

        // Combat tags players for player-owned entity damage.
        if (targetEntity instanceof Player && damagerEntity instanceof Tameable) {
            Tameable tameable = (Tameable) damagerEntity;
            AnimalTamer owner = tameable.getOwner();

            if (tameable.isTamed() && owner instanceof Player) {
                CombatTag.markForCombat((Player) owner, (Player) targetEntity);
            }
        }

        // Combat tags players for Iron Golem damage.
        if (damagerEntity instanceof IronGolem && targetEntity instanceof Player) {
            Bukkit.getOnlinePlayers().stream()
                    .filter(player -> damagerEntity.hasMetadata(player.getName()))
                    .findFirst()
                    .ifPresent(damager -> CombatTag.markForCombat(damager, (Player) targetEntity));
        }
    }

    /**
     * Handles players taking damage.
     *
     * @param event EntityDamageEvent
     */
    @EventHandler(ignoreCancelled = true)
    public static void onEntityDamage(@NotNull EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            PlayerData playerData = PlayerDataManager.getPlayerData(player);
            BukkitTask teleportToSpawnTask = playerData.getTeleportToSpawnTask();

            // Cancels pending teleportation when taking damage.
            if (teleportToSpawnTask != null) {
                MessageUtil.messagePlayer(player, "&cTeleportation cancelled, you took damage.");
                teleportToSpawnTask.cancel();
                playerData.setTeleportToSpawnTask(null);
            }
        }
    }

    /**
     * Handles players clicking inside of inventories.
     *
     * @param event InventoryClickEvent
     */
    @EventHandler
    public static void onInventoryClick(@NotNull InventoryClickEvent event) {
        // Nullability checks.
        if (event.getWhoClicked() == null
                || event.getClickedInventory() == null) {
            return;
        }

        // Player-related variables
        Player player = (Player) event.getWhoClicked();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        Location playerLocation = player.getLocation();

        // Event-related variables
        InventoryType clickedInventoryType = event.getClickedInventory().getType();
        String windowTitle = event.getView().getTitle();
        ItemStack currentItem = event.getCurrentItem();

        // Cancels inventory clicks based on inventory type.
        switch (clickedInventoryType) {
            case PLAYER:
                if (playerData.getActiveKit() == null) {
                    event.setCancelled(true);
                    player.updateInventory();
                    return;
                }
                break;

            case CRAFTING:
            case ANVIL:
            case BEACON:
            case HOPPER:
            case BREWING:
            case DROPPER:
            case FURNACE:
            case CREATIVE:
            case MERCHANT:
            case DISPENSER:
            case WORKBENCH:
            case ENCHANTING:
            case ENDER_CHEST:
                // Ignores players in creative mode with the modify permission.
                if (player.getGameMode() == GameMode.CREATIVE && player.hasPermission("kitpvp.modify")) {
                    break;
                }

                event.setCancelled(true);
                player.updateInventory();
                return;

            case CHEST:
            default:
                break;
        }

        // Cancels inventory clicks based on slot type.
        switch (event.getSlotType()) {
            case CRAFTING:
            case OUTSIDE:
            case FUEL:
            case RESULT:
            case ARMOR:
                event.setCancelled(true);
                player.updateInventory();
                return;

            case CONTAINER:
            case QUICKBAR:
            default:
                break;
        }

        // Ignores items without display names.
        if (currentItem.getItemMeta() == null
                || currentItem.getItemMeta().getDisplayName() == null) {
            return;
        }

        String itemName = ChatColor.stripColor(currentItem.getItemMeta().getDisplayName().trim());
        Kit kit = KitManager.getKit(itemName);
        int kitCost = (kit == null ? 0 : kit.getCost());
        String kitName = (kit == null ? null : kit.getName());

        // Ignores clicked inventories that are not chests.
        // Below, we handle the Kit Enchanter, Kit Selector, and Kit Shop.
        // These are all instances of CHEST inventory types.
        if (clickedInventoryType != InventoryType.CHEST) {
            return;
        }

        // Handles inventory clicks based on window title.
        switch (windowTitle) {
            case "Kit Enchanter":
                event.setCancelled(true);
                player.updateInventory();

                for (Enchants enchant : Enchants.values()) {
                    String enchantName = enchant.getFormattedName();

                    if (itemName.equals(enchantName)) {
                        // Checks if the player is in spawn.
                        if (!Regions.isInSafezone(playerLocation)) {
                            player.playSound(playerLocation, Sound.VILLAGER_NO, 1.0F, 1.0F);
                            MessageUtil.messagePlayer(player, "&cYou need to be in spawn to do this.");
                            event.setCancelled(true);
                            return;
                        }

                        // Checks if the player already has the enchantment.
                        if (playerData.getEnchants().contains(enchant)) {
                            player.playSound(playerLocation, Sound.VILLAGER_NO, 1.0F, 1.0F);
                            MessageUtil.messagePlayer(player, "&cYou already have the " + enchantName + " enchantment.");
                            event.setCancelled(true);
                            return;
                        }

                        // Checks if the player has enough coins.
                        if (playerData.getCoins() - enchant.getCost() < 0) {
                            player.playSound(playerLocation, Sound.VILLAGER_NO, 1.0F, 1.0F);
                            MessageUtil.messagePlayer(player, "&cYou do not have enough coins to purchase this enchant.");
                            event.setCancelled(true);
                            return;
                        }

                        // Purchases the enchantment for the player.
                        player.playSound(playerLocation, Sound.ANVIL_USE, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(player, "");
                        MessageUtil.messagePlayer(player, "&eThe &a" + enchantName + " &eenchantment has been purchased.");
                        MessageUtil.messagePlayer(player, "&eThis enchantment only lasts one life.");
                        MessageUtil.messagePlayer(player, "");
                        playerData.removeCoins(enchant.getCost());
                        playerData.getEnchants().add(enchant);

                        // Re-applies the player's active kit.
                        if (playerData.getActiveKit() != null) {
                            playerData.getActiveKit().apply(player);
                        }

                        // Re-opens the Kit Enchanter GUI.
                        new KitEnchanter(player);
                        break;
                    }
                }
                break;

            case "Kit Selector":
                event.setCancelled(true);
                player.updateInventory();

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
                        // Ignores invalid kits from being selected.
                        if (kit == null) {
                            MessageUtil.log(Level.INFO, "Invalid kit in Kit Selector for " + player.getName() + ".");
                            return;
                        }

                        // Applies the selected kit to the player.
                        kit.apply(player);
                        break;
                }
                break;

            case "Kit Shop":
                event.setCancelled(true);
                player.updateInventory();

                // Ignores invalid kits from being selected.
                if (kit == null) {
                    MessageUtil.log(Level.INFO, "Invalid kit in Kit Shop for " + player.getName() + ".");
                    return;
                }

                // Ignores players trying to purchase disabled kits.
                if (!kit.enabled()) {
                    MessageUtil.messagePlayer(player, "&cThis kit is currently disabled.");
                    return;
                }

                // Ignores players trying to purchase kits they cannot afford.
                if (playerData.getCoins() - kitCost < 0) {
                    MessageUtil.messagePlayer(player, "&cYou do not have enough coins to purchase " + kitName + ".");
                    return;
                }

                // Purchases the kit for the player.
                playerData.getOwnedKits().add(kit);
                playerData.removeCoins(kitCost);

                // Updates the player's inventory and sends a purchase message.
                MessageUtil.messagePlayer(player, "&aYou purchased the " + kitName + " kit for " + kitCost + " coins.");
                player.playSound(playerLocation, Sound.LEVEL_UP, 1, 1);
                player.closeInventory();
                break;

            default:
                break;
        }
    }

    /**
     * Handles players getting hungry.
     *
     * @param event FoodLevelChangeEvent
     */
    @EventHandler
    public static void onFoodChange(@NotNull FoodLevelChangeEvent event) {
        Player player = (Player) event.getEntity();

        // Cancels the event and sets the player's food level to 20.
        event.setCancelled(true);
        event.setFoodLevel(20);
        player.setSaturation(20);
    }

    /**
     * Handles mob spawning.
     *
     * @param event CreatureSpawnEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public static void onMobSpawn(@NotNull CreatureSpawnEvent event) {
        CreatureSpawnEvent.SpawnReason spawnReason = event.getSpawnReason();

        // Cancels all mob spawns except for spawner eggs, spawners, and custom events.
        switch (spawnReason) {
            case SPAWNER_EGG:
            case SPAWNER:
            case CUSTOM:
                return;

            default:
                event.setCancelled(true);
                break;
        }
    }

    /**
     * Handles right-clicking blocks and items.
     *
     * @param event PlayerInteractEvent
     */
    @EventHandler(priority = EventPriority.HIGH)
    public static void onRightClick(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        ItemStack item = event.getItem();
        Block block = event.getClickedBlock();
        Action action = event.getAction();

        // ???
        if (action.toString().contains("RIGHT") && block != null
                && block.getState() instanceof InventoryHolder) {
            event.setCancelled(true);
            return;
        }

        if (action.toString().contains("RIGHT") && item != null) {
            switch (item.getType()) {
                case WEB:
                case BLAZE_ROD:
                case IRON_BLOCK:
                case SLIME_BALL:
                case DISPENSER:
                    // Ignores right-clicking certain items.
                    break;

                case FISHING_ROD:
                    // Cancels using the fishing rod in spawn.
                    if (Regions.isInSafezone(player.getLocation())) {
                        event.setCancelled(true);
                    }
                    break;

                case POTION:
                    // Handles switching from using potions to using soup.
                    if (item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Using Potions")) {
                        event.setCancelled(true);
                        player.updateInventory();

                        playerData.setUsingSoup(true);
                        MessageUtil.messagePlayer(player, "&aYou are now using Soup.");
                        ItemStack healingItem = new ItemBuilder(Material.MUSHROOM_SOUP).name("&aUsing Soup &7(Right Click)").getItem();
                        player.getInventory().setItem(6, healingItem);
                        break;
                    }

                    // Cancels using potions in spawn.
                    if (Regions.isInSafezone(player.getLocation())) {
                        event.setCancelled(true);
                        player.updateInventory();
                    }
                    break;

                case MUSHROOM_SOUP:
                    // Handles switching from using soup to using potions.
                    if (item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Using Soup")) {
                        event.setCancelled(true);
                        player.updateInventory();

                        playerData.setUsingSoup(false);
                        MessageUtil.messagePlayer(player, "&aYou are now using Potions.");
                        ItemStack healingItem = new ItemBuilder(Material.POTION).hideInfo().durability(16421).name("&aUsing Potions &7(Right Click)").getItem();
                        player.getInventory().setItem(6, healingItem);
                        break;
                    }

                    // Cancels using soup in spawn.
                    if (Regions.isInSafezone(player.getLocation())) {
                        event.setCancelled(true);
                        break;
                    }

                    // Heals the player when using soup.
                    if (player.getHealth() < player.getMaxHealth()) {
                        event.setCancelled(true);
                        player.setHealth(Math.min(player.getHealth() + 7, player.getMaxHealth()));
                        player.setItemInHand(new ItemBuilder(Material.BOWL).name("&fBowl").getItem());
                    }
                    break;

                case WATCH:
                    // Handles using the Previous Kit item.
                    if (item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Previous Kit")
                            && playerData.getPreviousKit() != null && playerData.getActiveKit() == null) {
                        event.setCancelled(true);
                        playerData.getPreviousKit().apply(player);
                        MessageUtil.messagePlayer(player, "&aYou equipped the " + playerData.getPreviousKit().getName() + " kit.");
                        player.playSound(player.getLocation(), Sound.SLIME_WALK, 1, 1);
                        player.updateInventory();
                        player.closeInventory();
                    }
                    break;

                case SKULL_ITEM:
                    // Handles using the Your Stats item.
                    if (item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Your Stats")) {
                        event.setCancelled(true);
                        player.updateInventory();
                        StatsCmd.displayStats(player, true);
                    }
                    break;

                case NETHER_STAR:
                    // Handles using the Kit Selector item.
                    if (item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Kit Selector")) {
                        event.setCancelled(true);
                        player.updateInventory();
                        new KitSelector(player);
                    }
                    break;

                case ENCHANTED_BOOK:
                    // Handles using the Kit Enchanter item.
                    if (item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Kit Enchanter")) {
                        event.setCancelled(true);
                        player.updateInventory();
                        new KitEnchanter(player);
                    }
                    break;

                case ENDER_CHEST:
                    // Handles using the Kit Shop item.
                    if (item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Kit Shop")) {
                        event.setCancelled(true);
                        player.updateInventory();
                        new KitShop(player);
                    }
                    break;

                default:
                    // Handles using right-click items.
                    if (item.hasItemMeta() && item.getItemMeta().getDisplayName() != null
                            && item.getItemMeta().getDisplayName().contains("Right Click")) {
                        event.setCancelled(true);
                        player.updateInventory();
                    }
                    break;
            }
        }
    }

    /**
     * Handles player move events.
     *
     * @param event PlayerMoveEvent
     */
    @EventHandler
    public static void onPlayerMove(@NotNull PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        double deltaY = event.getTo().getY() - event.getFrom().getY();
        double deltaXZ = StrictMath.hypot(event.getTo().getX() - event.getFrom().getX(), event.getTo().getZ() - event.getFrom().getZ());
        boolean playerMoved = (deltaXZ > 0.05 || Math.abs(deltaY) > 0.05);

        // Ignores rotation updates.
        if (!playerMoved) {
            return;
        }

        // Cancels pending teleportation when moving.
        if (playerData.getTeleportToSpawnTask() != null) {
            MessageUtil.messagePlayer(player, "&cTeleportation cancelled, you moved.");
            playerData.getTeleportToSpawnTask().cancel();
            playerData.setTeleportToSpawnTask(null);
        }

        // Kills the player if they leave the map/fall into the void.
        if (player.getLocation().getY() < 0 && !player.getAllowFlight()) {
            DeathListener.handleDeath(player, false);
            return;
        }

        // Equips the player's previously used kit when they leave spawn without a kit equipped.
        if (playerData.getActiveKit() == null && !player.isDead() && !player.getAllowFlight()
                && !Regions.isInSafezone(event.getFrom())) {
            player.closeInventory();
            playerData.getPreviousKit().apply(player);
            MessageUtil.messagePlayer(player, "&cYour previous kit has been automatically applied.");
            return;
        }

        // Denies entry into spawn while combat tagged.
        // Also heals the player whilst in a safe zone.
        if (Regions.isInSafezone(event.getTo())) {
            if (Settings.combatTagDenyEnteringSpawn && CombatTag.isInCombat(player)) {
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

        // Removes the player's no-fall status.
        if (playerData.isNoFall() && !playerData.isPendingNoFallRemoval()
                && !Regions.isInSafezone(player.getLocation())) {
            playerData.setPendingNoFallRemoval(true);

            TaskUtil.runTaskLater(() -> {
                playerData.setPendingNoFallRemoval(false);

                if (playerData.isNoFall() && !Regions.isInSafezone(player.getLocation())) {
                    playerData.setNoFall(false);
                }
            }, 30L);
        }
    }

    /**
     * Handles players taking fall damage.
     *
     * @param event EntityDamageEvent
     */
    @EventHandler(ignoreCancelled = true)
    public static void onFallDamage(@NotNull EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            PlayerData playerData = PlayerDataManager.getPlayerData(player);

            // Cancels fall damage if the player has no-fall.
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL && playerData.isNoFall()) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Handles weather change events.
     *
     * @param event WeatherChangeEvent
     */
    @EventHandler
    public static void onWeatherChange(@NotNull WeatherChangeEvent event) {
        // Cancels weather changes.
        if (event.toWeatherState()) {
            event.setCancelled(true);
            event.getWorld().setStorm(false);
            event.getWorld().setThundering(false);
        }
    }

    /**
     * Handles thunder change events.
     *
     * @param event ThunderChangeEvent
     */
    @EventHandler
    public static void onThunderChange(@NotNull ThunderChangeEvent event) {
        // Cancels thunder changes.
        if (event.toThunderState()) {
            event.setCancelled(true);
            event.getWorld().setStorm(false);
            event.getWorld().setThundering(false);
        }
    }

    /**
     * Handles players breaking blocks.
     *
     * @param event BlockBreakEvent
     */
    @EventHandler
    public static void onBlockBreak(@NotNull BlockBreakEvent event) {
        Player player = event.getPlayer();

        // Ignores players in creative mode with the modify permission.
        if (player.getGameMode() == GameMode.CREATIVE
                && player.hasPermission("kitpvp.modify")) {
            return;
        }

        // Cancels the event.
        event.setCancelled(true);
    }

    /**
     * Handles players placing blocks.
     *
     * @param event BlockPlaceEvent
     */
    @EventHandler
    public static void onBlockPlace(@NotNull BlockPlaceEvent event) {
        Player player = event.getPlayer();

        // Ignores players in creative mode with the modify permission.
        if (player.getGameMode() == GameMode.CREATIVE
                && player.hasPermission("kitpvp.modify")) {
            return;
        }

        // Cancels the event.
        event.setCancelled(true);
    }

    /**
     * Handles blocks igniting.
     *
     * @param event BlockIgniteEvent
     */
    @EventHandler
    public static void onBlockIgnite(@NotNull BlockIgniteEvent event) {
        Player player = event.getPlayer();

        // Ignores players in creative mode with the modify permission.
        if (event.getCause() == BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL
                && player.getGameMode() == GameMode.CREATIVE
                && player.hasPermission("kitpvp.modify")) {
            return;
        }

        // Cancels the event.
        event.setCancelled(true);
    }

    /**
     * Handles blocks burning.
     *
     * @param event BlockBurnEvent
     */
    @EventHandler
    public static void onBlockBurn(@NotNull BlockBurnEvent event) {
        // Cancels the event.
        event.setCancelled(true);
    }

    /**
     * Handles leaves decaying.
     *
     * @param event LeavesDecayEvent
     */
    @EventHandler
    public static void onBlockDecay(@NotNull LeavesDecayEvent event) {
        // Cancels the event.
        event.setCancelled(true);
    }

    /**
     * Handles players interacting with beds.
     *
     * @param event PlayerBedEnterEvent
     */
    @EventHandler
    public static void onBedEnter(@NotNull PlayerBedEnterEvent event) {
        // Cancels the event.
        event.setCancelled(true);
    }

    /**
     * Handles experience changes.
     *
     * @param event PlayerTeleportEvent
     */
    @EventHandler
    public static void onExpChange(@NotNull PlayerExpChangeEvent event) {
        // Cancels the event.
        event.setAmount(0);
    }

    /**
     * Handles players picking up items.
     *
     * @param event PlayerPickupItemEvent
     */
    @EventHandler
    public static void onItemPickup(@NotNull PlayerPickupItemEvent event) {
        Player player = event.getPlayer();

        // Ignores players in creative mode with the modify permission.
        if (player.getGameMode() == GameMode.CREATIVE
                && player.hasPermission("kitpvp.modify")) {
            return;
        }

        // Cancels the event.
        event.setCancelled(true);
    }

    /**
     * Handles items getting damaged.
     *
     * @param event PlayerItemDamageEvent
     */
    @EventHandler
    public static void onItemDamage(@NotNull PlayerItemDamageEvent event) {
        Player player = event.getPlayer();

        // Cancels the event.
        event.setCancelled(true);
        player.updateInventory();
    }

    /**
     * Handles crop trampling.
     *
     * @param event EntityInteractEvent
     */
    @EventHandler
    public static void onCropTrample(@NotNull EntityInteractEvent event) {
        Block block = event.getBlock();

        // Cancels the event.
        if (block.getType() == Material.SOIL) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles block physics.
     *
     * @param event BlockPhysicsEvent
     */
    @EventHandler
    public static void onBlockPhysics(@NotNull BlockPhysicsEvent event) {
        // Cancels the event.
        switch (event.getChangedType()) {
            case SAND:
            case GRAVEL:
                event.setCancelled(true);
                break;

            default:
                break;
        }
    }
}
