package online.umbcraft.libraries.listeners;

import online.umbcraft.libraries.GoldenDupes;
import online.umbcraft.libraries.gui.BlacklistGUI;
import online.umbcraft.libraries.managers.BlacklistManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatListener implements Listener {

    // written from the main thread (GUI clicks) and read from the async chat thread
    public static final Map<UUID, String> awaitingCapInput = new ConcurrentHashMap<>();
    public static final Map<UUID, String> awaitingStripInput = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (awaitingCapInput.containsKey(uuid)) {
            event.setCancelled(true);
            String msg = event.getMessage().trim();
            String matName = awaitingCapInput.get(uuid);

            if (msg.equalsIgnoreCase("cancel")) {
                awaitingCapInput.remove(uuid);
                player.sendMessage("§cCancelled cap limit configuration.");
                runSync(player, () -> {
                    BlacklistGUI.openUniversalBlacklistMaterialEditor(player, matName);
                });
                return;
            }

            try {
                int limit = Integer.parseInt(msg);
                if (limit < 1) {
                    player.sendMessage("§cLimit must be at least 1.");
                    return;
                }

                awaitingCapInput.remove(uuid);

                // the blacklist is read by dupes on the main thread, so only modify it there
                runSync(player, () -> {
                    BlacklistManager.getConfig().set("universal_blacklist." + matName + ".cap_amount", limit);
                    BlacklistManager.save();
                    BlacklistManager.reload();
                    player.sendMessage("§aSet cap amount for " + matName + " to " + limit + ".");
                    BlacklistGUI.openUniversalBlacklistMaterialEditor(player, matName);
                });
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid number. Please type a valid number or 'cancel'.");
            }
        }
        else if (awaitingStripInput.containsKey(uuid)) {
            event.setCancelled(true);
            String msg = event.getMessage().trim();
            String matName = awaitingStripInput.get(uuid);

            if (msg.equalsIgnoreCase("cancel")) {
                awaitingStripInput.remove(uuid);
                player.sendMessage("§cCancelled strip enchantments configuration.");
                runSync(player, () -> {
                    BlacklistGUI.openUniversalBlacklistMaterialEditor(player, matName);
                });
                return;
            }

            String[] enchants = msg.split(",");
            for (int i = 0; i < enchants.length; i++) {
                enchants[i] = enchants[i].trim();
            }

            awaitingStripInput.remove(uuid);

            // the blacklist is read by dupes on the main thread, so only modify it there
            runSync(player, () -> {
                BlacklistManager.getConfig().set("universal_blacklist." + matName + ".strip_enchantments", Arrays.asList(enchants));
                BlacklistManager.save();
                BlacklistManager.reload();
                player.sendMessage("§aSet stripped enchantments for " + matName + ".");
                BlacklistGUI.openUniversalBlacklistMaterialEditor(player, matName);
            });
        }
    }

    // forget pending chat prompts so the maps don't grow with players who left mid-prompt
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        awaitingCapInput.remove(event.getPlayer().getUniqueId());
        awaitingStripInput.remove(event.getPlayer().getUniqueId());
    }

    // the Bukkit scheduler is unsupported on Folia; run on the player's own thread there instead
    private static void runSync(Player player, Runnable task) {
        if (GoldenDupes.isFolia()) {
            player.getScheduler().run(GoldenDupes.getInstance(), t -> task.run(), null);
        }
        else {
            Bukkit.getScheduler().runTask(GoldenDupes.getInstance(), task);
        }
    }
}
