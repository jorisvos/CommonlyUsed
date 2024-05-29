package nl.jorisvos.commonlyused.commands;

import nl.jorisvos.commonlyused.CommonlyUsed;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class SlimeChunkCommand implements CommandExecutor {
    private final CommonlyUsed plugin;

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
        if (!isSlimeChunk) {
            Location closestSlimeChunk = findClosestSlimeChunk(player.getLocation());
            if (closestSlimeChunk != null) {
                player.sendMessage(plugin.prefix + "§aThe closest slime chunk is at: §ex=" + closestSlimeChunk.getBlockX() + ", z=" + closestSlimeChunk.getBlockZ());
            } else {
                player.sendMessage(plugin.prefix + "§cNo slime chunk found in a radius of "+plugin.getSettings().getSlimeChunkSearchRadius()+" chunks.");
            }
        }
        return true;
    }

    // Find the closest slime chunk to player with the help of 'breadth-first search'
    private Location findClosestSlimeChunk(Location location) {
        World world = location.getWorld();
        long seed = world.getSeed();
        int startX = location.getChunk().getX();
        int startZ = location.getChunk().getZ();

        Queue<int[]> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        queue.add(new int[]{startX, startZ});
        visited.add(startX + "," + startZ);

        int[][] directions = {
                // x+1,     x-1,    y+1,     y-1
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}
        };

        while (!queue.isEmpty()) {
            int[] chunkCoords = queue.poll();
            int chunkX = chunkCoords[0];
            int chunkZ = chunkCoords[1];

            // calculate the difference between the X,Z coordinate of the start chunk and the X,Z coordinate of the current chunk.
            int dX = startX > chunkX ? startX - chunkX : chunkX - startX;
            int dZ = startZ > chunkZ ? startZ - chunkZ : chunkZ - startZ;

            // if difference is bigger then specified stop search and return null;
            int maxD = plugin.getSettings().getSlimeChunkSearchRadius();
            if (dX > maxD || dZ > maxD)
                return null;

            if (isSlimeChunk(chunkX, chunkZ, seed)) {
                return getChunkCenter(world.getChunkAt(chunkX, chunkZ));
            }

            for (int[] dir : directions) {
                int newChunkX = chunkX + dir[0];
                int newChunkZ = chunkZ + dir[1];
                String key = newChunkX + "," + newChunkZ;

                if (!visited.contains(key)) {
                    queue.add(new int[]{newChunkX, newChunkZ});
                    visited.add(key);
                }
            }
        }

        return null;
    }

    private Location getChunkCenter(Chunk chunk) {
        return chunk.getBlock(8, 0, 8).getLocation(); // Center of the chunk
    }

    private boolean isSlimeChunk(int chunkX, int chunkZ, long seed) {
        Random rnd = new Random(
                seed +
                        ((long) chunkX * chunkX * 0x4c1906) +
                        (chunkX * 0x5ac0dbL) +
                        ((long) chunkZ * chunkZ) * 0x4307a7L +
                        (chunkZ * 0x5f24fL) ^ 0x3ad8025f
        );
        return rnd.nextInt(10) == 0;
    }
}
