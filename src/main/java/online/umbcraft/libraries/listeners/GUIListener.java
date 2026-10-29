package online.umbcraft.libraries.listeners;

import online.umbcraft.libraries.gui.BlacklistGUI;
import online.umbcraft.libraries.managers.BlacklistManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.ConfigurationSection;

public class GUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        String title = e.getView().getTitle();

        if (title.startsWith("§4Universal Blacklist - Page ")) {
            // Clicks in the player's own inventory must stay allowed so they can pick up an item
            // for "Add New Material", but nothing may be moved into or collected out of the GUI.
            if (e.getClickedInventory() != e.getView().getTopInventory()) {
                if (e.isShiftClick() || e.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
                    e.setCancelled(true);
                }
                return;
            }
            e.setCancelled(true);

            if (e.getClickedInventory() == e.getView().getTopInventory()) {
                int page = Integer.parseInt(title.substring("§4Universal Blacklist - Page ".length())) - 1;
                ItemStack clickedItem = e.getCurrentItem();
                if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
                
                int slot = e.getRawSlot();
                
                if (slot < 45) {
                    if (clickedItem.getType() != Material.BLACK_STAINED_GLASS_PANE) {
                        String matName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
                        BlacklistGUI.openUniversalBlacklistMaterialEditor(p, matName);
                    }
                } else {
                    if (slot == 45 && page > 0) {
                        BlacklistGUI.openUniversalBlacklistEditor(p, page - 1);
                    } else if (slot == 49) {
                        p.closeInventory();
                    } else if (slot == 53 && clickedItem.getType() == Material.ARROW) {
                        BlacklistGUI.openUniversalBlacklistEditor(p, page + 1);
                    } else if (slot == 48) { // Add New Material
                        ItemStack cursor = e.getCursor();
                        if (cursor != null && cursor.getType() != Material.AIR) {
                            String name = cursor.getType().name();
                            if (!BlacklistManager.getConfig().contains("universal_blacklist." + name)) {
                                BlacklistManager.getConfig().set("universal_blacklist." + name + ".dupe", "BLOCK");
                                BlacklistManager.getConfig().set("universal_blacklist." + name + ".infinite", "BLOCK");
                                BlacklistManager.save();
                                BlacklistManager.reload();
                                p.sendMessage("§aAdded " + name + " to blacklist!");
                                BlacklistGUI.openUniversalBlacklistEditor(p, page);
                            } else {
                                p.sendMessage("§c" + name + " is already in the blacklist.");
                            }
                        } else {
                            p.sendMessage("§cClick this button with an item in your cursor to add it!");
                        }
                    }
                }
            }
        } else if (title.startsWith("§6Edit: ")) {
            e.setCancelled(true);
            
            if (e.getClickedInventory() == e.getView().getTopInventory()) {
                String matName = ChatColor.stripColor(title).replace("Edit: ", "");
                ConfigurationSection section = BlacklistManager.getConfig().getConfigurationSection("universal_blacklist." + matName);
                if (section == null) {
                    p.closeInventory();
                    return;
                }

                int slot = e.getRawSlot();
                ItemStack clickedItem = e.getCurrentItem();
                if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

                if (slot == 10) { // Infinite
                    String current = section.getString("infinite", "ALLOW");
                    String next = current.equals("ALLOW") ? "BLOCK" : "ALLOW";
                    section.set("infinite", next);
                    BlacklistManager.save();
                    BlacklistManager.reload();
                    BlacklistGUI.openUniversalBlacklistMaterialEditor(p, matName);
                } else if (slot == 11) { // Dupe
                    String current = section.getString("dupe", "ALLOW");
                    String[] actions = {"ALLOW", "BLOCK", "DELETE", "CAP", "STRIP", "TRANSFORM"};
                    int idx = 0;
                    for (int i=0; i<actions.length; i++) {
                        if (actions[i].equals(current)) {
                            idx = i;
                            break;
                        }
                    }
                    String next = actions[(idx + 1) % actions.length];
                    section.set("dupe", next);
                    BlacklistManager.save();
                    BlacklistManager.reload();
                    BlacklistGUI.openUniversalBlacklistMaterialEditor(p, matName);
                } else if (slot == 13 && clickedItem.getType() == Material.HOPPER) { // Cap
                    p.closeInventory();
                    ChatListener.awaitingCapInput.put(p.getUniqueId(), matName);
                    p.sendMessage("§aType the new cap amount for " + matName + " in chat. Type 'cancel' to abort.");
                } else if (slot == 14 && clickedItem.getType() == Material.ENCHANTED_BOOK) { // Strip
                    p.closeInventory();
                    ChatListener.awaitingStripInput.put(p.getUniqueId(), matName);
                    p.sendMessage("§aType the enchantments to strip, separated by commas (e.g. sharpness,unbreaking). Type 'cancel' to abort.");
                } else if (slot == 16) { // Delete
                    BlacklistManager.getConfig().set("universal_blacklist." + matName, null);
                    BlacklistManager.save();
                    BlacklistManager.reload();
                    p.sendMessage("§cRemoved " + matName + " from blacklist.");
                    BlacklistGUI.openUniversalBlacklistEditor(p, 0);
                } else if (slot == 22) { // Back
                    BlacklistGUI.openUniversalBlacklistEditor(p, 0);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        String title = e.getView().getTitle();
        if (!title.startsWith("§4Universal Blacklist - Page ") && !title.startsWith("§6Edit: ")) return;

        int topSize = e.getView().getTopInventory().getSize();
        for (int slot : e.getRawSlots()) {
            if (slot < topSize) {
                e.setCancelled(true);
                return;
            }
        }
    }
}
