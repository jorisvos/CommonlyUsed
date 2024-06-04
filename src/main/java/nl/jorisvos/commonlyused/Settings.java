package nl.jorisvos.commonlyused;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Settings {
    private final CommonlyUsed plugin;

    private final File configFile;
    private final File homeConfigFile;
    private final File warpConfigFile;
    private final File nicknameConfigFile;

    private FileConfiguration config;
    private FileConfiguration homeConfig;
    private FileConfiguration warpConfig;
    private FileConfiguration nicknameConfig;

    private int slimeChunkSearchRadius = 50;
    private boolean saveDeathAsLastLocation = true;
    // cooldowns
    private int teleportCooldown = 15;
    private int backCooldown = 30;
    private int homeCooldown = 60;
    private int spawnCooldown = 120;
    private int warpCooldown = 120;
    private int tpaCooldown = 120;
    private int tpacceptCooldown = 60;
    // delays
    private int backDelay = 3;
    private int homeDelay = 3;
    private int spawnDelay = 3;
    private int warpDelay = 3;
    private int tpaDelay = 3;

    // home locations
    private final Map<UUID, Location> homeLocations = new HashMap<>();
    // warp locations
    private final Map<String, Location> warpLocations = new HashMap<>();
    // nicknames
    private final Map<UUID, String> nicknames = new HashMap<>();

    public Settings(final CommonlyUsed plugin) {
        this.plugin = plugin;

        // Check if plugins DataFolder exists, if not create it
        if (!plugin.getDataFolder().exists()) {
            //noinspection ResultOfMethodCallIgnored
            plugin.getDataFolder().mkdirs();
        }

        configFile = new File(plugin.getDataFolder(), "config.yml");
        homeConfigFile = new File(plugin.getDataFolder(), "homes.yml");
        warpConfigFile = new File(plugin.getDataFolder(), "warps.yml");
        nicknameConfigFile = new File(plugin.getDataFolder(), "nicknames.yml");

        loadConfig();
        loadHomeConfig();
        loadWarpConfig();
        loadNicknameConfig();
    }

    private void loadConfig() {
        // Save the default configuration if it does not exist
        plugin.saveDefaultConfig();
        // Ensure the default values are copied to the configuration (without overriding)
        plugin.getConfig().options().copyDefaults(true);
        // Save the config with the defaults to the configuration
        plugin.saveConfig();

        // Initialize config file
        config = YamlConfiguration.loadConfiguration(configFile);
        reloadConfig();
    }
    private void loadHomeConfig() {
        // Initialize config file
        if (!homeConfigFile.exists()) {
            plugin.saveResource("homes.yml", false);
        }
        homeConfig = YamlConfiguration.loadConfiguration(homeConfigFile);
        reloadHomeConfig();
    }
    private void loadWarpConfig() {
        // Initialize config file
        if (!warpConfigFile.exists()) {
            plugin.saveResource("warps.yml", false);
        }
        warpConfig = YamlConfiguration.loadConfiguration(warpConfigFile);
        reloadWarpConfig();
    }
    private void loadNicknameConfig() {
        // Initialize config file
        if (!nicknameConfigFile.exists()) {
            plugin.saveResource("nicknames.yml", false);
        }
        nicknameConfig = YamlConfiguration.loadConfiguration(nicknameConfigFile);
        reloadNicknameConfig();
    }

    public void reloadConfig() {
        slimeChunkSearchRadius = config.getInt("slimeChunkSearchRadius");
        saveDeathAsLastLocation = config.getBoolean("saveDeathAsLastLocation");
        // cooldowns
        teleportCooldown = config.getInt("cooldowns.teleport");
        backCooldown = config.getInt("cooldowns.back");
        homeCooldown = config.getInt("cooldowns.home");
        spawnCooldown = config.getInt("cooldowns.spawn");
        warpCooldown = config.getInt("cooldowns.warp");
        tpaCooldown = config.getInt("cooldowns.tpa");
        tpacceptCooldown = config.getInt("cooldowns.tpaccept");
        // delays
        backDelay = config.getInt("delays.back");
        homeDelay = config.getInt("delays.home");
        spawnDelay = config.getInt("delays.spawn");
        warpDelay = config.getInt("delays.warp");
        tpaDelay = config.getInt("delays.tpa");
    }
    public void reloadHomeConfig() {
        for (String key : homeConfig.getKeys(false)) {
            UUID playerId = UUID.fromString(key);
            Location location = getLocationFromConfig(homeConfig, key);
            homeLocations.put(playerId, location);
        }
    }
    public void reloadWarpConfig() {
        for (String warpName : warpConfig.getKeys(false)) {
            Location location = getLocationFromConfig(warpConfig, warpName);
            warpLocations.put(warpName, location);
        }
    }
    public void reloadNicknameConfig() {
        for (String playerId : nicknameConfig.getKeys(false)) {
            nicknames.put(UUID.fromString(playerId), nicknameConfig.getString(playerId));
        }
    }

    public void saveHomeConfig() {
        homeConfig = new YamlConfiguration();
        for (UUID playerId : homeLocations.keySet()) {
            setLocationToConfig(homeConfig, playerId.toString(), homeLocations.get(playerId));
        }
        try {
            homeConfig.save(homeConfigFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save home locations to file.");
        }
    }
    public void saveWarpConfig() {
        warpConfig = new YamlConfiguration();
        for (String warpName : warpLocations.keySet()) {
            setLocationToConfig(warpConfig, warpName, warpLocations.get(warpName));
        }
        try {
            warpConfig.save(warpConfigFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save warp locations to file.");
        }
    }
    public void saveNicknameConfig() {
        nicknameConfig = new YamlConfiguration();
        for (UUID playerId : nicknames.keySet()) {
            nicknameConfig.set(playerId.toString(), nicknames.get(playerId));
        }
        try {
            nicknameConfig.save(nicknameConfigFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save nicknames to file.");
        }
    }

    private Location getLocationFromConfig(FileConfiguration config, String locationKey) {
        String worldName = config.getString(locationKey + ".world");
        double x = config.getDouble(locationKey + ".x");
        double y = config.getDouble(locationKey + ".y");
        double z = config.getDouble(locationKey + ".z");
        float yaw = (float) config.getDouble(locationKey + ".yaw");
        float pitch = (float) config.getDouble(locationKey + ".pitch");
        return new Location(plugin.getServer().getWorld(worldName), x, y, z, yaw, pitch);
    }
    private void setLocationToConfig(FileConfiguration config, String locationKey, Location location) {
        config.set(locationKey + ".world", location.getWorld().getName());
        config.set(locationKey + ".x", location.getX());
        config.set(locationKey + ".y", location.getY());
        config.set(locationKey + ".z", location.getZ());
        config.set(locationKey + ".yaw", location.getYaw());
        config.set(locationKey + ".pitch", location.getPitch());
    }

    // config.yml
    public int getSlimeChunkSearchRadius() { return slimeChunkSearchRadius; }
    public boolean getSaveDeathAsLastLocation() { return saveDeathAsLastLocation; }

    public int getTeleportCooldown() { return teleportCooldown; }
    public int getBackCooldown() { return backCooldown; }
    public int getHomeCooldown() { return homeCooldown; }
    public int getSpawnCooldown() { return spawnCooldown; }
    public int getWarpCooldown() { return warpCooldown; }
    public int getTpaCooldown() { return tpaCooldown; }
    public int getTpacceptCooldown() { return tpacceptCooldown; }

    public int getBackDelay() { return backDelay; }
    public int getHomeDelay() { return homeDelay; }
    public int getSpawnDelay() { return spawnDelay; }
    public int getWarpDelay() { return warpDelay; }
    public int getTpaDelay() { return tpaDelay; }

    // homes.yml
    public void addHome(UUID playerId, Location homeLocation) { homeLocations.put(playerId, homeLocation); }
    public boolean hasHome(UUID playerId) { return homeLocations.containsKey(playerId); }
    public Location getHome(UUID playerId) { return homeLocations.get(playerId); }
    public void removeHome(UUID playerId) { homeLocations.remove(playerId); }
    // warps.yml
    public void addWarp(String warpName, Location warpLocation) { warpLocations.put(warpName, warpLocation); }
    public boolean isWarp(String warpName) { return warpLocations.containsKey(warpName); }
    public Location getWarp(String warpName) { return warpLocations.get(warpName); }
    public void removeWarp(String warpName) { warpLocations.remove(warpName); }
    public List<String> warpNames() { return new ArrayList<>(warpLocations.keySet()); }
    public boolean isWarpEmpty() { return warpLocations.isEmpty(); }
    // nicknames.yml
    public void addNickname(UUID playerId, String nickname) { nicknames.put(playerId, nickname); }
    public boolean hasNickname(UUID playerId) { return nicknames.containsKey(playerId); }
    public String getNickname(UUID playerId) { return nicknames.get(playerId); }
    public void removeNickname(UUID playerId) { nicknames.remove(playerId); }
}
