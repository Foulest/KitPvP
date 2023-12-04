package net.foulest.kitpvp.cmds;

import lombok.NonNull;
import net.foulest.kitpvp.region.Spawn;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import org.bukkit.entity.Player;

/**
 * @author Foulest
 * @project KitPvP
 * <p>
 * Command for setting the server's spawn point.
 */
public class SetSpawnCmd {

    @Command(name = "setspawn", usage = "/setspawn", description = "Sets the spawn point.",
            permission = "kitpvp.setspawn", inGameOnly = true)
    public void onCommand(@NonNull CommandArgs args) {
        Player player = args.getPlayer();

        Spawn.setLocation(player.getLocation());
        MessageUtil.messagePlayer(player, "&aSpawn has been set.");
    }
}
