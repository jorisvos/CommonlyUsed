package nl.jorisvos.commonlyused.listeners;

import nl.jorisvos.commonlyused.CommonlyUsed;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class BackListener implements Listener {
    private final CommonlyUsed plugin;

    public BackListener(CommonlyUsed plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Location deathLocation = event.getEntity().getLocation();
        plugin.setLastLocation(event.getEntity().getUniqueId(), deathLocation);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!event.isCancelled() && event.getCause() == PlayerTeleportEvent.TeleportCause.COMMAND || event.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN) {
            Location from = event.getFrom();
            plugin.setLastLocation(event.getPlayer().getUniqueId(), from);
        }
    }
}
