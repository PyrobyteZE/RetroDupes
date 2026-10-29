package online.umbcraft.libraries.commands;

import online.umbcraft.libraries.GoldenDupes;
import online.umbcraft.libraries.gui.BlacklistGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminCommand implements CommandExecutor {

    private final ConfigCommand configCommand;

    public AdminCommand(GoldenDupes plugin) {
        this.configCommand = new ConfigCommand(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("goldendupes.admin")) {
            sender.sendMessage("§8§l[§6§lDupes§8§l] §cYou do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "blacklist":
            case "bl":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§8§l[§6§lDupes§8§l] §cOnly players can open the GUI.");
                    return true;
                }
                BlacklistGUI.openMainMenu((Player) sender);
                return true;

            case "config":
            case "cfg":
            case "settings":
                return configCommand.handleConfig(sender, args);

            case "help":
                sendHelp(sender);
                return true;

            default:
                sender.sendMessage("§8§l[§6§lDupes§8§l] §cUnknown subcommand: §e" + subCommand);
                sendHelp(sender);
                return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8§l========== §6§lGoldenDupes Commands §8§l==========");
        sender.sendMessage("");
        sender.sendMessage("§e/dupes blacklist §7- Open blacklist GUI");
        sender.sendMessage("§e/dupes config §7- Configure dupe settings");
        sender.sendMessage("§e/dupes config list §7- List all settings");
        sender.sendMessage("§e/dupes config reload §7- Reload config");
        sender.sendMessage("§e/dupes help §7- Show this help");
        sender.sendMessage("");
        sender.sendMessage("§7Example: §e/dupes config piston enabled false");
    }
}
