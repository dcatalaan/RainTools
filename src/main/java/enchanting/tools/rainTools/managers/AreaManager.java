package enchanting.tools.rainTools.managers;

import enchanting.tools.rainTools.RainTools;
import enchanting.tools.rainTools.models.RainArea;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AreaManager {
    
    private final RainTools plugin;
    private final Map<String, RainArea> areas;
    private final Map<UUID, Location> firstPositions;
    private final Map<UUID, Location> secondPositions;
    
    public AreaManager(RainTools plugin) {
        this.plugin = plugin;
        this.areas = new HashMap<>();
        this.firstPositions = new HashMap<>();
        this.secondPositions = new HashMap<>();
        
        loadAreas();
    }
    
    private void loadAreas() {
        Map<String, RainArea> loadedAreas = plugin.getConfigManager().loadAreas();
        areas.putAll(loadedAreas);
        plugin.getLogger().info("Cargadas " + areas.size() + " áreas de lluvia.");
    }
    
    public void setFirstPosition(Player player, Location location) {
        firstPositions.put(player.getUniqueId(), location);
        player.sendMessage("§6[Rain Tools] §ePrimera posición seleccionada: §f" + 
                          location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
    }
    
    public void setSecondPosition(Player player, Location location) {
        secondPositions.put(player.getUniqueId(), location);
        player.sendMessage("§6[Rain Tools] §eSegunda posición seleccionada: §f" + 
                          location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
        
        // Si ambas posiciones están seleccionadas, mostrar información del área
        Location pos1 = firstPositions.get(player.getUniqueId());
        if (pos1 != null) {
            int volume = calculateVolume(pos1, location);
            player.sendMessage("§6[Rain Tools] §eÁrea seleccionada: §f" + volume + " bloques");
            player.sendMessage("§6[Rain Tools] §eUsa §f/rainarea create <nombre> §epara crear el área.");
        }
    }
    
    public Location getFirstPosition(Player player) {
        return firstPositions.get(player.getUniqueId());
    }
    
    public Location getSecondPosition(Player player) {
        return secondPositions.get(player.getUniqueId());
    }
    
    public boolean hasCompleteSelection(Player player) {
        UUID playerId = player.getUniqueId();
        return firstPositions.containsKey(playerId) && secondPositions.containsKey(playerId);
    }
    
    public void clearSelection(Player player) {
        UUID playerId = player.getUniqueId();
        firstPositions.remove(playerId);
        secondPositions.remove(playerId);
        player.sendMessage("§6[Rain Tools] §eSelección borrada.");
    }
    
    public boolean createArea(String name, Player player) {
        if (!hasCompleteSelection(player)) {
            player.sendMessage("§c[Rain Tools] Debes seleccionar dos posiciones primero.");
            return false;
        }
        
        if (areas.containsKey(name)) {
            player.sendMessage("§c[Rain Tools] Ya existe un área con ese nombre.");
            return false;
        }
        
        Location pos1 = getFirstPosition(player);
        Location pos2 = getSecondPosition(player);
        
        // Verificar que ambas posiciones estén en el mismo mundo
        if (!pos1.getWorld().equals(pos2.getWorld())) {
            player.sendMessage("§c[Rain Tools] Ambas posiciones deben estar en el mismo mundo.");
            return false;
        }
        
        RainArea area = new RainArea(name, pos1, pos2);
        areas.put(name, area);
        
        // Guardar en la configuración
        plugin.getConfigManager().saveArea(area);
        
        int volume = area.getVolume();
        player.sendMessage("§a[Rain Tools] Área '" + name + "' creada exitosamente.");
        player.sendMessage("§a[Rain Tools] Volumen: " + volume + " bloques.");
        
        // Limpiar selección
        clearSelection(player);
        
        return true;
    }
    
    public boolean deleteArea(String name) {
        if (!areas.containsKey(name)) {
            return false;
        }
        
        // Detener la lluvia si está activa
        RainArea area = areas.get(name);
        if (area.isRaining()) {
            plugin.getRainManager().stopRain(area);
        }
        
        areas.remove(name);
        plugin.getConfigManager().deleteArea(name);
        
        return true;
    }
    
    public RainArea getArea(String name) {
        return areas.get(name);
    }
    
    public Map<String, RainArea> getAllAreas() {
        return new HashMap<>(areas);
    }
    
    public RainArea getAreaAtLocation(Location location) {
        for (RainArea area : areas.values()) {
            if (area.isLocationInArea(location)) {
                return area;
            }
        }
        return null;
    }
    
    public void reloadAreas() {
        areas.clear();
        loadAreas();
    }
    
    private int calculateVolume(Location pos1, Location pos2) {
        int width = Math.abs(pos2.getBlockX() - pos1.getBlockX()) + 1;
        int height = Math.abs(pos2.getBlockY() - pos1.getBlockY()) + 1;
        int depth = Math.abs(pos2.getBlockZ() - pos1.getBlockZ()) + 1;
        return width * height * depth;
    }
    
    public void saveArea(RainArea area) {
        areas.put(area.getName(), area);
        plugin.getConfigManager().saveArea(area);
    }
} 