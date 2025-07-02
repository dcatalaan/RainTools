package enchanting.tools.rainTools.commands;

import enchanting.tools.rainTools.RainTools;
import enchanting.tools.rainTools.models.RainArea;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaveItemCommand implements CommandExecutor, TabCompleter {
    
    private final RainTools plugin;
    
    public SaveItemCommand(RainTools plugin) {
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
            case "save":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage("errors.player-only"));
                    return true;
                }
                return handleSave((Player) sender, args);
            case "list":
                return handleList(sender);
            case "delete":
            case "remove":
                return handleDelete(sender, args);
            case "add":
                return handleAddToArea(sender, args);
            case "removearea":
                return handleRemoveFromArea(sender, args);
            default:
                if (args.length == 1) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(plugin.getLanguageManager().getMessage("errors.player-only"));
                        return true;
                    }
                    return handleDirectSave((Player) sender, args[0]);
                } else {
                    showHelp(sender);
                    return true;
                }
        }
    }
    
    private boolean handleSave(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getLanguageManager().getMessage("help.saveitem.save"));
            return true;
        }
        
        return saveCurrentItem(player, args[1]);
    }
    
    private boolean handleDirectSave(Player player, String itemName) {
        return saveCurrentItem(player, itemName);
    }
    
    private boolean saveCurrentItem(Player player, String itemName) {
        if (!player.hasPermission("raintools.item.save")) {
            player.sendMessage(plugin.getLanguageManager().getMessage("errors.no-permission"));
            return true;
        }
        
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("item", itemName);
        
        if (itemInHand == null || itemInHand.getType().isAir()) {
            player.sendMessage(plugin.getLanguageManager().getMessage("errors.no-item-in-hand"));
            return true;
        }
        
        if (plugin.getItemManager().hasItem(itemName)) {
            player.sendMessage(plugin.getLanguageManager().getMessage("errors.item-already-exists", placeholders));
            player.sendMessage(plugin.getLanguageManager().getMessage("info.update-available", placeholders));
            return true;
        }
        
        if (plugin.getItemManager().saveItem(itemName, itemInHand)) {
            player.sendMessage(plugin.getLanguageManager().getMessage("success.item-saved", placeholders));
        }
        
        return true;
    }
    
    private boolean handleList(CommandSender sender) {
        if (sender instanceof Player && !sender.hasPermission("raintools.item.save")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("errors.no-permission"));
            return true;
        }
        
        if (plugin.getItemManager().getItemCount() == 0) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("info.no-items"));
            return true;
        }
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("count", String.valueOf(plugin.getItemManager().getItemCount()));
        sender.sendMessage(plugin.getLanguageManager().getMessage("info.items-list-header", placeholders));
        
        for (String itemName : plugin.getItemManager().getItemNames()) {
            ItemStack item = plugin.getItemManager().getItemStack(itemName);
            if (item != null) {
                String displayName = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? 
                                   item.getItemMeta().getDisplayName() : item.getType().name();
                sender.sendMessage("§f- " + itemName + " §7(" + displayName + " x" + item.getAmount() + ")");
            }
        }
        
        return true;
    }
    
    private boolean handleDelete(CommandSender sender, String[] args) {
        if (sender instanceof Player && !sender.hasPermission("raintools.item.save")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("errors.no-permission"));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("help.saveitem.delete"));
            return true;
        }
        
        String itemName = args[1];
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("item", itemName);
        
        if (!plugin.getItemManager().hasItem(itemName)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("errors.item-not-found", placeholders));
            return true;
        }
        
        if (plugin.getItemManager().deleteItem(itemName)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("success.item-deleted", placeholders));
        }
        
        return true;
    }
    
    private boolean handleAddToArea(CommandSender sender, String[] args) {
        if (sender instanceof Player && !sender.hasPermission("raintools.item.save")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("errors.no-permission"));
            return true;
        }
        
        if (args.length < 4) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("help.saveitem.add"));
            return true;
        }
        
        String areaName = args[1];
        String itemName = args[2];
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("area", areaName);
        placeholders.put("item", itemName);
        
        RainArea area = plugin.getAreaManager().getArea(areaName);
        if (area == null) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("errors.area-not-found", placeholders));
            return true;
        }
        
        if (!plugin.getItemManager().hasItem(itemName)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("errors.item-not-found", placeholders));
            return true;
        }
        
        try {
            double probability = Double.parseDouble(args[3]);
            if (probability < 0.0 || probability > 1.0) {
                placeholders.put("value", args[3]);
                sender.sendMessage(plugin.getLanguageManager().getMessage("errors.invalid-value", placeholders));
                return true;
            }
            
            area.getSavedItems().put(itemName, probability);
            plugin.getAreaManager().saveArea(area);
            
            placeholders.put("chance", String.format("%.1f", probability * 100));
            sender.sendMessage(plugin.getLanguageManager().getMessage("success.item-added", placeholders));
            
        } catch (NumberFormatException e) {
            placeholders.put("value", args[3]);
            sender.sendMessage(plugin.getLanguageManager().getMessage("errors.invalid-value", placeholders));
        }
        
        return true;
    }
    
    private boolean handleRemoveFromArea(CommandSender sender, String[] args) {
        if (sender instanceof Player && !sender.hasPermission("raintools.item.save")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("errors.no-permission"));
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("help.saveitem.remove"));
            return true;
        }
        
        String areaName = args[1];
        String itemName = args[2];
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("area", areaName);
        placeholders.put("item", itemName);
        
        RainArea area = plugin.getAreaManager().getArea(areaName);
        if (area == null) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("errors.area-not-found", placeholders));
            return true;
        }
        
        if (!area.getSavedItems().containsKey(itemName)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("errors.item-not-found", placeholders));
            return true;
        }
        
        area.getSavedItems().remove(itemName);
        plugin.getAreaManager().saveArea(area);
        
        sender.sendMessage(plugin.getLanguageManager().getMessage("success.item-removed", placeholders));
        
        return true;
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage(plugin.getLanguageManager().getMessage("help.saveitem.header"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("help.saveitem.save"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("help.saveitem.save-alt"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("help.saveitem.list"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("help.saveitem.delete"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("help.saveitem.add"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("help.saveitem.remove"));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("save", "list", "delete", "add", "removearea"));
            completions.addAll(plugin.getItemManager().getItemNames());
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("delete")) {
                completions.addAll(plugin.getItemManager().getItemNames());
            } else if (subCommand.equals("add") || subCommand.equals("removearea")) {
                completions.addAll(plugin.getAreaManager().getAllAreas().keySet());
            }
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("add") || subCommand.equals("removearea")) {
                completions.addAll(plugin.getItemManager().getItemNames());
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("add")) {
            completions.addAll(Arrays.asList("0.1", "0.25", "0.5", "0.75", "1.0"));
        }
        
        return completions;
    }
} 