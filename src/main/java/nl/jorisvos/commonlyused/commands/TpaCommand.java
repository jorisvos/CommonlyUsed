package nl.jorisvos.commonlyused.commands;

import nl.jorisvos.commonlyused.CommonlyUsed;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TpaCommand implements CommandExecutor {
    private final CommonlyUsed plugin;

    private final Map<UUID, UUID> tpaList = new HashMap<>(); // UUID, UUID -> toPlayer, player
    private final Map<UUID, Long> tpaCooldowns = new HashMap<>();
    private final Map<UUID, Long> tpAcceptCooldowns = new HashMap<>();

    public TpaCommand(CommonlyUsed plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.prefix+"§cOnly players can execute this command.");
            return true;
        }

        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("tpa")) {
            tpa(player, args);
        } else if (command.getName().equalsIgnoreCase("tpaccept")) {
            tpaccept(player);
        } else if (command.getName().equalsIgnoreCase("tpdeny")) {
            tpdeny(player);
        } else if (command.getName().equalsIgnoreCase("tpcancel")) {
            tpcancel(player);
        }

        return true;
    }

    private void tpa(Player player, String[] args) {
        if (!player.hasPermission("commonlyused.command.tpa.tpa")) {
            player.sendMessage(plugin.prefix+"§cYou don't have permission to use this command.");
            return;
        } else if (plugin.isPlayerOnTeleportCooldown(player.getUniqueId())) {
            player.sendMessage(plugin.getTeleportCooldownMessage(player.getUniqueId()));
            return;
        } else if (tpaCooldowns.containsKey(player.getUniqueId()) && System.currentTimeMillis() - tpaCooldowns.get(player.getUniqueId()) < (plugin.getSettings().getTpaCooldown() * 1000L)) {
            long remainingTime = (plugin.getSettings().getTpaCooldown() * 1000L) - (System.currentTimeMillis() - tpaCooldowns.get(player.getUniqueId()));
            player.sendMessage(plugin.prefix + "§cYou must wait §6" + (remainingTime / 1000) + " §cseconds before you can use /tpa again.");
            return;
        } else if (args.length != 1) {
            player.sendMessage(plugin.prefix+"§cUsage: /tpa <playername>");
            return;
        }

        Player toPlayer = getPlayerByName(args[0]);
        if (toPlayer.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(plugin.prefix+"§cYou can not send a tpa request to yourself.");
            return;
        } else if (toPlayer == null) {
            player.sendMessage(plugin.prefix+"§cThat player is not online at the moment.");
            return;
        } else if (tpaList.containsKey(toPlayer.getUniqueId())) {
            player.sendMessage(plugin.prefix+"§cThat player already has a tpa request. You have to wait until he/she accepted or denied that request before another request to that player can be made.");
            return;
        } else if (tpaList.containsValue(player.getUniqueId())) {
            player.sendMessage(plugin.prefix+"§cYou already made a tpa request wait until that one is either accepted or denied. You can also cancel it yourself with §6/tpcancel§c.");
            return;
        }

        tpaList.put(toPlayer.getUniqueId(), player.getUniqueId());
        tpAcceptCooldowns.put(toPlayer.getUniqueId(), System.currentTimeMillis());
        player.sendMessage(plugin.prefix+"§aYou send a tpa request to: §6"+plugin.getDisplayName(toPlayer)+"§a. You can cancel this request by typing §e/tpcancel§a.");
        toPlayer.sendMessage(plugin.prefix+"§aPlayer §6"+plugin.getDisplayName(player)+" §ahas sent you a /tpa request. You can accept this request by typing §e/tpaccept§a. You can also deny this request by typing §e/tpdeny§a. You have §c"+plugin.getSettings().getTpacceptCooldown()+" §aseconds to do so.");
    }

    private void tpaccept(Player player) {
        if (!player.hasPermission("commonlyused.command.tpa.tpaccept")) {
            player.sendMessage(plugin.prefix+"§cYou don't have permission to use this command.");
            return;
        } else if (!tpaList.containsKey(player.getUniqueId())) {
            player.sendMessage(plugin.prefix+"§cThere are no tpa request for you at this moment. You'll get a message when someone does send you a request.");
            return;
        }

        Player fromPlayer = Bukkit.getPlayer(tpaList.get(player.getUniqueId()));
        if (fromPlayer == null) {
            player.sendMessage(plugin.prefix+"§cThe player that sent you a tpa request has gone offline. So the request will be canceled.");
            cleanTpa(null, player.getUniqueId());
            return;
        } else if (plugin.isPlayerOnTeleportCooldown(fromPlayer.getUniqueId())) {
            fromPlayer.sendMessage(plugin.prefix+"§cYour tpa request has been accepted, but it seems you teleported in between sending and accepting the tpa request. Your tpa request will now be canceled. Try again after the cooldown has expired.");
            fromPlayer.sendMessage(plugin.getTeleportCooldownMessage(player.getUniqueId()));
            player.sendMessage(plugin.prefix+"§cThe player that sent you this request has a teleport timeout. The tpa request will be canceled.");
            cleanTpa(fromPlayer.getUniqueId(), player.getUniqueId());
            return;
        } else if (tpaCooldowns.containsKey(fromPlayer.getUniqueId()) && System.currentTimeMillis() - tpaCooldowns.get(fromPlayer.getUniqueId()) < (plugin.getSettings().getTpaCooldown() * 1000L)) {
            long remainingTime = (plugin.getSettings().getTpaCooldown() * 1000L) - (System.currentTimeMillis() - tpaCooldowns.get(fromPlayer.getUniqueId()));
            fromPlayer.sendMessage(plugin.prefix+"§cYour tpa request has been accepted, but it seems you teleported in between sending and accepting the tpa request. Your tpa request will now be canceled. Try again after the cooldown has expired.");
            fromPlayer.sendMessage(plugin.prefix + "§cYou must wait §6" + (remainingTime / 1000) + " §cseconds before you can use /tpa again.");
            player.sendMessage(plugin.prefix+"§cThe player that sent you this request has a teleport timeout. The tpa request will be canceled.");
            cleanTpa(fromPlayer.getUniqueId(), player.getUniqueId());
            return;
        }

        // if tpaccept is run inside the timeout
        if (tpAcceptCooldowns.containsKey(player.getUniqueId()) && System.currentTimeMillis() - tpAcceptCooldowns.get(player.getUniqueId()) < (plugin.getSettings().getTpacceptCooldown() * 1000L)) {
            tpaCooldowns.put(fromPlayer.getUniqueId(), System.currentTimeMillis());
            plugin.teleportAfterDelay(fromPlayer, player.getLocation(), plugin.getSettings().getTpaDelay(), "Teleported to player[§6"+plugin.getDisplayName(player)+"§a] using tpa.");
            player.sendMessage(plugin.prefix+"§aTeleporting player[§6"+plugin.getDisplayName(fromPlayer)+"§a] in §6"+plugin.getSettings().getTpaDelay()+" §aseconds.");
            cleanTpa(fromPlayer.getUniqueId(), player.getUniqueId());
        } else { // too little too late
            fromPlayer.sendMessage(plugin.prefix+"§cYour tpa request has been accepted after the tpa accept cooldown so it will be canceled. You can try tpa again.");
            player.sendMessage(plugin.prefix+"§cYou waited too long with accepting the tpa request. The tpa request is canceled.");
            cleanTpa(fromPlayer.getUniqueId(), player.getUniqueId());
        }
    }

    private void tpdeny(Player player) {
        if (!player.hasPermission("commonlyused.command.tpa.tpdeny")) {
            player.sendMessage(plugin.prefix+"§cYou don't have permission to use this command.");
            return;
        } else if (!tpaList.containsKey(player.getUniqueId())) {
            player.sendMessage(plugin.prefix+"§cThere are no tpa request for you at this moment. You'll get a message when someone does send you a request.");
            return;
        }

        Player fromPlayer = Bukkit.getPlayer(tpaList.get(player.getUniqueId()));
        if (fromPlayer == null) {
            player.sendMessage(plugin.prefix+"§cThe player that sent you a tpa request has gone offline. So the request will be canceled.");
            cleanTpa(null, player.getUniqueId());
            return;
        }

        if (tpAcceptCooldowns.containsKey(player.getUniqueId()) || tpaList.containsKey(player.getUniqueId())) {
            cleanTpa(fromPlayer.getUniqueId(), player.getUniqueId());
        }
        player.sendMessage(plugin.prefix+"§eYou denied the tpa request from player[§6"+plugin.getDisplayName(fromPlayer)+"§e].");
        fromPlayer.sendMessage(plugin.prefix+"§cYour tpa request to player[§6"+plugin.getDisplayName(player)+"§c] has been denied.");
    }

    private void tpcancel(Player player) {
        if (!player.hasPermission("commonlyused.command.tpa.tpcancel")) {
            player.sendMessage(plugin.prefix+"§cYou don't have permission to use this command.");
            return;
        } else if (!tpaList.containsValue(player.getUniqueId())) {
            player.sendMessage(plugin.prefix+"§cThere are no tpa request from you at this moment.");
            return;
        }

        UUID toPlayerId = getKeyFromValue(player.getUniqueId());
        if (toPlayerId == null) {
            player.sendMessage(plugin.prefix+"§cThere are no tpa request from you at this moment.");
            return;
        }

        cleanTpa(player.getUniqueId(), toPlayerId);

        Player toPlayer = Bukkit.getPlayer(toPlayerId);
        if (toPlayer != null) {
            toPlayer.sendMessage(plugin.prefix+"§cThe tpa request from player[§6"+plugin.getDisplayName(player)+"§c] has been canceled.");
        }
        player.sendMessage(plugin.prefix+"§cYour tpa request has been canceled.");
    }

    private void cleanTpa(UUID fromPlayerId, UUID toPlayerId) {
        if (fromPlayerId != null) {

        }
        if (toPlayerId != null) {
            tpaList.remove(toPlayerId);
            tpAcceptCooldowns.remove(toPlayerId);
        }
    }

    private UUID getKeyFromValue(UUID value) {
        for (UUID toPlayerId : tpaList.keySet()) {
            if (tpaList.get(toPlayerId).equals(value)) {
                return toPlayerId;
            }
        }
        return null;
    }

    private Player getPlayerByName(String playerName) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getName().equalsIgnoreCase(playerName) || onlinePlayer.getDisplayName().equalsIgnoreCase(playerName) || onlinePlayer.getPlayerListName().equalsIgnoreCase(playerName)) {
                return onlinePlayer;
            }
        }
        return null;
    }
}
