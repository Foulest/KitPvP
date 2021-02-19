package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.listeners.CombatLog;
import net.foulest.kitpvp.utils.MessageUtil;
import net.foulest.kitpvp.utils.command.Command;
import net.foulest.kitpvp.utils.command.CommandArgs;
import org.bukkit.entity.Player;

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
public class CombatLogCmd {

    private static final CombatLog COMBAT_LOG = CombatLog.getInstance();

    @Command(name = "combatlog", aliases = {"combattag", "ct", "combat", "combattime"},
            description = "Displays your current combat tag timer.", usage = "/combatlog", inGameOnly = true)
    public void onCommand(CommandArgs args) {
        Player player = args.getPlayer();

        if (COMBAT_LOG.isInCombat(player)) {
            int timeLeft = COMBAT_LOG.getRemainingTime(player);

            MessageUtil.messagePlayer(player, "&cYou are in combat for " + timeLeft + " more "
                    + (timeLeft == 1 ? "second" : "seconds") + ".");

        } else {
            MessageUtil.messagePlayer(player, "&aYou are not in combat.");
        }
    }
}
