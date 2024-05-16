package nl.jorisvos.commonlyused;

import nl.jorisvos.commonlyused.commands.BackCommand;
import nl.jorisvos.commonlyused.commands.HomeCommands;
import nl.jorisvos.commonlyused.commands.SpawnCommands;
import nl.jorisvos.commonlyused.commands.WarpCommands;
import nl.jorisvos.commonlyused.listeners.BackListener;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CommonlyUsed extends JavaPlugin {
    public final String prefix = "§e[§r§bCommonly§r§9§lU§r§e] §r";
    private final Map<UUID, Location> lastLocations = new HashMap<>();
    private final Map<UUID, Long> teleportCooldowns = new HashMap<>();
    private final int teleportCooldown = 15000;

    @Override
    public void onEnable() {
        //TODO: add fancy startup message to console!

        // Register commands
        getCommand("back").setExecutor(new BackCommand(this));
        // home commands
        HomeCommands homeCommands = new HomeCommands(this);
        getCommand("sethome").setExecutor(homeCommands);
        getCommand("home").setExecutor(homeCommands);
        getCommand("delhome").setExecutor(homeCommands);
        // spawn commands
        SpawnCommands spawnCommands = new SpawnCommands(this);
        getCommand("setspawn").setExecutor(spawnCommands);
        getCommand("spawn").setExecutor(spawnCommands);
        // warp commands
        WarpCommands warpCommands = new WarpCommands(this);
        getCommand("setwarp").setExecutor(warpCommands);
        getCommand("delwarp").setExecutor(warpCommands);
        getCommand("warp").setExecutor(warpCommands);
        getCommand("warplist").setExecutor(warpCommands);

        // Register listeners
        getServer().getPluginManager().registerEvents(new BackListener(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public long getTeleportCooldown(UUID uniquePlayerId) {
        if (teleportCooldowns.containsKey(uniquePlayerId)) {
            return teleportCooldowns.get(uniquePlayerId);
        }
        return 0;
    }
    public boolean isPlayerOnTeleportCooldown(UUID uniquePlayerId) {
        return teleportCooldowns.containsKey(uniquePlayerId) && System.currentTimeMillis() - teleportCooldowns.get(uniquePlayerId) < teleportCooldown;
    }
    public String getTeleportCooldownMessage(UUID uniquePlayerId) {
        long remainingTime = teleportCooldown - (System.currentTimeMillis() - teleportCooldowns.get(uniquePlayerId));
        return prefix+"§cYou must wait §6" + (remainingTime / 1000) + " §cseconds before you can teleport using any command again.";
    }
    public void addTeleportCooldown(UUID uniquePlayerId) {
        teleportCooldowns.put(uniquePlayerId, System.currentTimeMillis());
    }

    public void teleportAfterDelay(Player player, Location location, int delayInSeconds, String completionMessage) {
        new BukkitRunnable() {
            int count = 3;
            @Override
            public void run() {
                if (count > 0) {
                    player.sendMessage(prefix+"§eTeleporting in §6" + count + " §eseconds...");
                    count--;
                } else {
                    this.cancel();
                    teleportCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                    player.teleport(location);
                    player.sendMessage(prefix+"§a"+completionMessage);
                }
            }
        }.runTaskTimer(this, 0, 20); // Run task every second (20 ticks)
    }

    public boolean hasLastLocation(UUID playerId) {
        return lastLocations.containsKey(playerId);
    }

    public Location getLastLocation(UUID playerId) {
        return lastLocations.get(playerId);
    }

    public void setLastLocation(UUID playerId, Location location) {
        lastLocations.put(playerId, location);
    }
}
