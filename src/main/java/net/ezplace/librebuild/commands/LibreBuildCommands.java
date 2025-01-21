package net.ezplace.librebuild.commands;

import com.sk89q.worldedit.math.BlockVector3;
import net.ezplace.librebuild.handlers.ItemHandler;
import net.ezplace.librebuild.schem.SchemReader;
import net.ezplace.librebuild.schem.SchemWriter;
import net.ezplace.librebuild.utils.LibreBuildMessages;
import net.ezplace.librebuild.utils.LibreBuildSettings;
import org.bukkit.Bukkit;
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
        if (!sender.hasPermission("librebuild.command.admin")) {
            sender.sendMessage("§f-----------§6Libre§0Build§f-----------");
            sender.sendMessage("§8Made by §4AndrewYerNau");
            sender.sendMessage("§8Version 1.0.0");
            return true;
        }

        if (args.length == 0) {
            return false;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                sendHelpMessage(sender);
                return true;

            case "create":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(LibreBuildMessages.getInstance().getMessage("command.console.error"));
                    return true;
                }
                Player player = (Player) sender;
                if (args.length != 8) {
                    player.sendMessage(LibreBuildMessages.getInstance().getMessage("command.create.usage"));
                    return true;
                }
                player.sendMessage(LibreBuildMessages.getInstance().getMessage("schem.create"));
                handleCreateCommand(player, args);
                return true;

            case "load":
                if (args.length != 2) {
                    sender.sendMessage(LibreBuildMessages.getInstance().getMessage("command.load.usage"));
                    return true;
                }
                if (sender instanceof Player) {
                    player = (Player) sender;
                    player.sendMessage(LibreBuildMessages.getInstance().getMessage("schem.load") + args[1] + "...");
                    handleLoadCommand(player, args[1]);
                } else {
                    sender.sendMessage(LibreBuildMessages.getInstance().getMessage("command.console.error"));
                }
                return true;

            case "item":
                if (args.length == 3 && args[1].equalsIgnoreCase("get")) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(LibreBuildMessages.getInstance().getMessage("command.console.error.item"));
                        return true;
                    }
                    player = (Player) sender;
                    handleItemGetCommand(player, args[2]);
                    return true;
                }

                if (args.length == 4 && args[1].equalsIgnoreCase("player")) {
                    Player target = Bukkit.getPlayer(args[2]);
                    if (target == null) {
                        sender.sendMessage(LibreBuildMessages.getInstance().getMessage("command.item.notonline"));
                        return true;
                    }
                    handleItemGetCommand(target, args[3]);
                    return true;
                }

                sender.sendMessage(LibreBuildMessages.getInstance().getMessage("command.give.usage"));
                sender.sendMessage(LibreBuildMessages.getInstance().getMessage("command.give.usage1"));
                sender.sendMessage(LibreBuildMessages.getInstance().getMessage("command.give.usage2"));
                return true;

            case "reload":
                LibreBuildSettings.getInstance().load();
                sender.sendMessage(LibreBuildMessages.getInstance().getMessage("plugin.reload"));
                return true;

            default:
                sender.sendMessage(LibreBuildMessages.getInstance().getMessage("command.notfound"));
                return true;
        }
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("")) {
            completions.add("");
            return completions;
        }

        if (args.length == 1) {
            completions.addAll(Arrays.asList("help", "reload", "create", "load", "item"));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("load")) {
                completions.addAll(getSchematicNames());
            } else if (args[0].equalsIgnoreCase("item")) {
                completions.addAll(Arrays.asList("get", "player"));
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("item") && args[1].equalsIgnoreCase("player")) {
            completions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
        } else if (args.length == 3 && args[0].equalsIgnoreCase("item") && args[1].equalsIgnoreCase("get")) {
            completions.addAll(getSchematicNames());
        } else if (args.length == 4 && args[0].equalsIgnoreCase("item") && args[1].equalsIgnoreCase("player")) {
            completions.addAll(getSchematicNames());
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

    private void sendHelpMessage(CommandSender player) {
        player.sendMessage(LibreBuildMessages.getInstance().getMessage("command.help.title"));
        player.sendMessage(LibreBuildMessages.getInstance().getMessage("command.help.help"));
        player.sendMessage(LibreBuildMessages.getInstance().getMessage("command.help.create"));
        player.sendMessage(LibreBuildMessages.getInstance().getMessage("command.help.load"));
        player.sendMessage(LibreBuildMessages.getInstance().getMessage("command.help.itemget"));
        player.sendMessage(LibreBuildMessages.getInstance().getMessage("command.help.itemplayer"));
    }

    private void handleCreateCommand(Player player, String[] args) {
        try {
            BlockVector3 min = BlockVector3.at(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
            BlockVector3 max = BlockVector3.at(Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]));
            String name = args[7];

            if (SchemWriter.saveSchematic(player, min, max, name)) {
                player.sendMessage(LibreBuildMessages.getInstance().getMessage("schem.success.save"));
            } else {
                player.sendMessage(LibreBuildMessages.getInstance().getMessage("schem.error.save"));
            }
        } catch (NumberFormatException e) {
            player.sendMessage(LibreBuildMessages.getInstance().getMessage("schem.error.coords"));
        }
    }

    private void handleLoadCommand(Player player, String name) {
        File file = new File(schematicsFolder, name + ".schem");
        if (!file.exists()) {
            player.sendMessage(LibreBuildMessages.getInstance().getMessage("command.item.notfound") + name + ".");
        } else {
            SchemReader.pasteSchematic(player, file);
            player.sendMessage(LibreBuildMessages.getInstance().getMessage("schem.placed"));
        }
    }

    private void handleItemGetCommand(Player player, String name) {
        File file = new File(schematicsFolder, name + ".schem");
        if (!file.exists()) {
            player.sendMessage(LibreBuildMessages.getInstance().getMessage("command.item.notfound") + name + ".");
        } else {
            ItemStack schematicItem = itemHandler.createSchematicItem(name);
            player.getInventory().addItem(schematicItem);
            player.sendMessage(LibreBuildMessages.getInstance().getMessage("command.item.get"));
        }
    }
}