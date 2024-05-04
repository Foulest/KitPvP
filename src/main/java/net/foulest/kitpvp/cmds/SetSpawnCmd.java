package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.region.Regions;
import net.foulest.kitpvp.region.Spawn;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Command for setting the spawn point.
 *
 * @author Foulest
 * @project KitPvP
 */
public class SetSpawnCmd {

    @Command(name = "setspawn", usage = "/setspawn", description = "Sets the spawn point.",
            permission = "kitpvp.setspawn", inGameOnly = true)
    public void onCommand(@NotNull CommandArgs args) {
        Player player = args.getPlayer();

        // Checks if the player is in a safezone.
        if (!Regions.isInSafezone(player.getLocation())) {
            MessageUtil.messagePlayer(player, "&cYou cannot set spawn outside of a safezone.");
            return;
        }

        // Sets the spawn point to the player's location.
        Spawn.setLocation(player.getLocation());
        MessageUtil.messagePlayer(player, "&aSpawn has been set.");
    }
}
