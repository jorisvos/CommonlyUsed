package nl.jorisvos.commonlyused.commands;

import nl.jorisvos.commonlyused.CommonlyUsed;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InventoryCommand implements CommandExecutor {
    private final CommonlyUsed plugin;

    public InventoryCommand(CommonlyUsed plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can execute this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(plugin.prefix+"§cYou didn't provide a valid argument. You use the command like this: /<command> [playerName]");
            return true;
        }

        Player otherPlayer = plugin.getServer().getPlayer(args[0]);
        if (otherPlayer == null) {
            player.sendMessage(plugin.prefix+"§cThat's not a valid player (name)!");
            return true;
        }

        player.closeInventory();
        player.openInventory(otherPlayer.getInventory());
        return true;
    }
}
