package nl.jorisvos.commonlyused.commands;

import nl.jorisvos.commonlyused.CommonlyUsed;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RealnameCommand implements CommandExecutor {
    private final CommonlyUsed plugin;

    public RealnameCommand(CommonlyUsed plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("commonlyused.command.nickname")) {
            sender.sendMessage(plugin.prefix + "§cYou don't have permission to use this command.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.prefix+"§cUsage: /realname <player>");
            return true;
        }

        String nickname = String.join(" ", args);
        Player target = getPlayerByNickname(nickname);

        if (target != null) {
            sender.sendMessage(plugin.prefix+"§aThe real in-game name of " + "§b" + nickname + "§a is: " + "§b" + target.getName());
        } else {
            sender.sendMessage(plugin.prefix+"§cPlayer with nickname " + "§b" + nickname + "§c not found.");
        }

        return true;
    }

    private Player getPlayerByNickname(String nickname) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getDisplayName().equalsIgnoreCase(nickname) || onlinePlayer.getPlayerListName().equalsIgnoreCase(nickname)) {
                return onlinePlayer;
            }
        }
        return null;
    }
}
