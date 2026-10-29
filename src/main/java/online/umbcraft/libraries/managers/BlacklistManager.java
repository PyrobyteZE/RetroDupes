package online.umbcraft.libraries.managers;

import online.umbcraft.libraries.GoldenDupes;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.NamespacedKey;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Universal Blacklist Manager ported from retrodupe-new
 * Handles actions: ALLOW, BLOCK, DELETE, CAP, STRIP, TRANSFORM
 * and Food Transformations.
 */
public class BlacklistManager {

    private static File file;
    private static FileConfiguration config;
    private static GoldenDupes plugin;
    
    private static final Map<Material, BlacklistEntry> blacklist = new HashMap<>();
    private static FoodTransformConfig foodTransformConfig;

    public static void init(GoldenDupes pl) {
        plugin = pl;
        file = new File(plugin.getDataFolder(), "blacklist.yml");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        
        // Setup initial default section if missing
        if (!config.contains("universal_blacklist")) {
            createDefaultBlacklist();
        }
        
        loadBlacklist();
    }
    
    public static void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void reload() {
        config = YamlConfiguration.loadConfiguration(file);
        loadBlacklist();
    }

    public static void loadBlacklist() {
        blacklist.clear();
        loadFoodTransformConfig();

        ConfigurationSection section = config.getConfigurationSection("universal_blacklist");
        if (section == null) {
            return;
        }

        for (String materialName : section.getKeys(false)) {
            try {
                Material material = Material.valueOf(materialName.toUpperCase());
                ConfigurationSection materialSection = section.getConfigurationSection(materialName);
                if (materialSection == null) continue;

                BlacklistEntry entry = new BlacklistEntry();
                String infiniteAction = materialSection.getString("infinite", "ALLOW");
                entry.infiniteAction = BlacklistAction.valueOf(infiniteAction.toUpperCase());

                String dupeAction = materialSection.getString("dupe", "ALLOW");
                entry.dupeAction = BlacklistAction.valueOf(dupeAction.toUpperCase());

                entry.capAmount = materialSection.getInt("cap_amount", 64);

                entry.stripEnchantments = new HashSet<>();
                List<String> enchantList = materialSection.getStringList("strip_enchantments");
                for (String enchName : enchantList) {
                    try {
                        Enchantment ench = Enchantment.getByKey(NamespacedKey.minecraft(enchName.toLowerCase()));
                        if (ench == null) {
                            ench = Enchantment.getByName(enchName.toUpperCase());
                        }
                        if (ench != null) {
                            entry.stripEnchantments.add(ench);
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Invalid enchantment: " + enchName);
                    }
                }

                if (materialSection.contains("transform_item")) {
                    ConfigurationSection transformSection = materialSection.getConfigurationSection("transform_item");
                    if (transformSection != null) {
                        entry.transformMaterial = Material.valueOf(transformSection.getString("material", materialName).toUpperCase());
                        entry.transformName = transformSection.getString("name");
                        entry.transformLore = transformSection.getStringList("lore");
                        entry.transformCustomModelData = transformSection.getInt("custom_model_data", -1);
                    }
                }

                blacklist.put(material, entry);

            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material in blacklist: " + materialName);
            }
        }
        plugin.getLogger().info("Loaded " + blacklist.size() + " materials into universal blacklist.");
    }

    private static void loadFoodTransformConfig() {
        foodTransformConfig = new FoodTransformConfig();
        ConfigurationSection section = config.getConfigurationSection("dupe_food_transform");
        if (section != null) {
            foodTransformConfig.enabled = section.getBoolean("enabled", true);
            foodTransformConfig.defaultNamePrefix = section.getString("default_name_prefix", "§cGenetically Modified ");
            foodTransformConfig.defaultLore = section.getStringList("default_lore");
            if (foodTransformConfig.defaultLore.isEmpty()) {
                foodTransformConfig.defaultLore.add("§8Tastes like plastic and regret");
            }
            
            String globalType = section.getString("global_effect.type", "HUNGER");
            int globalDuration = section.getInt("global_effect.duration_ticks", 1200);
            int globalAmp = section.getInt("global_effect.amplifier", 1);
            
            PotionEffectType globalPotionType = PotionEffectType.getByName(globalType.toUpperCase());
            if (globalPotionType != null) {
                foodTransformConfig.defaultEffects.add(new PotionEffect(globalPotionType, globalDuration, globalAmp));
            }

            ConfigurationSection customFoods = section.getConfigurationSection("custom_foods");
            if (customFoods != null) {
                for (String matKey : customFoods.getKeys(false)) {
                    try {
                        Material mat = Material.valueOf(matKey.toUpperCase());
                        ConfigurationSection matOverride = customFoods.getConfigurationSection(matKey);
                        if (matOverride != null) {
                            FoodOverride fOverride = new FoodOverride();
                            fOverride.enabled = matOverride.getBoolean("enabled", true);
                            fOverride.namePrefix = foodTransformConfig.defaultNamePrefix;
                            fOverride.lore = foodTransformConfig.defaultLore;
                            
                            if (fOverride.enabled) {
                                String type = matOverride.getString("effect.type", globalType);
                                int duration = matOverride.getInt("effect.duration_ticks", globalDuration);
                                int amp = matOverride.getInt("effect.amplifier", globalAmp);
                                
                                PotionEffectType pType = PotionEffectType.getByName(type.toUpperCase());
                                if (pType != null) {
                                    fOverride.effects = new ArrayList<>();
                                    fOverride.effects.add(new PotionEffect(pType, duration, amp));
                                } else {
                                    fOverride.effects = foodTransformConfig.defaultEffects;
                                }
                            }
                            foodTransformConfig.overrides.put(mat, fOverride);
                        }
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid food override material: " + matKey);
                    }
                }
            }
        }
    }

    private static void createDefaultBlacklist() {
        String[] blockedMaterials = {
            "BEDROCK", "BARRIER", "COMMAND_BLOCK", "CHAIN_COMMAND_BLOCK",
            "REPEATING_COMMAND_BLOCK", "STRUCTURE_BLOCK", "STRUCTURE_VOID",
            "JIGSAW", "DEBUG_STICK", "END_PORTAL_FRAME", "SPAWNER", "DRAGON_EGG"
        };
        
        for (String matName : blockedMaterials) {
            config.set("universal_blacklist." + matName + ".dupe", "BLOCK");
            config.set("universal_blacklist." + matName + ".infinite", "BLOCK");
        }
        
        // Set up default food transform config
        config.set("dupe_food_transform.enabled", true);
        config.set("dupe_food_transform.default_name_prefix", "§cGenetically Modified ");
        config.set("dupe_food_transform.default_lore", Arrays.asList("§8Tastes like plastic and regret"));
        config.set("dupe_food_transform.global_effect.type", "HUNGER");
        config.set("dupe_food_transform.global_effect.duration_ticks", 1200);
        config.set("dupe_food_transform.global_effect.amplifier", 1);
        
        save();
    }

    public static String getDupeBlockedReason(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;

        BlacklistEntry entry = blacklist.get(item.getType());
        if (entry == null) return null;

        switch (entry.dupeAction) {
            case BLOCK:
                return item.getType().name() + " is blacklisted from duplication.";
            default:
                return null;
        }
    }
    
    public static boolean deepScan(ItemStack item) {
        // Fallback for legacy calls from older logic, now just delegates to BLOCK check
        return getDupeBlockedReason(item) != null;
    }

    public static ItemStack processDuplicatedItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return item;

        item = processFoodTransform(item);
        if (item == null) return null;

        BlacklistEntry entry = blacklist.get(item.getType());
        if (entry == null || entry.dupeAction == BlacklistAction.ALLOW) {
            return item;
        }

        switch (entry.dupeAction) {
            case BLOCK:
            case DELETE:
                return null;

            case CAP:
                if (item.getAmount() > entry.capAmount) {
                    item.setAmount(entry.capAmount);
                }
                return item;

            case STRIP:
                if (item.hasItemMeta() && !entry.stripEnchantments.isEmpty()) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta.hasEnchants()) {
                        for (Enchantment ench : entry.stripEnchantments) {
                            if (meta.hasEnchant(ench)) {
                                meta.removeEnchant(ench);
                            }
                        }
                        item.setItemMeta(meta);
                    }
                }
                return item;

            case TRANSFORM:
                ItemStack transformed = new ItemStack(entry.transformMaterial != null ? entry.transformMaterial : item.getType());
                transformed.setAmount(item.getAmount());
                ItemMeta tMeta = transformed.getItemMeta();
                if (tMeta != null) {
                    if (entry.transformName != null) {
                        tMeta.setDisplayName(entry.transformName);
                    }
                    if (entry.transformLore != null && !entry.transformLore.isEmpty()) {
                        tMeta.setLore(entry.transformLore);
                    }
                    if (entry.transformCustomModelData != -1) {
                        tMeta.setCustomModelData(entry.transformCustomModelData);
                    }
                    transformed.setItemMeta(tMeta);
                }
                return transformed;

            default:
                return item;
        }
    }

    private static ItemStack processFoodTransform(ItemStack item) {
        if (foodTransformConfig == null || !foodTransformConfig.enabled || !item.getType().isEdible()) {
            return item;
        }

        FoodOverride fOverride = foodTransformConfig.overrides.get(item.getType());
        if (fOverride != null && !fOverride.enabled) {
            return item;
        }

        String prefix = fOverride != null ? fOverride.namePrefix : foodTransformConfig.defaultNamePrefix;
        List<String> loreStr = fOverride != null ? fOverride.lore : foodTransformConfig.defaultLore;

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String originalName = item.getType().name();
            if (meta.hasDisplayName()) {
                originalName = meta.getDisplayName();
            } else {
                originalName = originalName.substring(0, 1) + originalName.substring(1).toLowerCase().replace("_", " ");
            }
            
            meta.setDisplayName(prefix + originalName);
            
            if (loreStr != null && !loreStr.isEmpty()) {
                meta.setLore(loreStr);
            }

            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "duped_food");
            meta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);

            item.setItemMeta(meta);
        }
        
        // Potion effects will be handled by ItemConsumeEvent listener using the NBT tag or checking the name/lore

        return item;
    }
    
    public static FoodTransformConfig getFoodTransformConfig() {
        return foodTransformConfig;
    }
    
    public static FileConfiguration getConfig() {
        return config;
    }

    public enum BlacklistAction {
        ALLOW, BLOCK, DELETE, CAP, STRIP, TRANSFORM
    }

    public static class BlacklistEntry {
        public BlacklistAction infiniteAction = BlacklistAction.ALLOW;
        public BlacklistAction dupeAction = BlacklistAction.ALLOW;
        public int capAmount = 64;
        public Set<Enchantment> stripEnchantments = new HashSet<>();
        public Material transformMaterial = null;
        public String transformName = null;
        public List<String> transformLore = null;
        public int transformCustomModelData = -1;
    }

    public static class FoodTransformConfig {
        public boolean enabled = false;
        public String defaultNamePrefix = "§cGenetically Modified ";
        public List<String> defaultLore = new ArrayList<>();
        public List<PotionEffect> defaultEffects = new ArrayList<>();
        public Map<Material, FoodOverride> overrides = new HashMap<>();
    }

    public static class FoodOverride {
        public boolean enabled = true;
        public String namePrefix;
        public List<String> lore;
        public List<PotionEffect> effects;
    }
}
