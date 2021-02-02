package net.foulest.kitpvp.listeners;

import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.utils.*;
import net.foulest.kitpvp.utils.kits.Kit;
import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.List;

public class DeathListener implements Listener {

    private static final KitPvP kitPvP = KitPvP.getInstance();
    private static final KitListener kitListener = KitListener.getInstance();
    private static final Spawn spawn = Spawn.getInstance();
    private static final CombatLog combatLog = CombatLog.getInstance();

    public static void handleDeath(Player player) {
        KitUser receiver = KitUser.getInstance(player);
        Kit currentKit = receiver.getKit();
        Vector vec = new Vector();

        // Cancels deaths while in staff mode.
        if (receiver.isInStaffMode()) {
            return;
        }

        // Removes potential player created Wolves.
        if (receiver.hasKit()) {
            if (currentKit.getName().equals("Tamer")) {
                for (Entity entity : Bukkit.getWorld(player.getWorld().getUID()).getEntities()) {
                    if (entity.getType() == EntityType.WOLF) {
                        Wolf wolf = (Wolf) entity;

                        if (wolf.getOwner() == player) {
                            wolf.remove();
                        }
                    }
                }
            }

            // Removes potential player created Iron Golems.
            if (currentKit.getName().equals("Summoner")) {
                for (Entity entity : Bukkit.getWorld(player.getWorld().getUID()).getEntities()) {
                    if (entity.isValid() && entity.getType() == EntityType.IRON_GOLEM) {
                        IronGolem ironGolem = (IronGolem) entity;

                        if (ironGolem.hasMetadata(player.getName())) {
                            ironGolem.remove();
                        }
                    }
                }
            }
        }

        // On-death blood splatter effect.
        player.getWorld().playEffect(player.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
        player.getWorld().playEffect(player.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
        player.getWorld().playEffect(player.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);

        // Sets the player's current kit and adds a death.
        receiver.setPreviousKit(currentKit);
        receiver.addDeath();

        // Runs specific code if the player is killed by another player.
        if (combatLog.getLastAttacker(player) != null) {
            KitUser damager = KitUser.getInstance(combatLog.getLastAttacker(player));

            // Adds a kill to the damager.
            damager.addKill();
            damager.addKillstreak();
            damager.getPlayer().playSound(damager.getPlayer().getLocation(), Sound.CHICKEN_EGG_POP, 0.5f, 0.0f);

            // Run specific code if the damager is on a multiple of 5 killstreak.
            if (damager.getKillstreak() >= 5 && damager.getKillstreak() % 5 == 0) {
                // Sends all online players a killstreak message in chat.
                MiscUtils.broadcastMessage("&a" + damager.getPlayer().getName() + " &eis on a &a" + damager.getKillstreak() + " &ekillstreak!");

                // Refills the damager's inventory with soup.
                for (int i = 0; i < 36; ++i) {
                    damager.getPlayer().getInventory().addItem(new ItemBuilder(Material.MUSHROOM_SOUP).name("&fMushroom Soup").build());
                }

                // Re-adds the damager's kit items.
                List<ItemStack> kitItems = damager.getKit().getItems();
                for (int i = 0; i < kitItems.size(); ++i) {
                    damager.getPlayer().getInventory().setItem(i, kitItems.get(i));
                }
            }

            // Gives the damager coins and experience.
            int rewardAmount = 5 * (damager.getKillstreak() / 5);
            int coinsGiven = ConfigManager.get().getInt("kill.coins-bonus") + rewardAmount;
            int experienceGiven = ConfigManager.get().getInt("kill.experience-bonus") + rewardAmount;
            damager.addCoins(coinsGiven);
            damager.addExperience(experienceGiven);

            // Saves the damager's stats.
            damager.saveStats();

            // Prints kill messages to both the damager and receiver.
            MiscUtils.messagePlayer(receiver.getPlayer(), "&cYou were killed by &e" + damager.getPlayer().getName()
                    + " &con &e" + damager.getPlayer().getHealth() + " health.");
            MiscUtils.messagePlayer(damager.getPlayer(), "&eYou killed &a" + receiver.getPlayer().getName()
                    + "&e for &a" + coinsGiven + " coins &eand &a" + experienceGiven + " exp&e.");
        } else {
            MiscUtils.messagePlayer(player, "&cYou killed yourself.");
        }

        // Clears cooldowns.
        receiver.clearCooldowns();

        // Sends all online players a killstreak message in chat.
        if (receiver.getKillstreak() >= 5) {
            MiscUtils.broadcastMessage("&a" + receiver.getPlayer().getName() + " &edied and lost their &a" + receiver.getKillstreak() + " &ekillstreak.");
        }

        // Removes the player's combat tag.
        combatLog.remove(player);

        // Removes the player from a Vampire's drained effects list.
        kitListener.drainedEffects.remove(player.getUniqueId());

        // Removes knockback before teleporting the player to spawn.
        Bukkit.getScheduler().runTaskLater(kitPvP, () -> player.setVelocity(vec), 1L);

        // Prevents the player from reaching the respawn screen using NMS.
        Bukkit.getScheduler().runTask(kitPvP, () -> {
            CraftPlayer craftPlayer = (CraftPlayer) player;
            PacketPlayInClientCommand packet = new PacketPlayInClientCommand();

            try {
                Field a = PacketPlayInClientCommand.class.getDeclaredField("a");
                a.setAccessible(true);
                a.set(packet, PacketPlayInClientCommand.EnumClientCommand.PERFORM_RESPAWN);
            } catch (Exception e) {
                e.printStackTrace();
            }

            (craftPlayer.getHandle()).playerConnection.a(packet);
        });

        // Teleports the player to spawn.
        spawn.teleport(player);
        player.getInventory().setHeldItemSlot(0);
        player.playSound(player.getLocation(), Sound.FALL_BIG, 0.5f, 0.0f);

        // Resets the player's killstreak.
        receiver.resetKillStreak();

        // Saves the receiver's stats with the database.
        receiver.saveStats();
    }
}
