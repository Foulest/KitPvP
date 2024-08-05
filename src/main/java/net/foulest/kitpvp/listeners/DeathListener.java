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

import lombok.NoArgsConstructor;
import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.combattag.CombatTag;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.data.PlayerDataManager;
import net.foulest.kitpvp.kits.Kit;
import net.foulest.kitpvp.kits.type.Summoner;
import net.foulest.kitpvp.kits.type.Tamer;
import net.foulest.kitpvp.region.Spawn;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.NMSUtil;
import net.foulest.kitpvp.util.Settings;
import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

@NoArgsConstructor
public class DeathListener implements Listener {

    /**
     * Handles a player's death.
     *
     * @param receiver     The player that died.
     * @param onPlayerQuit Whether the player died from quitting.
     */
    static void handleDeath(Player receiver, boolean onPlayerQuit) {
        PlayerData receiverData = PlayerDataManager.getPlayerData(receiver);
        Kit currentKit = receiverData.getActiveKit();
        Vector vec = new Vector();

        // Cancels deaths while in flying.
        if (receiver.getAllowFlight()) {
            return;
        }

        // Removes potential player created Wolves.
        if (currentKit != null) {
            if (currentKit instanceof Tamer) {
                Bukkit.getWorld(receiver.getWorld().getUID()).getEntities().stream()
                        .filter(entity -> entity.getType() == EntityType.WOLF)
                        .map(Wolf.class::cast)
                        .filter(wolf -> wolf.getOwner() == receiver)
                        .forEach(Wolf::remove);
            }

            // Removes potential player created Iron Golems.
            if (currentKit instanceof Summoner) {
                Bukkit.getWorld(receiver.getWorld().getUID()).getEntities().stream()
                        .filter(entity -> entity.getType() == EntityType.IRON_GOLEM)
                        .map(IronGolem.class::cast)
                        .filter(golem -> golem.hasMetadata(receiver.getName()))
                        .forEach(IronGolem::remove);
            }
        }

        // On-death blood splatter effect.
        receiver.getWorld().playEffect(receiver.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
        receiver.getWorld().playEffect(receiver.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
        receiver.getWorld().playEffect(receiver.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);

        // Sets the player's current kit and adds a death.
        if (currentKit != null) {
            receiverData.setPreviousKit(currentKit);
        }
        receiverData.setDeaths(receiverData.getDeaths() + 1);

        // Runs specific code if the player is killed by another player.
        if (CombatTag.getLastAttacker(receiver) != null && CombatTag.getLastAttacker(receiver) != receiver) {
            Player damager = CombatTag.getLastAttacker(receiver);
            PlayerData damagerData = PlayerDataManager.getPlayerData(damager);

            // Adds a kill to the damager.
            damagerData.setKills(damagerData.getKills() + 1);
            damagerData.addKillstreak();
            damager.playSound(damager.getLocation(), Sound.CHICKEN_EGG_POP, 0.5f, 0.0f);

            // Run specific code if the damager is on a multiple of 5 killstreak.
            if (damagerData.getKillstreak() >= 5 && damagerData.getKillstreak() % 5 == 0) {
                // Sends all online players a killstreak message in chat.
                MessageUtil.broadcast("&6" + damager.getName() + " &eis on a &6" + damagerData.getKillstreak() + " &ekillstreak!");

                // Re-adds the damager's kit items.
                damager.getInventory().clear();
                damagerData.getActiveKit().apply(damager);
                damager.updateInventory();
            }

            // Gives the damager coins and experience.
            int rewardAmount = 5 * (damagerData.getKillstreak() / 5);
            int coinsGiven = Settings.coinsOnKill + rewardAmount;
            int experienceGiven = Settings.expOnKill + rewardAmount;
            damagerData.addCoins(coinsGiven);
            damagerData.addExperience(experienceGiven);

            // Removes the player's potential bounty.
            if (receiverData.getBounty() > 0 && Bukkit.getPlayer(receiverData.getBenefactor()) != damager) {
                if (Bukkit.getPlayer(receiverData.getBenefactor()) != null
                        && Bukkit.getPlayer(receiverData.getBenefactor()).isOnline()) {
                    Player benefactor = Bukkit.getPlayer(receiverData.getBenefactor());

                    benefactor.playSound(benefactor.getLocation(), Sound.DONKEY_IDLE, 1.0f, 1.0f);
                    MessageUtil.messagePlayer(benefactor, "&aYour $" + receiverData.getBounty()
                            + " bounty on " + receiver.getName() + " was claimed by " + damager.getName() + ".");
                }

                damager.playSound(damager.getLocation(), Sound.BLAZE_DEATH, 1.0f, 1.0f);
                damager.playSound(damager.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
                MessageUtil.messagePlayer(damager, "&eYou claimed the &a$" + receiverData.getBounty()
                        + " &ebounty on &a" + receiver.getName() + "&e's head.");
                damagerData.setCoins(damagerData.getCoins() + receiverData.getBounty());

                receiverData.removeBounty();
            }

            // Prints kill messages to both the damager and receiver.
            MessageUtil.messagePlayer(receiver.getPlayer(), "&eYou were killed by &c" + damager.getName()
                    + " &eon &6" + String.format("%.01f", damager.getHealth()) + "❤&e.");
            MessageUtil.messagePlayer(damager, "&eYou killed &a" + receiver.getPlayer().getName()
                    + "&e for &a" + coinsGiven + " coins &eand &a" + experienceGiven + " exp&e.");
        } else {
            MessageUtil.messagePlayer(receiver, "&cYou killed yourself.");
        }

        // Clears cooldowns.
        receiverData.clearCooldowns();

        // Sends all online players a killstreak message in chat.
        if (receiverData.getKillstreak() >= 5) {
            MessageUtil.broadcast("&a" + receiver.getPlayer().getName() + " &edied and lost their &a"
                    + receiverData.getKillstreak() + " &ekillstreak.");
        }

        // Removes the player's combat tag.
        CombatTag.remove(receiver);

        // Removes the player from a Vampire's drained effects list.
        KitListener.drainedEffects.remove(receiver.getUniqueId());

        if (!onPlayerQuit) {
            // Sets the player's experience bar (fixes a rare bug).
            receiverData.calcLevel(false);

            // Plays a death sound at the player's death location.
            receiver.playSound(receiver.getLocation(), Sound.FALL_BIG, 0.5f, 0.0f);

            // Removes knockback before teleporting the player to spawn.
            receiver.setVelocity(vec);
            new BukkitRunnable() {
                @Override
                public void run() {
                    receiver.setVelocity(vec);
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
