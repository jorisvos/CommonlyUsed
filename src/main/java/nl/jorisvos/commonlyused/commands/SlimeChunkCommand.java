package nl.jorisvos.commonlyused.commands;

import nl.jorisvos.commonlyused.CommonlyUsed;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SlimeChunkCommand implements CommandExecutor {
    private final CommonlyUsed plugin;
    private final Map<UUID, Long> backCooldowns = new HashMap<>();

    public SlimeChunkCommand(CommonlyUsed plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can execute this command.");
            return true;
        }

        Player player = (Player) sender;

        final boolean isSlimeChunk = player.getLocation().getChunk().isSlimeChunk();
        player.sendMessage(plugin.prefix+"§eisCurrentChunkSlimeChunk: "+(isSlimeChunk?"§atrue":"§cfalse"));
        return true;
    }
}
