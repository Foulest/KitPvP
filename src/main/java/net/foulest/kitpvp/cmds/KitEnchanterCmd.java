package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.utils.menus.KitEnchanter;
import net.foulest.kitpvp.utils.MessageUtil;
import net.foulest.kitpvp.utils.command.Command;
import net.foulest.kitpvp.utils.command.CommandArgs;

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
public class KitEnchanterCmd {

    // TODO: Implement this into EventListener
    @Command(name = "enchanter", aliases = {"upgrade"}, description = "Opens the Kit Enchanter.",
            usage = "/enchanter", inGameOnly = true)
    public void onCommand(CommandArgs args) {
        if (args.length() != 0) {
            MessageUtil.messagePlayer(args.getSender(), "&cUsage: /enchanter");
            return;
        }

        new KitEnchanter(args.getPlayer());
    }
}
