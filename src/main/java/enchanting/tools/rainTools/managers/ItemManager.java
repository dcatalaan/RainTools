package enchanting.tools.rainTools.managers;

import enchanting.tools.rainTools.RainTools;
import enchanting.tools.rainTools.models.SavedItem;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ItemManager {
    
    private final RainTools plugin;
    private final Map<String, SavedItem> savedItems;
    
    public ItemManager(RainTools plugin) {
        this.plugin = plugin;
        this.savedItems = new HashMap<>();
        
        loadItems();
    }
    
    private void loadItems() {
        Map<String, SavedItem> loadedItems = plugin.getConfigManager().loadItems();
        savedItems.putAll(loadedItems);
        plugin.getLogger().info("Cargados " + savedItems.size() + " items guardados.");
    }
    
    public boolean saveItem(String name, ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return false;
        }
        
        if (savedItems.containsKey(name)) {
            return false; // Item ya existe
        }
        
        SavedItem savedItem = new SavedItem(name, itemStack);
        savedItems.put(name, savedItem);
        
        // Guardar en la configuración
        plugin.getConfigManager().saveItem(savedItem);
        
        return true;
    }
    
    public boolean updateItem(String name, ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return false;
        }
        
        SavedItem savedItem = new SavedItem(name, itemStack);
        savedItems.put(name, savedItem);
        
        // Guardar en la configuración
        plugin.getConfigManager().saveItem(savedItem);
        
        return true;
    }
    
    public boolean deleteItem(String name) {
        if (!savedItems.containsKey(name)) {
            return false;
        }
        
        savedItems.remove(name);
        plugin.getConfigManager().deleteItem(name);
        
        return true;
    }
    
    public SavedItem getSavedItem(String name) {
        return savedItems.get(name);
    }
    
    public ItemStack getItemStack(String name) {
        SavedItem savedItem = savedItems.get(name);
        if (savedItem == null) {
            return null;
        }
        
        return savedItem.toItemStack();
    }
    
    public Set<String> getItemNames() {
        return savedItems.keySet();
    }
    
    public Map<String, SavedItem> getAllItems() {
        return new HashMap<>(savedItems);
    }
    
    public boolean hasItem(String name) {
        return savedItems.containsKey(name);
    }
    
    public void reloadItems() {
        savedItems.clear();
        loadItems();
    }
    
    public int getItemCount() {
        return savedItems.size();
    }
} 