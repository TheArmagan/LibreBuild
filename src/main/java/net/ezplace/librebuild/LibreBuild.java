package net.ezplace.librebuild;

import net.ezplace.librebuild.commands.LibreBuildCommands;
import net.ezplace.librebuild.handlers.GuardHandler;
import net.ezplace.librebuild.handlers.ItemHandler;
import net.ezplace.librebuild.listeners.StructurePlacementListener;
import net.ezplace.librebuild.utils.FileSchem;
import net.ezplace.librebuild.utils.LibreBuildMessages;
import net.ezplace.librebuild.utils.LibreBuildSettings;
import org.bukkit.plugin.java.JavaPlugin;

public class LibreBuild extends JavaPlugin {
    /**
    * TODO: PERMISSIONS, Users can only use the item, admins can manage everything
    * */
    private static LibreBuild instance;
    private ItemHandler itemHandler;
    private GuardHandler guardHandler;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("\n"+
                " █████        ███  █████                        ███████████              ███  ████      █████\n" +
                "░░███        ░░░  ░░███                        ░░███░░░░░███            ░░░  ░░███     ░░███ \n" +
                " ░███        ████  ░███████  ████████   ██████  ░███    ░███ █████ ████ ████  ░███   ███████ \n" +
                " ░███       ░░███  ░███░░███░░███░░███ ███░░███ ░██████████ ░░███ ░███ ░░███  ░███  ███░░███ \n" +
                " ░███        ░███  ░███ ░███ ░███ ░░░ ░███████  ░███░░░░░███ ░███ ░███  ░███  ░███ ░███ ░███ \n" +
                " ░███      █ ░███  ░███ ░███ ░███     ░███░░░   ░███    ░███ ░███ ░███  ░███  ░███ ░███ ░███ \n" +
                " ███████████ █████ ████████  █████    ░░██████  ███████████  ░░████████ █████ █████░░████████\n" +
                "░░░░░░░░░░░ ░░░░░ ░░░░░░░░  ░░░░░      ░░░░░░  ░░░░░░░░░░░    ░░░░░░░░ ░░░░░ ░░░░░  ░░░░░░░░ \n" + "Version: 1.1.1");

        getDataFolder().mkdirs();

        LibreBuildSettings.getInstance().load();
        LibreBuildMessages.getInstance().loadMessages();

        this.itemHandler = new ItemHandler(this);
        this.guardHandler = new GuardHandler();

        getServer().getPluginManager().registerEvents(new StructurePlacementListener(this, guardHandler), this);

        LibreBuildCommands commandExecutor = new LibreBuildCommands(FileSchem.SCHEMATIC_FOLDER, itemHandler);
        getCommand("librebuild").setExecutor(commandExecutor);
        getCommand("librebuild").setTabCompleter(commandExecutor);
        getLogger().info(LibreBuildMessages.getInstance().getMessage("plugin.enabled"));
    }

    @Override
    public void onDisable() {
        getLogger().info(LibreBuildMessages.getInstance().getMessage("plugin.disabled"));
    }

    public static LibreBuild getInstance() {
        return instance;
    }
}
