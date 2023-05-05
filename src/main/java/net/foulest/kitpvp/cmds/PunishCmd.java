package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Foulest
 * @project KitPvP
 * <p>
 * Command for punishing players.
 */
public class PunishCmd {

    @Command(name = "punish", description = "Punishes a player.",
            permission = "kitpvp.punish", usage = "/punish <player> <type> <duration> [-s] <identifier> <reason>")
    public void onCommand(CommandArgs args) {
        CommandSender sender = args.getSender();

        if (args.length() < 4) {
            MessageUtil.messagePlayer(sender, "&cUsage: /punish <player> <type> <duration> [-s] <identifier> <reason>");
            MessageUtil.messagePlayer(sender, "&7Example: /punish Foulest ban 30d -s AimAssistA Cheating");
            MessageUtil.messagePlayer(sender, "&7Enter 'none' for the duration if you don't want to set one.");
            return;
        }

        Player target = Bukkit.getPlayer(args.getArgs(0));
        String type = args.getArgs(1).toLowerCase();
        String duration = args.getArgs(2).toLowerCase();
        boolean silent = args.getArgs(3).equalsIgnoreCase("-s");
        String identifier = (silent ? args.getArgs(4) : args.getArgs(3));

        int index = (silent ? 5 : 4);

        String[] reasonArgs = new String[args.length() - index];
        for (int i = index; i < args.length(); i++) {
            reasonArgs[i - index] = args.getArgs(i);
        }
        String reason = String.join(" ", reasonArgs);
        String neatType;

        switch (type) {
            case "ban":
                neatType = "banned";
                break;

            case "mute":
            case "ipmute":
                neatType = "muted";
                break;

            case "ipban":
            case "banip":
            case "ban-ip":
                neatType = "IP-banned";
                break;

            case "kick":
                neatType = "kicked";
                break;

            default:
                neatType = "punished";
                break;
        }

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), type + (silent ? " -s " : "") + target.getName() + (duration.equals("none") ? "" : duration) + " " + reason);
        MessageUtil.broadcastWithPerm("&7&o" + target.getName() + " has been " + (silent ? "silently " : "") + neatType + " for: " + identifier, "kitpvp.punish");
    }
}
