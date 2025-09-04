package net.ezplace.librebuild.listeners;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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

        File schematicFile = new File(FileSchem.SCHEMATIC_FOLDER, schematicName + ".schem");

        if (!schematicFile.exists()) {
            player.sendMessage(LibreBuildMessages.getInstance().getMessage("schem.error.notfound"));
            return;
        }

        // SchemReader'dan doğru preview bilgilerini al
        SchemReader.PreviewInfo previewInfo = SchemReader.getPreviewInfo(player, schematicFile, loc);
        if (previewInfo == null) {
            player.sendMessage(LibreBuildMessages.getInstance().getMessage("schem.error.load"));
            return;
        }

        // Preview partikülleri göster
        particles.showPreview(player, new Location(loc.getWorld(), previewInfo.x1, previewInfo.y1, previewInfo.z1),
                              previewInfo.width, previewInfo.height, previewInfo.depth);

        // Korumalı bölge kontrolü
        if (guardHandler.isProtectedRegion(player, previewInfo.x1, previewInfo.y1, previewInfo.z1,
                                           previewInfo.x2, previewInfo.y2, previewInfo.z2)) {
            player.sendMessage(LibreBuildMessages.getInstance().getMessage("placement.error.restricted"));
            return;
        }

        long currentTime = System.currentTimeMillis();

        if (pendingPlacements.containsKey(player)) {
            long lastClickTime = clickTimes.getOrDefault(player, 0L);

            // 5 seconds timeout
            if (currentTime - lastClickTime <= 5000) {
                // Preview ile aynı pozisyonu kullan - player pozisyonu paste origin'ı olsun
                placeSchematic(player, loc, schematicName);
                player.getInventory().getItemInMainHand().setAmount(0);
                pendingPlacements.remove(player);
                clickTimes.remove(player);
            } else {
                player.sendMessage(LibreBuildMessages.getInstance().getMessage("placement.preview"));
                clickTimes.put(player, currentTime);
            }
        } else {
            player.sendMessage(LibreBuildMessages.getInstance().getMessage("placement.preview"));
            pendingPlacements.put(player, new Location(loc.getWorld(), previewInfo.x1, previewInfo.y1, previewInfo.z1));
            clickTimes.put(player, currentTime);
        }
    }

    private void placeSchematic(Player player, Location loc, String schematicName) {
        File file = new File(FileSchem.SCHEMATIC_FOLDER + "/" + schematicName + ".schem");
        player.sendMessage(LibreBuildMessages.getInstance().getMessage("schem.placing"));
        SchemReader.pasteSchematic(player, file, loc);
    }
}
