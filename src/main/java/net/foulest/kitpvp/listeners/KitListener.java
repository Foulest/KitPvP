package net.foulest.kitpvp.listeners;

import lombok.Data;
import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.combattag.CombatTag;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
import net.foulest.kitpvp.kits.Kit;
import net.foulest.kitpvp.kits.type.*;
import net.foulest.kitpvp.region.Regions;
import net.foulest.kitpvp.util.*;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;
import java.util.*;

@Data
public class KitListener implements Listener {

    private static final Random RANDOM = new SecureRandom();

    /**
     * Handles the Archer ability.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onArcherAbility(@NotNull PlayerInteractEvent event) {
        // Player data
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        Location playerLoc = player.getLocation();
        Kit playerKit = playerData.getActiveKit();

        // Ignores the event if the player isn't using the Archer ability.
        if (!(playerKit instanceof Archer)
                || !event.getAction().toString().contains("RIGHT")
                || player.getItemInHand().getType() != Material.FEATHER) {
            return;
        }

        // Ignores the event if the player is in spawn.
        if (Regions.isInSafezone(playerLoc)) {
            MessageUtil.messagePlayer(player, ConstantUtil.ABILITY_IN_SPAWN);
            return;
        }

        // Ignores the event if the player's ability is on cooldown.
        if (playerData.hasCooldown(true)) {
            return;
        }

        // Plays the ability sound.
        player.getWorld().playSound(playerLoc, Sound.BAT_TAKEOFF, 1, 1);

        // Remove the player's existing speed, resistance, and weakness effects.
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
        player.removePotionEffect(PotionEffectType.WEAKNESS);

        // Gives the player speed, resistance, weakness, and regeneration.
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Settings.archerKitDuration * 20, 2, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Settings.archerKitDuration * 20, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, Settings.archerKitDuration * 20, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Settings.archerKitDuration * 20, 1, false, false));

        // Create a task that restores the Archer's speed.
        TaskUtil.runTaskLater(() -> {
            if (playerData.getActiveKit() instanceof Archer) {
                player.removePotionEffect(PotionEffectType.SPEED);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
            }
        }, Settings.archerKitDuration * 20L);

        // Sets the player's ability cooldown.
        MessageUtil.messagePlayer(player, "&aYour ability has been used.");
        playerData.setCooldown(playerKit, Settings.archerKitCooldown, true);
    }

    /**
     * Handles the Fisherman ability.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onFishermanAbility(@NotNull PlayerFishEvent event) {
        // Ignores the event if the target is not a player.
        if (!(event.getCaught() instanceof Player)) {
            return;
        }

        // Player data
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        Location playerLoc = player.getLocation();
        Kit playerKit = playerData.getActiveKit();

        // Target data
        Player target = (Player) event.getCaught();
        PlayerData targetData = PlayerDataManager.getPlayerData(target);
        Location targetLoc = target.getLocation();

        if (target == player) {
            MessageUtil.messagePlayer(player, "&cYou can't hook yourself.");
            event.setCancelled(true);
            return;
        }

        // Marks both players for combat.
        CombatTag.markForCombat(player, target);

        // Ignores the event if the player isn't using the Fisherman ability.
        if (!(playerKit instanceof Fisherman)
                || event.getState() != PlayerFishEvent.State.CAUGHT_ENTITY) {
            return;
        }

        // Ignores the event if the player is in spawn.
        if (Regions.isInSafezone(playerLoc)) {
            MessageUtil.messagePlayer(player, ConstantUtil.ABILITY_IN_SPAWN);
            return;
        }

        // Ignores the event if the player's ability is on cooldown.
        if (playerData.hasCooldown(true)) {
            return;
        }

        // Ignores ineligible players.
        if (targetData.getActiveKit() == null
                || Regions.isInSafezone(targetLoc)) {
            event.setCancelled(true);
            return;
        }

        // Play the ability sound.
        target.getWorld().playSound(targetLoc, Sound.WATER, 1, 1);
        target.getWorld().playEffect(targetLoc, Effect.SPLASH, 1);
        player.playSound(playerLoc, Sound.SPLASH2, 1, 1);

        // Teleports the target to the player's location.
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Settings.fishermanKitDuration * 20, 0, false, false));
        MessageUtil.messagePlayer(target, "&cYou have been hooked by a Fisherman!");
        event.getCaught().teleport(playerLoc);

        // Sets the player's ability cooldown.
        MessageUtil.messagePlayer(player, "&aYour ability has been used.");
        playerData.setCooldown(playerKit, Settings.fishermanKitCooldown, true);
    }

    /**
     * Handles the Kangaroo ability.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onKangarooAbility(@NotNull PlayerInteractEvent event) {
        // Player data
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        Location playerLoc = player.getLocation();
        Kit playerKit = playerData.getActiveKit();

        // Ignores the event if the player isn't using the Kangaroo ability.
        if (!(playerKit instanceof Kangaroo)
                || !event.getAction().toString().contains("RIGHT")
                || player.getItemInHand().getType() != Material.FIREWORK) {
            return;
        }

        // Ignores the event if the player is not on the ground.
        if (!BlockUtil.isOnGroundOffset(player, 0.001)) {
            MessageUtil.messagePlayer(player, ConstantUtil.NOT_ON_GROUND);
            return;
        }

        // Ignores the event if the player is in spawn.
        if (Regions.isInSafezone(playerLoc)) {
            MessageUtil.messagePlayer(player, ConstantUtil.ABILITY_IN_SPAWN);
            return;
        }

        // Ignores the event if the player's ability is on cooldown.
        if (playerData.hasCooldown(true)) {
            return;
        }

        // Play the ability sound.
        player.getWorld().playSound(playerLoc, Sound.SLIME_WALK2, 1, 1);

        // Launches the player into the air.
        Vector direction = getKangarooLaunchVector(player);
        player.setVelocity(direction);

        // Sets the player's ability cooldown.
        MessageUtil.messagePlayer(player, "&aYour ability has been used.");
        playerData.setCooldown(playerKit, Settings.kangarooKitCooldown, true);
    }

    /**
     * Returns the launch vector for the Kangaroo ability.
     *
     * @param player The player.
     * @return The launch vector.
     */
    private static @NotNull org.bukkit.util.Vector getKangarooLaunchVector(@NotNull Player player) {
        Vector direction = player.getEyeLocation().getDirection();

        // Adjusts the direction based on whether the player is sneaking.
        if (player.isSneaking()) {
            direction.setY(0.3);
            direction.multiply(2.5);
        } else {
            direction.setY(1.2);
        }
        return direction;
    }

    /**
     * Handles the Mage ability.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onMageAbility(@NotNull PlayerInteractEvent event) {
        // Player data
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        Location playerLoc = player.getLocation();
        Kit playerKit = playerData.getActiveKit();

        // Ignores the event if the player isn't using the Mage ability.
        if (!(playerKit instanceof Mage)
                || !event.getAction().toString().contains("RIGHT")
                || player.getItemInHand().getType() != Material.GLOWSTONE_DUST) {
            return;
        }

        // Ignores the event if the player is in spawn.
        if (Regions.isInSafezone(playerLoc)) {
            MessageUtil.messagePlayer(player, ConstantUtil.ABILITY_IN_SPAWN);
            return;
        }

        // Ignores the event if the player's ability is on cooldown.
        if (playerData.hasCooldown(true)) {
            return;
        }

        // Gets the nearby players within a 5 block radius.
        Collection<Player> nearbyPlayers = getNearbyPlayers(player, 5, 5, 5);

        // Ignores the event if there are no players nearby.
        if (nearbyPlayers.isEmpty()) {
            player.playSound(playerLoc, Sound.VILLAGER_NO, 1, 1);
            MessageUtil.messagePlayer(player, "&cAbility failed: no players nearby.");
            playerData.setCooldown(playerKit, 5, true);
            return;
        }

        for (Player target : nearbyPlayers) {
            Location targetLoc = target.getLocation();

            // Give the target Slowness, Blindness, and Weakness for 3 seconds.
            target.playSound(targetLoc, Sound.FIZZ, 1, 1);
            target.getWorld().playSound(targetLoc, Sound.ZOMBIE_WOODBREAK, 0.5F, 1);
            target.getWorld().playEffect(targetLoc, Effect.LARGE_SMOKE, 1);
            target.getWorld().playEffect(targetLoc, Effect.LARGE_SMOKE, 1);
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Settings.mageKitDuration * 20, 1, false, false));
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Settings.mageKitDuration * 20, 1, false, false));
            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, Settings.mageKitDuration * 20, 1, false, false));
            MessageUtil.messagePlayer(target, "&cYou have been debuffed by a Mage!");
        }

        // Sets the player's ability cooldown.
        player.playSound(playerLoc, Sound.FIZZ, 1, 1);
        MessageUtil.messagePlayer(player, "&aYour ability has been used.");
        playerData.setCooldown(playerKit, Settings.mageKitCooldown, true);
    }

    /**
     * Handles the Ninja ability.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onNinjaAbility(@NotNull PlayerInteractEvent event) {
        // Player data
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        Location playerLoc = player.getLocation();
        Kit playerKit = playerData.getActiveKit();

        // Ignores the event if the player isn't using the Kangaroo ability.
        if (!(playerKit instanceof Ninja)
                || !event.getAction().toString().contains("RIGHT")
                || player.getItemInHand().getType() != Material.INK_SACK) {
            return;
        }

        // Ignores the event if the player is in spawn.
        if (Regions.isInSafezone(playerLoc)) {
            MessageUtil.messagePlayer(player, ConstantUtil.ABILITY_IN_SPAWN);
            return;
        }

        // Ignores the event if the player's ability is on cooldown.
        if (playerData.hasCooldown(true)) {
            return;
        }

        // Gives the player Invisibility.
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,
                Settings.ninjaKitDuration * 20, 0, false, false));

        // Plays a smoke sound effect.
        player.getWorld().playEffect(playerLoc, Effect.LARGE_SMOKE, 1, 1);

        // Hides the player from other players.
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target == player) {
                continue;
            }

            target.hidePlayer(player);
        }

        // Create a task that restores the player's visibility.
        TaskUtil.runTaskLater(() -> {
            if (playerData.getActiveKit() instanceof Ninja) {
                player.getWorld().playSound(playerLoc, Sound.BAT_IDLE, 1, 1);
                MessageUtil.messagePlayer(player, "&cYou are no longer invisible.");

                for (Player target : Bukkit.getOnlinePlayers()) {
                    if (target == player) {
                        continue;
                    }

                    target.showPlayer(player);
                }
            }
        }, Settings.ninjaKitDuration * 20L);

        // Sets the player's ability cooldown.
        player.getWorld().playSound(playerLoc, Sound.BAT_DEATH, 1, 1);
        MessageUtil.messagePlayer(player, "&aYou are now invisible.");
        playerData.setCooldown(playerKit, Settings.ninjaKitCooldown, true);
    }

    /**
     * Handles Ninjas damaging players while invisible.
     *
     * @param event EntityDamageByEntityEvent
     */
    @EventHandler(ignoreCancelled = true)
    public static void onNinjaHit(@NotNull EntityDamageByEntityEvent event) {
        Entity damagerEntity = event.getDamager();
        Entity targetEntity = event.getEntity();

        // Handles players damaging other entities.
        if (damagerEntity instanceof Player) {
            Player damager = (Player) damagerEntity;
            PlayerData damagerData = PlayerDataManager.getPlayerData(damager);

            // Cancels hits while Ninja is invisible.
            if (targetEntity instanceof Player) {
                if (damagerData.getActiveKit() instanceof Ninja
                        && damager.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    MessageUtil.messagePlayer(damager, "&cYou can't damage other players while invisible.");
                    event.setCancelled(true);
                }
            }
        }
    }

    /**
     * Handles the Pyro ability.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onPyroAbility(@NotNull PlayerInteractEvent event) {
        // Player data
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        Location playerLoc = player.getLocation();
        Kit playerKit = playerData.getActiveKit();

        // Ignores the event if the player isn't using the Tank ability.
        if (!(playerKit instanceof Pyro)
                || !event.getAction().toString().contains("RIGHT")
                || player.getItemInHand().getType() != Material.FIREBALL) {
            return;
        }

        // Ignores the event if the player is in spawn.
        if (Regions.isInSafezone(playerLoc)) {
            MessageUtil.messagePlayer(player, ConstantUtil.ABILITY_IN_SPAWN);
            return;
        }

        // Ignores the event if the player's ability is on cooldown.
        if (playerData.hasCooldown(true)) {
            return;
        }

        // Gets the nearby players within a 5 block radius.
        Collection<Player> nearbyPlayers = getNearbyPlayers(player, 5, 5, 5);

        // Ignores the event if there are no players nearby.
        if (nearbyPlayers.isEmpty()) {
            player.playSound(playerLoc, Sound.VILLAGER_NO, 1, 1);
            MessageUtil.messagePlayer(player, "&cAbility failed: no players nearby.");
            playerData.setCooldown(playerKit, 5, true);
            return;
        }

        // Play the ability sound and effect.
        player.getWorld().playSound(playerLoc, Sound.GHAST_FIREBALL, 1, 1);
        player.getWorld().playEffect(playerLoc, Effect.MOBSPAWNER_FLAMES, 1);

        for (Player target : nearbyPlayers) {
            Location targetLoc = target.getLocation();

            // Damage the target and light them on fire.
            target.damage(Settings.pyroKitDamage);
            target.setFireTicks(Settings.pyroKitDuration * 20);

            // Play a sound to the target.
            target.playSound(targetLoc, Sound.GHAST_FIREBALL, 1, 1);
            target.playEffect(targetLoc, Effect.MOBSPAWNER_FLAMES, 1);
            MessageUtil.messagePlayer(target, "&cYou have been set on fire by a Pyro!");
        }

        // Sets the player's ability cooldown.
        MessageUtil.messagePlayer(player, "&aYour ability has been used.");
        playerData.setCooldown(playerKit, Settings.pyroKitCooldown, true);
    }

    /**
     * Handles the Tank ability.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onTankAbility(@NotNull PlayerInteractEvent event) {
        // Player data
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        Location playerLoc = player.getLocation();
        Kit playerKit = playerData.getActiveKit();

        // Ignores the event if the player isn't using the Tank ability.
        if (!(playerKit instanceof Tank)
                || !event.getAction().toString().contains("RIGHT")
                || player.getItemInHand().getType() != Material.ANVIL) {
            return;
        }

        // Ignores the event if the player is in spawn.
        if (Regions.isInSafezone(playerLoc)) {
            MessageUtil.messagePlayer(player, ConstantUtil.ABILITY_IN_SPAWN);
            return;
        }

        // Ignores the event if the player's ability is on cooldown.
        if (playerData.hasCooldown(true)) {
            return;
        }

        // Play the ability sound.
        player.getWorld().playSound(playerLoc, Sound.ANVIL_BREAK, 1, 1);

        // Add the damage resistance effect.
        player.removePotionEffect(PotionEffectType.SLOW);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Settings.tankKitDuration * 20, 2, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Settings.tankKitDuration * 20, 2));

        // Create a task that restores the Tank's slowness after 5 seconds.
        TaskUtil.runTaskLater(() -> {
            if (playerData.getActiveKit() instanceof Tank) {
                player.removePotionEffect(PotionEffectType.SLOW);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 0, false, false));
            }
        }, Settings.tankKitDuration * 20L);

        // Sets the player's ability cooldown.
        MessageUtil.messagePlayer(player, "&aYour ability has been used.");
        playerData.setCooldown(playerKit, Settings.tankKitCooldown, true);
    }

    /**
     * Handles the Vampire ability.
     *
     * @param event The event.
     */
    @EventHandler
    @SuppressWarnings("NestedMethodCall")
    public static void onVampireAbility(@NotNull PlayerInteractEvent event) {
        // Player data
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        Location playerLoc = player.getLocation();
        Kit playerKit = playerData.getActiveKit();

        // Ignores the event if the player isn't using the Tank ability.
        if (!(playerKit instanceof Vampire)
                || !event.getAction().toString().contains("RIGHT")
                || player.getItemInHand().getType() != Material.REDSTONE) {
            return;
        }

        // Ignores the event if the player is in spawn.
        if (Regions.isInSafezone(playerLoc)) {
            MessageUtil.messagePlayer(player, ConstantUtil.ABILITY_IN_SPAWN);
            return;
        }

        // Ignores the event if the player's ability is on cooldown.
        if (playerData.hasCooldown(true)) {
            return;
        }

        // Create a task to notify the player that the Life Steal ability has ended.
        BukkitTask lifeStealTask = new BukkitRunnable() {
            @Override
            public void run() {
                player.playSound(playerLoc, Sound.BAT_IDLE, 1, 1);
                MessageUtil.messagePlayer(player, "&cLife Steal is no longer active.");
            }
        }.runTaskLater(KitPvP.instance, Settings.vampireKitDuration * 20L);

        // Cancel the Life Steal cooldown task if it's active.
        TaskUtil.runTaskLater(() -> playerData.setLifeStealCooldown(null), Settings.vampireKitDuration * 20L);

        // Set the Life Steal cooldown task.
        playerData.setLifeStealCooldown(lifeStealTask);

        // Sets the player's ability cooldown.
        player.playSound(playerLoc, Sound.BAT_HURT, 1, 1);
        MessageUtil.messagePlayer(player, "&aLife Steal has been activated.");
        playerData.setCooldown(playerKit, Settings.vampireKitCooldown, true);
    }

    /**
     * Handles the Life Steal ability.
     *
     * @param event The event.
     */
    @EventHandler(ignoreCancelled = true)
    public static void onVampireHit(@NotNull EntityDamageByEntityEvent event) {
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

        // Ignores the event if the damager is not using the Vampire kit.
        if (!(playerData.getActiveKit() instanceof Vampire)
                || targetData.getActiveKit() == null
                || Regions.isInSafezone(playerLoc)
                || Regions.isInSafezone(targetLoc)) {
            return;
        }

        // Gives the player half a heart per hit.
        if (playerData.getLifeStealCooldown() != null) {
            double health = player.getHealth();
            double maxHealth = player.getMaxHealth();
            player.playSound(playerLoc, Sound.CHICKEN_WALK, 1, 1);
            player.setHealth(Math.min(health + 1, maxHealth));
        }
    }

    /**
     * Gets the nearby players within a certain range.
     *
     * @param player The player.
     * @param x The x range.
     * @param y The y range.
     * @param z The z range.
     * @return The nearby players.
     */
    private static @NotNull Collection<Player> getNearbyPlayers(@NotNull Player player, double x, double y, double z) {
        Collection<Player> nearbyPlayers = new ArrayList<>();

        for (Entity entity : player.getNearbyEntities(x, y, z)) {
            // Ignores the entity if it's not a player.
            if (!(entity instanceof Player)) {
                continue;
            }

            Player target = (Player) entity;
            PlayerData targetData = PlayerDataManager.getPlayerData(target);
            Location targetLoc = target.getLocation();

            // Ignores ineligible players.
            if (targetData.getActiveKit() == null
                    || Regions.isInSafezone(targetLoc)
                    || (target.hasPotionEffect(PotionEffectType.INVISIBILITY)
                    && targetData.getActiveKit() instanceof Ninja)) {
                continue;
            }

            nearbyPlayers.add(target);
        }
        return nearbyPlayers;
    }
}
