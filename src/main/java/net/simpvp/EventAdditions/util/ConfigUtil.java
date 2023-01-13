package net.simpvp.EventAdditions.util;

import net.simpvp.EventAdditions.EventAdditions;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class ConfigUtil {
    private final File file;
    private final YamlConfiguration config;

    public ConfigUtil(Plugin plugin, String configFileName) {
        this.file = new File(EventAdditions.instance.getDataFolder(), configFileName);
        if (!this.file.exists()) {
            EventAdditions.instance.getLogger().warning("Creating a new " + configFileName + " file");
            plugin.saveResource(configFileName, false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public boolean save() {
        try {
            this.config.save(this.file);
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public File getFile() {
        return this.file;
    }

    public YamlConfiguration getConfig() {
        return this.config;
    }



}
