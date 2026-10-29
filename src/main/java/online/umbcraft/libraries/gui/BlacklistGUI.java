package online.umbcraft.libraries.gui;

import online.umbcraft.libraries.managers.BlacklistManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlacklistGUI {

    public static void openMainMenu(Player player) {
        openUniversalBlacklistEditor(player, 0);
    }

    public static void openUniversalBlacklistEditor(Player player, int page) {
        Inventory inv = Bukkit.createInventory(null, 54, "§4Universal Blacklist - Page " + (page + 1));

        ConfigurationSection section = BlacklistManager.getConfig().getConfigurationSection("universal_blacklist");
        if (section == null) {
            player.sendMessage("§cNo universal_blacklist section found in config!");
            return;
        }

        List<String> materials = new ArrayList<>(section.getKeys(false));
        int start = page * 45;

        for (int i = 0; i < 45; i++) {
            int index = start + i;
            if (index < materials.size()) {
                String materialName = materials.get(index);
                Material m = Material.getMaterial(materialName.toUpperCase());
                if (m == null) continue;

                ConfigurationSection matSection = section.getConfigurationSection(materialName);
                if (matSection == null) continue;

                String infiniteAction = matSection.getString("infinite", "ALLOW");
                String dupeAction = matSection.getString("dupe", "ALLOW");
                int capAmount = matSection.getInt("cap_amount", 64);
                List<String> stripEnchants = matSection.getStringList("strip_enchantments");

                List<String> lore = new ArrayList<>();
                lore.add("§7Infinite: §e" + infiniteAction);
                lore.add("§7Dupe: §e" + dupeAction);
                if (dupeAction.equals("CAP")) {
                    lore.add("§7Cap Amount: §f" + capAmount);
                }
                if (dupeAction.equals("STRIP") && !stripEnchants.isEmpty()) {
                    lore.add("§7Strip: §f" + String.join(", ", stripEnchants));
                }
                lore.add("");
                lore.add("§8§oClick to edit");

                inv.setItem(i, createGuiItem(m, "§e" + materialName, lore));
            }
        }

        fillEmpty(inv, Material.BLACK_STAINED_GLASS_PANE);

        if (page > 0) {
            inv.setItem(45, createGuiItem(Material.ARROW, "§e§l« Previous Page", Arrays.asList("§7Go to page " + page)));
        }
        
        inv.setItem(48, createGuiItem(Material.EMERALD, "§a§lAdd New Material", Arrays.asList("§7Click with an item in your", "§7cursor to add it to the blacklist.")));
        inv.setItem(49, createGuiItem(Material.BARRIER, "§c§lClose", Arrays.asList("§7Close the menu.")));
        inv.setItem(50, createGuiItem(Material.BOOK, "§6§lHelp", Arrays.asList(
            "§7ALLOW: No restrictions",
            "§7BLOCK: Prevent action",
            "§7CAP: Limit amount",
            "§7STRIP: Remove enchantments",
            "§7DELETE: Allow but delete after")));
            
        if (start + 45 < materials.size()) {
            inv.setItem(53, createGuiItem(Material.ARROW, "§e§lNext Page »", Arrays.asList("§7Go to page " + (page + 2))));
        }

        player.openInventory(inv);
    }

    public static void openUniversalBlacklistMaterialEditor(Player player, String materialName) {
        Inventory inv = Bukkit.createInventory(null, 27, "§6Edit: " + materialName);

        ConfigurationSection section = BlacklistManager.getConfig().getConfigurationSection("universal_blacklist." + materialName);
        if (section == null) {
            player.sendMessage("§cMaterial not found in blacklist!");
            return;
        }

        String infiniteAction = section.getString("infinite", "ALLOW");
        String dupeAction = section.getString("dupe", "ALLOW");
        int capAmount = section.getInt("cap_amount", 64);

        inv.setItem(10, createGuiItem(Material.END_CRYSTAL, "§bInfinite Action: §e" + infiniteAction, Arrays.asList("§7Click to cycle: ALLOW → BLOCK → ALLOW")));
        inv.setItem(11, createGuiItem(Material.CHEST, "§6Dupe Action: §e" + dupeAction, Arrays.asList("§7Click to cycle: ALLOW → BLOCK → DELETE → CAP → STRIP → TRANSFORM")));

        if (dupeAction.equals("CAP")) {
            inv.setItem(13, createGuiItem(Material.HOPPER, "§eCap Amount: §f" + capAmount, Arrays.asList("§7Click to edit cap amount via chat")));
        }

        if (dupeAction.equals("STRIP")) {
            List<String> stripEnchants = section.getStringList("strip_enchantments");
            inv.setItem(14, createGuiItem(Material.ENCHANTED_BOOK, "§5Strip Enchantments", Arrays.asList("§7Current: §f" + String.join(", ", stripEnchants), "", "§8§oClick to edit via chat")));
        }

        inv.setItem(16, createGuiItem(Material.BARRIER, "§c§lDelete from Blacklist", Arrays.asList("§7Removes this material entirely")));
        inv.setItem(22, createGuiItem(Material.ARROW, "§7Back to Blacklist Editor", Arrays.asList("§7Return to the main page")));

        player.openInventory(inv);
    }

    public static ItemStack createGuiItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public static void fillEmpty(Inventory inv, Material material) {
        ItemStack item = createGuiItem(material, " ", null);
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) {
                inv.setItem(i, item);
            }
        }
    }
}
