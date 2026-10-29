package online.umbcraft.libraries.listeners;

import online.umbcraft.libraries.GoldenDupes;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class HopperBuffListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHopperTransfer(InventoryMoveItemEvent event) {
        if (!GoldenDupes.getInstance().getConfig().getBoolean("hopper-buff.enabled", false)) {
            return;
        }

        // Only buff if a hopper is involved
        if (event.getSource().getType() != InventoryType.HOPPER && 
            event.getDestination().getType() != InventoryType.HOPPER) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) return;

        // If the item is already a full stack (or more?), no need to buff
        if (item.getAmount() >= item.getMaxStackSize()) return;

        Inventory sourceInv = event.getSource();
        
        // Find the item in the source inventory to see how much we can take
        for (ItemStack sourceStack : sourceInv.getContents()) {
            if (sourceStack == null || !sourceStack.isSimilar(item)) continue;

            int amountInSource = sourceStack.getAmount();
            int maxTransfer = item.getMaxStackSize();
            
            // Calculate how much we can move (up to a full stack)
            if (amountInSource > item.getAmount()) {
                int newAmount = Math.min(amountInSource, maxTransfer);
                item.setAmount(newAmount);
                // The event item is what gets moved. By increasing its amount here, 
                // the hopper moves the whole stack in one go.
                break;
            }
        }
    }
}
