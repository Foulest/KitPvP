package net.foulest.kitpvp.listeners;

import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.utils.*;
import net.foulest.kitpvp.utils.kits.Kit;
import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
public class DeathListener implements Listener {

    private static final DeathListener INSTANCE = new DeathListener();
    private static final KitPvP KITPVP = KitPvP.getInstance();
    private static final Spawn SPAWN = Spawn.getInstance();
    private static final CombatLog COMBAT_LOG = CombatLog.getInstance();

    public static DeathListener getInstance() {
        return INSTANCE;
    }

    public void handleDeath(Player receiver, boolean onPlayerQuit) {
        PlayerData receiverData = PlayerData.getInstance(receiver);
        Kit currentKit = receiverData.getKit();
        Vector vec = new Vector();

        // Cancels deaths while in flying.
        if (receiver.getAllowFlight()) {
            return;
        }

        // Removes potential player created Wolves.
        if (receiverData.getKit() != null) {
            String tamerKitName = "Tamer";
            String summonerKitName = "Summoner";

            if (currentKit.getName().equals(tamerKitName)) {
                for (Entity entity : Bukkit.getWorld(receiver.getWorld().getUID()).getEntities()) {
                    if (entity.getType() == EntityType.WOLF) {
                        Wolf wolf = (Wolf) entity;

                        if (wolf.getOwner() == receiver) {
                            wolf.remove();
                        }
                    }
                }
            }

            // Removes potential player created Iron Golems.
            if (currentKit.getName().equals(summonerKitName)) {
                for (Entity entity : Bukkit.getWorld(receiver.getWorld().getUID()).getEntities()) {
                    if (entity.isValid() && entity.getType() == EntityType.IRON_GOLEM) {
                        IronGolem ironGolem = (IronGolem) entity;

                        if (ironGolem.hasMetadata(receiver.getName())) {
                            ironGolem.remove();
                        }
                    }
                }
            }
        }

        // On-death blood splatter effect.
        receiver.getWorld().playEffect(receiver.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
        receiver.getWorld().playEffect(receiver.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
        receiver.getWorld().playEffect(receiver.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);

        // Sets the player's current kit and adds a death.
        receiverData.setPreviousKit(currentKit);
        receiverData.setDeaths(receiverData.getDeaths() + 1);

        // Runs specific code if the player is killed by another player.
        if (COMBAT_LOG.getLastAttacker(receiver) != null) {
            Player damager = COMBAT_LOG.getLastAttacker(receiver);
            PlayerData damagerData = PlayerData.getInstance(damager);

            // Adds a kill to the damager.
            damagerData.setKills(damagerData.getKills() + 1);
            damagerData.addKillstreak();
            damager.playSound(damager.getLocation(), Sound.CHICKEN_EGG_POP, 0.5f, 0.0f);

            // Run specific code if the damager is on a multiple of 5 killstreak.
            if (damagerData.getKillstreak() >= 5 && damagerData.getKillstreak() % 5 == 0) {
                // Sends all online players a killstreak message in chat.
                MessageUtil.broadcastMessage("&6" + damager.getName() + " &eis on a &6" + damagerData.getKillstreak() + " &ekillstreak!");

                // Re-adds the damager's kit items.
                damager.getInventory().clear();
                damagerData.getKit().apply(damager);
                damager.updateInventory();
            }

            // Gives the damager coins and experience.
            int rewardAmount = 5 * (damagerData.getKillstreak() / 5);
            int coinsGiven = ConfigManager.get().getInt("kill.coins-bonus") + rewardAmount;
            int experienceGiven = ConfigManager.get().getInt("kill.experience-bonus") + rewardAmount;
            damagerData.setCoins(damagerData.getCoins() + coinsGiven);
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

            // Saves the damager's stats.
            damagerData.saveStats();

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
            MessageUtil.broadcastMessage("&a" + receiver.getPlayer().getName() + " &edied and lost their &a"
                    + receiverData.getKillstreak() + " &ekillstreak.");
        }

        // Removes the player's combat tag.
        COMBAT_LOG.remove(receiver);

        // Removes the player from a Vampire's drained effects list.
        KitListener.DRAINED_EFFECTS.remove(receiver.getUniqueId());

        // Prevents the player from reaching the respawn screen using NMS.
        if (!onPlayerQuit) {
            // Teleports the player to spawn.
            SPAWN.teleport(receiver);
            receiver.getInventory().setHeldItemSlot(0);
            receiver.playSound(receiver.getLocation(), Sound.FALL_BIG, 0.5f, 0.0f);

            NMSUtil.getConnection(receiver).a(new PacketPlayInClientCommand(PacketPlayInClientCommand.EnumClientCommand.PERFORM_RESPAWN));

            // Removes knockback before teleporting the player to spawn.
            new BukkitRunnable() {
                @Override
                public void run() {
                    receiver.setVelocity(vec);
                }
            }.runTaskLater(KITPVP, 1L);
        }

        // Removes Feather Falling metadata from the player.
        if (receiver.hasMetadata("featherFalling")) {
            MessageUtil.messagePlayer(receiver, "");
            MessageUtil.messagePlayer(receiver, "&eYour &cFeather Falling &eenchantment was removed on death.");
            MessageUtil.messagePlayer(receiver, "");
            receiver.removeMetadata("featherFalling", KITPVP);
        }

        // Removes Protection metadata from the player.
        if (receiver.hasMetadata("protection")) {
            MessageUtil.messagePlayer(receiver, "");
            MessageUtil.messagePlayer(receiver, "&eYour &cProtection &eenchantment was removed on death.");
            MessageUtil.messagePlayer(receiver, "");
            receiver.removeMetadata("protection", KITPVP);
        }

        // Removes Power metadata from the player.
        if (receiver.hasMetadata("power")) {
            MessageUtil.messagePlayer(receiver, "");
            MessageUtil.messagePlayer(receiver, "&eYour &cPower &eenchantment was removed on death.");
            MessageUtil.messagePlayer(receiver, "");
            receiver.removeMetadata("power", KITPVP);
        }

        // Removes Sharpness metadata from the player.
        if (receiver.hasMetadata("sharpness")) {
            MessageUtil.messagePlayer(receiver, "");
            MessageUtil.messagePlayer(receiver, "&eYour &cSharpness &eenchantment was removed on death.");
            MessageUtil.messagePlayer(receiver, "");
            receiver.removeMetadata("sharpness", KITPVP);
        }

        // Resets the player's killstreak.
        receiverData.setKillstreak(0);

        // Saves the receiver's stats with the database.
        receiverData.saveStats();
    }
}
