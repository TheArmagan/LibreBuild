package net.ezplace.librebuild.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import net.ezplace.librebuild.handlers.GuardHandler;
import net.ezplace.librebuild.handlers.ItemHandler;
import net.ezplace.librebuild.schem.SchemReader;
import net.ezplace.librebuild.utils.FileSchem;
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
        ItemStack item = player.getInventory().getItemInMainHand();
        String schematicName = itemHandler.getSchematicName(item);

        if (schematicName == null) return;

        Location loc = player.getLocation();

        /**
         * TODO: Make the size dynamic so, if it has to place a schematic 100x100x100, the W H D is adapted
         * */
        int width = 50;
        int height = 50;
        int depth = 50;

        int x1 = loc.getBlockX();
        int y1 = loc.getBlockY();
        int z1 = loc.getBlockZ();
        int x2 = x1 + width - 1;
        int y2 = y1 + height - 1;
        int z2 = z1 + depth - 1;
        World weWorld = BukkitAdapter.adapt(loc.getWorld());
        if (guardHandler.isProtectedRegion(player, weWorld, x1, y1, z1, x2, y2, z2)) {
            player.sendMessage("§cNo puedes colocar esto en una zona protegida.");
            return;
        }

        /**
         * TODO: Change this part to give the player a bit of time to 2nd rightclick, now you have to fast double right click!
         * */
        particles.showPreview(loc,width,height,depth);
        if (pendingPlacements.containsKey(player) && pendingPlacements.get(player).equals(loc)) {
            placeSchematic(player, loc, schematicName, width, height, depth);
            player.getInventory().getItemInMainHand().setAmount(0);
            pendingPlacements.remove(player);
        } else {
            player.sendMessage("§bPrevisualización mostrada. Haz click derecho de nuevo para confirmar.");
            pendingPlacements.put(player, loc);
        }
    }

    private void placeSchematic(Player player, Location loc, String schematicName, int width, int height, int depth) {
        File file = new File(FileSchem.schematicsFolder + "/" + schematicName + ".schem");
        player.sendMessage("§aColocando esquemática...");
        SchemReader.pasteSchematic(player, file, loc);
    }
}
