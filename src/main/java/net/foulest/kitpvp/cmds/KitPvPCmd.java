package net.foulest.kitpvp.cmds;

import lombok.Getter;
import lombok.Setter;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.Settings;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Main command for KitPvP.
 *
 * @author Foulest
 * @project KitPvP
 */
@Getter
@Setter
public class KitPvPCmd {

    @Command(name = "kitpvp", description = "Main command for KitPvP.",
            permission = "kitpvp.main", usage = "/kitpvp")
    public void onCommand(@NotNull CommandArgs args) {
        CommandSender sender = args.getSender();

        // No additional arguments, display help menu.
        if (args.length() == 0) {
            handleHelp(sender, args);
            return;
        }

        // Handle sub-commands.
        String subCommand = args.getArgs(0);

        switch (subCommand.toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("kitpvp.reload")
                        && !(sender instanceof ConsoleCommandSender)) {
                    MessageUtil.messagePlayer(sender, "&cNo permission.");
                    return;
                }

                if (args.length() != 1) {
                    MessageUtil.messagePlayer(sender, "&cUsage: /kitpvp reload");
                    return;
                }

                Settings.loadSettings();
                MessageUtil.messagePlayer(sender, "&aReloaded the config files successfully.");
                break;

            default:
                handleHelp(sender, args);
                break;
        }
    }

    /**
     * Handles the help command.
     *
     * @param sender The command sender
     * @param args   The command arguments
     */
    private void handleHelp(@NotNull CommandSender sender, CommandArgs args) {
        if (!sender.hasPermission("kitpvp.main")
                && !(sender instanceof ConsoleCommandSender)) {
            MessageUtil.messagePlayer(sender, "&cNo permission.");
            return;
        }

        // A list of available commands with their usages.
        List<String> commands = Collections.singletonList(
                "&f/kitpvp reload &7- Reloads the config."
        );

        int itemsPerPage = 4;
        int maxPages = (int) Math.ceil((double) commands.size() / itemsPerPage);
        int page = 1;

        if (args.length() > 1) {
            try {
                page = Integer.parseInt(args.getArgs(1));
            } catch (NumberFormatException ex) {
                MessageUtil.messagePlayer(sender, "&cInvalid page number. Choose between 1 and " + maxPages + ".");
                return;
            }
        }

        if (page > maxPages || page < 1) {
            MessageUtil.messagePlayer(sender, "&cInvalid page number. Choose between 1 and " + maxPages + ".");
            return;
        }

        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(commands.size(), startIndex + itemsPerPage);

        MessageUtil.messagePlayer(sender, "");
        MessageUtil.messagePlayer(sender, "&eKitPvP Help &7(Page " + page + "/" + maxPages + ")");

        for (int i = startIndex; i < endIndex; i++) {
            MessageUtil.messagePlayer(sender, commands.get(i));
        }

        MessageUtil.messagePlayer(sender, "");
        MessageUtil.messagePlayer(sender, "&7Type &f/kitpvp help <page> &7for more commands.");
        MessageUtil.messagePlayer(sender, "");
    }
}
