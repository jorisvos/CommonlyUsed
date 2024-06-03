package nl.jorisvos.commonlyused.commands;

import nl.jorisvos.commonlyused.CommonlyUsed;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpectatorCommand implements CommandExecutor {
    private final CommonlyUsed plugin;

    public SpectatorCommand(CommonlyUsed plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can execute this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("commonlyused.command.spectator")) {
            player.sendMessage(plugin.prefix+"§cYou don't have permission to use this command.");
            return true;
        }

        if (player.getGameMode() == GameMode.SPECTATOR) {
            player.setGameMode(GameMode.SURVIVAL);
            player.sendMessage(plugin.prefix+"§6Spectator mode §cdisabled");
        } else {
            player.setGameMode(GameMode.SPECTATOR);
            player.sendMessage(plugin.prefix+"§6Spectator mode §aenabled");
        }

        return true;
    }
}
