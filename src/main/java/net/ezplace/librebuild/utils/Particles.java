package net.ezplace.librebuild.utils;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class Particles {
    private final Plugin plugin;
    private final Map<Player, BukkitRunnable> activeParticles = new HashMap<>();

    public Particles(Plugin plugin) {
        this.plugin = plugin;
    }

    public void showPreview(Player player, Location loc, int width, int height, int depth) {
        World world = loc.getWorld();
        if (world == null) return;

        int x1 = loc.getBlockX();
        int y1 = loc.getBlockY();
        int z1 = loc.getBlockZ();
        int x2 = x1 + width - 1;
        int y2 = y1 + height - 1;
        int z2 = z1 + depth - 1;

        Location[] vertices = {
                new Location(world, x1, y1, z1), new Location(world, x2, y1, z1),
                new Location(world, x1, y1, z2), new Location(world, x2, y1, z2),
                new Location(world, x1, y2, z1), new Location(world, x2, y2, z1),
                new Location(world, x1, y2, z2), new Location(world, x2, y2, z2)
        };

        if (LibreBuildSettings.PARTICLES) {
            stopPreview(player);

            BukkitRunnable particleTask = new BukkitRunnable() {
                int ticks = 0;

                @Override
                public void run() {
                    if (ticks >= 100) {
                        cancel();
                        activeParticles.remove(player);
                        return;
                    }

                    drawLine(world, vertices[0], vertices[1]);
                    drawLine(world, vertices[0], vertices[2]);
                    drawLine(world, vertices[1], vertices[3]);
                    drawLine(world, vertices[2], vertices[3]);

                    drawLine(world, vertices[4], vertices[5]);
                    drawLine(world, vertices[4], vertices[6]);
                    drawLine(world, vertices[5], vertices[7]);
                    drawLine(world, vertices[6], vertices[7]);

                    drawLine(world, vertices[0], vertices[4]);
                    drawLine(world, vertices[1], vertices[5]);
                    drawLine(world, vertices[2], vertices[6]);
                    drawLine(world, vertices[3], vertices[7]);

                    ticks++;
                }
            };

            activeParticles.put(player, particleTask);
            particleTask.runTaskTimer(plugin, 0, 5);
        }
    }

    public void stopPreview(Player player) {
        if (activeParticles.containsKey(player)) {
            activeParticles.get(player).cancel();
            activeParticles.remove(player);
        }
    }

    private void drawLine(World world, Location start, Location end) {
        int points = 30;
        double dx = (end.getX() - start.getX()) / points;
        double dy = (end.getY() - start.getY()) / points;
        double dz = (end.getZ() - start.getZ()) / points;

        for (int i = 0; i <= points; i++) {
            Location loc = start.clone().add(dx * i, dy * i, dz * i);
            world.spawnParticle(LibreBuildSettings.PARTICLES_TYPE, loc, 1,
                    new Particle.DustOptions(LibreBuildSettings.PARTICLES_COLOR, 1.0F));
        }
    }
}