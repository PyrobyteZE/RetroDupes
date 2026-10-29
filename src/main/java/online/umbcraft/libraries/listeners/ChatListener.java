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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatListener implements Listener {

    public static final Map<UUID, String> awaitingCapInput = new HashMap<>();
    public static final Map<UUID, String> awaitingStripInput = new HashMap<>();

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
                Bukkit.getScheduler().runTask(GoldenDupes.getInstance(), () -> {
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

                BlacklistManager.getConfig().set("universal_blacklist." + matName + ".cap_amount", limit);
                BlacklistManager.save();
                BlacklistManager.reload();
                
                awaitingCapInput.remove(uuid);
                player.sendMessage("§aSet cap amount for " + matName + " to " + limit + ".");

                Bukkit.getScheduler().runTask(GoldenDupes.getInstance(), () -> {
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
                Bukkit.getScheduler().runTask(GoldenDupes.getInstance(), () -> {
                    BlacklistGUI.openUniversalBlacklistMaterialEditor(player, matName);
                });
                return;
            }

            String[] enchants = msg.split(",");
            for (int i = 0; i < enchants.length; i++) {
                enchants[i] = enchants[i].trim();
            }

            BlacklistManager.getConfig().set("universal_blacklist." + matName + ".strip_enchantments", Arrays.asList(enchants));
            BlacklistManager.save();
            BlacklistManager.reload();

            awaitingStripInput.remove(uuid);
            player.sendMessage("§aSet stripped enchantments for " + matName + ".");

            Bukkit.getScheduler().runTask(GoldenDupes.getInstance(), () -> {
                BlacklistGUI.openUniversalBlacklistMaterialEditor(player, matName);
            });
        }
    }
}
