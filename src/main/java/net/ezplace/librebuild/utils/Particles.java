package net.ezplace.librebuild.utils;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Particles {
    private final Plugin plugin;

    public Particles(Plugin plugin) {
        this.plugin = plugin;
    }

    public void showPreview(Location loc, int width, int height, int depth) {
        World world = loc.getWorld();
        if (world == null) return;

        int x1 = loc.getBlockX();
        int y1 = loc.getBlockY();
        int z1 = loc.getBlockZ();
        int x2 = x1 + width - 1;
        int y2 = y1 + height - 1;
        int z2 = z1 + depth - 1;


        /**
         * Cuboid vertices
         * */
        Location[] vertices = {
                new Location(world, x1, y1, z1),
                new Location(world, x2, y1, z1),
                new Location(world, x1, y1, z2),
                new Location(world, x2, y1, z2),
                new Location(world, x1, y2, z1),
                new Location(world, x2, y2, z1),
                new Location(world, x1, y2, z2),
                new Location(world, x2, y2, z2)
        };

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 100) {
                    cancel();
                    return;
                }

                /**
                 * TODO: Join these vertex with the particle
                 * */
                for (Location vertex : vertices) {
                    world.spawnParticle(Particle.REDSTONE, vertex, 5,
                            new Particle.DustOptions(Color.BLUE, 1.0F));
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0, 5);
    }
}