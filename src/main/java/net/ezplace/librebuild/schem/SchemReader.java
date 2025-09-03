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
        // Bukkit yaw: 0=S (+Z), 90=W (-X), 180=N (-Z), 270=E (+X) -> yaw artışı sahada CCW anlamına gelir.
        int steps = (int)Math.round(yaw / 90.0) % 4;
        if (steps < 0) steps += 4;
        return steps; // 0,1,2,3 -> 0°,90°,180°,270° CCW
    }

    private static AffineTransform buildRotationTransform(int steps) {
        // Bukkit yaw artışı saat yönü; WorldEdit rotateY(+angle) CCW döndürür.
        // Saat yönü döndürmek istediğimiz için negatif açı.
        double angle = -steps * 90.0;
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

}