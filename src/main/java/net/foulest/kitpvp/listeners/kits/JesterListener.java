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
package net.foulest.kitpvp.listeners.kits;

import lombok.Data;
import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
import net.foulest.kitpvp.kits.Kit;
import net.foulest.kitpvp.kits.type.Jester;
import net.foulest.kitpvp.region.Regions;
import net.foulest.kitpvp.util.ConstantUtil;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.Settings;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

@Data
public class JesterListener implements Listener {

    /**
     * Handles the Jester ability.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onJesterAbility(@NotNull PlayerInteractEvent event) {
        // Ignores the event if the player isn't right-clicking.
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        Location playerLoc = player.getLocation();
        Kit playerKit = playerData.getActiveKit();
        ItemStack itemStack = event.getItem();
        Material abilityItem = Material.STICK;

        // Ignores the event if the given item does not match the desired item.
        if (itemStack == null
                || !itemStack.hasItemMeta()
                || !itemStack.getItemMeta().hasDisplayName()
                || itemStack.getType() != abilityItem) {
            return;
        }

        // Ignores the event if the player is not using the desired kit.
        if (!(playerKit instanceof Jester)) {
            return;
        }

        // Ignores the event if the player is in a safe zone.
        if (Regions.isInSafezone(playerLoc)) {
            MessageUtil.messagePlayer(player, ConstantUtil.ABILITY_IN_SPAWN);
            return;
        }

        // Ignores the event if the player's ability is on cooldown.
        if (playerData.hasCooldown(abilityItem, true)) {
            return;
        }

        // Launches a projectile from the Jester's eyes.
        Projectile projectile = player.launchProjectile(Snowball.class);

        // Play a sound effect at the player's location.
        player.getWorld().playSound(playerLoc, Sound.STEP_WOOD, 1.0F, 1.0F);
        player.getWorld().playSound(playerLoc, Sound.HORSE_WOOD, 1.0F, 1.0F);
        player.getWorld().playSound(playerLoc, Sound.SHEEP_WALK, 1.0F, 1.0F);

        // Set the projectile's metadata.
        projectile.setMetadata("Jester", new FixedMetadataValue(KitPvP.getInstance(), true));

        // Sets the player's ability cooldown.
        MessageUtil.messagePlayer(player, "&aYour ability has been used.");
        playerData.setCooldown(playerKit, abilityItem, Settings.jesterKitCooldown, true);
    }

    /**
     * Handles the Jester's projectile hitting the ground or players.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onJesterProjectileHit(@NotNull ProjectileHitEvent event) {
        Projectile entity = event.getEntity();

        // Ignores the event if the projectile is not from the Jester's ability.
        if (!entity.hasMetadata("Jester")) {
            return;
        }

        // Play glass breaking sounds where the ornament hit.
        Location location = entity.getLocation();
        entity.getWorld().playEffect(location, Effect.STEP_SOUND, 20);

        // Remove the projectile.
        entity.remove();
    }

    /**
     * Handles the Jester's projectile hitting an entity.
     *
     * @param event The event.
     */
    @EventHandler(ignoreCancelled = true)
    public static void onJesterProjectileHit(@NotNull EntityDamageByEntityEvent event) {
        // Ignores the event if the damager is not a projectile.
        if (!(event.getDamager() instanceof Projectile)) {
            return;
        }

        Projectile projectile = (Projectile) event.getDamager();

        // Ignores the event if the projectile is not from the Jester's ability.
        if (!projectile.hasMetadata("Jester")) {
            return;
        }

        // Ignores the event if the entity is not a living entity.
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        // Ignores the event if the shooter is not a player.
        if (!(projectile.getShooter() instanceof Player)) {
            return;
        }

        // Player data
        LivingEntity target = (LivingEntity) event.getEntity();

        // Deal damage to the target.
        event.setDamage(Settings.jesterKitDamage);

        // Inflict Wither III on the target.
        target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, Settings.jesterKitDuration * 20, 2));

        // Remove the projectile.
        projectile.remove();
    }

    /**
     * Handles the Jester's Sword ability.
     *
     * @param event The event.
     */
    @EventHandler(ignoreCancelled = true)
    public static void onJesterSwordHit(@NotNull EntityDamageByEntityEvent event) {
        // Ignores the event if the damager or target is not a player.
        if (!(event.getDamager() instanceof Player)
                || !(event.getEntity() instanceof Player)) {
            return;
        }

        // Player data
        Player player = (Player) event.getDamager();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        Location playerLoc = player.getLocation();

        // Target data
        Player target = (Player) event.getEntity();
        PlayerData targetData = PlayerDataManager.getPlayerData(target);
        Location targetLoc = target.getLocation();

        // Ignores the event if the damager is not using the Jester kit.
        if (!(playerData.getActiveKit() instanceof Jester)
                || targetData.getActiveKit() == null
                || Regions.isInSafezone(playerLoc)
                || Regions.isInSafezone(targetLoc)) {
            return;
        }

        // Ignores hits that aren't with the Jester's Sword.
        if (player.getItemInHand() == null
                || !player.getItemInHand().hasItemMeta()
                || !player.getItemInHand().getItemMeta().hasDisplayName()
                || !player.getItemInHand().getItemMeta().getDisplayName().contains("Jester's Sword")) {
            return;
        }

        double health = player.getHealth();
        double maxHealth = player.getMaxHealth();
        double damage = event.getDamage();

        // If a player's health is <50%, deal more damage, vice versa.
        if (health / maxHealth < 0.5) {
            event.setDamage(damage * 1.16666666667);
            player.getWorld().playSound(targetLoc, Sound.SILVERFISH_HIT, 0.5F, 1.0F);
        } else {
            event.setDamage(damage * 0.83333333333);
        }
    }
}
