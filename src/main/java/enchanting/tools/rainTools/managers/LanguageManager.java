package enchanting.tools.rainTools.managers;

import enchanting.tools.rainTools.RainTools;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LanguageManager {
    private final RainTools plugin;
    private final String defaultLanguage = "en";
    private String currentLanguage;
    private YamlConfiguration langConfig;
    private final Pattern placeholderPattern = Pattern.compile("%([^%]+)%");
    
    public LanguageManager(RainTools plugin) {
        this.plugin = plugin;
        this.currentLanguage = plugin.getConfig().getString("language", defaultLanguage);
        loadLanguage();
    }
    
    public void loadLanguage() {
        // Crear directorio de idiomas si no existe
        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists()) {
            langDir.mkdirs();
            saveDefaultLanguages(langDir);
        }
        
        // Cargar archivo de idioma
        File langFile = new File(langDir, currentLanguage + ".yml");
        if (!langFile.exists()) {
            plugin.getLogger().warning("Language file " + currentLanguage + ".yml not found, using default language.");
            currentLanguage = defaultLanguage;
            langFile = new File(langDir, defaultLanguage + ".yml");
            if (!langFile.exists()) {
                saveDefaultLanguages(langDir);
            }
        }
        
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }
    
    private void saveDefaultLanguages(File langDir) {
        saveResource(langDir, "en.yml");
        saveResource(langDir, "es.yml");
    }
    
    private void saveResource(File langDir, String fileName) {
        try (InputStream in = plugin.getResource("lang/" + fileName)) {
            if (in != null) {
                Files.copy(in, new File(langDir, fileName).toPath());
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save " + fileName + ": " + e.getMessage());
        }
    }
    
    public String getMessage(String path) {
        return getMessage(path, new HashMap<>());
    }
    
    public String getMessage(String path, Map<String, String> placeholders) {
        String message = langConfig.getString(path);
        if (message == null) {
            return "Missing message: " + path;
        }
        
        // Reemplazar prefijos especiales
        message = message.replace("%prefix%", langConfig.getString("prefix", "&6[Rain Tools]"));
        message = message.replace("%error-prefix%", langConfig.getString("error-prefix", "&c[Rain Tools]"));
        message = message.replace("%success-prefix%", langConfig.getString("success-prefix", "&a[Rain Tools]"));
        message = message.replace("%info-prefix%", langConfig.getString("info-prefix", "&e[Rain Tools]"));
        
        // Reemplazar placeholders personalizados
        Matcher matcher = placeholderPattern.matcher(message);
        StringBuffer buffer = new StringBuffer();
        
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String replacement = placeholders.getOrDefault(placeholder, matcher.group());
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(buffer);
        
        // Convertir códigos de color
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
    
    public void setLanguage(String language) {
        this.currentLanguage = language;
        loadLanguage();
    }
    
    public String getCurrentLanguage() {
        return currentLanguage;
    }
} 