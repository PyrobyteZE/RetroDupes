package online.umbcraft.libraries.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class RulesCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        sender.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "=== Chaos SMP Rules ===");
        
        sender.sendMessage(ChatColor.GOLD + "Player Rules:");
        sender.sendMessage(ChatColor.YELLOW + "1. No lag machines or intentionally crashing the server.");
        sender.sendMessage(ChatColor.YELLOW + "2. Other than that, you are free to do whatever you want!");
        
        sender.sendMessage("");
        
        sender.sendMessage(ChatColor.DARK_AQUA + "Admin Rules:");
        sender.sendMessage(ChatColor.AQUA + "1. Admins are NOT allowed to teleport to players.");
        sender.sendMessage(ChatColor.AQUA + "2. Admins are NOT allowed to spawn items in with commands.");
        sender.sendMessage(ChatColor.AQUA + "3. Admins are NOT allowed to use creative mode.");
        sender.sendMessage(ChatColor.RED + "   * Only Novelbyte is permitted to use creative mode.");
        
        sender.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "========================");
        
        return true;
    }
}
