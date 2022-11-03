package net.foulest.kitpvp.listeners;

import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.koth.KOTH;
import net.foulest.kitpvp.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class KOTHListener implements Listener {

    // TODO: revamp whole method by checking for players every 100ms instead of on player move (this sucks)

    @EventHandler
    public static void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        KOTH koth = KOTH.getActiveKoth();

        if (koth == null) {
            if (KOTH.kothRefresh != null) {
                KOTH.kothRefresh.cancel();
                KOTH.kothRefresh = null;
            }

            return;
        }

        if (KOTH.playersInKOTH.get(koth) == null) {
            KOTH.playersInKOTH.computeIfAbsent(koth, k -> new ArrayList<>());
        }

        if (KOTH.isInKOTH(event.getTo())) {
            if (!KOTH.playersInKOTH.get(koth).contains(player)) {
                KOTH.playersInKOTH.get(koth).add(player);
            }

        } else if (!KOTH.isInKOTH(event.getTo())) {
            KOTH.playersInKOTH.get(koth).remove(player);
        }

        if (KOTH.kothRefresh == null) {
            startTask(koth, player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public static void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        KOTH koth = KOTH.getActiveKoth();

        if (KOTH.getActiveKoth() == null) {
            if (KOTH.kothRefresh != null) {
                KOTH.kothRefresh.cancel();
                KOTH.kothRefresh = null;
            }

            return;
        }

        if (KOTH.playersInKOTH.get(koth) == null) {
            KOTH.playersInKOTH.computeIfAbsent(koth, k -> new ArrayList<>());
        }

        if (KOTH.isInKOTH(event.getTo())) {
            if (!KOTH.playersInKOTH.get(koth).contains(player)) {
                KOTH.playersInKOTH.get(koth).add(player);
            }

        } else if (!KOTH.isInKOTH(event.getTo())) {
            KOTH.playersInKOTH.get(koth).remove(player);
        }

        if (KOTH.kothRefresh == null) {
            startTask(KOTH.getActiveKoth(), player);
        }

//        if (activeKOTH.getTimeLeft() != activeKOTH.getCapTime()) {
//            if (activeKOTH.getTimeLeft() % 30 == 0 || (activeKOTH.getTimeLeft() < 30 && activeKOTH.getTimeLeft() % 5 == 0)) {
//                MessageUtil.broadcast("&9[KOTH] &6" + activeKOTH.getName() + " &eKOTH is being captured by &6"
//                        + activeKOTH.getCapper().getDisplayName() + " &c(" + KOTH.getTimeLeftNeat(activeKOTH) + ")");
//            }
//        }
    }

    public static void startTask(KOTH activeKOTH, Player player) {
        if (KOTH.kothRefresh == null) {
            KOTH.kothRefresh = (new BukkitRunnable() {
                @Override
                public void run() {
                    if (KOTH.playersInKOTH.get(activeKOTH).size() > 1) {
                        if (!activeKOTH.isContested()) {
                            activeKOTH.setContested(true);
                            activeKOTH.setLastCapper(activeKOTH.getCapper());
                            MessageUtil.broadcast("&9[KOTH] &6" + activeKOTH.getName() + " &eKOTH is being contested.");
                        }

                        if (KOTH.countdown != null) {
                            KOTH.countdown.cancel();
                            KOTH.countdown = null;
                        }

                    } else if (KOTH.playersInKOTH.get(activeKOTH).size() == 1) {
                        if (activeKOTH.getCapper() != player) {
                            activeKOTH.setCapper(KOTH.playersInKOTH.get(activeKOTH).get(0));

                            if (activeKOTH.getCapper() != activeKOTH.getLastCapper()) {
                                activeKOTH.setLastCapper(player);
                                activeKOTH.setTimeLeft(activeKOTH.getCapTime());
                            }
                        }

                        if (KOTH.countdown == null) {
                            KOTH.countdown = (new BukkitRunnable() {
                                @Override
                                public void run() {
                                    activeKOTH.setTimeLeft(activeKOTH.getTimeLeft() - 1);

                                    if (activeKOTH.getTimeLeft() == 0) {
                                        KOTH.countdown = null;
                                        cancel();

                                        KOTH.endKOTH(activeKOTH, activeKOTH.getCapper());
                                    }
                                }
                            }.runTaskTimer(KitPvP.instance, 0L, 20L));
                        }

                        if (activeKOTH.isContested()) {
                            activeKOTH.setContested(false);
                        }

                    } else if (KOTH.playersInKOTH.get(activeKOTH).size() == 0) {
                        if (activeKOTH.getCapper() != null) {
                            activeKOTH.setCapper(null);

                            if (activeKOTH.getCapTime() - activeKOTH.getTimeLeft() >= 30) {
                                MessageUtil.broadcast("&9[KOTH] &6" + activeKOTH.getName() + " &eKOTH is no longer being captured.");
                            }
                        }

                        if (activeKOTH.isContested()) {
                            activeKOTH.setContested(false);
                        }

                        if (KOTH.countdown != null) {
                            KOTH.countdown.cancel();
                            KOTH.countdown = null;
                        }

                        if (activeKOTH.getTimeLeft() != activeKOTH.getCapTime()) {
                            activeKOTH.setTimeLeft(activeKOTH.getCapTime());
                        }
                    }
                }
            }.runTaskTimer(KitPvP.instance, 0L, 0L));
        }
    }
}
