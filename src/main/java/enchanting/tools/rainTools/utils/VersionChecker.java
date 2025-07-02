package enchanting.tools.rainTools.utils;

import enchanting.tools.rainTools.RainTools;
import org.bukkit.Bukkit;

public class VersionChecker {
    private final RainTools plugin;
    private final String serverVersion;
    private final int majorVersion;
    private final int minorVersion;
    private final int patchVersion;
    
    public VersionChecker(RainTools plugin) {
        this.plugin = plugin;
        this.serverVersion = Bukkit.getBukkitVersion().split("-")[0];
        String[] versionParts = serverVersion.split("\\.");
        
        this.majorVersion = Integer.parseInt(versionParts[0]);
        this.minorVersion = versionParts.length > 1 ? Integer.parseInt(versionParts[1]) : 0;
        this.patchVersion = versionParts.length > 2 ? Integer.parseInt(versionParts[2]) : 0;
    }
    
    public boolean isCompatible() {
        // Requerir mínimo 1.16.5
        return majorVersion > 1 || 
               (majorVersion == 1 && minorVersion > 16) ||
               (majorVersion == 1 && minorVersion == 16 && patchVersion >= 5);
    }
    
    public String getServerVersion() {
        return serverVersion;
    }
    
    public boolean isVersionOrHigher(int major, int minor) {
        return majorVersion > major || 
               (majorVersion == major && minorVersion >= minor);
    }
    
    public boolean isVersionOrHigher(int major, int minor, int patch) {
        return majorVersion > major || 
               (majorVersion == major && minorVersion > minor) ||
               (majorVersion == major && minorVersion == minor && patchVersion >= patch);
    }
    
    public boolean isLegacy() {
        return !isVersionOrHigher(1, 13);
    }
    
    public boolean hasNewMaterials() {
        return isVersionOrHigher(1, 13);
    }
    
    public boolean hasNewParticles() {
        return isVersionOrHigher(1, 13);
    }
    
    public boolean hasNewSounds() {
        return isVersionOrHigher(1, 13);
    }
    
    public boolean hasNewInventory() {
        return isVersionOrHigher(1, 14);
    }
    
    public boolean hasNewBiomes() {
        return isVersionOrHigher(1, 16);
    }
} 