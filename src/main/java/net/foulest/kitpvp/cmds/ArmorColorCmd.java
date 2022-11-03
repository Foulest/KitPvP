package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.util.ItemBuilder;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Foulest
 * @project KitPvP
 * <p>
 * Command for applying RGB hex colors to any dyable leather armor
 * you have equipped. For testing new colors for new kits.
 */
public class ArmorColorCmd {

    @Command(name = "armorcolor", description = "Colors your chestplate with an RGB hex.",
            permission = "kitpvp.armorcolor", usage = "/armorcolor [hex]", inGameOnly = true)
    public void onCommand(CommandArgs args) {
        Player player = args.getPlayer();
        CommandSender sender = args.getSender();

        if (args.length() != 1) {
            MessageUtil.messagePlayer(sender, "&cUsage: /armorcolor [hex]");
            return;
        }

        if (args.getArgs(0).length() != 6) {
            MessageUtil.messagePlayer(sender, "&cInvalid hex.");
            return;
        }

        if (player.getInventory().getChestplate().getType() != Material.LEATHER_CHESTPLATE) {
            MessageUtil.messagePlayer(sender, "&cYou can't color that chestplate.");
            return;
        }

        ItemStack chestplate = new ItemBuilder(player.getInventory().getChestplate()).color(Color.fromRGB(Integer.parseInt(args.getArgs(0), 16))).getItem();
        player.getInventory().setChestplate(chestplate);
        player.updateInventory();

        MessageUtil.messagePlayer(sender, "&aColor 0x" + args.getArgs(0) + " has been applied.");
    }
}
