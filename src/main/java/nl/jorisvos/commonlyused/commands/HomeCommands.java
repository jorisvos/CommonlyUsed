package nl.jorisvos.commonlyused.commands;

import nl.jorisvos.commonlyused.CommonlyUsed;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HomeCommands implements CommandExecutor {
    private final CommonlyUsed plugin;
    private final Map<UUID, Location> homeLocations = new HashMap<>();
    private final Map<UUID, Long> setHomeCooldowns = new HashMap<>();
    private final Map<UUID, Long> homeCooldowns = new HashMap<>();
    private final File configFile;
    private FileConfiguration config;

    public HomeCommands(CommonlyUsed plugin) {
        this.plugin = plugin;

        // Initialize config file
        configFile = new File(plugin.getDataFolder(), "homes.yml");
        if (!configFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            configFile.getParentFile().mkdirs();
            plugin.saveResource("homes.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        // Load home locations from the config file
        loadHomeLocations();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.prefix+"§cOnly players can execute this command.");
            return true;
        }

        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("sethome")) {
            setHome(player);
        } else if (command.getName().equalsIgnoreCase("home")) {
            teleportHome(player);
        } else if (command.getName().equalsIgnoreCase("delhome")) {
            deleteHome(player);
        }

        return true;
    }

    private void setHome(Player player) {
        if (setHomeCooldowns.containsKey(player.getUniqueId()) && System.currentTimeMillis() - setHomeCooldowns.get(player.getUniqueId()) < 300000) {
            long remainingTime = 300000 - (System.currentTimeMillis() - setHomeCooldowns.get(player.getUniqueId()));
            player.sendMessage(plugin.prefix+"§cYou must wait §6" + (remainingTime / 1000) + " §cseconds before setting home again.");
            return;
        }

        homeLocations.put(player.getUniqueId(), player.getLocation());
        saveHomeLocations();
        setHomeCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        player.sendMessage(plugin.prefix+"§aHome location set.");
    }

    private void teleportHome(Player player) {
        if (!homeLocations.containsKey(player.getUniqueId())) {
            player.sendMessage(plugin.prefix + "§cYou have not set a home location yet.");
            return;
        }

        if (plugin.isPlayerOnTeleportCooldown(player.getUniqueId())) {
            player.sendMessage(plugin.getTeleportCooldownMessage(player.getUniqueId()));
            return;
        } else if (homeCooldowns.containsKey(player.getUniqueId()) && System.currentTimeMillis() - homeCooldowns.get(player.getUniqueId()) < (plugin.getSettings().getHomeCooldown() * 1000L)) {
            long remainingTime = (plugin.getSettings().getHomeCooldown() * 1000L) - (System.currentTimeMillis() - homeCooldowns.get(player.getUniqueId()));
            player.sendMessage(plugin.prefix + "§cYou must wait §6" + (remainingTime / 1000) + " §cseconds before you can teleport to home again.");
            return;
        }

        Location homeLocation = homeLocations.get(player.getUniqueId());
        homeCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        plugin.addTeleportCooldown(player.getUniqueId());
        plugin.teleportAfterDelay(player, homeLocation, plugin.getSettings().getHomeDelay(), "Teleported home.");
    }

    private void deleteHome(Player player) {
        if (!homeLocations.containsKey(player.getUniqueId())) {
            player.sendMessage(plugin.prefix + "§cYou have not set a home location yet.");
            return;
        }

        homeLocations.remove(player.getUniqueId());
        saveHomeLocations();
        player.sendMessage(plugin.prefix+"§aHome location removed.");
    }

    private void loadHomeLocations() {
        for (String key : config.getKeys(false)) {
            UUID playerId = UUID.fromString(key);
            String worldName = config.getString(key + ".world");
            double x = config.getDouble(key + ".x");
            double y = config.getDouble(key + ".y");
            double z = config.getDouble(key + ".z");
            float yaw = (float) config.getDouble(key + ".yaw");
            float pitch = (float) config.getDouble(key + ".pitch");
            Location location = new Location(plugin.getServer().getWorld(worldName), x, y, z, yaw, pitch);
            homeLocations.put(playerId, location);
        }
    }

    private void saveHomeLocations() {
        config = new YamlConfiguration();
        for (UUID playerId : homeLocations.keySet()) {
            Location location = homeLocations.get(playerId);
            config.set(playerId + ".world", location.getWorld().getName());
            config.set(playerId + ".x", location.getX());
            config.set(playerId + ".y", location.getY());
            config.set(playerId + ".z", location.getZ());
            config.set(playerId + ".yaw", location.getYaw());
            config.set(playerId + ".pitch", location.getPitch());
        }
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save home locations to file.");
        }
    }
}