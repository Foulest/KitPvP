package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.menus.KitEnchanter;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;

/**
 * @author Foulest
 * @project KitPvP
 * <p>
 * Command for opening the Kit Enchanter menu.
 */
public class KitEnchanterCmd {

    @Command(name = "enchanter", aliases = {"kitenchanter"}, description = "Opens the Kit Enchanter.",
            usage = "/enchanter", inGameOnly = true, permission = "kitpvp.kitenchanter")
    public void onCommand(CommandArgs args) {
        if (args.length() != 0) {
            MessageUtil.messagePlayer(args.getSender(), "&cUsage: /enchanter");
            return;
        }

        new KitEnchanter(args.getPlayer());
    }
}
