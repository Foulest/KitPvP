package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.utils.kits.KitEnchanter;
import net.foulest.kitpvp.utils.MiscUtils;
import net.foulest.kitpvp.utils.command.Command;
import net.foulest.kitpvp.utils.command.CommandArgs;

public class KitEnchanterCmd {

    // TODO: Actually implement this into EventListener
    @Command(name = "enchanter", aliases = {"upgrade"}, description = "Opens the Kit Enchanter.",
            usage = "/enchanter", inGameOnly = true)
    public void onCommand(CommandArgs args) {
        if (args.length() != 0) {
            MiscUtils.messagePlayer(args.getSender(), "&cUsage: /enchanter");
            return;
        }

        new KitEnchanter(args.getPlayer());
    }
}
