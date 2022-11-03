package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.region.Spawn;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.Settings;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import org.bukkit.command.CommandSender;

/**
 * @author Foulest
 * @project KitPvP
 * <p>
 * Command for reloading the plugin's config file.
 */
public class ReloadCmd {

    @Command(name = "reloadcfg", description = "Reloads the plugin's config file.",
            permission = "kitpvp.reloadcfg", usage = "/reloadcfg")
    public void onCommand(CommandArgs args) {
        CommandSender sender = args.getSender();

        if (args.length() != 0) {
            MessageUtil.messagePlayer(sender, "&cUsage: /reloadcfg");
            return;
        }

        Settings.loadSettings();
        Spawn.load();
        MessageUtil.messagePlayer(sender, "&aReloaded the config file successfully.");
    }
}
