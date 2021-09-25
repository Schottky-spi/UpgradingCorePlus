package de.schottky.util;

import de.schottky.UpgradingCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Timers {

    public static void runLater(int ticks, Runnable runnable) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(JavaPlugin.getPlugin(UpgradingCorePlugin.class), runnable, ticks);
    }
}
