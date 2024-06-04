package nl.jorisvos.commonlyused.commands;

import nl.jorisvos.commonlyused.CommonlyUsed;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NicknameCommand implements CommandExecutor {
    private final CommonlyUsed plugin;

    public NicknameCommand(CommonlyUsed plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can execute this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("commonlyused.command.nickname")) {
            player.sendMessage(plugin.prefix + "§cYou don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(plugin.prefix+"§eIf you are sure you want to reset your nickname to your original Minecraft name type §c/nickname confirm");
            return true;
        } else if (args[0].equals("confirm")) {
            removePlayerNickname(player);
            return true;
        }

        String nickname = String.join(" ", args);
        if (nickname.equals(player.getName())) {
            removePlayerNickname(player);
        } else {
            setPlayerNickname(player, nickname);
        }

        return true;
    }

    private void setPlayerNickname(Player player, String nickname) {
        String formattedNickname = ChatColor.translateAlternateColorCodes('&', nickname);
        plugin.getSettings().addNickname(player.getUniqueId(), formattedNickname);
        plugin.getSettings().saveNicknameConfig();

        player.setDisplayName(formattedNickname);
        player.setPlayerListName(formattedNickname);
        player.sendMessage(plugin.prefix+"§aYour nickname has been changed to: §6" + formattedNickname);
    }

    private void removePlayerNickname(Player player) {
        plugin.getSettings().removeNickname(player.getUniqueId());
        plugin.getSettings().saveNicknameConfig();

        player.setDisplayName(player.getName());
        player.setPlayerListName(player.getName());
        player.sendMessage(plugin.prefix+"§aYour nickname has been reset to your original Minecraft name.");
    }
}
