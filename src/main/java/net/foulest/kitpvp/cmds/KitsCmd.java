package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.utils.MiscUtils;
import net.foulest.kitpvp.utils.PlayerData;
import net.foulest.kitpvp.utils.Regions;
import net.foulest.kitpvp.utils.command.Command;
import net.foulest.kitpvp.utils.command.CommandArgs;
import net.foulest.kitpvp.utils.kits.Kit;
import net.foulest.kitpvp.utils.kits.KitManager;
import net.foulest.kitpvp.utils.kits.KitSelector;
import org.bukkit.entity.Player;

public class KitsCmd {

    private final KitManager kitManager = KitManager.getInstance();

    @Command(name = "kit", aliases = {"kits", "kitselector"}, usage = "/kit [name]",
            description = "Selects a kit or opens the Kit Selector.", inGameOnly = true)
    public void onCommand(CommandArgs args) {
        Player player = args.getPlayer();

        if (!Regions.getInstance().isInSafezone(player)) {
            MiscUtils.messagePlayer(player, "&cYou must be in spawn to use this command.");
            return;
        }

        if (args.length() > 1) {
            MiscUtils.messagePlayer(player, "&cUsage: /kit [name]");
            return;
        }

        if (args.length() == 0) {
            new KitSelector(player);
            return;
        }

        Kit kit = kitManager.valueOf(args.getArgs(0));

        if (kit == null) {
            MiscUtils.messagePlayer(player, "&cCould not find the kit you wanted; opening the Kit Selector.");
            new KitSelector(player);
            return;
        }

        kit.apply(player);
    }
}
