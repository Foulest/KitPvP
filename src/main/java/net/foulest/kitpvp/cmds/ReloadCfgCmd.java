package net.foulest.kitpvp.cmds;

import lombok.NonNull;
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
public class ReloadCfgCmd {

    // TODO: Turn this into a /kitpvp command with a menu.
    //  Take a look at the /vulture command for reference.

    @Command(name = "reloadcfg", description = "Reloads the plugin's config file.",
            permission = "kitpvp.reloadcfg", usage = "/reloadcfg")
    public void onCommand(@NonNull CommandArgs args) {
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
