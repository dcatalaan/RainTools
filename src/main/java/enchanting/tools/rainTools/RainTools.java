package enchanting.tools.rainTools;

import enchanting.tools.rainTools.commands.RainAreaCommand;
import enchanting.tools.rainTools.commands.SaveItemCommand;
import enchanting.tools.rainTools.listeners.AreaSelectionListener;
import enchanting.tools.rainTools.managers.AreaManager;
import enchanting.tools.rainTools.managers.ConfigManager;
import enchanting.tools.rainTools.managers.ItemManager;
import enchanting.tools.rainTools.managers.LanguageManager;
import enchanting.tools.rainTools.managers.RainManager;
import enchanting.tools.rainTools.utils.VersionChecker;
import org.bukkit.plugin.java.JavaPlugin;

public final class RainTools extends JavaPlugin {

    private static RainTools instance;
    private AreaManager areaManager;
    private ConfigManager configManager;
    private ItemManager itemManager;
    private RainManager rainManager;
    private LanguageManager languageManager;
    private VersionChecker versionChecker;

    @Override
    public void onEnable() {
        instance = this;
        
        // Verificar versión del servidor
        versionChecker = new VersionChecker(this);
        if (!versionChecker.isCompatible()) {
            getLogger().severe("Este plugin requiere Spigot/Paper 1.16.5 o superior!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Guardar configuración por defecto
        saveDefaultConfig();
        
        // Inicializar managers
        configManager = new ConfigManager(this);
        areaManager = new AreaManager(this);
        itemManager = new ItemManager(this);
        rainManager = new RainManager(this);
        languageManager = new LanguageManager(this);
        
        // Registrar comandos
        registerCommands();
        
        // Registrar eventos
        registerEvents();
        
        getLogger().info("Rain Tools ha sido habilitado correctamente!");
        getLogger().info("Versión del servidor: " + versionChecker.getServerVersion());
    }

    @Override
    public void onDisable() {
        if (rainManager != null) {
            rainManager.stopAllRains();
        }
        getLogger().info("Rain Tools ha sido deshabilitado.");
    }
    
    private void registerCommands() {
        getCommand("rainarea").setExecutor(new RainAreaCommand(this));
        getCommand("saveitem").setExecutor(new SaveItemCommand(this));
    }
    
    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new AreaSelectionListener(this), this);
    }
    
    public static RainTools getInstance() {
        return instance;
    }
    
    public AreaManager getAreaManager() {
        return areaManager;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public ItemManager getItemManager() {
        return itemManager;
    }
    
    public RainManager getRainManager() {
        return rainManager;
    }
    
    public LanguageManager getLanguageManager() {
        return languageManager;
    }
    
    public VersionChecker getVersionChecker() {
        return versionChecker;
    }
}
