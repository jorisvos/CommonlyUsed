package nl.jorisvos.commonlyused.commands;

import nl.jorisvos.commonlyused.CommonlyUsed;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HomeCommands implements CommandExecutor {
    private final CommonlyUsed plugin;
    private final Map<UUID, Long> setHomeCooldowns = new HashMap<>();
    private final Map<UUID, Long> homeCooldowns = new HashMap<>();

    public HomeCommands(CommonlyUsed plugin) {
        this.plugin = plugin;
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

        plugin.getSettings().addHome(player.getUniqueId(), player.getLocation());
        plugin.getSettings().saveHomeConfig();
        setHomeCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        player.sendMessage(plugin.prefix+"§aHome location set.");
    }

    private void teleportHome(Player player) {
        if (!plugin.getSettings().hasHome(player.getUniqueId())) {
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

        Location homeLocation = plugin.getSettings().getHome(player.getUniqueId());
        homeCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        plugin.teleportAfterDelay(player, homeLocation, plugin.getSettings().getHomeDelay(), "Teleported home.");
    }

    private void deleteHome(Player player) {
        if (!plugin.getSettings().hasHome(player.getUniqueId())) {
            player.sendMessage(plugin.prefix + "§cYou have not set a home location yet.");
            return;
        }

        plugin.getSettings().removeHome(player.getUniqueId());
        plugin.getSettings().saveHomeConfig();
        player.sendMessage(plugin.prefix+"§aHome location removed.");
    }
}