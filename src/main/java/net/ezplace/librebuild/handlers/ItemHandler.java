package net.ezplace.librebuild.handlers;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class ItemHandler {
    private final NamespacedKey key;

    public ItemHandler(Plugin plugin) {
        this.key = new NamespacedKey(plugin, "schematic_name");
    }

    public ItemStack createSchematicItem(String schematicName) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setDisplayName("§aBuild: " + schematicName);
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, schematicName);
        item.setItemMeta(meta);

        return item;
    }

    public String getSchematicName(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }

        return meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }
}
