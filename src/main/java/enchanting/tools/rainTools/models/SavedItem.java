package enchanting.tools.rainTools.models;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SavedItem {
    
    private String name;
    private Material material;
    private int amount;
    private String displayName;
    private List<String> lore;
    private Map<String, Integer> enchantments;
    private String serializedItemStack; // Para almacenar el ItemStack completo serializado
    
    public SavedItem(String name, ItemStack itemStack) {
        this.name = name;
        this.material = itemStack.getType();
        this.amount = itemStack.getAmount();
        
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            this.displayName = meta.getDisplayName();
            this.lore = meta.getLore();
            
            // Convertir encantamientos a un formato serializable
            this.enchantments = new HashMap<>();
            for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
                this.enchantments.put(entry.getKey().getKey().toString(), entry.getValue());
            }
        }
        
        // Serializar el ItemStack completo para preservar todos los NBT tags
        this.serializedItemStack = itemStackToBase64(itemStack);
    }
    
    // Constructor para deserialización
    public SavedItem(String name, Material material, int amount, String displayName, 
                     List<String> lore, Map<String, Integer> enchantments, String serializedItemStack) {
        this.name = name;
        this.material = material;
        this.amount = amount;
        this.displayName = displayName;
        this.lore = lore;
        this.enchantments = enchantments;
        this.serializedItemStack = serializedItemStack;
    }
    
    public ItemStack toItemStack() {
        try {
            // Intentar deserializar desde la cadena serializada primero
            if (serializedItemStack != null && !serializedItemStack.isEmpty()) {
                return itemStackFromBase64(serializedItemStack);
            }
        } catch (Exception e) {
            // Si falla, crear el item manualmente
        }
        
        // Crear item manualmente como respaldo
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            if (displayName != null && !displayName.isEmpty()) {
                meta.setDisplayName(displayName);
            }
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore);
            }
            if (enchantments != null) {
                for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
                    try {
                        Enchantment enchantment = Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft(entry.getKey()));
                        if (enchantment != null) {
                            meta.addEnchant(enchantment, entry.getValue(), true);
                        }
                    } catch (Exception e) {
                        // Ignorar encantamientos inválidos
                    }
                }
            }
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    private String itemStackToBase64(ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            return "";
        }
    }
    
    private ItemStack itemStackFromBase64(String data) throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
        BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
        ItemStack item = (ItemStack) dataInput.readObject();
        dataInput.close();
        return item;
    }
    
    // Getters y Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }
    
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
    
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    
    public List<String> getLore() { return lore; }
    public void setLore(List<String> lore) { this.lore = lore; }
    
    public Map<String, Integer> getEnchantments() { return enchantments; }
    public void setEnchantments(Map<String, Integer> enchantments) { this.enchantments = enchantments; }
    
    public String getSerializedItemStack() { return serializedItemStack; }
    public void setSerializedItemStack(String serializedItemStack) { this.serializedItemStack = serializedItemStack; }
} 