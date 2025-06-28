package net.ezplace.librebuild.utils;

import java.io.File;
import org.bukkit.Bukkit;

public class FileSchem {
    public static File SCHEMATIC_FOLDER = new File(Bukkit.getPluginManager().getPlugin("WorldEdit").getDataFolder(), "schematics/");
}