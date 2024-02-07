package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.kits.Kit;
import net.foulest.kitpvp.kits.KitManager;
import net.foulest.kitpvp.menus.KitSelector;
import net.foulest.kitpvp.region.Regions;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Command for selecting a kit or opening the Kit Selector.
 *
 * @author Foulest
 * @project KitPvP
 */
public class KitsCmd {

    @Command(name = "kit", aliases = {"kits", "kitselector"}, usage = "/kit [name]",
            description = "Selects a kit or opens the Kit Selector.", inGameOnly = true,
            permission = "kitpvp.kitselector")
    public void onCommand(@NotNull CommandArgs args) {
        Player player = args.getPlayer();

        // Checks if the player is in spawn.
        if (!Regions.isInSafezone(player.getLocation())) {
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

        Kit kit = KitManager.getKit(args.getArgs(0));

        if (kit == null) {
            MessageUtil.messagePlayer(player, "&cCould not find the kit you wanted; opening the Kit Selector.");
            new KitSelector(player);
            return;
        }

        kit.apply(player);
    }
}
