package nl.jorisvos.commonlyused;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Settings {
    private final FileConfiguration config;

    private int slimeChunkSearchRadius = 50;
    private boolean saveDeathAsLastLocation = true;
    // cooldowns
    private int teleportCooldown = 15;
    private int backCooldown = 30;
    private int homeCooldown = 60;
    private int spawnCooldown = 120;
    private int warpCooldown = 120;
    // delays
    private int backDelay = 3;
    private int homeDelay = 3;
    private int spawnDelay = 3;
    private int warpDelay = 3;

    public Settings(final CommonlyUsed plugin) {
        // Initialize config file
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            configFile.getParentFile().mkdirs();
            plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        loadConfig();
    }

    private void loadConfig() {
        slimeChunkSearchRadius = config.getInt("slimeChunkSearchRadius");
        saveDeathAsLastLocation = config.getBoolean("saveDeathAsLastLocation");
        // cooldowns
        teleportCooldown = config.getInt("cooldowns.teleport");
        backCooldown = config.getInt("cooldowns.back");
        homeCooldown = config.getInt("cooldowns.home");
        spawnCooldown = config.getInt("cooldowns.spawn");
        warpCooldown = config.getInt("cooldowns.warp");
        // delays
        backDelay = config.getInt("delays.back");
        homeDelay = config.getInt("delays.home");
        spawnDelay = config.getInt("delays.spawn");
        warpDelay = config.getInt("delays.warp");
    }

    public int getSlimeChunkSearchRadius() { return slimeChunkSearchRadius; }
    public boolean getSaveDeathAsLastLocation() { return saveDeathAsLastLocation; }

    public int getTeleportCooldown() { return teleportCooldown; }
    public int getBackCooldown() { return backCooldown; }
    public int getHomeCooldown() { return homeCooldown; }
    public int getSpawnCooldown() { return spawnCooldown; }
    public int getWarpCooldown() { return warpCooldown; }

    public int getBackDelay() { return backDelay; }
    public int getHomeDelay() { return homeDelay; }
    public int getSpawnDelay() { return spawnDelay; }
    public int getWarpDelay() { return warpDelay; }

}
