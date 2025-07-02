package enchanting.tools.rainTools.listeners;

import enchanting.tools.rainTools.RainTools;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class AreaSelectionListener implements Listener {
    
    private final RainTools plugin;
    
    public AreaSelectionListener(RainTools plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        // Verificar que el jugador tenga un hacha de diamante en la mano
        if (item == null || item.getType() != Material.DIAMOND_AXE) {
            return;
        }
        
        // Verificar permisos
        if (!player.hasPermission("raintools.area.create")) {
            return;
        }
        
        // Solo procesar clics izquierdo y derecho en bloques
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        if (event.getClickedBlock() == null) {
            return;
        }
        
        event.setCancelled(true); // Cancelar el evento para evitar romper/colocar bloques
        
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            // Clic izquierdo = primera posición
            plugin.getAreaManager().setFirstPosition(player, event.getClickedBlock().getLocation());
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            // Clic derecho = segunda posición
            plugin.getAreaManager().setSecondPosition(player, event.getClickedBlock().getLocation());
        }
    }
} 