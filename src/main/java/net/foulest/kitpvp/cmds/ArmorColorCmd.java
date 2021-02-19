package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.utils.ItemBuilder;
import net.foulest.kitpvp.utils.MessageUtil;
import net.foulest.kitpvp.utils.command.Command;
import net.foulest.kitpvp.utils.command.CommandArgs;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
public class ArmorColorCmd {

    private static final int HEX_LENGTH = 6;

    @Command(name = "armorcolor", description = "Colors your chestplate with an RGB hex.",
            permission = "kitpvp.armorcolor", usage = "/armorcolor [hex]", inGameOnly = true)
    public void onCommand(CommandArgs args) {
        Player player = args.getPlayer();
        CommandSender sender = args.getSender();

        if (args.length() != 1) {
            MessageUtil.messagePlayer(sender, "&cUsage: /armorcolor [hex]");
            return;
        }

        if (args.getArgs(0).length() != HEX_LENGTH) {
            MessageUtil.messagePlayer(sender, "&cInvalid hex.");
            return;
        }

        if (player.getInventory().getChestplate().getType() != Material.LEATHER_CHESTPLATE) {
            MessageUtil.messagePlayer(sender, "&cYou can't color that chestplate.");
            return;
        }

        ItemStack chestplate = new ItemBuilder(player.getInventory().getChestplate()).color(Color.fromRGB(Integer.parseInt(args.getArgs(0), 16))).build();
        player.getInventory().setChestplate(chestplate);
        player.updateInventory();

        MessageUtil.messagePlayer(sender, "&aColor 0x" + args.getArgs(0) + " has been applied.");
    }
}
