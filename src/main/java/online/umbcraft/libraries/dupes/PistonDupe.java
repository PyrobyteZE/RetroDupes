package online.umbcraft.libraries.dupes;

import online.umbcraft.libraries.GoldenDupes;
import online.umbcraft.libraries.utils.ConfigPath;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashMap;

public class PistonDupe extends Dupe implements Listener {

    public PistonDupe() {
    }

    // Automatically dupes an item frame when it is exposed to the back of a retracting piston
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPistonRetract(final BlockPistonRetractEvent e) {
        World world = e.getBlock().getWorld();
        Vector pistonLoc = e.getBlock().getLocation().toVector();

        int xoff = -1 * e.getDirection().getModX();
        int yoff = -1 * e.getDirection().getModY();
        int zoff = -1 * e.getDirection().getModZ();

        // amount to offset the regular piston coords by
        Vector buttOffset = new Vector(0.5*xoff + 0.5, 0.5*yoff + 0.5, 0.5*zoff + 0.5);

        // center of the face of the butt of the piston
        Location pistonButtInWorld = pistonLoc.add(buttOffset).toLocation(world);

        // all entities pressed against the butt of the piston
        Collection<Entity> nearbyEntities = world.getNearbyEntities(pistonButtInWorld, 0.05, 0.05, 0.05);

        for (Entity frameEntity : nearbyEntities) {
            if (!(frameEntity instanceof ItemFrame)) continue;
            
            ItemFrame frame = (ItemFrame) frameEntity;
            ItemStack frameItem = frame.getItem();
            
            if (frameItem == null || frameItem.getType().isAir()) continue;

            // Load limit from config (or default to 1)
            int limit = GoldenDupes.getInstance().getConfig().getInt("piston-dupe.max-items", 64);
            
            if (GoldenDupes.isFolia()) {
                Bukkit.getRegionScheduler().run(GoldenDupes.getInstance(), frame.getLocation(), t -> {
                    processDupe(frame, frameItem, limit);
                });
            } else {
                processDupe(frame, frameItem, limit);
            }
            return;
        }
    }
    
    private void processDupe(ItemFrame frame, ItemStack frameItem, int limit) {
        ItemStack duped = this.dupe(frameItem, limit);
        if (duped == null || duped.getType().isAir()) return;

        BlockFace[] faces = {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
        Block frameBlock = frame.getLocation().getBlock();
        Hopper adjacentHopper = null;

        for (BlockFace face : faces) {
            Block adj = frameBlock.getRelative(face);
            if (adj.getState() instanceof Hopper) {
                adjacentHopper = (Hopper) adj.getState();
                break;
            }
        }

        if (adjacentHopper != null) {
            // Attempt to add to hopper
            HashMap<Integer, ItemStack> leftover = adjacentHopper.getInventory().addItem(duped);
            if (!leftover.isEmpty()) {
                // Drop any items that didn't fit in the hopper
                for (ItemStack remaining : leftover.values()) {
                    frame.getWorld().dropItemNaturally(frame.getLocation(), remaining);
                }
            }
        } else {
            // Drop on the ground
            frame.getWorld().dropItemNaturally(frame.getLocation(), duped);
        }
    }
}
