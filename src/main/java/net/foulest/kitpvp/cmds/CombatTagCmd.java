package net.foulest.kitpvp.cmds;

import lombok.NonNull;
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
public class CombatTagCmd {

    @Command(name = "combattag", aliases = {"combatlog", "ct", "combat", "combattime", "combattimer"},
            description = "Displays your current combat tag timer.",
            permission = "kitpvp.combattag", usage = "/combattag", inGameOnly = true)
    public void onCommand(@NonNull CommandArgs args) {
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
