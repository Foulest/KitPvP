package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.utils.ItemBuilder;
import net.foulest.kitpvp.utils.MiscUtils;
import net.foulest.kitpvp.utils.command.Command;
import net.foulest.kitpvp.utils.command.CommandArgs;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ArmorColorCmd {

    @Command(name = "armorcolor", description = "Colors your chestplate with an RGB hex.", permission = "kitpvp.armorcolor", usage = "/armorcolor [hex]", inGameOnly = true)
    public void onCommand(CommandArgs args) {
        Player player = args.getPlayer();
        CommandSender sender = args.getSender();

        if (args.length() != 1) {
            MiscUtils.messagePlayer(sender, "&cUsage: /armorcolor [hex]");
            return;
        }

        if (args.getArgs(0).length() != 6) {
            MiscUtils.messagePlayer(sender, "&cInvalid hex.");
            return;
        }

        if (player.getInventory().getChestplate().getType() != Material.LEATHER_CHESTPLATE) {
            MiscUtils.messagePlayer(sender, "&cYou can't color that chestplate.");
            return;
        }

        ItemStack chestplate = new ItemBuilder(player.getInventory().getChestplate()).color(Color.fromRGB(Integer.parseInt(args.getArgs(0), 16))).build();
        player.getInventory().setChestplate(chestplate);
        player.updateInventory();

        MiscUtils.messagePlayer(sender, "&aColor 0x" + args.getArgs(0) + " has been applied.");
    }
}
