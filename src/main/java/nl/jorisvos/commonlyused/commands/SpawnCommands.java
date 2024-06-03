package nl.jorisvos.commonlyused.commands;

import nl.jorisvos.commonlyused.CommonlyUsed;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.stream.Collectors;

public class SpawnCommands implements CommandExecutor, TabCompleter {
    private final CommonlyUsed plugin;
    private final FileConfiguration config;
    private final Map<UUID, Long> spawnCooldowns = new HashMap<>();

    public SpawnCommands(CommonlyUsed plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can execute this command.");
            return true;
        }

        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("setspawn")) {
            setSpawn(player, args);
        } else if (command.getName().equalsIgnoreCase("spawn")) {
            spawn(player);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> playerNames = new ArrayList<>();
            playerNames.add("<x> <y> <z>");
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerNames.add(player.getName());
            }
            return playerNames.stream().filter(name -> name.startsWith(args[0])).collect(Collectors.toList());
        } else if (args.length > 1 && args.length <= 3) {
            List<String> playerNames = new ArrayList<>();
            playerNames.add("<x> <y> <z>");
            return playerNames;
        }
        return null;
    }

    private void setSpawn(Player player, String[] args) {
        if (!player.hasPermission("commonlyused.command.setspawn")) {
            player.sendMessage(plugin.prefix+"§cYou don't have permission to use this command.");
            return;
        }

        if (args.length == 0) {
            setWorldSpawn(player, player.getLocation());
        } else if (args.length == 1) {
            if (!player.hasPermission("commonlyused.command.setspawn.player")) {
                player.sendMessage(plugin.prefix+"§cYou don't have permission to set the spawn to another player's location.");
                return;
            }
            Player target = plugin.getServer().getPlayerExact(args[0]);
            if (target == null) {
                player.sendMessage(plugin.prefix+"§cPlayer not found.");
                return;
            }

            setWorldSpawn(player, target.getLocation(), target.getWorld());
        } else if (args.length == 3) {
            if (!player.hasPermission("commonlyused.command.setspawn.custom")) {
                player.sendMessage(plugin.prefix+"§cYou don't have permission to set a custom spawn location.");
                return;
            }

            double x, y, z;
            try {
                x = Double.parseDouble(args[0]);
                y = Double.parseDouble(args[1]);
                z = Double.parseDouble(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage(plugin.prefix+"§cInvalid coordinates provided.");
                return;
            }

            Location customSpawn = new Location(player.getWorld(), x, y, z);
            setWorldSpawn(player, customSpawn);
        } else {
            player.sendMessage(plugin.prefix+"§cUsage: /setspawn [<player>/<x> <y> <z>]");
        }
    }

    private void spawn(Player player) {
        if (!player.hasPermission("commonlyused.command.spawn")) {
            player.sendMessage(plugin.prefix+"§cYou don't have permission to use this command.");
            return;
        }

        if (plugin.isPlayerOnTeleportCooldown(player.getUniqueId())) {
            player.sendMessage(plugin.getTeleportCooldownMessage(player.getUniqueId()));
            return;
        } else if (spawnCooldowns.containsKey(player.getUniqueId()) && System.currentTimeMillis() - spawnCooldowns.get(player.getUniqueId()) < (plugin.getSettings().getSpawnCooldown()*1000L)) {
            long remainingTime = (plugin.getSettings().getSpawnCooldown()*1000L) - (System.currentTimeMillis() - spawnCooldowns.get(player.getUniqueId()));
            player.sendMessage(plugin.prefix+"§cYou must wait §6" + (remainingTime / 1000) + " §cseconds before you can teleport to spawn again.");
            return;
        }

        spawnCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        plugin.addTeleportCooldown(player.getUniqueId());
        World world = player.getWorld();
        Location spawnLocation = getWorldSpawn(world);
        if (spawnLocation == null) {
            player.sendMessage(plugin.prefix+"§cNo spawn set for this world. Teleporting to world spawn.");
            spawnLocation = world.getSpawnLocation();
        }
        plugin.teleportAfterDelay(player, spawnLocation, plugin.getSettings().getSpawnDelay(), "Teleported to spawn.");
    }

    private void setWorldSpawn(Player player, Location location) {
        setWorldSpawn(player, location, player.getWorld());
    }
    private void setWorldSpawn(Player player, Location location, World world) {
        config.set("spawns." + world.getName() + ".world", location.getWorld().getName());
        config.set("spawns." + world.getName() + ".x", location.getX());
        config.set("spawns." + world.getName() + ".y", location.getY());
        config.set("spawns." + world.getName() + ".z", location.getZ());
        config.set("spawns." + world.getName() + ".yaw", location.getYaw());
        config.set("spawns." + world.getName() + ".pitch", location.getPitch());
        plugin.saveConfig();
        player.sendMessage(plugin.prefix+"§aSpawn set.");
    }

    private Location getWorldSpawn(World world) {
        if (config.contains("spawns." + world.getName())) {
            String worldName = config.getString("spawns." + world.getName() + ".world");
            double x = config.getDouble("spawns." + world.getName() + ".x");
            double y = config.getDouble("spawns." + world.getName() + ".y");
            double z = config.getDouble("spawns." + world.getName() + ".z");
            float yaw = (float) config.getDouble("spawns." + world.getName() + ".yaw");
            float pitch = (float) config.getDouble("spawns." + world.getName() + ".pitch");
            return new Location(plugin.getServer().getWorld(worldName), x, y, z, yaw, pitch);
        }
        return null;
    }
}