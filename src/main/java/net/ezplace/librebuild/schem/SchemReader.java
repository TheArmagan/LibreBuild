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
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.math.BlockVector3;
import net.ezplace.librebuild.utils.LibreBuildMessages;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class SchemReader {

    public static Clipboard loadSchematic(File file) {
        Clipboard clipboard = null;
        ClipboardFormat format = ClipboardFormats.findByFile(file);

        if (format == null) {
            Bukkit.getLogger().severe(LibreBuildMessages.getInstance().getMessage("schem.error.format") + file.getAbsolutePath());
            return null;
        }

        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            clipboard = reader.read();
        } catch (IOException e) {
            Bukkit.getLogger().severe(LibreBuildMessages.getInstance().getMessage("schem.error.read")  + e.getMessage());
        }

        return clipboard;
    }

    public static void pasteSchematic(Player player, File file, Location loc) {
        Clipboard clipboard = loadSchematic(file);
        if (clipboard == null) {
            player.sendMessage(LibreBuildMessages.getInstance().getMessage("schem.error.load") );
            return;
        }

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(player.getWorld()))) {
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()))
                    .ignoreAirBlocks(true)
                    .build();

            Operations.complete(operation);
            player.sendMessage(LibreBuildMessages.getInstance().getMessage("schem.success.paste") + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
        } catch (Exception e) {
            player.sendMessage(LibreBuildMessages.getInstance().getMessage("schem.error.paste") );
            e.printStackTrace();
        }
    }

    public static void pasteSchematic(Player player, File file) {
        Location loc = player.getLocation();
        Clipboard clipboard = loadSchematic(file);
        if (clipboard == null) {
            player.sendMessage(LibreBuildMessages.getInstance().getMessage("schem.error.load") );
            return;
        }

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(player.getWorld()))) {
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()))
                    .ignoreAirBlocks(true)
                    .build();

            Operations.complete(operation);
            player.sendMessage(LibreBuildMessages.getInstance().getMessage("schem.success.paste")  + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
        } catch (Exception e) {
            player.sendMessage(LibreBuildMessages.getInstance().getMessage("schem.error.paste") );
            e.printStackTrace();
        }
    }
}
