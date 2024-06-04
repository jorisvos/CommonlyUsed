package nl.jorisvos.commonlyused.commands;

import nl.jorisvos.commonlyused.CommonlyUsed;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SpeedCommand implements CommandExecutor, TabCompleter {
    private final CommonlyUsed plugin;

    public SpeedCommand(CommonlyUsed plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can execute this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("commonlyused.command.speed")) {
            player.sendMessage(plugin.prefix + "§cYou don't have permission to use this command.");
            return true;
        }

        if (args.length < 1 || args.length > 2) {
            player.sendMessage(plugin.prefix+"§cUsage: /speed [<walk|fly>/reset] <value>");
            return true;
        }

        if (args[0].equalsIgnoreCase("reset")) {
            player.setWalkSpeed(0.2f);
            player.setFlySpeed(0.1f);
            player.sendMessage(plugin.prefix+"§aYour walk- and fly speed have been reset to default.");
            return true;
        }

        String type = args.length == 2 ? args[0].toLowerCase() : "default";
        float speed;
        try {
            speed = Float.parseFloat(args[args.length == 2 ? 1 : 0]);
        } catch (NumberFormatException e) {
            player.sendMessage(plugin.prefix+"§cInvalid speed value.");
            return true;
        }

        switch (type) {
            case "walk":
                setPlayerWalkSpeed(player, speed);
                break;
            case "fly":
                setPlayerFlySpeed(player, speed);
                break;
            default:
                if (player.isFlying()) {
                    setPlayerFlySpeed(player, speed);
                } else {
                    setPlayerWalkSpeed(player, speed);
                }
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            options.add("walk");
            options.add("fly");
            options.add("fly 0.1");
            options.add("walk 0.2");
            options.add("reset");
            return options.stream().filter(name -> name.startsWith(args[0])).collect(Collectors.toList());
        }
        return null;
    }

    public void setPlayerWalkSpeed(Player player, float speed) {
        // Ensure the speed is within a reasonable range
        if (speed > 1.0f) speed = 1.0f;
        if (speed < -1.0f) speed = -1.0f;

        player.setWalkSpeed(speed);
        player.sendMessage(plugin.prefix+"§aYour walk speed has been set to §6" + speed);
    }
    public void setPlayerFlySpeed(Player player, float speed) {
        // Ensure the speed is within a reasonable range
        if (speed > 1.0f) speed = 1.0f;
        if (speed < -1.0f) speed = -1.0f;

        player.setFlySpeed(speed);
        player.sendMessage(plugin.prefix+"§aYour fly speed has been set to §6" + speed);
    }
}
