package net.ezplace.librebuild.utils;

import net.ezplace.librebuild.LibreBuild;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class LibreBuildSettings {
    private final static LibreBuildSettings instance = new LibreBuildSettings();

    private File file;
    private YamlConfiguration config;

    public static String LANGUAGE;

    public static Material ITEM_MATERIAL;
    public static String ITEM_PREFIX;

    public static boolean PARTICLES;
    public static org.bukkit.Color PARTICLES_COLOR;
    public static Particle PARTICLES_TYPE;
    public static boolean REPLACE_AIR;

    private LibreBuildSettings(){

    }
    public static LibreBuildSettings getInstance() {
        return instance;
    }

    public void load(){
        file = new File(LibreBuild.getInstance().getDataFolder(), "config.yml");

        if (!file.exists()){
            LibreBuild.getInstance().saveResource("config.yml",false);
        }

        config = new YamlConfiguration();
        config.options().parseComments(true);

        try {
            config.load(file);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        LANGUAGE = String.valueOf(config.getString("Lang"));

        ITEM_MATERIAL = Material.valueOf(config.getString("Item.Material"));
        ITEM_PREFIX = String.valueOf(config.getString("Item.Prefix"));

        PARTICLES = config.getBoolean("Particles.Enabled");
        PARTICLES_TYPE = Particle.valueOf(config.getString("Particles.Type"));
        REPLACE_AIR = config.getBoolean("ReplaceAir");
        try {
            String hexColor = config.getString("Particles.Color", "#FFFFFF");
            java.awt.Color tempColor = java.awt.Color.decode(hexColor);
            PARTICLES_COLOR = org.bukkit.Color.fromRGB(tempColor.getRed(), tempColor.getGreen(), tempColor.getBlue());
        } catch (NumberFormatException e) {
            PARTICLES_COLOR = org.bukkit.Color.WHITE;
            LibreBuild.getInstance()
                    .getLogger()
                    .warning(LibreBuildMessages.getInstance().getMessage(LibreBuildMessages.getInstance().getMessage("color.error")));
        }
    }

    public void save(){
        try{
            config.save(file);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void set(String path, Object value){
        config.set(path,value);
        save();
    }

}
