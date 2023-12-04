package net.foulest.kitpvp.util.command;

import lombok.NonNull;
import net.foulest.kitpvp.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;

/**
 * @author minnymin3
 * @project KitPvP
 * <p>
 * <a href="https://github.com/mcardy/CommandFramework">...</a>
 */
public class BukkitCompleter implements TabCompleter {

    private final Map<String, Entry<Method, Object>> completers = new HashMap<>();

    public void addCompleter(@NonNull String label, @NonNull Method method, @NonNull Object obj) {
        completers.put(label, new AbstractMap.SimpleEntry<>(method, obj));
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command,
                                      @NonNull String label, @NonNull String[] args) {
        for (int i = args.length; i >= 0; i--) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(label.toLowerCase());

            for (int x = 0; x < i; x++) {
                if (!args[x].isEmpty() && !(" ").equals(args[x])) {
                    buffer.append(".").append(args[x].toLowerCase());
                }
            }

            String cmdLabel = buffer.toString();

            if (completers.containsKey(cmdLabel)) {
                Entry<Method, Object> entry = completers.get(cmdLabel);

                try {
                    return (List<String>) entry.getKey().invoke(entry.getValue(),
                            new CommandArgs(sender, command, label, args, cmdLabel.split("\\.").length - 1));
                } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException ex) {
                    MessageUtil.log(Level.WARNING, "Could not tab complete " + entry.getKey().getName() + " for " + entry
                            .getValue().getClass().getName());
                    ex.printStackTrace();
                }
            }
        }
        return Collections.emptyList();
    }
}
