package net.ezplace.librebuild.commands;

import com.sk89q.worldedit.math.BlockVector3;
import net.ezplace.librebuild.handlers.ItemHandler;
import net.ezplace.librebuild.schem.SchemReader;
import net.ezplace.librebuild.schem.SchemWriter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LibreBuildCommands implements CommandExecutor, TabCompleter {

    private final File schematicsFolder;
    private final ItemHandler itemHandler;

    public LibreBuildCommands(File schematicsFolder, ItemHandler itemHandler) {
        this.schematicsFolder = schematicsFolder;
        this.itemHandler = itemHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§aUsa /lb help para ver los comandos disponibles.");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando solo puede ser usado por jugadores.");
            return true;
        }

        Player player = (Player) sender;
        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                sendHelpMessage(player);
                return true;
            case "create":
                if (args.length != 8) {
                    player.sendMessage("§cUso correcto: /lb create <x1 y1 z1> <x2 y2 z2> <nombre>");
                    return true;
                }
                player.sendMessage("§eCreando esquemática...");
                handleCreateCommand(player, args);
                return true;
            case "load":
                if (args.length != 2) {
                    player.sendMessage("§cUso correcto: /lb load <nombre>");
                    return true;
                }
                player.sendMessage("§eCargando esquemática " + args[1] + "...");
                handleLoadCommand(player, args[1]);
                return true;
            case "item":
                if (args.length == 3 && args[1].equalsIgnoreCase("get")) {
                    player.sendMessage("§eDando item con esquemática " + args[2] + "...");
                    handleItemGetCommand(player, args[2]);
                    return true;
                }
                player.sendMessage("§cUso correcto: /lb item get <nombre>");
                return true;
            default:
                player.sendMessage("§cComando no reconocido. Usa /lb help.");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("help", "create", "load", "item"));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("load") || (args[0].equalsIgnoreCase("item") && args[1].equalsIgnoreCase("get"))) {
                completions.addAll(getSchematicNames());
            } else if (args[0].equalsIgnoreCase("item")) {
                completions.add("get");
            }
        }

        return completions;
    }

    private List<String> getSchematicNames() {
        List<String> schemList = new ArrayList<>();
        if (schematicsFolder.exists() && schematicsFolder.isDirectory()) {
            File[] files = schematicsFolder.listFiles((dir, name) -> name.endsWith(".schem") || name.endsWith(".schematic"));
            if (files != null) {
                for (File file : files) {
                    schemList.add(file.getName().replace(".schem", "").replace(".schematic", ""));
                }
            }
        }
        return schemList;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage("§a--- Comandos de LibreBuild ---");
        player.sendMessage("§e/lb help §7- Muestra esta ayuda.");
        player.sendMessage("§e/lb create <x1 y1 z1> <x2 y2 z2> <nombre> §7- Guarda una estructura.");
        player.sendMessage("§e/lb load <nombre> §7- Carga una estructura y la pega.");
        player.sendMessage("§e/lb item get <nombre> §7- Obtiene un ítem con el schematic.");
    }

    private void handleCreateCommand(Player player, String[] args) {
        try {
            BlockVector3 min = BlockVector3.at(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
            BlockVector3 max = BlockVector3.at(Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]));
            String name = args[7];

            if (SchemWriter.saveSchematic(player, min, max, name)) {
                player.sendMessage("§aEsquemática '" + name + "' guardada correctamente.");
            } else {
                player.sendMessage("§cError al guardar la esquemática.");
            }
        } catch (NumberFormatException e) {
            player.sendMessage("§cLas coordenadas deben ser números enteros.");
        }
    }

    private void handleLoadCommand(Player player, String name) {
        File file = new File(schematicsFolder, name + ".schem");
        if (!file.exists()) {
            player.sendMessage("§cNo se encontró la esquemática '" + name + "'.");
        } else {
            SchemReader.pasteSchematic(player, file);
            player.sendMessage("§aEsquemática '" + name + "' cargada correctamente.");
        }
    }

    private void handleItemGetCommand(Player player, String name) {
        File file = new File(schematicsFolder, name + ".schem");
        if (!file.exists()) {
            player.sendMessage("§cNo se encontró la esquemática '" + name + "'.");
        } else {
            ItemStack schematicItem = itemHandler.createSchematicItem(name);
            player.getInventory().addItem(schematicItem);
            player.sendMessage("§aRecibiste el ítem de la esquemática '" + name + "'.");
        }
    }
}