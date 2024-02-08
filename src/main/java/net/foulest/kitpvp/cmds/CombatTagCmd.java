package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.combattag.CombatTag;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.Settings;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Command for checking your combat tag timer.
 *
 * @author Foulest
 * @project KitPvP
 */
public class CombatTagCmd {

    @Command(name = "combattag", aliases = {"combatlog", "ct", "combat", "combattime", "combattimer"},
            description = "Displays your current combat tag timer.",
            permission = "kitpvp.combattag", usage = "/combattag", inGameOnly = true)
    public void onCommand(@NotNull CommandArgs args) {
        Player player = args.getPlayer();

        // Prints the usage message.
        if (args.length() != 0) {
            MessageUtil.messagePlayer(player, "&cUsage: /combattag");
            return;
        }

        // Checks if the combat tag feature is enabled.
        if (!Settings.combatTagEnabled) {
            MessageUtil.messagePlayer(player, "&cThat command is disabled.");
            return;
        }

        if (CombatTag.isInCombat(player)) {
            int timeLeft = CombatTag.getRemainingTime(player);

            MessageUtil.messagePlayer(player, "&cYou are in combat for " + timeLeft
                    + " more " + (timeLeft == 1 ? "second" : "seconds") + ".");
        } else {
            MessageUtil.messagePlayer(player, "&aYou are not in combat.");
        }
    }
}
