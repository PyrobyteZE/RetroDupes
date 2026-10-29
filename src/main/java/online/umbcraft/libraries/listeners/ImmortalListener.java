package online.umbcraft.libraries.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class ImmortalListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        // Check for the permission
        if (player.hasPermission("retrodupe.immortal")) {
            double currentHealth = player.getHealth();
            double finalDamage = event.getFinalDamage();

            // If the damage would leave them with less than 1.0 HP (half a heart)
            if (currentHealth - finalDamage < 1.0) {
                
                // If they are already at or below half a heart, just cancel the damage
                if (currentHealth <= 1.0) {
                    event.setCancelled(true);
                    player.setHealth(1.0); // Ensure they stay at exactly half a heart
                } else {
                    // Otherwise, reduce the damage so it only takes them down to half a heart
                    double allowedDamage = currentHealth - 1.0;
                    event.setDamage(EntityDamageEvent.DamageModifier.BASE, allowedDamage);
                    
                    // Clear other modifiers (armor, resistance, etc.) to ensure the math is exact
                    for (EntityDamageEvent.DamageModifier modifier : EntityDamageEvent.DamageModifier.values()) {
                        if (modifier != EntityDamageEvent.DamageModifier.BASE && event.isApplicable(modifier)) {
                            event.setDamage(modifier, 0);
                        }
                    }
                }
            }
        }
    }
}
