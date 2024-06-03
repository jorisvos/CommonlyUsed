package nl.jorisvos.commonlyused.commands;

import nl.jorisvos.commonlyused.CommonlyUsed;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlyCommand implements CommandExecutor {
    private final CommonlyUsed plugin;

    public FlyCommand(CommonlyUsed plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can execute this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("commonlyused.command.fly")) {
            player.sendMessage(plugin.prefix+"§cYou don't have permission to use this command.");
            return true;
        }

        if (player.getAllowFlight()) {
            player.setAllowFlight(false);
            player.setFlying(false);
            player.sendMessage(plugin.prefix+"§6Fly mode §cdisabled.");
        } else {
            player.setAllowFlight(true);
            player.setFlying(true);
            player.sendMessage(plugin.prefix+"§6Fly mode §aenabled.");
        }

        return true;
    }
}
