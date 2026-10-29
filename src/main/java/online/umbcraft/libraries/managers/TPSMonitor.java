package online.umbcraft.libraries.managers;

import online.umbcraft.libraries.GoldenDupes;
import org.bukkit.Bukkit;

public class TPSMonitor {

    private static double minTps = 18.0;
    private static boolean enabled = false;

    public static void init(GoldenDupes plugin) {
        enabled = plugin.getConfig().getBoolean("tps-protection.enabled", false);
        minTps = plugin.getConfig().getDouble("tps-protection.min-tps", 18.0);
    }

    public static boolean isTPSOk() {
        if (!enabled) return true;
        try {
            double[] tps = Bukkit.getTPS();
            // getTPS() returns [1m, 5m, 15m], we check 1m.
            if (tps[0] < minTps) {
                return false;
            }
        } catch (NoSuchMethodError | Exception e) {
            // Fallback if not on Paper/Folia and method missing (Spigot throws NoSuchMethodError, an Error)
        }
        return true;
    }
}
