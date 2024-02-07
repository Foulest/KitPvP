package net.foulest.kitpvp.util;

import lombok.Getter;
import lombok.Setter;
import net.foulest.kitpvp.KitPvP;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scheduler.BukkitWorker;

import java.util.List;

@Getter
@Setter
@SuppressWarnings("unused")
public class TaskUtil {

    public static void runTask(Runnable runnable) {
        Bukkit.getScheduler().runTask(KitPvP.instance, runnable);
    }

    public static void runTaskAsynchronously(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(KitPvP.instance, runnable);
    }

    public static void runTaskLater(Runnable runnable, long delay) {
        Bukkit.getScheduler().runTaskLater(KitPvP.instance, runnable, delay);
    }

    public static void runTaskLaterAsynchronously(Runnable runnable, long delay) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(KitPvP.instance, runnable, delay);
    }

    public static void runTaskTimer(Runnable runnable, long delay, long period) {
        Bukkit.getScheduler().runTaskTimer(KitPvP.instance, runnable, delay, period);
    }

    public static void runTaskTimerAsynchronously(Runnable runnable, long delay, long period) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(KitPvP.instance, runnable, delay, period);
    }

    public static void scheduleSyncDelayedTask(Runnable runnable) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(KitPvP.instance, runnable);
    }

    public static void scheduleSyncDelayedTask(Runnable runnable, long period) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(KitPvP.instance, runnable, period);
    }

    public static void scheduleSyncRepeatingTask(Runnable runnable, long delay, long period) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(KitPvP.instance, runnable, delay, period);
    }

    public static void cancelAllTasks() {
        Bukkit.getScheduler().cancelAllTasks();
    }

    public static void cancelTask(int taskId) {
        Bukkit.getScheduler().cancelTask(taskId);
    }

    public static boolean isCurrentlyRunning(int taskId) {
        return Bukkit.getScheduler().isCurrentlyRunning(taskId);
    }

    public static boolean isQueued(int taskId) {
        return Bukkit.getScheduler().isQueued(taskId);
    }

    public static List<BukkitWorker> getActiveWorkers() {
        return Bukkit.getScheduler().getActiveWorkers();
    }

    public static List<BukkitTask> getPendingTasks() {
        return Bukkit.getScheduler().getPendingTasks();
    }
}
