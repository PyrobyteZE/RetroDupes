package online.umbcraft.libraries.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AdminTabCompleter implements TabCompleter {

    private static final List<String> MAIN_COMMANDS = Arrays.asList("blacklist", "config", "help");
    private static final List<String> CONFIG_SUBCOMMANDS = Arrays.asList("list", "reload");
    private static final List<String> DUPE_TYPES = Arrays.asList(
        "autocraft", "donkey", "nether", "anvil", "piston",
        "tps", "hopper", "non-stackables", "shulkers", "totems"
    );

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("goldendupes.admin")) {
            return completions;
        }

        if (args.length == 1) {
            // First argument: main subcommands
            return filterMatching(MAIN_COMMANDS, args[0]);
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("config")) {
                // Second argument after "config": list, reload, or dupe types
                List<String> options = new ArrayList<>(CONFIG_SUBCOMMANDS);
                options.addAll(DUPE_TYPES);
                return filterMatching(options, args[1]);
            }
            // For blacklist and help, no more arguments needed
            return completions;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("config")) {
            // Third argument: settings for each dupe type
            String dupeType = args[1].toLowerCase();
            List<String> settings = getSettingsForType(dupeType);
            return filterMatching(settings, args[2]);
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("config")) {
            // Fourth argument: value suggestions
            String setting = args[2].toLowerCase();
            List<String> values = getValueSuggestions(setting);
            return filterMatching(values, args[3]);
        }

        // For anything else, return empty list to prevent player name suggestions
        return completions;
    }

    private List<String> getSettingsForType(String dupeType) {
        switch (dupeType) {
            case "autocraft":
                return Arrays.asList("enabled", "vanilla", "items-by-click", "multiplier", "max-items");
            case "donkey":
                return Arrays.asList("enabled", "boats");
            case "nether":
            case "portal":
                return Arrays.asList("enabled", "tick-delay");
            case "anvil":
                return Arrays.asList("enabled", "full-inventory");
            case "piston":
                return Arrays.asList("enabled", "tick-delay", "non-players", "max-items");
            case "tps":
            case "tps-protection":
                return Arrays.asList("enabled", "min-tps");
            case "hopper":
            case "hopper-buff":
                return Arrays.asList("enabled");
            case "non-stackables":
            case "nonstackables":
                return Arrays.asList("dupe", "stack-to");
            case "shulkers":
            case "shulker":
                return Arrays.asList("dupe", "stack-to");
            case "totems":
            case "totem":
                return Arrays.asList("dupe", "stack-to");
            default:
                return new ArrayList<>();
        }
    }

    private List<String> getValueSuggestions(String setting) {
        // Boolean settings
        if (setting.equals("enabled") || setting.equals("enable") || setting.equals("dupe") ||
            setting.equals("vanilla") || setting.equals("items-by-click") || setting.equals("ipc") ||
            setting.equals("boats") || setting.equals("boat") || setting.equals("full-inventory") ||
            setting.equals("fullinv") || setting.equals("non-players") || setting.equals("nonplayer")) {
            return Arrays.asList("true", "false");
        }

        // Numeric settings - provide common values
        if (setting.equals("multiplier") || setting.equals("multi")) {
            return Arrays.asList("1", "2", "3", "4", "5");
        }

        if (setting.equals("max-items") || setting.equals("max")) {
            return Arrays.asList("16", "32", "64");
        }

        if (setting.equals("tick-delay") || setting.equals("delay")) {
            return Arrays.asList("1", "5", "10", "20");
        }

        if (setting.equals("stack-to") || setting.equals("stack")) {
            return Arrays.asList("1", "16", "32", "64");
        }

        if (setting.equals("min-tps") || setting.equals("min")) {
            return Arrays.asList("15.0", "18.0", "19.0", "19.5");
        }

        return new ArrayList<>();
    }

    private List<String> filterMatching(List<String> options, String input) {
        String lowerInput = input.toLowerCase();
        return options.stream()
            .filter(option -> option.toLowerCase().startsWith(lowerInput))
            .collect(Collectors.toList());
    }
}
