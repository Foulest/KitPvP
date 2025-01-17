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
import net.foulest.kitpvp.kits.type.Pyro;
import net.foulest.kitpvp.region.Regions;
import net.foulest.kitpvp.util.ConstantUtil;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.Settings;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

@Data
public class PyroListener implements Listener {

    /**
     * Handles the Pyro ability.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onPyroAbility(@NotNull PlayerInteractEvent event) {
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
        Material abilityItem = Material.FLINT_AND_STEEL;

        // Ignores the event if the given item does not match the desired item.
        if (itemStack == null
                || !itemStack.hasItemMeta()
                || !itemStack.getItemMeta().hasDisplayName()
                || itemStack.getType() != abilityItem) {
            return;
        }

        // Ignores the event if the player is not using the desired kit.
        if (!(playerKit instanceof Pyro)) {
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

        // Cancel the event to prevent the player from using the flint and steel.
        event.setCancelled(true);

        // Launches a projectile just to get the velocity, then removes it.
        Projectile flare = player.launchProjectile(Snowball.class);
        flare.setFireTicks(99999);

        // Play a sound effect at the player's location.
        player.getWorld().playSound(playerLoc, Sound.FIREWORK_LAUNCH, 1.0F, 1.0F);
        player.getWorld().playSound(playerLoc, Sound.FIREWORK_BLAST, 1.0F, 1.0F);

        // Set the projectile's metadata.
        flare.setMetadata("Pyro", new FixedMetadataValue(KitPvP.getInstance(), true));

        // Sets the player's ability cooldown.
        MessageUtil.messagePlayer(player, "&aYour ability has been used.");
        playerData.setCooldown(playerKit, abilityItem, Settings.pyroKitCooldown, true);
    }

    /**
     * Handles the Pyro's flare hitting the ground or players.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onPyroFlareHit(@NotNull ProjectileHitEvent event) {
        Projectile entity = event.getEntity();

        // Ignores the event if the projectile is not from the Pyro's ability.
        if (!entity.hasMetadata("Pyro")) {
            return;
        }

        // Play fire and sizzling effects at the projectile's location.
        // Essentially, a flare hitting the ground.
        Location location = entity.getLocation();
        entity.getWorld().playEffect(location, Effect.MOBSPAWNER_FLAMES, 1);
        entity.getWorld().playEffect(location, Effect.SMOKE, 1);
        entity.getWorld().playSound(location, Sound.FIZZ, 1, 1);

        // Remove the projectile.
        entity.remove();
    }

    /**
     * Handles the Pyro's flare hitting an entity.
     *
     * @param event The event.
     */
    @EventHandler(ignoreCancelled = true)
    public static void onPyroFlareHit(@NotNull EntityDamageByEntityEvent event) {
        // Ignores the event if the damager is not a projectile.
        if (!(event.getDamager() instanceof Projectile)) {
            return;
        }

        Projectile projectile = (Projectile) event.getDamager();

        // Ignores the event if the projectile is not from the Pyro's ability.
        if (!projectile.hasMetadata("Pyro")) {
            return;
        }

        // Ignores the event if the entity is not a player.
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        // Ignores the event if the shooter is not a player.
        if (!(projectile.getShooter() instanceof Player)) {
            return;
        }

        // Player data
        Player target = (Player) event.getEntity();
        String targetName = target.getName();
        Player shooter = (Player) projectile.getShooter();

        // Deal damage to the target.
        event.setDamage(Settings.pyroKitDamage);

        // Inflict fire damage to the target.
        target.setFireTicks(Settings.pyroKitDuration * 20);

        // Send messages to the players.
        MessageUtil.messagePlayer(target, "&cYou have been hit by the Pyro's flare!");
        MessageUtil.messagePlayer(shooter, "&aYou hit &e" + targetName + " &awith your flare!");

        // Remove the projectile.
        projectile.remove();
    }

//    /**
//     * Handles the Pyro ability.
//     *
//     * @param event The event.
//     */
//    @EventHandler
//    public static void onPyroAbility(@NotNull PlayerInteractEvent event) {
//        // Ignores the event if the player isn't right-clicking.
//        if (event.getAction() != Action.RIGHT_CLICK_AIR
//                && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
//            return;
//        }
//
//        Player player = event.getPlayer();
//        PlayerData playerData = PlayerDataManager.getPlayerData(player);
//        Location playerLoc = player.getLocation();
//        Kit playerKit = playerData.getActiveKit();
//        ItemStack item = event.getItem();
//
//        // Checks for common ability exclusions.
//        if (AbilityUtil.shouldBeExcluded(playerLoc, player, playerData, playerKit, item, Material.FIREBALL)) {
//            return;
//        }
//
//        // Gets the nearby players within a 5 block radius.
//        Collection<Player> nearbyPlayers = AbilityUtil.getNearbyPlayers(player, 5, 5, 5);
//
//        // Ignores the event if there are no players nearby.
//        if (nearbyPlayers.isEmpty()) {
//            player.playSound(playerLoc, Sound.VILLAGER_NO, 1, 1);
//            MessageUtil.messagePlayer(player, "&cAbility failed: no players nearby.");
//            playerData.setCooldown(playerKit, Material.FIREBALL, 3, true);
//            return;
//        }
//
//        // Play the ability sound and effect.
//        player.getWorld().playSound(playerLoc, Sound.GHAST_FIREBALL, 1, 1);
//        player.getWorld().playEffect(playerLoc, Effect.MOBSPAWNER_FLAMES, 1);
//
//        for (Player target : nearbyPlayers) {
//            Location targetLoc = target.getLocation();
//
//            // Lights targets on fire.
//            target.setFireTicks(Settings.pyroKitDuration * 20);
//
//            // Play a sound to the target.
//            target.playSound(targetLoc, Sound.GHAST_FIREBALL, 1, 1);
//            target.getWorld().playEffect(targetLoc, Effect.MOBSPAWNER_FLAMES, 1);
//            MessageUtil.messagePlayer(target, "&cYou have been set on fire by a Pyro!");
//        }
//
//        // Sets the player's ability cooldown.
//        MessageUtil.messagePlayer(player, "&aYour ability has been used.");
//        playerData.setCooldown(playerKit, Material.FIREBALL, Settings.pyroKitCooldown, true);
//    }

    /**
     * Handles the Axtinguisher ability.
     *
     * @param event The event.
     */
    @EventHandler(ignoreCancelled = true)
    public static void onAxtinguisherHit(@NotNull EntityDamageByEntityEvent event) {
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
        String targetName = target.getName();

        // Ignores the event if the damager is not using the Pyro kit.
        if (!(playerData.getActiveKit() instanceof Pyro)
                || targetData.getActiveKit() == null
                || Regions.isInSafezone(playerLoc)
                || Regions.isInSafezone(targetLoc)) {
            return;
        }

        // Ignores hits that aren't with the Axtinguisher.
        if (player.getItemInHand() == null
                || !player.getItemInHand().hasItemMeta()
                || !player.getItemInHand().getItemMeta().hasDisplayName()
                || !player.getItemInHand().getItemMeta().getDisplayName().contains("Axtinguisher")) {
            return;
        }

        // ----------------------------------------------------------------
        // If a player hits a target with the Axtinguisher...
        // 1. If the target is on fire...
        // 1a. Extinguish the target.
        // 1b. Deal damage to the target.
        // 1c. Play effects at the target's location.
        // 2. If the hit was a kill, give the Pyro a speed boost.
        // ----------------------------------------------------------------

        // 1. If the target is on fire...
        if (target.getFireTicks() > 0) {
            // 1a. Extinguish the target.
            MessageUtil.messagePlayer(player, "&aYou have extinguished &e" + targetName + "&a!");
            target.setFireTicks(0);

            if (!(targetData.getActiveKit() instanceof Pyro)) {
                // 1b. Deal damage to the target.
                MessageUtil.messagePlayer(target, "&cYou have been axtinguished by a Pyro!");
                target.damage(Settings.pyroKitDamage);

                // 1c. Play effects at the target's location.
                target.getWorld().playSound(targetLoc, Sound.FIZZ, 1, 1);
                target.getWorld().playEffect(targetLoc, Effect.CRIT, 1);

                // 2. If the hit was a kill, give the Pyro a speed boost.
                if (target.getHealth() - event.getFinalDamage() - Settings.pyroKitDamage < -4.0
                        && event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                    MessageUtil.messagePlayer(player, "&aYou have been given a speed boost on kill! | Target Health: " + (target.getHealth() - event.getFinalDamage() - Settings.pyroKitDamage));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 3 * 20, 1, true, true));
                }
            }
        }
    }

    /**
     * Handles hitting players with the Powerjack.
     *
     * @param event The event.
     */
    @EventHandler(ignoreCancelled = true)
    public static void onPowerjackHit(@NotNull EntityDamageByEntityEvent event) {
        // Ignores the event if the damager or target is not a player.
        if (!(event.getDamager() instanceof Player)
                || !(event.getEntity() instanceof Player)) {
            return;
        }

        // Player data
        Player player = (Player) event.getDamager();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        // Target data
        Player target = (Player) event.getEntity();
        PlayerData targetData = PlayerDataManager.getPlayerData(target);

        // ----------------------------------------------------------------
        // If the player, who's holding a Powerjack, attacks the target...
        // 1. Check if the damage dealt was a kill.
        // 2. If it is, heal the Pyro for 6.0 health.
        // 3. If it isn't, ignore the event.
        //
        // If the player attacks the target, who's holding a Powerjack...
        // 1. Apply a 20% damage vulnerability on hit.
        // ----------------------------------------------------------------

        // If the player, who's holding a Powerjack, attacks the target...
        if (playerData.getActiveKit() instanceof Pyro
                && player.getItemInHand() != null
                && player.getItemInHand().hasItemMeta()
                && player.getItemInHand().getItemMeta().hasDisplayName()
                && player.getItemInHand().getItemMeta().getDisplayName().contains("Powerjack")) {

            // 1. Check if the damage dealt was a kill.
            if (target.getHealth() - event.getFinalDamage() <= 0) {

                // 2. If it is, heal the Pyro for 6.0 health.
                double health = player.getHealth();
                double maxHealth = player.getMaxHealth();
                player.setHealth(Math.min(health + 6.0, maxHealth));
                MessageUtil.messagePlayer(player, "&aYou have been healed by the Powerjack!");
            }
        }

        // If the player attacks the target, who's holding a Powerjack...
        if (targetData.getActiveKit() instanceof Pyro
                && target.getItemInHand() != null
                && target.getItemInHand().hasItemMeta()
                && target.getItemInHand().getItemMeta().hasDisplayName()
                && target.getItemInHand().getItemMeta().getDisplayName().contains("Powerjack")) {

            // 1. Apply a 20% damage vulnerability on hit.
            double damage = event.getDamage();
            event.setDamage(damage * 1.2);
        }
    }

    /**
     * Handles moving while holding the Powerjack.
     *
     * @param event The event.
     */
    @EventHandler(ignoreCancelled = true)
    public static void onPowerjackMove(@NotNull PlayerMoveEvent event) {
        // Player data
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        // Ignores the event if the damager is not using the Pyro kit.
        if (!(playerData.getActiveKit() instanceof Pyro)) {
            return;
        }

        // ----------------------------------------------------------------
        // If the player is holding the Powerjack...
        // 1. Check if they have a Speed effect with a higher amplifier than 0.
        // 2. If they do, don't add the Powerjack bonus.
        // 3. If they don't, add the Powerjack bonus.
        //
        // If the player is not holding the Powerjack...
        // 1. Check if they have a Speed effect with a higher amplifier than 0.
        // 2. If they do, don't remove the Speed effect.
        // 3. If they don't, remove the Speed effect.
        // ----------------------------------------------------------------

        // If the player is holding the Powerjack...
        if (player.getItemInHand() != null
                && player.getItemInHand().hasItemMeta()
                && player.getItemInHand().getItemMeta().hasDisplayName()
                && player.getItemInHand().getItemMeta().getDisplayName().contains("Powerjack")) {

            // 1. Check if they have a Speed effect with a higher amplifier than 0.
            for (PotionEffect effect : player.getActivePotionEffects()) {
                if (effect.getType().equals(PotionEffectType.SPEED)) {

                    // 2. If they do, don't add the Powerjack bonus.
                    if (effect.getAmplifier() > 0) {
                        return;
                    }
                }
            }

            // 3. If they don't, add the Powerjack bonus.
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));

        } else {
            // If the player is not holding the Powerjack...

            // 1. Check if they have a Speed effect with a higher amplifier than 0.
            for (PotionEffect effect : player.getActivePotionEffects()) {
                if (effect.getType().equals(PotionEffectType.SPEED)) {

                    // 2. If they do, don't remove the Speed effect.
                    if (effect.getAmplifier() > 0) {
                        return;
                    }

                    // 3. If they don't, remove the Speed effect.
                    player.removePotionEffect(PotionEffectType.SPEED);
                }
            }
        }
    }
}
