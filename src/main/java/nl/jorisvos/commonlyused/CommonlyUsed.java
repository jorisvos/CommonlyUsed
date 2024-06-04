package nl.jorisvos.commonlyused;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import nl.jorisvos.commonlyused.commands.*;
import nl.jorisvos.commonlyused.listeners.PlayerListener;
import nl.jorisvos.commonlyused.tabcompleters.PlayerNameTabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CommonlyUsed extends JavaPlugin {
    public final String prefix = "§e[§r§bCommonly§r§9§lU§r§e] §r";
    private Settings settings;
    private final Map<UUID, Location> lastLocations = new HashMap<>();
    private final Map<UUID, Long> teleportCooldowns = new HashMap<>();

    @Override
    public void onEnable() {
        //TODO: add fancy startup message to console!

        // Register commands
        getCommand("back").setExecutor(new BackCommand(this));
        getCommand("slimechunk").setExecutor(new SlimeChunkCommand(this));
        getCommand("craft").setExecutor(new CraftingBenchCommand());
        getCommand("enderchest").setExecutor(new EnderChestCommand());
        getCommand("inventory").setExecutor(new InventoryCommand(this));
        getCommand("fly").setExecutor(new FlyCommand(this));
        getCommand("spectator").setExecutor(new SpectatorCommand(this));
        getCommand("heal").setExecutor(new HealCommand(this));
        getCommand("nickname").setExecutor(new NicknameCommand(this));
        getCommand("realname").setExecutor(new RealnameCommand(this));
        SpeedCommand speedCommand = new SpeedCommand(this);
        getCommand("speed").setExecutor(speedCommand);
        // home commands
        HomeCommands homeCommands = new HomeCommands(this);
        getCommand("sethome").setExecutor(homeCommands);
        getCommand("home").setExecutor(homeCommands);
        getCommand("delhome").setExecutor(homeCommands);
        // spawn commands
        SpawnCommands spawnCommands = new SpawnCommands(this);
        getCommand("setspawn").setExecutor(spawnCommands);
        getCommand("spawn").setExecutor(spawnCommands);
        // warp commands
        WarpCommands warpCommands = new WarpCommands(this);
        getCommand("setwarp").setExecutor(warpCommands);
        getCommand("delwarp").setExecutor(warpCommands);
        getCommand("warp").setExecutor(warpCommands);
        getCommand("warplist").setExecutor(warpCommands);
        // tpa commands
        TpaCommand tpaCommands = new TpaCommand(this);
        getCommand("tpa").setExecutor(tpaCommands);
        getCommand("tpaccept").setExecutor(tpaCommands);
        getCommand("tpdeny").setExecutor(tpaCommands);
        getCommand("tpcancel").setExecutor(tpaCommands);

        // Register tab completer
        getCommand("inventory").setTabCompleter(new PlayerNameTabCompleter());
        getCommand("warp").setTabCompleter(warpCommands);
        getCommand("delwarp").setTabCompleter(warpCommands);
        getCommand("speed").setTabCompleter(speedCommand);
        getCommand("realname").setTabCompleter(new PlayerNameTabCompleter());

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // Load settings
        settings = new Settings(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public Settings getSettings() {
        return settings;
    }

    public boolean isPlayerOnTeleportCooldown(UUID uniquePlayerId) {
        return teleportCooldowns.containsKey(uniquePlayerId) && System.currentTimeMillis() - teleportCooldowns.get(uniquePlayerId) < (settings.getTeleportCooldown() * 1000L);
    }
    public String getTeleportCooldownMessage(UUID uniquePlayerId) {
        long remainingTime = (settings.getTeleportCooldown() * 1000L) - (System.currentTimeMillis() - teleportCooldowns.get(uniquePlayerId));
        return prefix+"§cYou must wait §6" + (remainingTime / 1000) + " §cseconds before you can teleport using any command again.";
    }
    public String getTeleportCooldownMessageForPlayer(UUID uniquePlayerId) {
        long remainingTime = (settings.getTeleportCooldown() * 1000L) - (System.currentTimeMillis() - teleportCooldowns.get(uniquePlayerId));
        return prefix+"§cPlayer[§6"+getDisplayName(uniquePlayerId)+"§c] must wait §6" + (remainingTime / 1000) + " §cseconds before he/she can teleport using any command again.";
    }

    public void teleportAfterDelay(Player player, Location location, int delayInSeconds, String completionMessage) {
        new BukkitRunnable() {
            int count = delayInSeconds;
            @Override
            public void run() {
                if (count > 0) {
                    player.sendMessage(prefix+"§eTeleporting in §6" + count + " §eseconds...");
                    count--;
                } else {
                    this.cancel();
                    teleportCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                    player.teleport(location);
                    player.sendMessage(prefix+"§a"+completionMessage);
                }
            }
        }.runTaskTimer(this, 0, 20); // Run task every second (20 ticks)
    }

    public boolean hasLastLocation(UUID playerId) {
        return lastLocations.containsKey(playerId);
    }

    public Location getLastLocation(UUID playerId) {
        return lastLocations.get(playerId);
    }

    public void setLastLocation(UUID playerId, Location location) {
        lastLocations.put(playerId, location);
    }

    public TextComponent getClickableMessage(String message, String command) {
        TextComponent textComponent = new TextComponent(message);
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));

        Text hoverText = new Text(command);
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText);

        textComponent.setHoverEvent(hoverEvent);
        return textComponent;
    }
    public void sendClickableMessage(Player player, BaseComponent[] baseComponent) {
        player.spigot().sendMessage(baseComponent);
    }

    public String getDisplayName(UUID playerId) { return getDisplayName(Bukkit.getPlayer(playerId)); }
    public String getDisplayName(Player player) {
        if (player == null) {
            return "[player=offline]";
        } else if (settings.hasNickname(player.getUniqueId())) {
            return settings.getNickname(player.getUniqueId());
        } else {
            return player.getName();
        }
    }
}
