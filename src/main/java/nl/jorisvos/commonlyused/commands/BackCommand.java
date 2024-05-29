package nl.jorisvos.commonlyused.commands;

import nl.jorisvos.commonlyused.CommonlyUsed;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BackCommand implements CommandExecutor {
    private final CommonlyUsed plugin;
    private final Map<UUID, Long> backCooldowns = new HashMap<>();

    public BackCommand(CommonlyUsed plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can execute this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!plugin.hasLastLocation(player.getUniqueId())) {
            player.sendMessage(plugin.prefix + "§cYou have no last location to teleport back to.");
            return true;
        }

        if (plugin.isPlayerOnTeleportCooldown(player.getUniqueId())) {
            player.sendMessage(plugin.getTeleportCooldownMessage(player.getUniqueId()));
            return true;
        } else if (backCooldowns.containsKey(player.getUniqueId()) && System.currentTimeMillis() - backCooldowns.get(player.getUniqueId()) < (plugin.getSettings().getBackCooldown() * 1000L)) {
            long remainingTime = (plugin.getSettings().getBackCooldown() * 1000L) - (System.currentTimeMillis() - backCooldowns.get(player.getUniqueId()));
            player.sendMessage(plugin.prefix+"§cYou must wait §6" + (remainingTime / 1000) + " §cseconds before you can teleport to your last location again.");
            return true;
        }

        backCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        plugin.addTeleportCooldown(player.getUniqueId());
        plugin.teleportAfterDelay(player, plugin.getLastLocation(player.getUniqueId()), plugin.getSettings().getBackDelay(), "Teleported to your last location.");
        return true;
    }
}