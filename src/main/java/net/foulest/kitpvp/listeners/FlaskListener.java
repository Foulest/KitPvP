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

import lombok.Data;
import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
import net.foulest.kitpvp.region.Regions;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.Settings;
import net.foulest.kitpvp.util.item.ItemBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

@Data
public class FlaskListener implements Listener {

    public static ItemStack FLASK = new ItemBuilder(Material.POTION).name("&aFlask &7(Right Click)")
            .lore("&7Regenerates 5 hearts over 3 seconds.")
            .hideInfo().durability(8229).getItem();

    public static ItemStack EMPTY_FLASK = new ItemBuilder(Material.GLASS_BOTTLE).name("&cFlask &7(On Cooldown)")
            .lore("&7Regenerates 5 hearts over 3 seconds.")
            .hideInfo().getItem();

    /**
     * Sets a cooldown for a specific kit.
     */
    private static void setFlaskCooldown(@NotNull PlayerData playerData) {
        Player player = playerData.getPlayer();

        BukkitTask cooldownTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Set the player's flask back to a potion.
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item == null
                            || item.getItemMeta() == null
                            || item.getItemMeta().getDisplayName() == null) {
                        continue;
                    }

                    int itemAmount = item.getAmount();

                    if (item.getItemMeta().getDisplayName().contains("Flask")) {
                        FLASK.setAmount(itemAmount);
                        player.getInventory().remove(item);
                        player.getInventory().addItem(FLASK);
                        player.updateInventory();
                        return;
                    }
                }
            }
        }.runTaskLater(KitPvP.instance, Settings.flaskCooldown * 20L);

        // Set the Flask cooldown and regeneration tasks.
        playerData.setFlaskCooldownTask(cooldownTask);
    }

    static void addFlaskToInventory(@NotNull Player player, int amount) {
        boolean hasFlasks = false;

        // Check if the player already has a flask.
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null
                    || item.getItemMeta() == null
                    || item.getItemMeta().getDisplayName() == null) {
                continue;
            }

            if (item.getItemMeta().getDisplayName().contains("Flask")) {
                hasFlasks = true;

                // Update the Flask amount.
                int itemAmount = item.getAmount();

                if (itemAmount < Settings.flaskAmount) {
                    item.setAmount(Math.min(itemAmount + amount, Settings.flaskAmount));
                    player.updateInventory();
                    return;
                }
            }
        }

        if (!hasFlasks) {
            // Add the Flask to the player's inventory.
            FLASK.setAmount(Math.min(amount, Settings.flaskAmount));
            player.getInventory().addItem(FLASK);
            player.updateInventory();
        }
    }

    /**
     * Handles right-clicking blocks and items.
     *
     * @param event PlayerInteractEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public static void onRightClick(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        ItemStack item = event.getItem();
        Action action = event.getAction();

        double health = player.getHealth();
        double maxHealth = player.getMaxHealth();

        if (action.toString().contains("RIGHT") && item != null) {
            Location playerLoc = player.getLocation();

            switch (item.getType()) {
                case POTION:
                    // Cancels using potions in spawn.
                    if (Regions.isInSafezone(playerLoc)) {
                        if (item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Flask")) {
                            MessageUtil.messagePlayer(player, "&cYou cannot use Flasks in spawn.");
                        }

                        event.setCancelled(true);
                        player.updateInventory();
                        return;
                    }

                    // Check if the item contains the name "Flask".
                    if (!item.hasItemMeta()
                            || !item.getItemMeta().hasDisplayName()
                            || !item.getItemMeta().getDisplayName().contains("Flask")) {
                        break;
                    }

                    int itemAmount = item.getAmount();

                    // We can assume the player right-clicked the Flask.
                    // The Flask is ready to be used, as it's not a GLASS_BOTTLE.

                    // Cancels using the Flask if the player is already regenerating health.
                    if (player.hasPotionEffect(PotionEffectType.REGENERATION)) {
                        MessageUtil.messagePlayer(player, "&cYou are already regenerating health.");
                        event.setCancelled(true);
                        player.updateInventory();
                        return;
                    }

                    if (health < maxHealth) {
                        // Removes the Flask from the player's inventory if there is only one.
                        // Otherwise, sets the Flask to a GLASS_BOTTLE with the amount of the Flask.
                        if (itemAmount == 1) {
                            player.getInventory().remove(item);
                        } else {
                            EMPTY_FLASK.setAmount(itemAmount - 1);
                            player.setItemInHand(EMPTY_FLASK);
                        }

                        event.setCancelled(true);
                        player.updateInventory();

                        // Set the Flask cooldown.
                        setFlaskCooldown(playerData);

                        // Send the player a message and play a sound.
                        player.playSound(playerLoc, Sound.DRINK, 1, 1);

                        // Heal the player for 5 hearts (10 health) over 3 seconds.
                        PotionEffect regeneration = new PotionEffect(PotionEffectType.REGENERATION, 3 * 20, 3);
                        player.addPotionEffect(regeneration);
                    } else {
                        MessageUtil.messagePlayer(player, "&cYou are already at full health.");
                        event.setCancelled(true);
                        player.updateInventory();
                    }
                    break;

                case GLASS_BOTTLE:
                    // Handles using the Flask item.
                    if (item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Flask")) {
                        event.setCancelled(true);
                        player.updateInventory();

                        // Cancels using the Flask if it's on cooldown.
                        if (playerData.getFlaskCooldownTask() != null) {
                            MessageUtil.messagePlayer(player, "&cThe Flask is still on cooldown.");
                            break;
                        }
                    }
                    break;

                default:
                    break;
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

        Player player = (Player) event.getWhoClicked();
        ItemStack currentItem = event.getCurrentItem();

        // Nullability checks.
        if (currentItem == null
                || !currentItem.hasItemMeta()
                || !currentItem.getItemMeta().hasDisplayName()
                || !currentItem.getItemMeta().getDisplayName().contains("Flask")) {
            return;
        }

        // Cancels players clicking flasks.
        event.setCancelled(true);
        player.updateInventory();
    }
}
