package net.ezplace.librebuild.utils;

import net.ezplace.librebuild.LibreBuild;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LibreBuildMessages {
    private static LibreBuildMessages instance = new LibreBuildMessages();
    private YamlConfiguration messages;
    private Map<String, String> messageCache = new HashMap<>();

    private LibreBuildMessages() {}

    public static LibreBuildMessages getInstance() {
        return instance;
    }

    public void loadMessages() {

        File langFolder = new File(LibreBuild.getInstance().getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        File file = new File(langFolder, LibreBuildSettings.LANGUAGE + ".yml");

        if (!file.exists()) {
            LibreBuild.getInstance().saveResource("lang/" + LibreBuildSettings.LANGUAGE + ".yml", false);
        }

        messages = YamlConfiguration.loadConfiguration(file);
        messageCache.clear();

        messages.getKeys(true).forEach(key -> messageCache.put(key, messages.getString(key, key)));
    }

    public String getMessage(String key, Map<String, String> placeholders) {
        String message = messageCache.getOrDefault(key, key);

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }

        return message.replace("&", "§");
    }

    public String getMessage(String key) {
        return getMessage(key, null);
    }
}
