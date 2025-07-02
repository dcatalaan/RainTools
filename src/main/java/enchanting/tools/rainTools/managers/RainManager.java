package enchanting.tools.rainTools.managers;

import enchanting.tools.rainTools.RainTools;
import enchanting.tools.rainTools.models.RainArea;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RainManager {
    
    private final RainTools plugin;
    private final Map<String, BukkitTask> activeRains;
    private final Map<String, List<Entity>> rainEntities;
    private final Random random;
    
    public RainManager(RainTools plugin) {
        this.plugin = plugin;
        this.activeRains = new HashMap<>();
        this.rainEntities = new HashMap<>();
        this.random = new Random();
    }
    
    public boolean startRain(RainArea area) {
        if (!area.isEnabled()) {
            return false;
        }
        
        if (area.isRaining()) {
            return false; // Ya está lloviendo
        }
        
        if (area.isOneTime() && area.hasRainedOnce()) {
            return false; // Es de una sola vez y ya llovió
        }
        
        area.setRaining(true);
        rainEntities.put(area.getName(), new ArrayList<>());
        
        // Programar limpieza automática después de 20 minutos
        new BukkitRunnable() {
            @Override
            public void run() {
                if (area.isRaining()) {
                    cleanupRainEntities(area.getName());
                }
            }
        }.runTaskLater(plugin, 24000L); // 20 minutos = 24000 ticks
        
        if (area.isOneTime()) {
            // Para áreas de una sola vez, programar que se detenga después de 5 segundos
            area.setHasRainedOnce(true);
            
            BukkitTask task = new BukkitRunnable() {
                private int ticksElapsed = 0;
                private final int maxTicks = 100; // 5 segundos (20 ticks por segundo)
                
                @Override
                public void run() {
                    if (!area.isEnabled() || !area.isRaining()) {
                        stopRain(area);
                        return;
                    }
                    
                    // Ejecutar lluvia mientras no hayan pasado los 5 segundos
                    if (ticksElapsed < maxTicks) {
                        performRain(area);
                        ticksElapsed += area.getTimerTicks();
                    } else {
                        // Han pasado 5 segundos, detener la lluvia
                        stopRain(area);
                    }
                }
            }.runTaskTimer(plugin, 0L, area.getTimerTicks());
            
            activeRains.put(area.getName(), task);
        } else {
            // Para áreas normales (repetitivas)
            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!area.isEnabled() || !area.isRaining()) {
                        stopRain(area);
                        return;
                    }
                    
                    performRain(area);
                }
            }.runTaskTimer(plugin, 0L, area.getTimerTicks());
            
            activeRains.put(area.getName(), task);
        }
        
        return true;
    }
    
    public boolean stopRain(RainArea area) {
        String areaName = area.getName();
        BukkitTask task = activeRains.get(areaName);
        
        if (task != null) {
            task.cancel();
            activeRains.remove(areaName);
        }
        
        // Eliminar todas las entidades generadas por esta área
        List<Entity> entities = rainEntities.get(areaName);
        if (entities != null) {
            for (Entity entity : entities) {
                if (entity != null && !entity.isDead()) {
                    entity.remove();
                }
            }
            rainEntities.remove(areaName);
        }
        
        area.setRaining(false);
        
        return true;
    }
    
    public void stopAllRains() {
        for (BukkitTask task : activeRains.values()) {
            task.cancel();
        }
        activeRains.clear();
        
        // Eliminar todas las entidades de todas las áreas
        for (List<Entity> entities : rainEntities.values()) {
            for (Entity entity : entities) {
                if (entity != null && !entity.isDead()) {
                    entity.remove();
                }
            }
        }
        rainEntities.clear();
        
        // Marcar todas las áreas como no lloviendo
        for (RainArea area : plugin.getAreaManager().getAllAreas().values()) {
            area.setRaining(false);
        }
    }
    
    private void performRain(RainArea area) {
        int rainCount = area.getRainPerSecond();
        List<Entity> areaEntities = rainEntities.get(area.getName());
        
        for (int i = 0; i < rainCount; i++) {
            Location rainLocation = getRandomLocationInArea(area);
            if (rainLocation == null) continue;
            
            // Determinar qué va a caer
            if (shouldSpawnExperienceOrbs(area)) {
                spawnExperienceOrbs(area, rainLocation, areaEntities);
            }
            
            if (shouldSpawnItems(area)) {
                spawnItems(area, rainLocation, areaEntities);
            }
            
            if (shouldSpawnEntities(area)) {
                spawnEntities(area, rainLocation, areaEntities);
            }
        }
    }
    
    private Location getRandomLocationInArea(RainArea area) {
        int minX = Math.min(area.getPos1().getBlockX(), area.getPos2().getBlockX());
        int maxX = Math.max(area.getPos1().getBlockX(), area.getPos2().getBlockX());
        int minY = Math.min(area.getPos1().getBlockY(), area.getPos2().getBlockY());
        int maxY = Math.max(area.getPos1().getBlockY(), area.getPos2().getBlockY());
        int minZ = Math.min(area.getPos1().getBlockZ(), area.getPos2().getBlockZ());
        int maxZ = Math.max(area.getPos1().getBlockZ(), area.getPos2().getBlockZ());
        
        int randomX = minX + random.nextInt(maxX - minX + 1);
        int randomY = minY + random.nextInt(maxY - minY + 1);
        int randomZ = minZ + random.nextInt(maxZ - minZ + 1);
        
        // Crear la ubicación aleatoria dentro del área
        Location randomLocation = new Location(area.getWorld(), randomX, randomY, randomZ);
        
        return randomLocation;
    }
    
    private boolean shouldSpawnExperienceOrbs(RainArea area) {
        return area.getExperienceOrbs() > 0;
    }
    
    private boolean shouldSpawnItems(RainArea area) {
        return !area.getSavedItems().isEmpty();
    }
    
    private boolean shouldSpawnEntities(RainArea area) {
        return !area.getEntities().isEmpty();
    }
    
    private void spawnExperienceOrbs(RainArea area, Location location, List<Entity> areaEntities) {
        int orbCount = area.getExperienceOrbs();
        int minExp = area.getMinExpPerOrb();
        int maxExp = area.getMaxExpPerOrb();
        
        for (int i = 0; i < orbCount; i++) {
            int experience = minExp + random.nextInt(Math.max(1, maxExp - minExp + 1));
            
            // Añadir un poco de dispersión aleatoria
            double offsetX = (random.nextDouble() - 0.5) * 2.0;
            double offsetZ = (random.nextDouble() - 0.5) * 2.0;
            
            Location orbLocation = location.clone().add(offsetX, 0, offsetZ);
            
            ExperienceOrb orb = (ExperienceOrb) area.getWorld().spawnEntity(orbLocation, EntityType.EXPERIENCE_ORB);
            orb.setExperience(experience);
            areaEntities.add(orb);
        }
    }
    
    private void spawnItems(RainArea area, Location location, List<Entity> areaEntities) {
        Map<String, Double> savedItems = area.getSavedItems();
        
        for (Map.Entry<String, Double> entry : savedItems.entrySet()) {
            String itemName = entry.getKey();
            double probability = entry.getValue();
            
            if (random.nextDouble() <= probability) {
                ItemStack itemToSpawn = plugin.getItemManager().getItemStack(itemName);
                if (itemToSpawn != null) {
                    // Añadir dispersión aleatoria
                    double offsetX = (random.nextDouble() - 0.5) * 2.0;
                    double offsetZ = (random.nextDouble() - 0.5) * 2.0;
                    
                    Location itemLocation = location.clone().add(offsetX, 0, offsetZ);
                    
                    // Spawnear múltiples items si está configurado
                    for (int i = 0; i < area.getItemsPerBlock(); i++) {
                        Item item = area.getWorld().dropItem(itemLocation, itemToSpawn.clone());
                        areaEntities.add(item);
                    }
                }
            }
        }
    }
    
    private void spawnEntities(RainArea area, Location location, List<Entity> areaEntities) {
        Map<EntityType, Double> entities = area.getEntities();
        
        for (Map.Entry<EntityType, Double> entry : entities.entrySet()) {
            EntityType entityType = entry.getKey();
            double probability = entry.getValue();
            
            if (random.nextDouble() <= probability) {
                // Verificar que el tipo de entidad no sea un bloque sólido
                if (isValidEntityType(entityType)) {
                    // Añadir dispersión aleatoria
                    double offsetX = (random.nextDouble() - 0.5) * 2.0;
                    double offsetZ = (random.nextDouble() - 0.5) * 2.0;
                    
                    Location entityLocation = location.clone().add(offsetX, 0, offsetZ);
                    
                    try {
                        Entity entity = area.getWorld().spawnEntity(entityLocation, entityType);
                        areaEntities.add(entity);
                    } catch (Exception e) {
                        plugin.getLogger().warning("No se pudo spawnear entidad " + entityType.name() + ": " + e.getMessage());
                    }
                }
            }
        }
    }
    
    private boolean isValidEntityType(EntityType entityType) {
        // Filtrar tipos de entidades que no deberían caer como lluvia
        return entityType != EntityType.PLAYER &&
               entityType != EntityType.ARMOR_STAND &&
               entityType != EntityType.ITEM_FRAME &&
               entityType != EntityType.PAINTING &&
               entityType.isSpawnable() &&
               entityType.isAlive();
    }
    
    public boolean isRaining(RainArea area) {
        return activeRains.containsKey(area.getName()) && area.isRaining();
    }
    
    public int getActiveRainCount() {
        return activeRains.size();
    }
    
    public Map<String, BukkitTask> getActiveRains() {
        return new HashMap<>(activeRains);
    }
    
    private void cleanupRainEntities(String areaName) {
        List<Entity> entities = rainEntities.get(areaName);
        if (entities != null) {
            for (Entity entity : entities) {
                if (entity != null && !entity.isDead()) {
                    entity.remove();
                }
            }
            entities.clear(); // Limpiar la lista pero mantenerla para nuevas entidades
            plugin.getLogger().info("[Rain Tools] Se han limpiado las entidades del área '" + areaName + "' después de 20 minutos.");
        }
    }
} 