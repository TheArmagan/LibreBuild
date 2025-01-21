package net.ezplace.librebuild.handlers;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.BukkitWorldGuardPlatform;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.ezplace.librebuild.utils.LibreBuildMessages;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.Location;

public class GuardHandler {
    private final WorldGuardPlugin worldGuardPlugin;
    private final BukkitWorldGuardPlatform platform;
    private final RegionContainer regionContainer;

    public GuardHandler() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (!(plugin instanceof WorldGuardPlugin)) {
            throw new IllegalStateException(LibreBuildMessages.getInstance().getMessage("plugin.missing.worldguard"));
        }
        this.worldGuardPlugin = (WorldGuardPlugin) plugin;

        this.platform = (BukkitWorldGuardPlatform) WorldGuard.getInstance().getPlatform();
        this.regionContainer = platform.getRegionContainer();
        if (this.regionContainer == null) {
            throw new IllegalStateException(LibreBuildMessages.getInstance().getMessage("plugin.region.exception"));
        }
    }

    public boolean isProtectedRegion(Player player, int x1, int y1, int z1, int x2, int y2, int z2) {
        World world = BukkitAdapter.adapt(player.getWorld());

        RegionManager regionManager = regionContainer.get(world);
        if (regionManager == null) {
            player.sendMessage(LibreBuildMessages.getInstance().getMessage("plugin.region.exception2"));
            return false;
        }

        int minX = Math.min(x1, x2);
        int minY = Math.min(y1, y2);
        int minZ = Math.min(z1, z2);
        int maxX = Math.max(x1, x2);
        int maxY = Math.max(y1, y2);
        int maxZ = Math.max(z1, z2);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Location loc = new Location(player.getWorld(), x, y, z);
                    if (isLocationInProtectedRegion(loc)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isLocationInProtectedRegion(Location loc) {
        World world = BukkitAdapter.adapt(loc.getWorld());
        RegionManager regionManager = regionContainer.get(world);
        if (regionManager == null) {
            return false;
        }

        ApplicableRegionSet regions = regionManager.getApplicableRegions(BukkitAdapter.asBlockVector(loc));
        return !regions.getRegions().isEmpty();
    }
}
