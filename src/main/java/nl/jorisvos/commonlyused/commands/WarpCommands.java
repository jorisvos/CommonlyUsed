package nl.jorisvos.commonlyused.commands;

import nl.jorisvos.commonlyused.CommonlyUsed;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class WarpCommands implements CommandExecutor, TabCompleter {
    private final CommonlyUsed plugin;
    private final Map<String, Location> warpLocations = new HashMap<>();
    private final Map<UUID, Long> warpCooldowns = new HashMap<>();
    private final File configFile;
    private FileConfiguration config;

    public WarpCommands(CommonlyUsed plugin) {
        this.plugin = plugin;

        // Initialize config file
        configFile = new File(plugin.getDataFolder(), "warps.yml");
        if (!configFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            configFile.getParentFile().mkdirs();
            plugin.saveResource("warps.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        // Load warp locations from the config file
        loadWarpLocations();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.prefix+"§cOnly players can execute this command.");
            return true;
        }

        Player player = (Player) sender;
        if (command.getName().equalsIgnoreCase("warplist")) {
            warpList(player);
        } else if (args.length != 1) {
            player.sendMessage(plugin.prefix+"§cYou didn't provide a valid argument. You use the command like this: /<command> [warpname]");
        } else if (command.getName().equalsIgnoreCase("setwarp")) {
            setWarp(player, args[0]);
        } else if (command.getName().equalsIgnoreCase("delwarp")) {
            delWarp(player, args[0]);
        } else if (command.getName().equalsIgnoreCase("warp")) {
            warp(player, args[0]);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> warpNames = new ArrayList<>(warpLocations.keySet());
            return warpNames.stream().filter(name -> name.startsWith(args[0])).collect(Collectors.toList());
        }
        return null;
    }

    private void setWarp(Player player, String warpName) {
        if (!player.hasPermission("commonlyused.command.setwarp")) {
            player.sendMessage(plugin.prefix+"§cYou don't have permission to use this command.");
            return;
        }

        if (warpName.isBlank()) {
            player.sendMessage(plugin.prefix+"§cYou can't leave the warp name empty.");
            return;
        }

        warpLocations.put(warpName, player.getLocation());
        saveWarpLocations();
        player.sendMessage(plugin.prefix+"§aWarp[§6"+warpName+"§a] location set.");
    }

    private void delWarp(Player player, String warpName) {
        if (!player.hasPermission("commonlyused.command.delwarp")) {
            player.sendMessage(plugin.prefix+"§cYou don't have permission to use this command.");
            return;
        }

        if (warpName.isBlank()) {
            player.sendMessage(plugin.prefix+"§cYou can't leave the warp name empty.");
            return;
        }

        warpLocations.remove(warpName);
        saveWarpLocations();
        player.sendMessage(plugin.prefix+"§aWarp[§6"+warpName+"§a] location removed.");
    }

    private void warp(Player player, String warpName) {
        if (!player.hasPermission("commonlyused.command.warp")) {
            player.sendMessage(plugin.prefix+"§cYou don't have permission to use this command.");
            return;
        }

        if (warpName.isBlank()) {
            player.sendMessage(plugin.prefix+"§cYou can't leave the warp name empty.");
            return;
        } else if (!warpLocations.containsKey(warpName)) {
            player.sendMessage(plugin.prefix+"§cThat's not a valid warp name!");
            return;
        }

        if (plugin.isPlayerOnTeleportCooldown(player.getUniqueId())) {
            player.sendMessage(plugin.getTeleportCooldownMessage(player.getUniqueId()));
            return;
        } else if (warpCooldowns.containsKey(player.getUniqueId()) && System.currentTimeMillis() - warpCooldowns.get(player.getUniqueId()) < (plugin.getSettings().getWarpCooldown() * 1000L)) {
            long remainingTime = (plugin.getSettings().getWarpCooldown() * 1000L) - (System.currentTimeMillis() - warpCooldowns.get(player.getUniqueId()));
            player.sendMessage(plugin.prefix + "§cYou must wait §6" + (remainingTime / 1000) + " §cseconds before you can warp again.");
            return;
        }

        Location warpLocation = warpLocations.get(warpName);
        warpCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        plugin.addTeleportCooldown(player.getUniqueId());
        plugin.teleportAfterDelay(player, warpLocation, plugin.getSettings().getWarpDelay(), "Teleported to warp[§6"+warpName+"§a].");
    }

    private void warpList(Player player) {
        if (!player.hasPermission("commonlyused.command.warplist")) {
            player.sendMessage(plugin.prefix+"§cYou don't have permission to use this command.");
            return;
        }

        if (warpLocations.isEmpty()) {
            player.sendMessage(plugin.prefix+"§cThere are no warps.");
            return;
        }

        StringBuilder warpList = new StringBuilder(plugin.prefix+"§aWarp list:");
        for (String warpName : warpLocations.keySet()) {
            Location location = warpLocations.get(warpName);
            warpList.append("\n§f - §6").append(warpName).append("[§d").append(location.getWorld().getName()).append("§6]");
        }
        player.sendMessage(warpList.toString());
    }

    private void loadWarpLocations() {
        for (String warpName : config.getKeys(false)) {
            String worldName = config.getString(warpName + ".world");
            double x = config.getDouble(warpName + ".x");
            double y = config.getDouble(warpName + ".y");
            double z = config.getDouble(warpName + ".z");
            float yaw = (float) config.getDouble(warpName + ".yaw");
            float pitch = (float) config.getDouble(warpName + ".pitch");
            Location location = new Location(plugin.getServer().getWorld(worldName), x, y, z, yaw, pitch);
            warpLocations.put(warpName, location);
        }
    }

    private void saveWarpLocations() {
        config = new YamlConfiguration();
        for (String warpName : warpLocations.keySet()) {
            Location location = warpLocations.get(warpName);
            config.set(warpName + ".world", location.getWorld().getName());
            config.set(warpName + ".x", location.getX());
            config.set(warpName + ".y", location.getY());
            config.set(warpName + ".z", location.getZ());
            config.set(warpName + ".yaw", location.getYaw());
            config.set(warpName + ".pitch", location.getPitch());
        }
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save warp locations to file.");
        }
    }
}
