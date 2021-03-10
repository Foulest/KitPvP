package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.utils.MessageUtil;
import net.foulest.kitpvp.utils.Regions;
import net.foulest.kitpvp.utils.command.Command;
import net.foulest.kitpvp.utils.command.CommandArgs;
import net.foulest.kitpvp.utils.kits.Kit;
import net.foulest.kitpvp.utils.kits.KitManager;
import net.foulest.kitpvp.utils.menus.KitSelector;
import org.bukkit.entity.Player;

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
public class KitsCmd {

    private static final KitManager KIT_MANAGER = KitManager.getInstance();
    private static final Regions REGIONS = Regions.getInstance();

    @Command(name = "kit", aliases = {"kits", "kitselector"}, usage = "/kit [name]",
            description = "Selects a kit or opens the Kit Selector.", inGameOnly = true)
    public void onCommand(CommandArgs args) {
        Player player = args.getPlayer();

        if (!REGIONS.isInSafezone(player)) {
            MessageUtil.messagePlayer(player, "&cYou must be in spawn to use this command.");
            return;
        }

        if (args.length() > 1) {
            MessageUtil.messagePlayer(player, "&cUsage: /kit [name]");
            return;
        }

        if (args.length() == 0) {
            new KitSelector(player);
            return;
        }

        Kit kit = KIT_MANAGER.valueOf(args.getArgs(0));

        if (kit == null) {
            MessageUtil.messagePlayer(player, "&cCould not find the kit you wanted; opening the Kit Selector.");
            new KitSelector(player);
            return;
        }

        kit.apply(player);
    }
}
