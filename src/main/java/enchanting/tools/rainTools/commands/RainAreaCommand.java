package enchanting.tools.rainTools.commands;

import enchanting.tools.rainTools.RainTools;
import enchanting.tools.rainTools.models.RainArea;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RainAreaCommand implements CommandExecutor, TabCompleter {
    
    private final RainTools plugin;
    
    public RainAreaCommand(RainTools plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage("errors.player-only"));
                    return true;
                }
                return handleCreate((Player) sender, args);
            case "delete":
            case "remove":
                return handleDelete(sender, args);
            case "list":
                return handleList(sender);
            case "start":
                return handleStart(sender, args);
            case "stop":
                return handleStop(sender, args);
            case "info":
                return handleInfo(sender, args);
            case "config":
            case "configure":
                return handleConfig(sender, args);
            case "reload":
                return handleReload(sender);
            default:
                showHelp(sender);
                return true;
        }
    }
    
    private boolean handleCreate(Player player, String[] args) {
        if (!player.hasPermission("raintools.area.create")) {
            player.sendMessage(plugin.getLanguageManager().getMessage("errors.no-permission"));
            return true;
        }
        
        if (args.length < 2) {
            player.sendMessage(plugin.getLanguageManager().getMessage("help.rainarea.create"));
            return true;
        }
        
        String areaName = args[1];
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("area", areaName);
        
        if (plugin.getAreaManager().createArea(areaName, player)) {
            player.sendMessage(plugin.getLanguageManager().getMessage("success.area-created", placeholders));
        } else {
            player.sendMessage(plugin.getLanguageManager().getMessage("errors.area-already-exists", placeholders));
        }
        
        return true;
    }
    
    private boolean handleDelete(CommandSender sender, String[] args) {
        if (sender instanceof Player && !sender.hasPermission("raintools.area.delete")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("errors.no-permission"));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("help.rainarea.delete"));
            return true;
        }
        
        String areaName = args[1];
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("area", areaName);
        
        if (plugin.getAreaManager().deleteArea(areaName)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("success.area-deleted", placeholders));
        } else {
            sender.sendMessage(plugin.getLanguageManager().getMessage("errors.area-not-found", placeholders));
        }
        
        return true;
    }
    
    private boolean handleList(CommandSender sender) {
        if (sender instanceof Player && !sender.hasPermission("raintools.area.manage")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("errors.no-permission"));
            return true;
        }
        
        Map<String, RainArea> areas = plugin.getAreaManager().getAllAreas();
        
        if (areas.isEmpty()) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("info.no-areas"));
            return true;
        }
        
        sender.sendMessage(plugin.getLanguageManager().getMessage("info.areas-list-header"));
        for (RainArea area : areas.values()) {
            String status = area.isRaining() ? 
                          plugin.getLanguageManager().getMessage("area-status.active") :
                          (area.isEnabled() ? 
                           plugin.getLanguageManager().getMessage("area-status.inactive") :
                           plugin.getLanguageManager().getMessage("area-status.disabled"));
            
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("area", area.getName());
            placeholders.put("volume", String.valueOf(area.getVolume()));
            placeholders.put("status", status);
            
            sender.sendMessage("§f- " + area.getName() + " " + status + " §f(Volume: " + area.getVolume() + " blocks)");
        }
        
        return true;
    }
    
    private boolean handleStart(CommandSender sender, String[] args) {
        if (sender instanceof Player && !sender.hasPermission("raintools.area.manage")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("errors.no-permission"));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("help.rainarea.start"));
            return true;
        }
        
        String areaName = args[1];
        RainArea area = plugin.getAreaManager().getArea(areaName);
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("area", areaName);
        
        if (area == null) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("errors.area-not-found", placeholders));
            return true;
        }
        
        if (!area.isEnabled()) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("errors.area-disabled", placeholders));
            return true;
        }
        
        if (area.isRaining()) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("errors.area-already-raining", placeholders));
            return true;
        }
        
        if (area.isOneTime() && area.hasRainedOnce()) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("errors.area-onetime-used", placeholders));
            return true;
        }
        
        if (plugin.getRainManager().startRain(area)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("success.area-started", placeholders));
        }
        
        return true;
    }
    
    private boolean handleStop(CommandSender sender, String[] args) {
        if (sender instanceof Player && !sender.hasPermission("raintools.area.manage")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("errors.no-permission"));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("help.rainarea.stop"));
            return true;
        }
        
        String areaName = args[1];
        RainArea area = plugin.getAreaManager().getArea(areaName);
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("area", areaName);
        
        if (area == null) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("errors.area-not-found", placeholders));
            return true;
        }
        
        if (!area.isRaining()) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("errors.area-not-raining", placeholders));
            return true;
        }
        
        if (plugin.getRainManager().stopRain(area)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("success.area-stopped", placeholders));
        }
        
        return true;
    }
    
    private boolean handleInfo(CommandSender sender, String[] args) {
        if (sender instanceof Player && !sender.hasPermission("raintools.area.manage")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("errors.no-permission"));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("help.rainarea.info"));
            return true;
        }
        
        String areaName = args[1];
        RainArea area = plugin.getAreaManager().getArea(areaName);
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("area", areaName);
        
        if (area == null) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("errors.area-not-found", placeholders));
            return true;
        }
        
        sender.sendMessage(plugin.getLanguageManager().getMessage("info.area-info-header", placeholders));
        
        // World info
        placeholders.put("world", area.getWorld().getName());
        sender.sendMessage(plugin.getLanguageManager().getMessage("area-info.world", placeholders));
        
        // Position 1
        placeholders.put("x", String.valueOf(area.getPos1().getBlockX()));
        placeholders.put("y", String.valueOf(area.getPos1().getBlockY()));
        placeholders.put("z", String.valueOf(area.getPos1().getBlockZ()));
        sender.sendMessage(plugin.getLanguageManager().getMessage("area-info.pos1", placeholders));
        
        // Position 2
        placeholders.put("x", String.valueOf(area.getPos2().getBlockX()));
        placeholders.put("y", String.valueOf(area.getPos2().getBlockY()));
        placeholders.put("z", String.valueOf(area.getPos2().getBlockZ()));
        sender.sendMessage(plugin.getLanguageManager().getMessage("area-info.pos2", placeholders));
        
        // Other properties
        placeholders.put("value", String.valueOf(area.isEnabled()));
        sender.sendMessage(plugin.getLanguageManager().getMessage("area-info.enabled", placeholders));
        
        placeholders.put("value", String.valueOf(area.isOneTime()));
        sender.sendMessage(plugin.getLanguageManager().getMessage("area-info.onetime", placeholders));
        
        placeholders.put("value", String.valueOf(area.hasRainedOnce()));
        sender.sendMessage(plugin.getLanguageManager().getMessage("area-info.has-rained", placeholders));
        
        placeholders.put("value", String.valueOf(area.getItemsPerBlock()));
        sender.sendMessage(plugin.getLanguageManager().getMessage("area-info.items-per-block", placeholders));
        
        placeholders.put("value", String.valueOf(area.getTimerTicks()));
        sender.sendMessage(plugin.getLanguageManager().getMessage("area-info.timer-ticks", placeholders));
        
        placeholders.put("value", String.valueOf(area.getRainPerSecond()));
        sender.sendMessage(plugin.getLanguageManager().getMessage("area-info.rain-per-second", placeholders));
        
        placeholders.put("value", String.valueOf(area.getExperienceOrbs()));
        sender.sendMessage(plugin.getLanguageManager().getMessage("area-info.exp-orbs", placeholders));
        
        placeholders.put("value", String.valueOf(area.getSavedItems().size()));
        sender.sendMessage(plugin.getLanguageManager().getMessage("area-info.saved-items", placeholders));
        
        placeholders.put("value", String.valueOf(area.getEntities().size()));
        sender.sendMessage(plugin.getLanguageManager().getMessage("area-info.entities", placeholders));
        
        return true;
    }
    
    private boolean handleConfig(CommandSender sender, String[] args) {
        if (sender instanceof Player && !sender.hasPermission("raintools.area.manage")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("errors.no-permission"));
            return true;
        }
        
        if (args.length < 4) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("help.rainarea.config"));
            return true;
        }
        
        String areaName = args[1];
        String property = args[2].toLowerCase();
        String value = args[3];
        
        RainArea area = plugin.getAreaManager().getArea(areaName);
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("area", areaName);
        
        if (area == null) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("errors.area-not-found", placeholders));
            return true;
        }
        
        try {
            switch (property) {
                case "enabled":
                    boolean enabled = Boolean.parseBoolean(value);
                    area.setEnabled(enabled);
                    break;
                case "onetime":
                    boolean oneTime = Boolean.parseBoolean(value);
                    area.setOneTime(oneTime);
                    break;
                case "resetonetime":
                    area.setHasRainedOnce(false);
                    break;
                case "itemsperblock":
                    int itemsPerBlock = Integer.parseInt(value);
                    area.setItemsPerBlock(itemsPerBlock);
                    break;
                case "timer":
                    long timer = Long.parseLong(value);
                    area.setTimerTicks(timer);
                    break;
                case "rainpersecond":
                    int rainPerSecond = Integer.parseInt(value);
                    area.setRainPerSecond(rainPerSecond);
                    break;
                case "exprorbs":
                    int expOrbs = Integer.parseInt(value);
                    area.setExperienceOrbs(expOrbs);
                    break;
                case "minexp":
                    int minExp = Integer.parseInt(value);
                    area.setMinExpPerOrb(minExp);
                    break;
                case "maxexp":
                    int maxExp = Integer.parseInt(value);
                    area.setMaxExpPerOrb(maxExp);
                    break;
                default:
                    placeholders.put("value", property);
                    sender.sendMessage(plugin.getLanguageManager().getMessage("errors.invalid-value", placeholders));
                    return true;
            }
            
            plugin.getAreaManager().saveArea(area);
            sender.sendMessage(plugin.getLanguageManager().getMessage("success.config-updated"));
            
        } catch (NumberFormatException e) {
            placeholders.put("value", value);
            sender.sendMessage(plugin.getLanguageManager().getMessage("errors.invalid-value", placeholders));
        }
        
        return true;
    }
    
    private boolean handleReload(CommandSender sender) {
        if (sender instanceof Player && !sender.hasPermission("raintools.area.manage")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("errors.no-permission"));
            return true;
        }
        
        plugin.getRainManager().stopAllRains();
        plugin.getAreaManager().reloadAreas();
        plugin.getItemManager().reloadItems();
        plugin.getLanguageManager().loadLanguage();
        
        sender.sendMessage(plugin.getLanguageManager().getMessage("success.reload-complete"));
        
        return true;
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage(plugin.getLanguageManager().getMessage("help.rainarea.header"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("help.rainarea.create"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("help.rainarea.delete"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("help.rainarea.list"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("help.rainarea.start"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("help.rainarea.stop"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("help.rainarea.info"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("help.rainarea.config"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("help.rainarea.reload"));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("create", "delete", "list", "start", "stop", "info", "config", "reload"));
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("delete") || subCommand.equals("start") || subCommand.equals("stop") || 
                subCommand.equals("info") || subCommand.equals("config")) {
                completions.addAll(plugin.getAreaManager().getAllAreas().keySet());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("config")) {
            completions.addAll(Arrays.asList("enabled", "onetime", "itemsperblock", "timer", "rainpersecond", "exprorbs", "minexp", "maxexp", "resetonetime"));
        }
        
        return completions;
    }
} 