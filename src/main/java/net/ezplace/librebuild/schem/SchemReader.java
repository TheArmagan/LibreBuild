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
            Bukkit.getLogger().severe("¡Formato de schematic no reconocido para el archivo: " + file.getAbsolutePath());
            return null;
        }

        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            clipboard = reader.read();
        } catch (IOException e) {
            Bukkit.getLogger().severe("Error al leer el archivo schematic: " + e.getMessage());
        }

        return clipboard;
    }

    public static void pasteSchematic(Player player, File file, Location loc) {
        Clipboard clipboard = loadSchematic(file);
        if (clipboard == null) {
            player.sendMessage("§cError al cargar la esquemática.");
            return;
        }

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(player.getWorld()))) {
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()))
                    .ignoreAirBlocks(true)
                    .build();

            Operations.complete(operation);
            player.sendMessage("§aSchematic colocada correctamente en " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
        } catch (Exception e) {
            player.sendMessage("§cError al colocar la schematic.");
            e.printStackTrace();
        }
    }

    public static void pasteSchematic(Player player, File file) {
        Location loc = player.getLocation();
        Clipboard clipboard = loadSchematic(file);
        if (clipboard == null) {
            player.sendMessage("§cError al cargar la esquemática.");
            return;
        }

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(player.getWorld()))) {
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()))
                    .ignoreAirBlocks(true)
                    .build();

            Operations.complete(operation);
            player.sendMessage("§aSchematic colocada correctamente en " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
        } catch (Exception e) {
            player.sendMessage("§cError al colocar la schematic.");
            e.printStackTrace();
        }
    }
}
