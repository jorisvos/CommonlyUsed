package nl.jorisvos.commonlyused.listeners;

import nl.jorisvos.commonlyused.CommonlyUsed;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerListener implements Listener {
    private final CommonlyUsed plugin;

    public PlayerListener(CommonlyUsed plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (plugin.getSettings().getSaveDeathAsLastLocation()) {
            Location deathLocation = event.getEntity().getLocation();
            plugin.setLastLocation(event.getEntity().getUniqueId(), deathLocation);
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!event.isCancelled() && event.getCause() == PlayerTeleportEvent.TeleportCause.COMMAND || event.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN) {
            Location from = event.getFrom();
            plugin.setLastLocation(event.getPlayer().getUniqueId(), from);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player.getAllowFlight() && player.getGameMode() == GameMode.SURVIVAL) {
            Location groundLocation = player.getLocation();
            groundLocation.setY(player.getWorld().getHighestBlockYAt(groundLocation)+1);
            player.teleport(groundLocation);
            player.setAllowFlight(false);
            player.setFlying(false);
            Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin(
                    "CommonlyUsed"),
                    () -> player.sendMessage(plugin.prefix+"§cFly mode disabled due to logout."),
                    1L);
        }
    }
}
