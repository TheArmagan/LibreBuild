package net.ezplace.librebuild;

import net.ezplace.librebuild.commands.LibreBuildCommands;
import net.ezplace.librebuild.handlers.GuardHandler;
import net.ezplace.librebuild.handlers.ItemHandler;
import net.ezplace.librebuild.listeners.StructurePlacementListener;
import net.ezplace.librebuild.utils.FileSchem;
import org.bukkit.plugin.java.JavaPlugin;

public class LibreBuild extends JavaPlugin {
    /**
    * TODO: PERMISSIONS, Users can only use the item, admins can manage everything
    * */
    private ItemHandler itemHandler;
    private GuardHandler guardHandler;

    @Override
    public void onEnable() {
        getLogger().info("\n################\n" +
                "## LIBREBUILD ##\n" +
                "################\n" +
                "Version: 1.0.0");
        
        getDataFolder().mkdirs();

        this.itemHandler = new ItemHandler(this);
        this.guardHandler = new GuardHandler();

        getServer().getPluginManager().registerEvents(new StructurePlacementListener(this, guardHandler), this);

        LibreBuildCommands commandExecutor = new LibreBuildCommands(FileSchem.schematicsFolder, itemHandler);
        getCommand("librebuild").setExecutor(commandExecutor);
        getCommand("librebuild").setTabCompleter(commandExecutor);
    }

    @Override
    public void onDisable() {
        getLogger().info("LibreBuild ha sido desactivado.");
    }
}
