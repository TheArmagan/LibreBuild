package net.ezplace.librebuild.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.world.World;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.ezplace.librebuild.LibreBuild;
import net.ezplace.librebuild.handlers.GuardHandler;
import net.ezplace.librebuild.handlers.ItemHandler;
import net.ezplace.librebuild.schem.SchemReader;
import net.ezplace.librebuild.utils.FileSchem;
import net.ezplace.librebuild.utils.LibreBuildMessages;
import net.ezplace.librebuild.utils.Particles;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class StructurePlacementListener implements Listener {

    private final Plugin plugin;
    private final ItemHandler itemHandler;
    private final Particles particles;
    private final GuardHandler guardHandler;
    private final Map<Player, Location> pendingPlacements = new HashMap<>();
    private final Map<Player, Long> clickTimes = new HashMap<>();


    public StructurePlacementListener(Plugin plugin, GuardHandler guardHandler) {
        this.plugin = plugin;
        this.itemHandler = new ItemHandler(plugin);
        this.particles = new Particles(plugin);
        this.guardHandler = guardHandler;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        Player player = event.getPlayer();
        if (!player.hasPermission("librebuild.item.use")) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        String schematicName = itemHandler.getSchematicName(item);
        if (schematicName == null) return;

        Location loc = player.getLocation();

        File schematicFile = new File(FileSchem.schematicsFolder,  schematicName + ".schem");

        try {
            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
            if (format == null) {
                player.sendMessage("§cFormato de schematic no válido.");
                return;
            }

            ClipboardReader reader = format.getReader(new FileInputStream(schematicFile));
            Clipboard clipboard = reader.read();

            int width = clipboard.getDimensions().x();
            int height = clipboard.getDimensions().y();
            int depth = clipboard.getDimensions().z();

            int x1 = loc.getBlockX();
            int y1 = loc.getBlockY();
            int z1 = loc.getBlockZ();
            int x2 = x1 + width - 1;
            int y2 = y1 + height - 1;
            int z2 = z1 + depth - 1;
            particles.showPreview(player,loc, width, height, depth);

            if (guardHandler.isProtectedRegion(player, x1, y1, z1, x2, y2, z2)) {
                player.sendMessage(LibreBuildMessages.getInstance().getMessage("placement.error.restricted"));
                return;
            }

            particles.showPreview(player,loc, width, height, depth);

            long currentTime = System.currentTimeMillis();

            if (pendingPlacements.containsKey(player)) {
                long lastClickTime = clickTimes.getOrDefault(player, 0L);

                // 5 seconds timeout
                if (currentTime - lastClickTime <= 5000) {
                    placeSchematic(player, loc, schematicName, width, height, depth);
                    player.getInventory().getItemInMainHand().setAmount(0);
                    pendingPlacements.remove(player);
                    clickTimes.remove(player);
                } else {
                    player.sendMessage(LibreBuildMessages.getInstance().getMessage("placement.preview"));
                    clickTimes.put(player, currentTime);
                }
            } else {
                player.sendMessage(LibreBuildMessages.getInstance().getMessage("placement.preview"));
                pendingPlacements.put(player, loc);
                clickTimes.put(player, currentTime);
            }
        } catch (IOException e) {
            e.printStackTrace();
            player.sendMessage(LibreBuildMessages.getInstance().getMessage("schem.error.load"));
        }
    }

    private void placeSchematic(Player player, Location loc, String schematicName, int width, int height, int depth) {
        File file = new File(FileSchem.schematicsFolder + "/" + schematicName + ".schem");
        player.sendMessage(LibreBuildMessages.getInstance().getMessage("schem.placing"));
        SchemReader.pasteSchematic(player, file, loc);
    }
}
