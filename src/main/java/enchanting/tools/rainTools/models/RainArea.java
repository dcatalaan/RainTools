package enchanting.tools.rainTools.models;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public class RainArea {
    
    private String name;
    private Location pos1;
    private Location pos2;
    private World world;
    
    // Configuración de lluvia
    private boolean enabled;
    private boolean oneTime;
    private int itemsPerBlock;
    private long timerTicks; // Timer en ticks (20 ticks = 1 segundo)
    private int rainPerSecond;
    
    // Items y entidades que pueden caer
    private Map<String, Double> savedItems; // nombre del item -> probabilidad
    private Map<EntityType, Double> entities; // tipo de entidad -> probabilidad
    private int experienceOrbs;
    private int minExpPerOrb;
    private int maxExpPerOrb;
    
    // Estado de la lluvia
    private boolean isRaining;
    private boolean hasRainedOnce;
    
    public RainArea(String name, Location pos1, Location pos2) {
        this.name = name;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.world = pos1.getWorld();
        
        // Valores por defecto
        this.enabled = true;
        this.oneTime = false;
        this.itemsPerBlock = 1;
        this.timerTicks = 20; // 1 segundo
        this.rainPerSecond = 1;
        
        this.savedItems = new HashMap<>();
        this.entities = new HashMap<>();
        this.experienceOrbs = 0;
        this.minExpPerOrb = 1;
        this.maxExpPerOrb = 5;
        
        this.isRaining = false;
        this.hasRainedOnce = false;
    }
    
    // Getters y Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Location getPos1() { return pos1; }
    public void setPos1(Location pos1) { this.pos1 = pos1; }
    
    public Location getPos2() { return pos2; }
    public void setPos2(Location pos2) { this.pos2 = pos2; }
    
    public World getWorld() { return world; }
    public void setWorld(World world) { this.world = world; }
    
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public boolean isOneTime() { return oneTime; }
    public void setOneTime(boolean oneTime) { this.oneTime = oneTime; }
    
    public int getItemsPerBlock() { return itemsPerBlock; }
    public void setItemsPerBlock(int itemsPerBlock) { this.itemsPerBlock = itemsPerBlock; }
    
    public long getTimerTicks() { return timerTicks; }
    public void setTimerTicks(long timerTicks) { this.timerTicks = timerTicks; }
    
    public int getRainPerSecond() { return rainPerSecond; }
    public void setRainPerSecond(int rainPerSecond) { this.rainPerSecond = rainPerSecond; }
    
    public Map<String, Double> getSavedItems() { return savedItems; }
    public void setSavedItems(Map<String, Double> savedItems) { this.savedItems = savedItems; }
    
    public Map<EntityType, Double> getEntities() { return entities; }
    public void setEntities(Map<EntityType, Double> entities) { this.entities = entities; }
    
    public int getExperienceOrbs() { return experienceOrbs; }
    public void setExperienceOrbs(int experienceOrbs) { this.experienceOrbs = experienceOrbs; }
    
    public int getMinExpPerOrb() { return minExpPerOrb; }
    public void setMinExpPerOrb(int minExpPerOrb) { this.minExpPerOrb = minExpPerOrb; }
    
    public int getMaxExpPerOrb() { return maxExpPerOrb; }
    public void setMaxExpPerOrb(int maxExpPerOrb) { this.maxExpPerOrb = maxExpPerOrb; }
    
    public boolean isRaining() { return isRaining; }
    public void setRaining(boolean raining) { this.isRaining = raining; }
    
    public boolean hasRainedOnce() { return hasRainedOnce; }
    public void setHasRainedOnce(boolean hasRainedOnce) { this.hasRainedOnce = hasRainedOnce; }
    
    // Métodos utilitarios
    public boolean isLocationInArea(Location loc) {
        if (loc.getWorld() != world) return false;
        
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        
        return loc.getBlockX() >= minX && loc.getBlockX() <= maxX &&
               loc.getBlockY() >= minY && loc.getBlockY() <= maxY &&
               loc.getBlockZ() >= minZ && loc.getBlockZ() <= maxZ;
    }
    
    public int getVolume() {
        int width = Math.abs(pos2.getBlockX() - pos1.getBlockX()) + 1;
        int height = Math.abs(pos2.getBlockY() - pos1.getBlockY()) + 1;
        int depth = Math.abs(pos2.getBlockZ() - pos1.getBlockZ()) + 1;
        return width * height * depth;
    }
} 