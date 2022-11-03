package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.koth.KOTH;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Foulest
 * @project KitPvP
 * <p>
 * Command for managing all KOTH related commands.
 */
public class KOTHCmd {

    @Command(name = "koth", description = "Command for managing all KOTH related commands.", usage = "/koth")
    public void onCommand(CommandArgs args) {
        Player player = args.getPlayer();
        CommandSender sender = args.getSender();

        if (args.length() == 0) {
            KOTH.sendKOTHStatus(sender);
            return;
        }

        switch (args.getArgs(0)) {
            case "help":
                if (!player.hasPermission("kitpvp.koth")) {
                    MessageUtil.messagePlayer(sender, "&cUsage: /koth");
                    return;
                }

                MessageUtil.messagePlayer(sender, "&cKOTH Commands:");
                MessageUtil.messagePlayer(sender, "&c/koth create <name> <regionName> <capTime> <coinReward> <expReward>");
                MessageUtil.messagePlayer(sender, "&c/koth delete <name>");
                MessageUtil.messagePlayer(sender, "&c/koth start <name>");
                MessageUtil.messagePlayer(sender, "&c/koth stop/end");
                MessageUtil.messagePlayer(sender, "&c/koth list");
                return;

            case "create":
                if (!player.hasPermission("kitpvp.koth")) {
                    MessageUtil.messagePlayer(sender, "&cUsage: /koth");
                    return;
                }

                if (args.length() != 6) {
                    MessageUtil.messagePlayer(sender, "&cUsage: /koth create <name> <regionName> <capTime> <coins> <exp>");
                    return;
                }

                String name = args.getArgs(1);
                String regionName = args.getArgs(2);
                String internalName = StringUtils.remove(WordUtils.capitalizeFully(name.replaceAll("\\s", ""), new char[]{'_'}), "_").toLowerCase();
                int capTime;
                int coinReward;
                int expReward;

                try {
                    capTime = Integer.parseInt(args.getArgs(3));
                    coinReward = Integer.parseInt(args.getArgs(4));
                    expReward = Integer.parseInt(args.getArgs(5));
                } catch (NumberFormatException ex) {
                    MessageUtil.messagePlayer(sender, "&cUsage: /koth create <name> <regionName> <capTime> <coinReward> <expReward>");
                    return;
                }

                if (name.length() < 3 || name.length() > 16 || !name.matches("[a-zA-Z]+")) {
                    MessageUtil.messagePlayer(sender, "&cThe KOTH name is invalid.");
                    return;
                }

                if (regionName.length() < 3 || regionName.length() > 16 || !regionName.matches("[a-zA-Z]+")) {
                    MessageUtil.messagePlayer(sender, "&cThe region name is invalid.");
                    return;
                }

                if (capTime <= 0 || capTime > 3600) {
                    MessageUtil.messagePlayer(sender, "&cThe KOTH cap time is invalid.");
                    return;
                }

                if (coinReward < 0 || coinReward > 999999) {
                    MessageUtil.messagePlayer(sender, "&cThe coin reward is invalid.");
                    return;
                }

                if (expReward < 0 || expReward > 999999) {
                    MessageUtil.messagePlayer(sender, "&cThe EXP reward is invalid.");
                    return;
                }

                if (!KOTH.kothList.isEmpty()) {
                    for (KOTH koth : KOTH.kothList) {
                        if (koth.getName().equals(name)) {
                            MessageUtil.messagePlayer(sender, "&cThe KOTH name is already in use.");
                            return;
                        }

                        if (koth.getRegionName().equals(regionName)) {
                            MessageUtil.messagePlayer(sender, "&cThe region name is already in use.");
                            return;
                        }
                    }
                }

                KOTH.addNewKOTH(internalName, name, regionName, capTime, coinReward, expReward);
                MessageUtil.messagePlayer(sender, "&aKOTH '" + name + "' was successfully created.");
                return;

            case "delete":
                if (args.length() != 2) {
                    MessageUtil.messagePlayer(sender, "&cUsage: /koth delete <name>");
                    return;
                }

                String name1 = args.getArgs(1).toLowerCase();

                if (!KOTH.kothList.isEmpty()) {
                    for (KOTH koth : KOTH.kothList) {
                        if (koth.getInternalName().equals(name1)) {
                            KOTH.deleteKOTH(koth);
                            MessageUtil.messagePlayer(sender, "&aKOTH '" + name1 + "' was deleted successfully.");
                            return;
                        }
                    }

                    MessageUtil.messagePlayer(sender, "&aKOTH '" + name1 + "' not found.");
                }
                return;

            case "start":
                if (args.length() != 2) {
                    MessageUtil.messagePlayer(sender, "&cUsage: /koth start <name>");
                    return;
                }

                String name2 = args.getArgs(1).toLowerCase();

                if (!KOTH.kothList.isEmpty()) {
                    for (KOTH koth : KOTH.kothList) {
                        if (koth.getInternalName().equals(name2)) {
                            KOTH.startKOTH(koth);
                            MessageUtil.messagePlayer(sender, "&aKOTH '" + name2 + "' was started successfully.");
                            return;
                        }
                    }

                    MessageUtil.messagePlayer(sender, "&cKOTH '" + name2 + "' not found.");
                }
                return;

            case "stop":
            case "end":
                if (args.length() != 1) {
                    MessageUtil.messagePlayer(sender, "&cUsage: /koth stop/end");
                    return;
                }

                if (KOTH.getActiveKoth() == null) {
                    MessageUtil.messagePlayer(sender, "&cThere are no active KOTHs.");
                    return;
                }

                KOTH.endKOTH(KOTH.getActiveKoth(), null);
                return;

            case "list":
                if (args.length() != 1) {
                    MessageUtil.messagePlayer(sender, "&cUsage: /koth list");
                    return;
                }

                if (KOTH.kothList.isEmpty()) {
                    MessageUtil.messagePlayer(sender, "&cThere are no KOTHs listed.");
                    return;
                }

                MessageUtil.messagePlayer(sender, "");
                MessageUtil.messagePlayer(sender, " &9&lKOTH List");

                for (KOTH koth : KOTH.kothList) {
                    MessageUtil.messagePlayer(sender, "&7  * &e" + koth.getName() + " &7(" + koth.getLocX()
                            + ", " + koth.getLocY() + ", " + koth.getLocZ() + ")");
                }

                MessageUtil.messagePlayer(sender, "");
                return;

            default:
                if (!player.hasPermission("kitpvp.koth")) {
                    MessageUtil.messagePlayer(sender, "&cUsage: /koth");
                    return;
                }

                MessageUtil.messagePlayer(sender, "&cInvalid command. See /koth help for help.");
                break;
        }
    }
}
