package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.utils.ItemBuilder;
import net.foulest.kitpvp.utils.MessageUtil;
import net.foulest.kitpvp.utils.PlayerData;
import net.foulest.kitpvp.utils.Regions;
import net.foulest.kitpvp.utils.command.Command;
import net.foulest.kitpvp.utils.command.CommandArgs;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
public class PotionsCmd {

    private static final Regions REGIONS = Regions.getInstance();

    @Command(name = "potions", aliases = {"pots"}, description = "Sets your healing item to Potions.",
            usage = "/potions", inGameOnly = true)
    public void onCommand(CommandArgs args) {
        Player player = args.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        if (!REGIONS.isInSafezone(player)) {
            MessageUtil.messagePlayer(player, "&cYou must be in spawn to use this command.");
            return;
        }

        if (args.length() != 0) {
            MessageUtil.messagePlayer(args.getSender(), "&cUsage: /pots");
            return;
        }

        if (!playerData.isUsingSoup()) {
            MessageUtil.messagePlayer(args.getSender(), "&cYou are already using Potions.");
            return;
        }

        playerData.setUsingSoup(false);
        playerData.saveStats();
        MessageUtil.messagePlayer(player, "&aYou are now using Potions.");

        if (playerData.getKit() == null) {
            ItemStack healingItem = new ItemBuilder(Material.MUSHROOM_SOUP).name("&aUsing Soup &7(Right Click)").getItem();
            player.getInventory().setItem(6, healingItem);
        } else {
            playerData.getKit().apply(player);
        }
    }
}
