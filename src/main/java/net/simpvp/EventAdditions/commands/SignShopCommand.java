package net.simpvp.EventAdditions.commands;

import net.simpvp.EventAdditions.EventAdditions;
import net.simpvp.EventAdditions.gameObjects.ShopItem;
import net.simpvp.EventAdditions.gameObjects.ShopSign;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SignShopCommand implements CommandExecutor {

    public static List<ShopSign> shops = new ArrayList<>();

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof BlockCommandSender)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run in a command block");
            return true;
        }

        BlockCommandSender cmdBlock = (BlockCommandSender) sender;
        if (!EventAdditions.listOfWorlds.contains(cmdBlock.getBlock().getWorld().getName())) {
            cmdBlock.sendMessage(ChatColor.RED + "This world does not have EventAdditions enabled");
            return true;
        }

        if (args.length == 1 && Objects.equals(args[0], "delete")) {
            shops.clear();
            cmdBlock.sendMessage("Deleted all shops");
            return true;
        }

        if (args.length < 5) {
            cmdBlock.sendMessage(ChatColor.RED + "Usage: /signshop x y z cost shopItemId1 shopItemId2 ...");
            return true;
        }


        int x, y, z, cost;
        try {
            x = Integer.parseInt(args[0]);
            y = Integer.parseInt(args[1]);
            z = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "The coordinates must be integers.");
            return true;
        }

        Location location = new Location(cmdBlock.getBlock().getWorld(), x, y, z);

        try {
            cost = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "The cost must be an integer.");
            return true;
        }

        List<ShopItem> customItems = new ArrayList<>();

        // Match the ids from the command with the ids in the shopItems list
        for (int i = 4; i < args.length; i++) {
            boolean itemFound = false;
            for (int j = 0; j < ShopItemCommand.shopItems.size(); j++) {
                if (ShopItemCommand.shopItems.get(j).isItemId(args[i])) {
                    customItems.add(ShopItemCommand.shopItems.get(j));
                    itemFound = true;
                }
            }
            if (!itemFound) {
                cmdBlock.sendMessage("No matching item found for id " + args[i]);
                return false;
            }
        }

        ShopSign shopSign = new ShopSign(location, customItems, cost);
        shops.add(shopSign);
        cmdBlock.sendMessage("Created new sign shop");

        return true;
    }
}





/*// Loop through the remaining arguments, two at a time
        for (int i = 4; i < args.length; i += 2) {
            String materialString = args[i];
            String quantityString = args[i + 1];

            Material item = Material.getMaterial(materialString);
            if (item == null) {
                cmdBlock.sendMessage("Invalid material: " + materialString);
                return true;
            }

            int quantity;
            try {
                quantity = Integer.parseInt(quantityString);
            } catch (NumberFormatException e) {
                cmdBlock.sendMessage("Invalid quantity: " + quantityString);
                return true;
            }

            // Add the item and quantity to the map
            items.put(item, quantity);
        }*/