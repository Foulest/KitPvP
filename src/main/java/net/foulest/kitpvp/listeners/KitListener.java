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

import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.combattag.CombatTag;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
import net.foulest.kitpvp.kits.type.*;
import net.foulest.kitpvp.region.Regions;
import net.foulest.kitpvp.util.*;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Handles all kit-related events in the plugin.
 *
 * @author Foulest
 */
public class KitListener implements Listener {

    static final Map<UUID, Collection<PotionEffect>> drainedEffects = new HashMap<>();
    private static final Map<UUID, Location> imprisonedPlayers = new HashMap<>();
    private static final Random RANDOM = new SecureRandom();

    /**
     * Handles the Burrower ability, which creates a room of bricks above the player.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onBurrowerAbility(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        List<Location> roomLocations = getRoomLocations(player.getLocation());

        if (playerData.getActiveKit() instanceof Burrower
                && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.BRICK) {

            // Ignores the event if the player is in spawn.
            if (Regions.isInSafezone(player.getLocation())) {
                MessageUtil.messagePlayer(player, ConstantUtil.ABILITY_IN_SPAWN);
                return;
            }

            // Ignores the event if the player's ability is on cooldown.
            if (playerData.hasCooldown(true)) {
                return;
            }

            // Checks if there's enough space above the player to burrow.
            for (Location loc : roomLocations) {
                if (loc.getBlock().getType() != Material.AIR) {
                    MessageUtil.messagePlayer(player, "&cThere's not enough space above you to burrow.");
                    playerData.setCooldown(new Burrower(), 5, true);
                    return;
                }
            }

            // Adds the room of bricks above the player.
            Collection<BlockState> pendingRollback = new ArrayList<>();
            for (Location location : roomLocations) {
                pendingRollback.add(location.getBlock().getState());
                location.getBlock().setType(Material.BRICK);
            }
            roomLocations.get(0).getBlock().setType(Material.GLOWSTONE);

            // Teleports the player into the room.
            player.teleport(player.getLocation().add(0.0, 10.0, 0.0));

            // Rolls back the room after the set ability duration.
            TaskUtil.runTaskLater(() -> {
                for (BlockState block : pendingRollback) {
                    rollback(block);
                }
            }, Settings.burrowerKitDuration * 20L);

            // Gives the player the no-fall status.
            playerData.setNoFall(true);
            playerData.setPendingNoFallRemoval(true);

            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getActiveKit(), Settings.burrowerKitCooldown, true);
        }
    }

    /**
     * Handles the Cactus ability, which poisons the target on hit.
     *
     * @param event The event.
     */
    @EventHandler(ignoreCancelled = true)
    public static void onCactusHit(@NotNull EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player receiver = (Player) event.getEntity();
            PlayerData damagerData = PlayerDataManager.getPlayerData(damager);
            PlayerData receiverData = PlayerDataManager.getPlayerData(receiver);

            // Inflicts the poison effect on the target.
            if (damagerData.getActiveKit() instanceof Cactus
                    && receiverData.getActiveKit() != null
                    && damager.getItemInHand().getType() == Material.CACTUS
                    && !Regions.isInSafezone(damager.getLocation())
                    && !Regions.isInSafezone(receiver.getLocation())) {

                receiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON,
                        Settings.cactusKitPassiveDuration * 20, 0, false, false));
            }
        }
    }

    /**
     * Handles the Dragon ability, which shoots a wave of fire that damages nearby players.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onDragonAbility(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        if (playerData.getActiveKit() instanceof Dragon
                && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.FIREBALL) {

            // Ignores the event if the player is in spawn.
            if (Regions.isInSafezone(player.getLocation())) {
                MessageUtil.messagePlayer(player, ConstantUtil.ABILITY_IN_SPAWN);
                return;
            }

            // Ignores the event if the player's ability is on cooldown.
            if (playerData.hasCooldown(true)) {
                return;
            }

            Collection<Player> nearbyPlayers = new ArrayList<>();

            // Adds nearby players to a list.
            for (Entity entity : player.getNearbyEntities(Settings.dragonKitRange, Settings.dragonKitRange, Settings.dragonKitRange)) {
                if (entity instanceof Player) {
                    Player nearbyPlayer = (Player) entity;
                    PlayerData nearbyPlayerData = PlayerDataManager.getPlayerData(nearbyPlayer);

                    if (nearbyPlayerData.getActiveKit() != null
                            && !Regions.isInSafezone(nearbyPlayer.getLocation())) {
                        nearbyPlayers.add(nearbyPlayer);
                    }
                }
            }

            // Ignores the event if no players are nearby.
            if (nearbyPlayers.isEmpty()) {
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                MessageUtil.messagePlayer(player, "&cAbility failed; no players found nearby.");
                playerData.setCooldown(playerData.getActiveKit(), 5, true);
                return;
            }

            // Plays flame effects and fireball sounds.
            player.getWorld().playEffect(player.getLocation(), Effect.MOBSPAWNER_FLAMES, 1);
            player.getWorld().playEffect(player.getEyeLocation(), Effect.MOBSPAWNER_FLAMES, 1);
            player.playSound(player.getEyeLocation(), Sound.GHAST_FIREBALL, 1.0F, 0.0F);

            // Damages and strikes nearby players with lightning.
            for (Player nearbyPlayer : nearbyPlayers) {
                PlayerData nearbyPlayerData = PlayerDataManager.getPlayerData(nearbyPlayer);

                if (player.hasLineOfSight(nearbyPlayer)
                        && nearbyPlayerData.getActiveKit() != null
                        && !Regions.isInSafezone(nearbyPlayer.getLocation())) {
                    nearbyPlayer.damage(Settings.dragonKitDamage, player);
                    nearbyPlayer.setFireTicks(Settings.dragonKitDuration * 20);
                }
            }

            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getActiveKit(), Settings.dragonKitCooldown, true);
        }
    }

    /**
     * Handles the Fisherman ability, which pulls the target towards the player.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onFishermanAbility(@NotNull PlayerFishEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        if (event.getCaught() instanceof Player) {
            Player receiver = (Player) event.getCaught();
            PlayerData receiverData = PlayerDataManager.getPlayerData(receiver);

            // Marks both players for combat.
            CombatTag.markForCombat(player, receiver);

            if (playerData.getActiveKit() instanceof Fisherman
                    && event.getState() == PlayerFishEvent.State.CAUGHT_ENTITY) {

                // Ignores the event if the player is in spawn.
                if (Regions.isInSafezone(player.getLocation())) {
                    MessageUtil.messagePlayer(player, ConstantUtil.ABILITY_IN_SPAWN);
                    return;
                }

                // Ignores the event if the player's ability is on cooldown.
                if (playerData.hasCooldown(true)) {
                    return;
                }

                if (receiverData.getActiveKit() != null && !Regions.isInSafezone(receiver.getLocation())) {
                    // Teleports the target to the player's location.
                    imprisonedPlayers.remove(receiver.getUniqueId());
                    event.getCaught().teleport(player.getLocation());

                    MessageUtil.messagePlayer(player, "&aYour ability has been used.");
                    playerData.setCooldown(playerData.getActiveKit(), Settings.fishermanKitCooldown, true);
                } else {
                    event.setCancelled(true);
                }
            }
        }
    }

    /**
     * Handles the Ghost ability, which plays a sound when the player moves.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onGhostMove(@NotNull PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        if (playerData.getActiveKit() instanceof Ghost && !player.isSneaking()
                && !Regions.isInSafezone(player.getLocation())) {

            // Plays a sound when the player moves.
            for (Entity entity : player.getNearbyEntities(Settings.ghostKitRange, Settings.ghostKitRange, Settings.ghostKitRange)) {
                if (entity instanceof Player) {
                    ((Player) entity).playSound(player.getLocation(), Sound.CHICKEN_WALK, 0.01F, 0.5F);
                }
            }
        }
    }

    /**
     * Handles the Hulk ability, which launches other players into the air.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onHulkAbility(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        if (playerData.getActiveKit() instanceof Hulk
                && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.PISTON_STICKY_BASE) {

            // Ignores the event if the player is in spawn.
            if (Regions.isInSafezone(player.getLocation())) {
                MessageUtil.messagePlayer(player, ConstantUtil.ABILITY_IN_SPAWN);
                return;
            }

            // Ignores the event if the player's ability is on cooldown.
            if (playerData.hasCooldown(true)) {
                return;
            }

            Collection<Player> nearbyPlayers = new ArrayList<>();

            // Adds nearby players to a list.
            for (Entity entity : player.getNearbyEntities(Settings.hulkKitRange, Settings.hulkKitRange, Settings.hulkKitRange)) {
                if (entity instanceof Player) {
                    Player nearbyPlayer = (Player) entity;
                    PlayerData nearbyPlayerData = PlayerDataManager.getPlayerData(nearbyPlayer);

                    if (nearbyPlayerData.getActiveKit() != null
                            && !Regions.isInSafezone(nearbyPlayer.getLocation())) {
                        nearbyPlayers.add(nearbyPlayer);
                    }
                }
            }

            // Ignores the event if no players are nearby.
            if (nearbyPlayers.isEmpty()) {
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                MessageUtil.messagePlayer(player, "&cAbility failed; no players found nearby.");
                playerData.setCooldown(playerData.getActiveKit(), 5, true);
                return;
            }

            // Creates an explosion at the player's location.
            player.getWorld().createExplosion(player.getLocation(), 0.0F, false);

            // Damages and launches nearby players into the air.
            for (Player nearbyPlayer : nearbyPlayers) {
                PlayerData nearbyPlayerData = PlayerDataManager.getPlayerData(nearbyPlayer);

                if (nearbyPlayerData.getActiveKit() != null
                        && !Regions.isInSafezone(nearbyPlayer.getLocation())) {
                    // Gets the direction to launch the player in.
                    Vector direction = nearbyPlayer.getEyeLocation().getDirection();
                    direction.multiply(Settings.hulkKitMultiplier);
                    direction.setY(1.0);

                    // Damages and launches the player into the air.
                    nearbyPlayer.setVelocity(direction);
                    nearbyPlayer.damage(Settings.hulkKitDamage, player);
                }
            }

            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getActiveKit(), Settings.hulkKitCooldown, true);
        }
    }

    /**
     * Handles the Imprisoner ability, which shoots a projectile that imprisons the target in a cage.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onImprisonerAbility(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        if (playerData.getActiveKit() instanceof Imprisoner
                && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.DISPENSER) {

            // Ignores the event if the player is in spawn.
            if (Regions.isInSafezone(player.getLocation())) {
                MessageUtil.messagePlayer(player, ConstantUtil.ABILITY_IN_SPAWN);
                return;
            }

            // Ignores the event if the player's ability is on cooldown.
            if (playerData.hasCooldown(true)) {
                return;
            }

            // Shoots a snowball that imprisons the target in a cage.
            player.launchProjectile(Snowball.class).setMetadata("imprisoner",
                    new FixedMetadataValue(KitPvP.instance, true));

            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getActiveKit(), Settings.imprisonerKitCooldown, true);
        }
    }

    /**
     * Handles the Imprisoner ability, which imprisons the target in a cage when hit.
     *
     * @param event The event.
     */
    @EventHandler(ignoreCancelled = true)
    public static void onImprisonerHit(@NotNull EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Snowball) {
            Snowball snowball = (Snowball) event.getDamager();

            if (snowball.getShooter() instanceof Player) {
                Player damager = (Player) snowball.getShooter();
                PlayerData damagerData = PlayerDataManager.getPlayerData(damager);

                if (event.getEntity() instanceof Player) {
                    Player receiver = (Player) event.getEntity();
                    PlayerData receiverData = PlayerDataManager.getPlayerData(damager);

                    if (damagerData.getActiveKit() instanceof Imprisoner
                            && receiverData.getActiveKit() != null
                            && snowball.hasMetadata("imprisoner")
                            && !imprisonedPlayers.containsKey(receiver.getUniqueId())
                            && !Regions.isInSafezone(receiver.getLocation())) {

                        // Checks if there's enough space above the target to imprison them.
                        List<Block> cageBlocks = getCageBlocks(receiver.getLocation().add(0.0, Settings.imprisonerKitHeight, 0.0));
                        for (Block cageBlock : cageBlocks) {
                            if (cageBlock.getType() != Material.AIR) {
                                MessageUtil.messagePlayer(damager, "&cThere's not enough space above the target.");
                                damagerData.setCooldown(damagerData.getActiveKit(), 5, true);
                                return;
                            }
                        }

                        // Adds the cage blocks to a list for rollback.
                        Collection<BlockState> pendingRollback = new ArrayList<>();
                        for (Block block : cageBlocks) {
                            pendingRollback.add(block.getState());
                        }

                        // Adds the cage blocks to the world.
                        cageBlocks.get(0).setType(Material.MOSSY_COBBLESTONE);
                        for (int i = 1; i < 9; ++i) {
                            cageBlocks.get(i).setType(Material.IRON_FENCE);
                        }
                        cageBlocks.get(9).setType(Material.MOSSY_COBBLESTONE);
                        cageBlocks.get(10).setType(Material.LAVA);

                        // Damages the target.
                        receiver.damage(Settings.imprisonerKitDamage, damager);

                        // Teleports the target into the cage.
                        Location prisonLoc = receiver.getLocation().add(0.0, Settings.imprisonerKitHeight, 0.0);
                        prisonLoc.setX(prisonLoc.getBlockX() + 0.5);
                        prisonLoc.setY(Math.floor(prisonLoc.getY()));
                        prisonLoc.setZ(prisonLoc.getBlockZ() + 0.5);
                        receiver.teleport(prisonLoc);

                        // Adds the target to the imprisoned players list.
                        imprisonedPlayers.put(receiver.getUniqueId(), prisonLoc);

                        // Rolls back the cage after the set ability duration.
                        TaskUtil.runTaskLater(() -> {
                            imprisonedPlayers.remove(receiver.getUniqueId());

                            for (BlockState block : pendingRollback) {
                                rollback(block);
                            }
                        }, Settings.imprisonerKitDuration * 20L);
                    }
                }
            }
        }
    }

    /**
     * Handles the Kangaroo ability, which launches the player into the air.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onKangarooAbility(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        if (playerData.getActiveKit() instanceof Kangaroo
                && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.FIREWORK) {

            // Ignores the event if the player is not on the ground.
            if (!BlockUtil.isOnGroundOffset(player, 0.001)) {
                MessageUtil.messagePlayer(player, ConstantUtil.NOT_ON_GROUND);
                return;
            }

            // Ignores the event if the player is in spawn.
            if (Regions.isInSafezone(player.getLocation())) {
                MessageUtil.messagePlayer(player, ConstantUtil.ABILITY_IN_SPAWN);
                return;
            }

            // Ignores the event if the player's ability is on cooldown.
            if (playerData.hasCooldown(true)) {
                return;
            }

            // Launches the player into the air.
            Vector direction = getKangarooLaunchVector(player);
            player.setVelocity(direction);

            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getActiveKit(), Settings.kangarooKitCooldown, true);
        }
    }

    /**
     * Returns the launch vector for the Kangaroo ability.
     *
     * @param player The player.
     * @return The launch vector.
     */
    private static @NotNull Vector getKangarooLaunchVector(@NotNull Player player) {
        Vector direction = player.getEyeLocation().getDirection();

        if (player.isSneaking()) {
            direction.setY(Settings.kangarooKitSneakingHeight);

            if (Settings.kangarooKitSneakingMultiplier != 0.0) {
                direction.multiply(Settings.kangarooKitSneakingMultiplier);
            }
        } else {
            direction.setY(Settings.kangarooKitNormalHeight);

            if (Settings.kangarooKitNormalMultiplier != 0.0) {
                direction.multiply(Settings.kangarooKitNormalMultiplier);
            }
        }
        return direction;
    }

    /**
     * Handles the Mage ability, which gives the player a random potion effect.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onMageAbility(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        if (playerData.getActiveKit() instanceof Mage
                && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.GLOWSTONE_DUST) {

            // Ignores the event if the player is in spawn.
            if (Regions.isInSafezone(player.getLocation())) {
                MessageUtil.messagePlayer(player, ConstantUtil.ABILITY_IN_SPAWN);
                return;
            }

            // Ignores the event if the player's ability is on cooldown.
            if (playerData.hasCooldown(true)) {
                return;
            }

            // Checks if the player's effects are still drained.
            if (drainedEffects.containsKey(player.getUniqueId())) {
                MessageUtil.messagePlayer(player, "&cAbility failed; your effects are still drained.");
                return;
            }

            // List of available potion effects.
            List<PotionEffectType> effects = Arrays.asList(
                    PotionEffectType.SPEED, PotionEffectType.SLOW,
                    PotionEffectType.FAST_DIGGING, PotionEffectType.SLOW_DIGGING,
                    PotionEffectType.INCREASE_DAMAGE, PotionEffectType.HEAL,
                    PotionEffectType.HARM, PotionEffectType.JUMP,
                    PotionEffectType.CONFUSION, PotionEffectType.REGENERATION,
                    PotionEffectType.DAMAGE_RESISTANCE, PotionEffectType.FIRE_RESISTANCE,
                    PotionEffectType.WATER_BREATHING, PotionEffectType.INVISIBILITY,
                    PotionEffectType.BLINDNESS, PotionEffectType.NIGHT_VISION,
                    PotionEffectType.WEAKNESS, PotionEffectType.POISON,
                    PotionEffectType.WITHER, PotionEffectType.HEALTH_BOOST,
                    PotionEffectType.ABSORPTION);

            // Randomly selects and applies an effect from the list.
            PotionEffectType randomEffect = effects.get(RANDOM.nextInt(effects.size()));
            int amplifier = RANDOM.nextInt(3);
            int duration = Math.max(5, (RANDOM.nextInt(30) + 1)) * 20;
            player.addPotionEffect(new PotionEffect(randomEffect, duration, amplifier, false, false));

            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getActiveKit(), Settings.mageKitCooldown, true);
        }
    }

    /**
     * Handles the Monk ability, which swaps the items in the target's hotbar.
     *
     * @param event The event.
     */
    @EventHandler(ignoreCancelled = true)
    public static void onMonkAbility(@NotNull PlayerInteractEntityEvent event) {
        Player damager = event.getPlayer();
        PlayerData damagerData = PlayerDataManager.getPlayerData(damager);

        if (event.getRightClicked() instanceof Player) {
            Player receiver = (Player) event.getRightClicked();
            PlayerData receiverData = PlayerDataManager.getPlayerData(receiver);

            if (damagerData.getActiveKit() instanceof Monk
                    && damager.getItemInHand().getType() == Material.BLAZE_ROD
                    && receiverData.getActiveKit() != null
                    && !Regions.isInSafezone(receiver.getLocation())) {

                // Ignores the event if the player is in spawn.
                if (Regions.isInSafezone(damager.getLocation())) {
                    MessageUtil.messagePlayer(damager, ConstantUtil.ABILITY_IN_SPAWN);
                    return;
                }

                // Ignores the event if the player's ability is on cooldown.
                if (damagerData.hasCooldown(true)) {
                    return;
                }

                // Generates a shuffled list of hotbar indices (0-8).
                List<Integer> slots = IntStream.range(0, 9).boxed().collect(Collectors.toList());
                Collections.shuffle(slots);

                // Swaps items in the hotbar based on the shuffled indices.
                PlayerInventory inventory = receiver.getInventory();
                ItemStack[] hotbar = new ItemStack[9];

                // Stores the current items in a temporary array.
                for (int i = 0; i < 9; i++) {
                    hotbar[i] = inventory.getItem(i);
                }

                // Swaps items based on the shuffled list.
                for (int i = 0; i < 9; i++) {
                    inventory.setItem(i, hotbar[slots.get(i)]);
                }

                receiver.updateInventory();
                MessageUtil.messagePlayer(receiver, "&cYour items have been swapped by a Monk!");

                MessageUtil.messagePlayer(damager, "&aYour ability has been used.");
                damagerData.setCooldown(damagerData.getActiveKit(), Settings.monkKitCooldown, true);
            }
        }
    }

    /**
     * Handles the Spiderman ability, which shoots a projectile that creates a web.
     *
     * @param event The event.
     */
    @EventHandler(ignoreCancelled = true)
    public static void onSpidermanAbility(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        if (playerData.getActiveKit() instanceof Spiderman
                && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.WEB) {

            // Ignores the event if the player is in spawn.
            if (Regions.isInSafezone(player.getLocation())) {
                MessageUtil.messagePlayer(player, ConstantUtil.ABILITY_IN_SPAWN);
                return;
            }

            // Ignores the event if the player's ability is on cooldown.
            if (playerData.hasCooldown(true)) {
                return;
            }

            // Shoots a snowball that creates a web around a player.
            player.launchProjectile(Snowball.class).setMetadata("spiderman",
                    new FixedMetadataValue(KitPvP.instance, true));

            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getActiveKit(), Settings.spidermanKitCooldown, true);
        }
    }

    /**
     * Handles the Spiderman ability, which traps the target in webs when hit.
     *
     * @param event The event.
     */
    @EventHandler(ignoreCancelled = true)
    public static void onSpidermanHit(@NotNull EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Snowball) {
            Snowball snowball = (Snowball) event.getDamager();

            if (snowball.getShooter() instanceof Player && event.getEntity() instanceof Player) {
                Player damager = (Player) snowball.getShooter();
                Player receiver = (Player) event.getEntity();
                PlayerData damagerData = PlayerDataManager.getPlayerData(damager);
                PlayerData receiverData = PlayerDataManager.getPlayerData(receiver);

                if (damagerData.getActiveKit() instanceof Spiderman) {
                    // Checks if the target is in spawn.
                    if (receiverData.getActiveKit() == null || Regions.isInSafezone(receiver.getLocation())) {
                        damager.playSound(damager.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(damager, "&cYou can't use your ability on players in spawn.");
                        damagerData.setCooldown(damagerData.getActiveKit(), 5, true);
                        return;
                    }

                    if (snowball.hasMetadata("spiderman")) {
                        Collection<BlockState> blockStates = new ArrayList<>();
                        Block block = receiver.getLocation().getBlock();

                        // Ignores water and lava and moves the target to the nearest air block.
                        while (block.getType() == Material.STATIONARY_WATER
                                || block.getType() == Material.STATIONARY_LAVA) {
                            receiver.getLocation().add(0.0, 1.0, 0.0);
                            block = receiver.getLocation().getBlock();
                        }

                        // Ignores webs and ladders and adds the surrounding blocks to a list for rollback.
                        for (Location loc : getSurroundingLocations(receiver.getLocation())) {
                            if (loc.getBlock().getType() == Material.WEB) {
                                continue;
                            }

                            for (Location loc2 : getSurroundingLocations(loc)) {
                                if (loc2.getBlock().getType() != Material.LADDER) {
                                    blockStates.add(loc.getBlock().getState());
                                }
                            }
                        }

                        // Adds webs around the target.
                        if (block.getType() != Material.WEB) {
                            blockStates.add(block.getState());
                        }
                        for (BlockState blockState : blockStates) {
                            blockState.getBlock().setType(Material.WEB);
                        }

                        // Rolls back the web after the set ability duration.
                        TaskUtil.runTaskLater(() -> {
                            for (BlockState blockState : blockStates) {
                                rollback(blockState);
                            }
                        }, Settings.spidermanKitDuration * 20L);
                    }
                }
            }
        }
    }

    /**
     * Handles the Summoner ability, which summons an iron golem that attacks nearby players.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onSummonerAbility(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        if (playerData.getActiveKit() instanceof Summoner
                && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.IRON_BLOCK) {

            // Ignores the event if the player is in spawn.
            if (Regions.isInSafezone(player.getLocation())) {
                MessageUtil.messagePlayer(player, ConstantUtil.ABILITY_IN_SPAWN);
                return;
            }

            // Ignores the event if the player's ability is on cooldown.
            if (playerData.hasCooldown(true)) {
                return;
            }

            Collection<Player> nearbyPlayers = new ArrayList<>();

            // Adds nearby players to a list.
            for (Entity entity : player.getNearbyEntities(Settings.summonerKitRange, Settings.summonerKitRange, Settings.summonerKitRange)) {
                if (entity instanceof Player) {
                    Player nearbyPlayer = (Player) entity;
                    PlayerData nearbyPlayerData = PlayerDataManager.getPlayerData(nearbyPlayer);

                    if (nearbyPlayerData.getActiveKit() != null
                            && !Regions.isInSafezone(nearbyPlayer.getLocation())) {
                        nearbyPlayers.add(nearbyPlayer);
                    }
                }
            }

            // Ignores the event if no players are nearby.
            if (nearbyPlayers.isEmpty()) {
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                MessageUtil.messagePlayer(player, "&cAbility failed; no players found nearby.");
                playerData.setCooldown(playerData.getActiveKit(), 5, true);
                return;
            }

            // Spawns an iron golem that attacks nearby players.
            IronGolem ironGolem = (IronGolem) player.getWorld().spawnEntity(player.getLocation(), EntityType.IRON_GOLEM);
            ironGolem.setMetadata(player.getName(), new FixedMetadataValue(KitPvP.instance, true));
            ironGolem.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 99999, 1, false, false));

            // Sets the iron golem's target to nearby players.
            for (Player nearbyPlayer : nearbyPlayers) {
                PlayerData nearbyPlayerData = PlayerDataManager.getPlayerData(nearbyPlayer);

                if (nearbyPlayerData.getActiveKit() != null
                        && !Regions.isInSafezone(nearbyPlayer.getLocation())) {
                    ironGolem.setTarget(nearbyPlayer);
                }
            }

            // Removes the iron golem after the set ability duration.
            TaskUtil.runTaskLater(ironGolem::remove, Settings.summonerKitDuration * 20L);

            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getActiveKit(), Settings.summonerKitCooldown, true);
        }
    }

    /**
     * Handles the Tamer ability, which spawns wolves that follow the player.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onTamerAbility(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        if (playerData.getActiveKit() instanceof Tamer
                && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.BONE) {

            // Ignores the event if the player is in spawn.
            if (Regions.isInSafezone(player.getLocation())) {
                MessageUtil.messagePlayer(player, ConstantUtil.ABILITY_IN_SPAWN);
                return;
            }

            // Ignores the event if the player's ability is on cooldown.
            if (playerData.hasCooldown(true)) {
                return;
            }

            Collection<Wolf> wolves = new ArrayList<>();

            // Spawns wolves that follow the player.
            for (int i = 0; i < Settings.tamerKitAmount; ++i) {
                Wolf wolf = (Wolf) player.getWorld().spawnEntity(player.getLocation(), EntityType.WOLF);
                wolf.setOwner(player);
                wolf.isAngry();
                wolf.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 1));
                wolves.add(wolf);
            }

            // Removes the wolves after the set ability duration.
            TaskUtil.runTaskLater(() -> {
                for (Wolf wolf : wolves) {
                    wolf.remove();
                }
            }, Settings.tamerKitDuration * 20L);

            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getActiveKit(), Settings.tamerKitCooldown, true);
        }
    }

    /**
     * Handles the Thor ability, which strikes nearby players with lightning.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onThorAbility(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        if (playerData.getActiveKit() instanceof Thor
                && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.IRON_AXE) {

            // Ignores the event if the player is in spawn.
            if (Regions.isInSafezone(player.getLocation())) {
                MessageUtil.messagePlayer(player, ConstantUtil.ABILITY_IN_SPAWN);
                return;
            }

            // Ignores the event if the player's ability is on cooldown.
            if (playerData.hasCooldown(true)) {
                return;
            }

            Collection<Player> nearbyPlayers = new ArrayList<>();

            // Adds nearby players to a list.
            for (Entity entity : player.getNearbyEntities(Settings.thorKitRange, Settings.thorKitRange, Settings.thorKitRange)) {
                if (entity instanceof Player) {
                    Player nearbyPlayer = (Player) entity;
                    PlayerData nearbyPlayerData = PlayerDataManager.getPlayerData(nearbyPlayer);

                    if (nearbyPlayerData.getActiveKit() != null
                            && !Regions.isInSafezone(nearbyPlayer.getLocation())) {
                        nearbyPlayers.add(nearbyPlayer);
                    }
                }
            }

            // Ignores the event if no players are nearby.
            if (nearbyPlayers.isEmpty()) {
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                MessageUtil.messagePlayer(player, "&cAbility failed; no players found nearby.");
                playerData.setCooldown(playerData.getActiveKit(), 5, true);
                return;
            }

            // Damages and strikes nearby players with lightning.
            for (Player nearbyPlayer : nearbyPlayers) {
                PlayerData nearbyPlayerData = PlayerDataManager.getPlayerData(nearbyPlayer);

                if (nearbyPlayerData.getActiveKit() != null
                        && !Regions.isInSafezone(nearbyPlayer.getLocation())) {
                    player.getWorld().strikeLightningEffect(nearbyPlayer.getLocation());
                    nearbyPlayer.damage(Settings.thorKitDamage, player);
                    nearbyPlayer.setFireTicks(Settings.thorKitDuration * 20);
                }
            }

            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getActiveKit(), Settings.thorKitCooldown, true);
        }
    }

    /**
     * Handles the Timelord ability, which freezes nearby players.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onTimelordAbility(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        if (playerData.getActiveKit() instanceof Timelord
                && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.WATCH) {

            // Ignores the event if the player is in spawn.
            if (Regions.isInSafezone(player.getLocation())) {
                MessageUtil.messagePlayer(player, ConstantUtil.ABILITY_IN_SPAWN);
                return;
            }

            // Ignores the event if the player's ability is on cooldown.
            if (playerData.hasCooldown(true)) {
                return;
            }

            Collection<Player> nearbyPlayers = new ArrayList<>();

            // Adds nearby players to a list.
            for (Entity entity : player.getNearbyEntities(Settings.timelordKitRange, Settings.timelordKitRange, Settings.timelordKitRange)) {
                if (entity instanceof Player) {
                    Player nearbyPlayer = (Player) entity;
                    PlayerData nearbyPlayerData = PlayerDataManager.getPlayerData(nearbyPlayer);

                    if (nearbyPlayerData.getActiveKit() != null
                            && !Regions.isInSafezone(nearbyPlayer.getLocation())) {
                        nearbyPlayers.add(nearbyPlayer);
                    }
                }
            }

            // Ignores the event if no players are nearby.
            if (nearbyPlayers.isEmpty()) {
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                MessageUtil.messagePlayer(player, "&cAbility failed; no players found nearby.");
                playerData.setCooldown(playerData.getActiveKit(), 5, true);
                return;
            }

            // Plays a wither shoot sound effect.
            player.playSound(player.getLocation(), Sound.WITHER_SHOOT, 1, 1);

            // Freezes nearby players.
            for (Player nearbyPlayer : nearbyPlayers) {
                PlayerData nearbyPlayerData = PlayerDataManager.getPlayerData(nearbyPlayer);

                if (nearbyPlayerData.getActiveKit() != null
                        && !Regions.isInSafezone(nearbyPlayer.getLocation())) {
                    // Plays a sound effect and particle effects.
                    nearbyPlayer.playSound(nearbyPlayer.getLocation(), Sound.GHAST_FIREBALL, 1, 1);
                    nearbyPlayer.getWorld().playEffect(nearbyPlayer.getLocation(), Effect.STEP_SOUND, 152);
                    nearbyPlayer.getWorld().playEffect(nearbyPlayer.getLocation().add(0.0, 1.0, 0.0), Effect.STEP_SOUND, 152);

                    // Freezes the player using potion effects.
                    nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Settings.timelordKitDuration * 20, 128, false, false));
                    nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Settings.timelordKitDuration * 20, 10, false, false));
                    nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Settings.timelordKitDuration * 20, 254, false, false));
                }
            }

            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getActiveKit(), Settings.timelordKitCooldown, true);
        }
    }

    /**
     * Handles the Vampire ability, which drains the target's potion effects.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onVampireAbility(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        if (playerData.getActiveKit() instanceof Vampire
                && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.REDSTONE) {

            // Ignores the event if the player is in spawn.
            if (Regions.isInSafezone(player.getLocation())) {
                MessageUtil.messagePlayer(player, ConstantUtil.ABILITY_IN_SPAWN);
                return;
            }

            // Ignores the event if the player's ability is on cooldown.
            if (playerData.hasCooldown(true)) {
                return;
            }

            Collection<Player> nearbyPlayers = new ArrayList<>();
            Collection<PotionEffect> playerEffects = new ArrayList<>();

            // Adds nearby players to a list.
            for (Entity entity : player.getNearbyEntities(Settings.vampireKitRange, Settings.vampireKitRange, Settings.vampireKitRange)) {
                if (entity instanceof Player) {
                    Player nearbyPlayer = (Player) entity;
                    PlayerData nearbyPlayerData = PlayerDataManager.getPlayerData(nearbyPlayer);

                    if (nearbyPlayerData.getActiveKit() != null
                            && !Regions.isInSafezone(nearbyPlayer.getLocation())) {
                        nearbyPlayers.add(nearbyPlayer);
                    }
                }
            }

            // Ignores the event if no players are nearby.
            if (nearbyPlayers.isEmpty()) {
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                MessageUtil.messagePlayer(player, "&cAbility failed; no players found nearby.");
                playerData.setCooldown(playerData.getActiveKit(), 5, true);
                return;
            }

            // Adds all potion effects from nearby players to a list.
            for (Player nearbyPlayer : nearbyPlayers) {
                PlayerData nearbyPlayerData = PlayerDataManager.getPlayerData(nearbyPlayer);

                if (nearbyPlayerData.getActiveKit() != null
                        && !Regions.isInSafezone(nearbyPlayer.getLocation())) {
                    playerEffects.addAll(nearbyPlayer.getActivePotionEffects());
                }
            }

            // Ignores the event if no potion effects are found.
            if (playerEffects.isEmpty()) {
                MessageUtil.messagePlayer(player, "&cAbility failed; no effects found to drain.");
                playerData.setCooldown(playerData.getActiveKit(), 5, true);
                return;
            }

            // Plays a sound effect.
            player.playSound(player.getLocation(), Sound.CAT_HISS, 1, 1);

            // Drains the potion effects of nearby players.
            for (Player nearbyPlayer : nearbyPlayers) {
                UUID nearbyPlayerId = nearbyPlayer.getUniqueId();

                // Puts the target's potion effects into a map for rollback.
                drainedEffects.put(nearbyPlayerId, nearbyPlayer.getActivePotionEffects());

                MessageUtil.messagePlayer(nearbyPlayer, "&cYour effects have been drained by a Vampire!");

                for (PotionEffect potionEffect : drainedEffects.get(nearbyPlayerId)) {
                    if (potionEffect.getType() != PotionEffectType.INCREASE_DAMAGE) {
                        // Plays a sound effect and removes the target's potion effects.
                        nearbyPlayer.playSound(nearbyPlayer.getLocation(), Sound.CAT_HISS, 1, 1);
                        nearbyPlayer.removePotionEffect(potionEffect.getType());

                        // Adds the target's potion effects to the player.
                        player.addPotionEffect(potionEffect);
                        MessageUtil.messagePlayer(player, ("&aYou drained the " + potionEffect.getType().getName()
                                + " effect from " + nearbyPlayer.getName() + "."));
                    }
                }

                // Restores the target's potion effects after 10 seconds.
                TaskUtil.runTaskLater(() -> {
                    Collection<PotionEffect> effectsToRestore = drainedEffects.remove(nearbyPlayerId);

                    if (effectsToRestore != null && !effectsToRestore.isEmpty()) {
                        if (nearbyPlayer.isOnline()) {
                            // Clears current potion effects if necessary.
                            nearbyPlayer.getActivePotionEffects().forEach(effect -> nearbyPlayer.removePotionEffect(effect.getType()));

                            // Restores the original potion effects.
                            effectsToRestore.forEach(nearbyPlayer::addPotionEffect);
                            MessageUtil.messagePlayer(nearbyPlayer, "&cYour drained effects have been restored.");
                        }

                        if (player.isOnline()) {
                            effectsToRestore.forEach(effect -> {
                                // Check and apply only if the potion effect is relevant.
                                if (effect.getType() != PotionEffectType.INCREASE_DAMAGE) {
                                    player.addPotionEffect(effect);
                                }
                            });
                            MessageUtil.messagePlayer(player, "&aYour drained effects were restored.");
                        }
                    }
                }, Settings.vampireKitDuration * 20L);
            }

            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getActiveKit(), Settings.vampireKitCooldown, true);
        }
    }

    /**
     * Handles the Vampire ability, which gives the player regeneration when hitting another player.
     *
     * @param event The event.
     */
    @EventHandler(ignoreCancelled = true)
    public static void onVampireHit(@NotNull EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player receiver = (Player) event.getEntity();
            PlayerData damagerData = PlayerDataManager.getPlayerData(damager);
            PlayerData receiverData = PlayerDataManager.getPlayerData(receiver);

            // Gives the player regeneration when hitting another player.
            if (damagerData.getActiveKit() instanceof Vampire
                    && receiverData.getActiveKit() != null
                    && !Regions.isInSafezone(damager.getLocation())
                    && !Regions.isInSafezone(receiver.getLocation())) {

                damager.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,
                        Settings.vampireKitPassiveDuration, 0, false, false));
            }
        }
    }

    /**
     * Handles the Zen ability, which teleports the player to the nearest player.
     *
     * @param event The event.
     */
    @EventHandler
    public static void onZenAbility(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        if (playerData.getActiveKit() instanceof Zen
                && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.SLIME_BALL) {

            // Ignores the event if the player is in spawn.
            if (Regions.isInSafezone(player.getLocation())) {
                MessageUtil.messagePlayer(player, ConstantUtil.ABILITY_IN_SPAWN);
                return;
            }

            // Ignores the event if the player's ability is on cooldown.
            if (playerData.hasCooldown(true)) {
                return;
            }

            Player closest = null;
            double closestDistance = 0;
            Collection<Player> nearbyPlayers = new ArrayList<>();

            // Adds nearby players to a list.
            for (Entity entity : player.getNearbyEntities(Settings.zenKitRange, Settings.zenKitRange, Settings.zenKitRange)) {
                if (entity instanceof Player) {
                    Player nearbyPlayer = (Player) entity;
                    PlayerData nearbyPlayerData = PlayerDataManager.getPlayerData(nearbyPlayer);

                    if (nearbyPlayerData.getActiveKit() != null
                            && !Regions.isInSafezone(nearbyPlayer.getLocation())) {
                        nearbyPlayers.add(nearbyPlayer);
                    }
                }
            }

            // Ignores the event if no players are nearby.
            if (nearbyPlayers.isEmpty()) {
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                MessageUtil.messagePlayer(player, "&cAbility failed; no players found nearby.");
                playerData.setCooldown(playerData.getActiveKit(), 5, true);
                return;
            }

            // Finds the closest player to teleport to.
            for (Player nearbyPlayer : nearbyPlayers) {
                PlayerData nearbyPlayerData = PlayerDataManager.getPlayerData(nearbyPlayer);

                if (nearbyPlayerData.getActiveKit() != null
                        && !Regions.isInSafezone(nearbyPlayer.getLocation())) {
                    double distance = nearbyPlayer.getLocation().distanceSquared(player.getLocation());

                    if (closest == null || distance < closestDistance) {
                        closest = nearbyPlayer;
                        closestDistance = distance;
                    }
                }
            }

            if (closest != null) {
                // Removes the player from the list of imprisoned players.
                imprisonedPlayers.remove(player.getUniqueId());

                // Plays a sound effect before teleporting.
                player.getWorld().playEffect(player.getLocation(), Effect.ENDER_SIGNAL, 1);
                player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);

                // Teleports the player to the closest player.
                player.teleport(closest.getLocation());

                // Plays a sound effect after teleporting.
                player.getWorld().playEffect(player.getLocation(), Effect.ENDER_SIGNAL, 1);
                player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);

                // Applies blindness to the target.
                closest.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,
                        Settings.zenKitDuration * 20, 0, false, false));

                MessageUtil.messagePlayer(player, "&aYou teleported to " + closest.getDisplayName() + ".");
                playerData.setCooldown(playerData.getActiveKit(), Settings.zenKitCooldown, true);

            } else {
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                MessageUtil.messagePlayer(player, "&cAbility failed; no players found nearby.");
                playerData.setCooldown(playerData.getActiveKit(), 5, true);
            }
        }
    }

    /**
     * Gets the blocks that make up a cage.
     *
     * @param location The location of the cage.
     * @return The blocks that make up the cage.
     */
    private static @NotNull List<Block> getCageBlocks(@NotNull Location location) {
        List<Block> list = new ArrayList<>();
        list.add(location.clone().add(0.0, -1.0, 0.0).getBlock());
        list.add(location.clone().add(-1.0, 0.0, 0.0).getBlock());
        list.add(location.clone().add(0.0, 0.0, 1.0).getBlock());
        list.add(location.clone().add(0.0, 0.0, -1.0).getBlock());
        list.add(location.clone().add(1.0, 0.0, 0.0).getBlock());
        list.add(location.clone().add(-1.0, 0.0, -1.0).getBlock());
        list.add(location.clone().add(-1.0, 0.0, 1.0).getBlock());
        list.add(location.clone().add(1.0, 0.0, -1.0).getBlock());
        list.add(location.clone().add(1.0, 0.0, 1.0).getBlock());
        list.add(location.clone().add(0.0, 2.0, 0.0).getBlock());
        list.add(location.getBlock());
        list.add(location.add(0.0, 1.0, 0.0).getBlock());
        return list;
    }

    /**
     * Gets the locations of the blocks that make up a cage.
     *
     * @param location The location of the cage.
     * @return The locations of the blocks that make up the cage.
     */
    private static @NotNull List<Location> getPlatform(@NotNull Location location) {
        List<Location> list = new ArrayList<>();
        list.add(location.clone());
        list.add(location.clone().add(-1.0, 0.0, 0.0));
        list.add(location.clone().add(0.0, 0.0, -1.0));
        list.add(location.clone().add(1.0, 0.0, 0.0));
        list.add(location.clone().add(0.0, 0.0, 1.0));
        list.add(location.clone().add(-1.0, 0.0, -1.0));
        list.add(location.clone().add(1.0, 0.0, -1.0));
        list.add(location.clone().add(1.0, 0.0, 1.0));
        list.add(location.clone().add(-1.0, 0.0, 1.0));
        return list;
    }

    /**
     * Gets the locations of the blocks that make up a room.
     *
     * @param location The location of the room.
     * @return The locations of the blocks that make up the room.
     */
    private static @NotNull List<Location> getRoomLocations(@NotNull Location location) {
        location.add(0.0, 9.0, 0.0);

        List<Location> list = new ArrayList<>(getPlatform(location));

        for (int i = 0; i < 3; ++i) {
            location.add(0.0, 1.0, 0.0);
            list.add(location.clone().add(0.0, 0.0, -2.0));
            list.add(location.clone().add(0.0, 0.0, 2.0));
            list.add(location.clone().add(2.0, 0.0, 0.0));
            list.add(location.clone().add(-2.0, 0.0, 0.0));
            list.add(location.clone().add(-2.0, 0.0, 2.0));
            list.add(location.clone().add(-2.0, 0.0, -2.0));
            list.add(location.clone().add(2.0, 0.0, -2.0));
            list.add(location.clone().add(2.0, 0.0, 2.0));
            list.add(location.clone().add(1.0, 0.0, 2.0));
            list.add(location.clone().add(-1.0, 0.0, 2.0));
            list.add(location.clone().add(-2.0, 0.0, 1.0));
            list.add(location.clone().add(-2.0, 0.0, -1.0));
            list.add(location.clone().add(-1.0, 0.0, -2.0));
            list.add(location.clone().add(1.0, 0.0, -2.0));
            list.add(location.clone().add(2.0, 0.0, -1.0));
            list.add(location.clone().add(2.0, 0.0, 1.0));
        }

        list.addAll(getPlatform(location.add(0.0, 1.0, 0.0)));
        return list;
    }

    /**
     * Gets the locations of the blocks surrounding a location.
     *
     * @param location The location.
     * @return The locations of the blocks surrounding the location.
     */
    private static @NotNull List<Location> getSurroundingLocations(@NotNull Location location) {
        List<Location> list = new ArrayList<>();
        list.add(location.clone().add(-1.0, 0.0, 0.0));
        list.add(location.clone().add(0.0, 0.0, 1.0));
        list.add(location.clone().add(0.0, 0.0, -1.0));
        list.add(location.clone().add(1.0, 0.0, 0.0));
        return list;
    }

    /**
     * Rolls back a block state.
     *
     * @param blockState The block state.
     */
    private static void rollback(BlockState blockState) {
        if (blockState instanceof Sign) {
            Sign sign = (Sign) blockState;
            Location location = sign.getLocation();
            location.getWorld().getBlockAt(location).setType(blockState.getType());
            Sign sign2 = (Sign) location.getWorld().getBlockAt(location).getState();

            for (int i = 0; i < 4; ++i) {
                sign2.setLine(i, sign.getLines()[i]);
            }

            sign2.update(true);
        } else {
            blockState.update(true);
        }
    }
}
