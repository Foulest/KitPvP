package net.foulest.kitpvp.utils.command;

import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
public class BukkitCommand extends org.bukkit.command.Command {

    private final Plugin owningPlugin;
    private final CommandExecutor executor;
    protected BukkitCompleter completer;

    protected BukkitCommand(String label, CommandExecutor executor, Plugin owner) {
        super(label);
        this.executor = executor;
        owningPlugin = owner;
        usageMessage = "";
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        boolean success;
        String newLine = "\n";
        String commandPlaceholder = "<command>";

        if (!owningPlugin.isEnabled()) {
            return false;
        }

        if (!testPermission(sender)) {
            return true;
        }

        success = executor.onCommand(sender, this, commandLabel, args);

        if (!success && usageMessage.length() > 0) {
            for (String line : usageMessage.replace(commandPlaceholder, commandLabel).split(newLine)) {
                sender.sendMessage(line);
            }
        }

        return success;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        Validate.notNull(sender, "Sender cannot be null.");
        Validate.notNull(args, "Arguments cannot be null.");
        Validate.notNull(alias, "Alias cannot be null.");

        List<String> completions = null;

        if (completer != null) {
            completions = completer.onTabComplete(sender, this, alias, args);
        }

        if (completions == null && executor instanceof TabCompleter) {
            completions = ((TabCompleter) executor).onTabComplete(sender, this, alias, args);
        }

        if (completions == null) {
            return super.tabComplete(sender, alias, args);
        }

        return completions;
    }
}