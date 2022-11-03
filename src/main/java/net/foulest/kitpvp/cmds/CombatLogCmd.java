package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.listeners.CombatLog;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import org.bukkit.entity.Player;

/**
 * @author Foulest
 * @project KitPvP
 * <p>
 * Command for displaying your current combat tag timer.
 */
public class CombatLogCmd {

    @Command(name = "combatlog", aliases = {"combattag", "ct", "combat", "combattime"},
            description = "Displays your current combat tag timer.", usage = "/combatlog", inGameOnly = true)
    public void onCommand(CommandArgs args) {
        Player player = args.getPlayer();

        if (CombatLog.isInCombat(player)) {
            int timeLeft = CombatLog.getRemainingTime(player);

            MessageUtil.messagePlayer(player, "&cYou are in combat for " + timeLeft + " more "
                    + (timeLeft == 1 ? "second" : "seconds") + ".");

        } else {
            MessageUtil.messagePlayer(player, "&aYou are not in combat.");
        }
    }
}
