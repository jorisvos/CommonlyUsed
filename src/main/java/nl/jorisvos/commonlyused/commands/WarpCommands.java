package nl.jorisvos.commonlyused.commands;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import nl.jorisvos.commonlyused.CommonlyUsed;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class WarpCommands implements CommandExecutor, TabCompleter {
    private final CommonlyUsed plugin;
    private final Map<UUID, Long> warpCooldowns = new HashMap<>();

    public WarpCommands(CommonlyUsed plugin) {
        this.plugin = plugin;
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
            List<String> warpNames = plugin.getSettings().warpNames();
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

        plugin.getSettings().addWarp(warpName, player.getLocation());
        plugin.getSettings().saveWarpConfig();
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

        plugin.getSettings().removeWarp(warpName);
        plugin.getSettings().saveWarpConfig();
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
        } else if (!plugin.getSettings().isWarp(warpName)) {
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

        Location warpLocation = plugin.getSettings().getWarp(warpName);
        warpCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        plugin.teleportAfterDelay(player, warpLocation, plugin.getSettings().getWarpDelay(), "Teleported to warp[§6"+warpName+"§a].");
    }

    private void warpList(Player player) {
        if (!player.hasPermission("commonlyused.command.warplist")) {
            player.sendMessage(plugin.prefix+"§cYou don't have permission to use this command.");
            return;
        }

        if (plugin.getSettings().isWarpEmpty()) {
            player.sendMessage(plugin.prefix+"§cThere are no warps.");
            return;
        }

        ComponentBuilder builder = new ComponentBuilder(plugin.prefix+"§aWarp list:");
        for (String warpName : plugin.getSettings().warpNames()) {
            Location location = plugin.getSettings().getWarp(warpName);
            String message = "§f - §6"+warpName+"[§d"+location.getWorld().getName()+"§6]";
            String command = "/warp "+warpName;
            TextComponent textComponent = plugin.getClickableMessage(message, command);
            builder.append("\n").append(textComponent);
        }
        plugin.sendClickableMessage(player, builder.create());
    }
}
