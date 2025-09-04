package net.ezplace.librebuild.schem;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.session.ClipboardHolder;
import net.ezplace.librebuild.utils.LibreBuildMessages;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class SchemReader {

    // =================== PUBLIC API ===================
    public static void pasteSchematic(Player player, File file) { pasteSchematic(player, file, player.getLocation()); }

    public static void pasteSchematic(Player player, File file, Location loc) {
        Clipboard clipboard = loadSchematic(file);
        if (clipboard == null) {
            player.sendMessage(LibreBuildMessages.getInstance().getMessage("schem.error.load"));
            return;
        }
        int steps = yawToRotationSteps(player.getLocation().getYaw());

        directPasteWithWorldEdit(clipboard, loc, steps);

        player.sendMessage(LibreBuildMessages.getInstance().getMessage("schem.placed"));
    }

    // =================== LOAD ===================
    public static Clipboard loadSchematic(File file) {
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) {
            Bukkit.getLogger().severe(LibreBuildMessages.getInstance().getMessage("schem.error.format") + file.getAbsolutePath());
            return null;
        }
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            return reader.read();
        } catch (IOException e) {
            Bukkit.getLogger().severe(LibreBuildMessages.getInstance().getMessage("schem.error.read") + e.getMessage());
            return null;
        }
    }

    // =================== ROTATION HELPERS ===================
    private static int yawToRotationSteps(float yaw) {
        // Normalize yaw to 0-360 range
        yaw = yaw % 360;
        if (yaw < 0) yaw += 360;

        // Bukkit yaw: 0=S (+Z), 90=W (-X), 180=N (-Z), 270=E (+X)
        // WorldEdit rotation: 0° = no rotation, 90° = 90° CW, 180° = 180°, 270° = 270° CW
        // We want structure to face same direction as player
        int steps = (Math.round(yaw / -90.0f) % 4) + 1;
        if (steps < 0) steps += 4; // Ensure steps is in 0-3 range
        if (steps > 3) steps -= 4;
        return steps;
    }

    private static AffineTransform buildRotationTransform(int steps) {
        // WorldEdit rotateY(+angle) = CCW rotation
        // We want structure to match player facing direction
        // Use positive angle for CCW rotation
        double angle = steps * 90.0;
        return new AffineTransform().rotateY(angle);
    }

    // =================== DIRECT PASTE VIA WORLDEDIT (NO ANIMATION) ===================
    public static ClipboardHolder directPasteWithWorldEdit(Clipboard clipboard, Location loc, int steps) {
        org.bukkit.World bw = loc.getWorld();
        if (bw == null) return null;
        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(bw);
        AffineTransform transform = buildRotationTransform(steps);
        ClipboardHolder holder = new ClipboardHolder(clipboard);
        holder.setTransform(holder.getTransform().combine(transform));
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld)) {
            Operation op = holder
                    .createPaste(editSession)
                    .to(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()))
                    .ignoreAirBlocks(true) // Hava bloklarını yapıştırma
                    .build();
            Operations.complete(op);
        } catch (Exception e) {
            Bukkit.getLogger().severe("Paste failed: " + e.getMessage());
        }
        return holder;
    }

    public static ClipboardHolder buildClipboard(Player player, File file) {
        Clipboard clipboard = loadSchematic(file);
        if (clipboard == null) {
            return null;
        }

        int steps = yawToRotationSteps(player.getLocation().getYaw());
        AffineTransform transform = buildRotationTransform(steps);

        ClipboardHolder holder = new ClipboardHolder(clipboard);
        holder.setTransform(holder.getTransform().combine(transform));

        return holder;
    }

    // =================== PREVIEW HELPERS ===================
    public static PreviewInfo getPreviewInfo(Player player, File file, Location playerLoc) {
        Clipboard clipboard = loadSchematic(file);
        if (clipboard == null) return null;

        int steps = yawToRotationSteps(player.getLocation().getYaw());
        AffineTransform transform = buildRotationTransform(steps);

        // Original region ve origin
        com.sk89q.worldedit.regions.Region originalRegion = clipboard.getRegion();
        BlockVector3 originalMin = originalRegion.getMinimumPoint();
        BlockVector3 originalMax = originalRegion.getMaximumPoint();
        BlockVector3 originalOrigin = clipboard.getOrigin();

        // Transform uygula - tüm köşe noktalarını hesapla
        BlockVector3[] corners = {
            originalMin,
            BlockVector3.at(originalMax.x(), originalMin.y(), originalMin.z()),
            BlockVector3.at(originalMin.x(), originalMax.y(), originalMin.z()),
            BlockVector3.at(originalMin.x(), originalMin.y(), originalMax.z()),
            BlockVector3.at(originalMax.x(), originalMax.y(), originalMin.z()),
            BlockVector3.at(originalMax.x(), originalMin.y(), originalMax.z()),
            BlockVector3.at(originalMin.x(), originalMax.y(), originalMax.z()),
            originalMax
        };

        // Tüm köşeleri transform et
        BlockVector3[] transformedCorners = new BlockVector3[corners.length];
        for (int i = 0; i < corners.length; i++) {
            transformedCorners[i] = transform.apply(corners[i].toVector3()).toBlockPoint();
        }

        // Transform edilmiş origin
        BlockVector3 transformedOrigin = transform.apply(originalOrigin.toVector3()).toBlockPoint();

        // Transform edilmiş region'ın min/max noktalarını bul
        int minX = transformedCorners[0].x(), maxX = transformedCorners[0].x();
        int minY = transformedCorners[0].y(), maxY = transformedCorners[0].y();
        int minZ = transformedCorners[0].z(), maxZ = transformedCorners[0].z();

        for (BlockVector3 corner : transformedCorners) {
            minX = Math.min(minX, corner.x());
            maxX = Math.max(maxX, corner.x());
            minY = Math.min(minY, corner.y());
            maxY = Math.max(maxY, corner.y());
            minZ = Math.min(minZ, corner.z());
            maxZ = Math.max(maxZ, corner.z());
        }

        // WorldEdit paste mantığı: player pozisyonu = transform edilmiş origin pozisyonu
        // Bu durumda schematic'in sol alt köşesi (minPoint) şu pozisyonda olur:
        int x1 = playerLoc.getBlockX() + (minX - transformedOrigin.x());
        int y1 = playerLoc.getBlockY() + (minY - transformedOrigin.y());
        int z1 = playerLoc.getBlockZ() + (minZ - transformedOrigin.z());

        int width = maxX - minX + 2;
        int height = maxY - minY + 2;
        int depth = maxZ - minZ + 2;

        int x2 = x1 + width;
        int y2 = y1 + height;
        int z2 = z1 + depth;

        return new PreviewInfo(x1, y1, z1, x2, y2, z2, width, height, depth);
    }

    public static class PreviewInfo {
        public final int x1, y1, z1, x2, y2, z2;
        public final int width, height, depth;

        public PreviewInfo(int x1, int y1, int z1, int x2, int y2, int z2, int width, int height, int depth) {
            this.x1 = x1;
            this.y1 = y1;
            this.z1 = z1;
            this.x2 = x2;
            this.y2 = y2;
            this.z2 = z2;
            this.width = width;
            this.height = height;
            this.depth = depth;
        }
    }
}
