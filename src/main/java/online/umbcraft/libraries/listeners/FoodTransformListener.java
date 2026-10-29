package online.umbcraft.libraries.listeners;

import online.umbcraft.libraries.GoldenDupes;
import online.umbcraft.libraries.managers.BlacklistManager;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;

import java.util.List;

public class FoodTransformListener implements Listener {

    private final GoldenDupes plugin;
    private final NamespacedKey key;

    public FoodTransformListener(GoldenDupes plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "duped_food");
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;
        
        ItemMeta meta = item.getItemMeta();
        if (meta.getPersistentDataContainer().has(key, PersistentDataType.BYTE)) {
            Player player = event.getPlayer();
            
            BlacklistManager.FoodTransformConfig config = BlacklistManager.getFoodTransformConfig();
            if (config != null && config.enabled) {
                BlacklistManager.FoodOverride override = config.overrides.get(item.getType());
                List<PotionEffect> effects = (override != null && override.effects != null) ? override.effects : config.defaultEffects;
                
                for (PotionEffect effect : effects) {
                    player.addPotionEffect(effect);
                }
            }
        }
    }
}
