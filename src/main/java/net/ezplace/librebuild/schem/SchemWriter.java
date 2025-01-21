package net.ezplace.librebuild.schem;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.math.BlockVector3;
import net.ezplace.librebuild.utils.FileSchem;
import net.ezplace.librebuild.utils.LibreBuildMessages;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SchemWriter {

    public static boolean saveSchematic(Player player, BlockVector3 min, BlockVector3 max, String fileName) {
        World world = BukkitAdapter.adapt(player.getWorld());
        CuboidRegion region = new CuboidRegion(min, max);
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
            Operations.complete(new ForwardExtentCopy(
                    world, region, clipboard, region.getMinimumPoint()
            ));
        } catch (Exception e) {
            player.sendMessage(LibreBuildMessages.getInstance().getMessage("schem.error.copy"));
            e.printStackTrace();
            return false;
        }

        File file = new File(FileSchem.schematicsFolder, fileName + ".schem");
        file.getParentFile().mkdirs();

        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC.getWriter(new FileOutputStream(file))) {
            writer.write(clipboard);
            player.sendMessage(LibreBuildMessages.getInstance().getMessage("schem.saveas"));
            return true;
        } catch (IOException e) {
            player.sendMessage(LibreBuildMessages.getInstance().getMessage("schem.error.save"));
            e.printStackTrace();
            return false;
        }
    }
}
