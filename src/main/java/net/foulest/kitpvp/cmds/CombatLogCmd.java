package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.listeners.CombatLog;
import net.foulest.kitpvp.utils.MiscUtils;
import net.foulest.kitpvp.utils.command.Command;
import net.foulest.kitpvp.utils.command.CommandArgs;
import org.bukkit.entity.Player;

public class CombatLogCmd {

    private final CombatLog combatLog = CombatLog.getInstance();

    @Command(name = "combatlog", aliases = {"combattag", "ct", "combat", "combattime"},
            description = "Displays your current combat tag timer.", usage = "/combatlog", inGameOnly = true)
    public void onCommand(CommandArgs args) {
        Player player = args.getPlayer();

        if (combatLog.isInCombat(player)) {
            int timeLeft = combatLog.getRemainingTime(player);

            MiscUtils.messagePlayer(player, "&cYou are in combat for " + timeLeft + " more " + (timeLeft == 1 ? "second" : "seconds") + ".");
        } else {
            MiscUtils.messagePlayer(player, "&aYou are not in combat.");
        }
    }
}
