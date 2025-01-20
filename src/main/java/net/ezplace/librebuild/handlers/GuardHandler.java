package net.ezplace.librebuild.handlers;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.BukkitWorldGuardPlatform;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GuardHandler {
    private final WorldGuardPlugin worldGuardPlugin;
    private final BukkitWorldGuardPlatform platform;
    private final com.sk89q.worldguard.protection.regions.RegionContainer regionContainer;

    public GuardHandler() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (!(plugin instanceof WorldGuardPlugin)) {
            throw new IllegalStateException("⚠ WorldGuard no está instalado o no es accesible.");
        }
        this.worldGuardPlugin = (WorldGuardPlugin) plugin;

        this.platform = (BukkitWorldGuardPlatform) WorldGuard.getInstance().getPlatform();

        this.regionContainer = platform.getRegionContainer();
        if (this.regionContainer == null) {
            throw new IllegalStateException("⚠ No se pudo obtener el RegionContainer de WorldGuard.");
        }
    }

    public boolean isProtectedRegion(Player player, World world, int x1, int y1, int z1, int x2, int y2, int z2) {
        RegionContainer regionContainer = this.platform.getRegionContainer();
        if (regionContainer == null) {
            player.sendMessage("§c⚠ Error: WorldGuard no está inicializado correctamente.");
            return false;
        } else {
            RegionManager regionManager = regionContainer.get(world);
            if (regionManager == null) {
                player.sendMessage("§c⚠ No se pudo obtener el manejador de regiones.");
                return false;
            } else {
                List<ProtectedRegion> regions = new ArrayList(regionManager.getRegions().values());
                Iterator var12 = regions.iterator();

                ProtectedRegion region;
                do {
                    if (!var12.hasNext()) {
                        return false;
                    }

                    region = (ProtectedRegion)var12.next();
                } while(!this.isRegionIntersectingVolume(region, x1, y1, z1, x2, y2, z2));

                return true;
            }
        }
    }

    private boolean isRegionIntersectingVolume(ProtectedRegion region, int x1, int y1, int z1, int x2, int y2, int z2) {
        int minX = Math.min(x1, x2);
        int minY = Math.min(y1, y2);
        int minZ = Math.min(z1, z2);
        int maxX = Math.max(x1, x2);
        int maxY = Math.max(y1, y2);
        int maxZ = Math.max(z1, z2);
        return region.getMinimumPoint().x() <= maxX &&
                region.getMaximumPoint().x() >= minX &&
                region.getMinimumPoint().y() <= maxY &&
                region.getMaximumPoint().y() >= minY &&
                region.getMinimumPoint().z() <= maxZ &&
                region.getMaximumPoint().z() >= minZ;
    }
}
