package enchanting.tools.rainTools.managers;

import enchanting.tools.rainTools.RainTools;
import enchanting.tools.rainTools.models.RainArea;
import enchanting.tools.rainTools.models.SavedItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {
    
    private final RainTools plugin;
    private FileConfiguration config;
    private File configFile;
    private File areasFile;
    private File itemsFile;
    private FileConfiguration areasConfig;
    private FileConfiguration itemsConfig;
    
    public ConfigManager(RainTools plugin) {
        this.plugin = plugin;
        setupConfig();
        loadConfigs();
    }
    
    private void setupConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        areasFile = new File(plugin.getDataFolder(), "areas.yml");
        itemsFile = new File(plugin.getDataFolder(), "items.yml");
        
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }
        
        if (!areasFile.exists()) {
            try {
                areasFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("No se pudo crear areas.yml: " + e.getMessage());
            }
        }
        
        if (!itemsFile.exists()) {
            try {
                itemsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("No se pudo crear items.yml: " + e.getMessage());
            }
        }
    }
    
    private void loadConfigs() {
        config = plugin.getConfig();
        areasConfig = YamlConfiguration.loadConfiguration(areasFile);
        itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
    }
    
    public void saveArea(RainArea area) {
        String path = "areas." + area.getName();
        
        areasConfig.set(path + ".world", area.getWorld().getName());
        areasConfig.set(path + ".pos1.x", area.getPos1().getBlockX());
        areasConfig.set(path + ".pos1.y", area.getPos1().getBlockY());
        areasConfig.set(path + ".pos1.z", area.getPos1().getBlockZ());
        areasConfig.set(path + ".pos2.x", area.getPos2().getBlockX());
        areasConfig.set(path + ".pos2.y", area.getPos2().getBlockY());
        areasConfig.set(path + ".pos2.z", area.getPos2().getBlockZ());
        
        areasConfig.set(path + ".enabled", area.isEnabled());
        areasConfig.set(path + ".oneTime", area.isOneTime());
        areasConfig.set(path + ".itemsPerBlock", area.getItemsPerBlock());
        areasConfig.set(path + ".timerTicks", area.getTimerTicks());
        areasConfig.set(path + ".rainPerSecond", area.getRainPerSecond());
        
        // Guardar items
        if (!area.getSavedItems().isEmpty()) {
            ConfigurationSection itemsSection = areasConfig.createSection(path + ".savedItems");
            for (Map.Entry<String, Double> entry : area.getSavedItems().entrySet()) {
                itemsSection.set(entry.getKey(), entry.getValue());
            }
        }
        
        // Guardar entidades
        if (!area.getEntities().isEmpty()) {
            ConfigurationSection entitiesSection = areasConfig.createSection(path + ".entities");
            for (Map.Entry<EntityType, Double> entry : area.getEntities().entrySet()) {
                entitiesSection.set(entry.getKey().name(), entry.getValue());
            }
        }
        
        // Guardar configuración de experiencia
        areasConfig.set(path + ".experienceOrbs", area.getExperienceOrbs());
        areasConfig.set(path + ".minExpPerOrb", area.getMinExpPerOrb());
        areasConfig.set(path + ".maxExpPerOrb", area.getMaxExpPerOrb());
        
        saveAreasConfig();
    }
    
    public Map<String, RainArea> loadAreas() {
        Map<String, RainArea> areas = new HashMap<>();
        
        ConfigurationSection areasSection = areasConfig.getConfigurationSection("areas");
        if (areasSection == null) return areas;
        
        for (String areaName : areasSection.getKeys(false)) {
            try {
                String worldName = areasSection.getString(areaName + ".world");
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    plugin.getLogger().warning("Mundo '" + worldName + "' no encontrado para el área '" + areaName + "'");
                    continue;
                }
                
                int x1 = areasSection.getInt(areaName + ".pos1.x");
                int y1 = areasSection.getInt(areaName + ".pos1.y");
                int z1 = areasSection.getInt(areaName + ".pos1.z");
                int x2 = areasSection.getInt(areaName + ".pos2.x");
                int y2 = areasSection.getInt(areaName + ".pos2.y");
                int z2 = areasSection.getInt(areaName + ".pos2.z");
                
                Location pos1 = new Location(world, x1, y1, z1);
                Location pos2 = new Location(world, x2, y2, z2);
                
                RainArea area = new RainArea(areaName, pos1, pos2);
                
                // Cargar configuración
                area.setEnabled(areasSection.getBoolean(areaName + ".enabled", true));
                area.setOneTime(areasSection.getBoolean(areaName + ".oneTime", false));
                area.setItemsPerBlock(areasSection.getInt(areaName + ".itemsPerBlock", 1));
                area.setTimerTicks(areasSection.getLong(areaName + ".timerTicks", 20));
                area.setRainPerSecond(areasSection.getInt(areaName + ".rainPerSecond", 1));
                
                // Cargar items guardados
                ConfigurationSection itemsSection = areasSection.getConfigurationSection(areaName + ".savedItems");
                if (itemsSection != null) {
                    Map<String, Double> savedItems = new HashMap<>();
                    for (String itemName : itemsSection.getKeys(false)) {
                        savedItems.put(itemName, itemsSection.getDouble(itemName));
                    }
                    area.setSavedItems(savedItems);
                }
                
                // Cargar entidades
                ConfigurationSection entitiesSection = areasSection.getConfigurationSection(areaName + ".entities");
                if (entitiesSection != null) {
                    Map<EntityType, Double> entities = new HashMap<>();
                    for (String entityName : entitiesSection.getKeys(false)) {
                        try {
                            EntityType entityType = EntityType.valueOf(entityName);
                            entities.put(entityType, entitiesSection.getDouble(entityName));
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Tipo de entidad inválido: " + entityName);
                        }
                    }
                    area.setEntities(entities);
                }
                
                // Cargar configuración de experiencia
                area.setExperienceOrbs(areasSection.getInt(areaName + ".experienceOrbs", 0));
                area.setMinExpPerOrb(areasSection.getInt(areaName + ".minExpPerOrb", 1));
                area.setMaxExpPerOrb(areasSection.getInt(areaName + ".maxExpPerOrb", 5));
                
                areas.put(areaName, area);
                
            } catch (Exception e) {
                plugin.getLogger().severe("Error cargando área '" + areaName + "': " + e.getMessage());
            }
        }
        
        return areas;
    }
    
    public void deleteArea(String areaName) {
        areasConfig.set("areas." + areaName, null);
        saveAreasConfig();
    }
    
    public void saveItem(SavedItem item) {
        String path = "items." + item.getName();
        
        itemsConfig.set(path + ".material", item.getMaterial().name());
        itemsConfig.set(path + ".amount", item.getAmount());
        itemsConfig.set(path + ".displayName", item.getDisplayName());
        itemsConfig.set(path + ".lore", item.getLore());
        itemsConfig.set(path + ".enchantments", item.getEnchantments());
        itemsConfig.set(path + ".serializedItemStack", item.getSerializedItemStack());
        
        saveItemsConfig();
    }
    
    public Map<String, SavedItem> loadItems() {
        Map<String, SavedItem> items = new HashMap<>();
        
        ConfigurationSection itemsSection = itemsConfig.getConfigurationSection("items");
        if (itemsSection == null) return items;
        
        for (String itemName : itemsSection.getKeys(false)) {
            try {
                String materialName = itemsSection.getString(itemName + ".material");
                Material material = Material.valueOf(materialName);
                int amount = itemsSection.getInt(itemName + ".amount", 1);
                String displayName = itemsSection.getString(itemName + ".displayName");
                List<String> lore = itemsSection.getStringList(itemName + ".lore");
                String serializedItemStack = itemsSection.getString(itemName + ".serializedItemStack");
                
                // Cargar encantamientos
                Map<String, Integer> enchantments = new HashMap<>();
                ConfigurationSection enchantmentsSection = itemsSection.getConfigurationSection(itemName + ".enchantments");
                if (enchantmentsSection != null) {
                    for (String enchantmentName : enchantmentsSection.getKeys(false)) {
                        enchantments.put(enchantmentName, enchantmentsSection.getInt(enchantmentName));
                    }
                }
                
                SavedItem item = new SavedItem(itemName, material, amount, displayName, lore, enchantments, serializedItemStack);
                items.put(itemName, item);
                
            } catch (Exception e) {
                plugin.getLogger().severe("Error cargando item '" + itemName + "': " + e.getMessage());
            }
        }
        
        return items;
    }
    
    public void deleteItem(String itemName) {
        itemsConfig.set("items." + itemName, null);
        saveItemsConfig();
    }
    
    private void saveAreasConfig() {
        try {
            areasConfig.save(areasFile);
        } catch (IOException e) {
            plugin.getLogger().severe("No se pudo guardar areas.yml: " + e.getMessage());
        }
    }
    
    private void saveItemsConfig() {
        try {
            itemsConfig.save(itemsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("No se pudo guardar items.yml: " + e.getMessage());
        }
    }
    
    public FileConfiguration getConfig() {
        return config;
    }
} 