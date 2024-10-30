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
import net.foulest.kitpvp.combattag.CombatTag;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
import net.foulest.kitpvp.kits.Kit;
import net.foulest.kitpvp.region.Spawn;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.NMSUtil;
import net.foulest.kitpvp.util.Settings;
import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Handles all player deaths.
 *
 * @author Foulest
 */
@Data
public class DeathListener implements Listener {

    /**
     * Handles a player's death.
     *
     * @param receiver     The player that died.
     * @param onPlayerQuit Whether the player died from quitting.
     */
    static void handleDeath(@NotNull Player receiver, boolean onPlayerQuit) {
        // Cancels deaths while in flying.
        if (receiver.getAllowFlight()) {
            return;
        }

        // Receiver data
        World world = receiver.getWorld();
        Location receiverLoc = receiver.getLocation();
        String receiverName = receiver.getName();
        PlayerData receiverData = PlayerDataManager.getPlayerData(receiver);
        int receiverKillstreak = receiverData.getKillstreak();
        int receiverDeaths = receiverData.getDeaths();
        int receiverBounty = receiverData.getBounty();
        UUID receiverBenefactor = receiverData.getBenefactor();
        Kit currentKit = receiverData.getActiveKit();

        // On-death blood splatter effect.
        world.playEffect(receiverLoc, Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
        world.playEffect(receiverLoc, Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
        world.playEffect(receiverLoc, Effect.STEP_SOUND, Material.REDSTONE_BLOCK);

        // Sets the player's current kit and adds a death.
        if (currentKit != null) {
            receiverData.setPreviousKit(currentKit);
        }
        receiverData.setDeaths(receiverDeaths + 1);

        // Runs specific code if the player is killed by another player.
        if (CombatTag.getLastAttacker(receiver) != null && CombatTag.getLastAttacker(receiver) != receiver) {

            // Damager data
            Player damager = CombatTag.getLastAttacker(receiver);
            String damagerName = damager.getName();
            PlayerData damagerData = PlayerDataManager.getPlayerData(damager);
            Location damagerLoc = damager.getLocation();
            double damagerHealth = damager.getHealth();
            int damagerKills = damagerData.getKills();
            int damagerKillstreak = damagerData.getKillstreak();
            int damagerCoins = damagerData.getCoins();

            // Adds a Flask to the damager's inventory.
            FlaskListener.addFlaskToInventory(damager, FlaskListener.MAX_FLASKS);

            // Adds a kill to the damager.
            damagerData.setKills(damagerKills + 1);
            damagerData.addKillstreak();
            damager.playSound(damagerLoc, Sound.CHICKEN_EGG_POP, 0.5f, 0.0f);

            // Run specific code if the damager is on a multiple of 5 killstreak.
            if (damagerKillstreak >= 5 && damagerKillstreak % 5 == 0) {
                // Sends all online players a killstreak message in chat.
                MessageUtil.broadcast("&6" + damagerName + " &eis on a &6" + damagerKillstreak + " &ekillstreak!");

                // Re-adds the damager's kit items.
                damager.getInventory().clear();
                damagerData.getActiveKit().apply(damager);
                damager.updateInventory();
            }

            // Gives the damager coins and experience.
            int rewardAmount = 5 * (damagerKillstreak / 5);
            int coinsGiven = Settings.coinsOnKill + rewardAmount;
            int experienceGiven = Settings.expOnKill + rewardAmount;
            damagerData.addCoins(coinsGiven);
            damagerData.addExperience(experienceGiven);

            // Removes the player's potential bounty.
            if (receiverBounty > 0 && Bukkit.getPlayer(receiverBenefactor) != damager) {
                if (Bukkit.getPlayer(receiverBenefactor) != null
                        && Bukkit.getPlayer(receiverBenefactor).isOnline()) {
                    Player benefactor = Bukkit.getPlayer(receiverBenefactor);
                    Location benefactorLoc = benefactor.getLocation();

                    benefactor.playSound(benefactorLoc, Sound.DONKEY_IDLE, 1.0f, 1.0f);
                    MessageUtil.messagePlayer(benefactor, "&aYour $" + receiverBounty
                            + " bounty on " + receiverName + " was claimed by " + damagerName + ".");
                }

                damager.playSound(damagerLoc, Sound.BLAZE_DEATH, 1.0f, 1.0f);
                damager.playSound(damagerLoc, Sound.LEVEL_UP, 1.0f, 1.0f);
                MessageUtil.messagePlayer(damager, "&eYou claimed the &a$" + receiverBounty
                        + " &ebounty on &a" + receiverName + "&e's head.");
                damagerData.setCoins(damagerCoins + receiverBounty);

                receiverData.removeBounty();
            }

            // Prints kill messages to both the damager and receiver.
            MessageUtil.messagePlayer(receiver, "&eYou were killed by &c" + damagerName
                    + " &eon &6" + String.format("%.01f", damagerHealth) + "\u2764&e.");
            MessageUtil.messagePlayer(damager, "&eYou killed &a" + receiverName
                    + "&e for &a" + coinsGiven + " coins &eand &a" + experienceGiven + " exp&e.");
        } else {
            MessageUtil.messagePlayer(receiver, "&cYou killed yourself.");
        }

        // Clears cooldowns.
        receiverData.clearCooldowns();

        // Sends all online players a killstreak message in chat.
        if (receiverKillstreak >= 5) {
            MessageUtil.broadcast("&a" + receiverName + " &edied and lost their &a"
                    + receiverKillstreak + " &ekillstreak.");
        }

        // Removes the player's combat tag.
        CombatTag.remove(receiver);

        if (!onPlayerQuit) {
            // Sets the player's experience bar (fixes a rare bug).
            receiverData.calcLevel(false);

            // Plays a death sound at the player's death location.
            receiver.playSound(receiverLoc, Sound.FALL_BIG, 0.5f, 0.0f);

            // Removes knockback before teleporting the player to spawn.
            receiver.setVelocity(new Vector());
            new BukkitRunnable() {
                @Override
                public void run() {
                    receiver.setVelocity(new Vector());
                }
            }.runTaskLater(KitPvP.instance, 1L);

            // Sends a respawn packet to the player.
            NMSUtil.getConnection(receiver).a(new PacketPlayInClientCommand(PacketPlayInClientCommand.EnumClientCommand.PERFORM_RESPAWN));

            // Teleports the player to spawn.
            receiver.getInventory().setHeldItemSlot(0);
            Spawn.teleport(receiver);
            receiver.getInventory().setHeldItemSlot(0);
        }

        // Removes enchantments from the player.
        if (!receiverData.getEnchants().isEmpty()) {
            receiverData.getEnchants().clear();
            MessageUtil.messagePlayer(receiver, "&cYour enchantments were removed on death.");
        }

        // Resets the player's killstreak.
        receiverData.setKillstreak(0);
    }
}
