package online.umbcraft.libraries.commands;

import online.umbcraft.libraries.GoldenDupes;
import online.umbcraft.libraries.utils.ConfigPath;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigCommand {

    private final GoldenDupes plugin;

    public ConfigCommand(GoldenDupes plugin) {
        this.plugin = plugin;
    }

    public boolean handleConfig(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendConfigHelp(sender);
            return true;
        }

        String dupeType = args[1].toLowerCase();

        // Handle list command
        if (dupeType.equals("list")) {
            listAllSettings(sender);
            return true;
        }

        // Handle reload command
        if (dupeType.equals("reload")) {
            plugin.onReloadCommand();
            sender.sendMessage("§8§l[§6§lDupes§8§l] §aConfiguration reloaded!");
            return true;
        }

        // Require at least 3 args for setting values
        if (args.length < 3) {
            sendConfigHelp(sender);
            return true;
        }

        String setting = args[2].toLowerCase();
        String value = args.length >= 4 ? args[3] : null;

        return handleDupeConfig(sender, dupeType, setting, value);
    }

    private boolean handleDupeConfig(CommandSender sender, String dupeType, String setting, String value) {
        FileConfiguration config = plugin.getConfig();
        String path = null;
        Object newValue = null;

        switch (dupeType) {
            case "autocraft":
                path = getAutocraftPath(setting);
                newValue = parseValue(setting, value, "autocraft");
                break;
            case "donkey":
                path = getDonkeyPath(setting);
                newValue = parseValue(setting, value, "donkey");
                break;
            case "nether":
            case "portal":
                path = getNetherPath(setting);
                newValue = parseValue(setting, value, "nether");
                break;
            case "anvil":
                path = getAnvilPath(setting);
                newValue = parseValue(setting, value, "anvil");
                break;
            case "piston":
                path = getPistonPath(setting);
                newValue = parseValue(setting, value, "piston");
                break;
            case "tps":
            case "tps-protection":
                path = getTpsPath(setting);
                newValue = parseValue(setting, value, "tps");
                break;
            case "hopper":
            case "hopper-buff":
                path = getHopperPath(setting);
                newValue = parseValue(setting, value, "hopper");
                break;
            case "non-stackables":
            case "nonstackables":
                path = getNonStackablesPath(setting);
                newValue = parseValue(setting, value, "non-stackables");
                break;
            case "shulkers":
            case "shulker":
                path = getShulkersPath(setting);
                newValue = parseValue(setting, value, "shulkers");
                break;
            case "totems":
            case "totem":
                path = getTotemsPath(setting);
                newValue = parseValue(setting, value, "totems");
                break;
            default:
                sender.sendMessage("§8§l[§6§lDupes§8§l] §cUnknown dupe type: §e" + dupeType);
                sender.sendMessage("§8§l[§6§lDupes§8§l] §7Valid types: §eautocraft, donkey, nether, anvil, piston, tps, hopper, non-stackables, shulkers, totems");
                return true;
        }

        if (path == null || newValue == null) {
            sender.sendMessage("§8§l[§6§lDupes§8§l] §cInvalid setting: §e" + setting);
            return true;
        }

        // Show current value if no new value provided
        if (value == null) {
            Object currentValue = config.get(path);
            sender.sendMessage("§8§l[§6§lDupes§8§l] §7" + path + " = §e" + currentValue);
            return true;
        }

        // Set new value
        config.set(path, newValue);
        plugin.saveConfig();
        plugin.onReloadCommand();

        sender.sendMessage("§8§l[§6§lDupes§8§l] §aSet §e" + path + " §a= §b" + newValue);
        return true;
    }

    private String getAutocraftPath(String setting) {
        switch (setting) {
            case "enabled": case "enable": return ConfigPath.AUTOCRAFT_DO.path();
            case "vanilla": return ConfigPath.AUTOCRAFT_VANILLA.path();
            case "items-by-click": case "ipc": return ConfigPath.AUTOCRAFT_IPC.path();
            case "multiplier": case "multi": return ConfigPath.AUTOCRAFT_MULTIPLIER.path();
            case "max-items": case "max": return ConfigPath.AUTOCRAFT_MAX_ITEMS.path();
            default: return null;
        }
    }

    private String getDonkeyPath(String setting) {
        switch (setting) {
            case "enabled": case "enable": return ConfigPath.DONKEY_DO.path();
            case "boats": case "boat": return ConfigPath.DONKEY_BOATS.path();
            default: return null;
        }
    }

    private String getNetherPath(String setting) {
        switch (setting) {
            case "enabled": case "enable": return ConfigPath.NETHER_DO.path();
            case "tick-delay": case "delay": return ConfigPath.NETHER_TICKDELAY.path();
            default: return null;
        }
    }

    private String getAnvilPath(String setting) {
        switch (setting) {
            case "enabled": case "enable": return ConfigPath.ANVIL_DO.path();
            case "full-inventory": case "fullinv": return ConfigPath.ANVIL_FULLINV.path();
            default: return null;
        }
    }

    private String getPistonPath(String setting) {
        switch (setting) {
            case "enabled": case "enable": return ConfigPath.PISTON_DO.path();
            case "tick-delay": case "delay": return ConfigPath.PISTON_TICKDELAY.path();
            case "non-players": case "nonplayer": return ConfigPath.PISTON_NONPLAYER.path();
            case "max-items": case "max": return "piston-dupe.max-items";
            default: return null;
        }
    }

    private String getTpsPath(String setting) {
        switch (setting) {
            case "enabled": case "enable": return "tps-protection.enabled";
            case "min-tps": case "min": return "tps-protection.min-tps";
            default: return null;
        }
    }

    private String getHopperPath(String setting) {
        switch (setting) {
            case "enabled": case "enable": return "hopper-buff.enabled";
            default: return null;
        }
    }

    private String getNonStackablesPath(String setting) {
        switch (setting) {
            case "dupe": case "enabled": return ConfigPath.NON_STACK_DO_DUPE.path();
            case "stack-to": case "stack": return ConfigPath.NON_STACK_STACKSIZE.path();
            default: return null;
        }
    }

    private String getShulkersPath(String setting) {
        switch (setting) {
            case "dupe": case "enabled": return ConfigPath.SHULKERS_DO_DUPE.path();
            case "stack-to": case "stack": return ConfigPath.SHULKERS_STACKSIZE.path();
            default: return null;
        }
    }

    private String getTotemsPath(String setting) {
        switch (setting) {
            case "dupe": case "enabled": return ConfigPath.TOTEMS_DO_DUPE.path();
            case "stack-to": case "stack": return ConfigPath.TOTEMS_STACKSIZE.path();
            default: return null;
        }
    }

    private Object parseValue(String setting, String value, String dupeType) {
        if (value == null) return null;

        // Boolean settings
        if (setting.equals("enabled") || setting.equals("enable") || setting.equals("dupe") ||
            setting.equals("vanilla") || setting.equals("items-by-click") || setting.equals("ipc") ||
            setting.equals("boats") || setting.equals("boat") || setting.equals("full-inventory") ||
            setting.equals("fullinv") || setting.equals("non-players") || setting.equals("nonplayer")) {
            return parseBoolean(value);
        }

        // Integer settings
        if (setting.equals("multiplier") || setting.equals("multi") || setting.equals("max-items") ||
            setting.equals("max") || setting.equals("tick-delay") || setting.equals("delay") ||
            setting.equals("stack-to") || setting.equals("stack")) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        // Double settings (TPS)
        if (setting.equals("min-tps") || setting.equals("min")) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }

    private Boolean parseBoolean(String value) {
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes") ||
            value.equalsIgnoreCase("on") || value.equals("1")) {
            return true;
        }
        if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("no") ||
            value.equalsIgnoreCase("off") || value.equals("0")) {
            return false;
        }
        return null;
    }

    private void listAllSettings(CommandSender sender) {
        FileConfiguration config = plugin.getConfig();

        sender.sendMessage("§8§l========== §6§lDupe Configuration §8§l==========");
        sender.sendMessage("");

        sender.sendMessage("§6§lAutocraft Dupe:");
        sender.sendMessage("  §7enabled: §e" + config.getBoolean(ConfigPath.AUTOCRAFT_DO.path()));
        sender.sendMessage("  §7vanilla: §e" + config.getBoolean(ConfigPath.AUTOCRAFT_VANILLA.path()));
        sender.sendMessage("  §7items-by-click: §e" + config.getBoolean(ConfigPath.AUTOCRAFT_IPC.path()));
        sender.sendMessage("  §7multiplier: §e" + config.getInt(ConfigPath.AUTOCRAFT_MULTIPLIER.path()));
        sender.sendMessage("  §7max-items: §e" + config.getInt(ConfigPath.AUTOCRAFT_MAX_ITEMS.path()));

        sender.sendMessage("");
        sender.sendMessage("§6§lDonkey Dupe:");
        sender.sendMessage("  §7enabled: §e" + config.getBoolean(ConfigPath.DONKEY_DO.path()));
        sender.sendMessage("  §7boats: §e" + config.getBoolean(ConfigPath.DONKEY_BOATS.path()));

        sender.sendMessage("");
        sender.sendMessage("§6§lNether Portal Dupe:");
        sender.sendMessage("  §7enabled: §e" + config.getBoolean(ConfigPath.NETHER_DO.path()));
        sender.sendMessage("  §7tick-delay: §e" + config.getInt(ConfigPath.NETHER_TICKDELAY.path()));

        sender.sendMessage("");
        sender.sendMessage("§6§lAnvil Dupe:");
        sender.sendMessage("  §7enabled: §e" + config.getBoolean(ConfigPath.ANVIL_DO.path()));
        sender.sendMessage("  §7full-inventory: §e" + config.getBoolean(ConfigPath.ANVIL_FULLINV.path()));

        sender.sendMessage("");
        sender.sendMessage("§6§lPiston Dupe:");
        sender.sendMessage("  §7enabled: §e" + config.getBoolean(ConfigPath.PISTON_DO.path()));
        sender.sendMessage("  §7tick-delay: §e" + config.getInt(ConfigPath.PISTON_TICKDELAY.path()));
        sender.sendMessage("  §7non-players: §e" + config.getBoolean(ConfigPath.PISTON_NONPLAYER.path()));
        sender.sendMessage("  §7max-items: §e" + config.getInt("piston-dupe.max-items"));

        sender.sendMessage("");
        sender.sendMessage("§6§lOther Settings:");
        sender.sendMessage("  §7TPS Protection: §e" + config.getBoolean("tps-protection.enabled") + " §7(min: §e" + config.getDouble("tps-protection.min-tps") + "§7)");
        sender.sendMessage("  §7Hopper Buff: §e" + config.getBoolean("hopper-buff.enabled"));
        sender.sendMessage("  §7Non-Stackables: §7dupe=§e" + config.getBoolean(ConfigPath.NON_STACK_DO_DUPE.path()) + " §7stack=§e" + config.getInt(ConfigPath.NON_STACK_STACKSIZE.path()));
        sender.sendMessage("  §7Shulkers: §7dupe=§e" + config.getBoolean(ConfigPath.SHULKERS_DO_DUPE.path()) + " §7stack=§e" + config.getInt(ConfigPath.SHULKERS_STACKSIZE.path()));
        sender.sendMessage("  §7Totems: §7dupe=§e" + config.getBoolean(ConfigPath.TOTEMS_DO_DUPE.path()) + " §7stack=§e" + config.getInt(ConfigPath.TOTEMS_STACKSIZE.path()));

        sender.sendMessage("");
        sender.sendMessage("§7Use §e/dupes config <type> <setting> [value] §7to modify");
    }

    private void sendConfigHelp(CommandSender sender) {
        sender.sendMessage("§8§l========== §6§lDupe Config Commands §8§l==========");
        sender.sendMessage("");
        sender.sendMessage("§e/dupes config list §7- Show all settings");
        sender.sendMessage("§e/dupes config reload §7- Reload configuration");
        sender.sendMessage("§e/dupes config <type> <setting> [value]");
        sender.sendMessage("");
        sender.sendMessage("§6§lDupe Types:");
        sender.sendMessage("  §eautocraft, donkey, nether, anvil, piston");
        sender.sendMessage("  §etps, hopper, non-stackables, shulkers, totems");
        sender.sendMessage("");
        sender.sendMessage("§6§lExamples:");
        sender.sendMessage("  §e/dupes config piston enabled false");
        sender.sendMessage("  §e/dupes config piston max-items 32");
        sender.sendMessage("  §e/dupes config autocraft multiplier 3");
        sender.sendMessage("  §e/dupes config tps min-tps 18.0");
        sender.sendMessage("  §e/dupes config shulkers stack-to 64");
    }
}
