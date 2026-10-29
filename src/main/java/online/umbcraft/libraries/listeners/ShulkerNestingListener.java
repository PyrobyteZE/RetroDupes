package online.umbcraft.libraries.listeners;

import online.umbcraft.libraries.GoldenDupes;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.HashMap;

public class ShulkerNestingListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHopperMove(InventoryMoveItemEvent event) {
        if (event.getSource().getType() == InventoryType.HOPPER && event.getDestination().getType() == InventoryType.SHULKER_BOX) {
            ItemStack movedItem = event.getItem();

            // Is the item being moved a Shulker Box?
            if (isShulkerBox(movedItem.getType())) {
                
                // Check if the moved shulker box has any shulker boxes inside it
                if (movedItem.hasItemMeta() && movedItem.getItemMeta() instanceof BlockStateMeta) {
                    BlockStateMeta bsm = (BlockStateMeta) movedItem.getItemMeta();
                    if (bsm.getBlockState() instanceof ShulkerBox) {
                        ShulkerBox innerShulker = (ShulkerBox) bsm.getBlockState();
                        for (ItemStack content : innerShulker.getInventory().getContents()) {
                            if (content != null && isShulkerBox(content.getType())) {
                                // Block nesting if it contains another shulker box
                                event.setCancelled(true);
                                return;
                            }
                        }
                    }
                }

                // If it's clean, we must manually move it because Vanilla blocks Shulker-in-Shulker.
                Inventory dest = event.getDestination();
                Inventory source = event.getSource();

                // Check if destination has space
                HashMap<Integer, ItemStack> leftover = dest.addItem(movedItem);
                
                if (leftover.isEmpty()) {
                    // Success! It fit in the destination.
                    // Now we must remove it from the hopper.
                    // The event will try to move it and fail, so we cancel the event.
                    event.setCancelled(true);
                    
                    // We need to remove the item from the source hopper manually.
                    for (int i = 0; i < source.getSize(); i++) {
                        ItemStack sourceItem = source.getItem(i);
                        if (sourceItem != null && sourceItem.isSimilar(movedItem)) {
                            int newAmount = sourceItem.getAmount() - movedItem.getAmount();
                            if (newAmount > 0) {
                                sourceItem.setAmount(newAmount);
                            } else {
                                source.setItem(i, new ItemStack(Material.AIR));
                            }
                            break;
                        }
                    }
                } else {
                    // Didn't fit, cancel the event and let it stay in the hopper
                    event.setCancelled(true);
                }
            }
        }
    }

    private boolean isShulkerBox(Material material) {
        return material.name().endsWith("SHULKER_BOX");
    }
}
