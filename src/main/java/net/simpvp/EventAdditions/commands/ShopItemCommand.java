package net.simpvp.EventAdditions.commands;

import net.simpvp.EventAdditions.EventAdditions;
import net.simpvp.EventAdditions.gameObjects.ShopItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShopItemCommand implements CommandExecutor {

    public static List<ShopItem> shopItems = new ArrayList<>();

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof BlockCommandSender)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run in a command block");
            return false;
        }

        BlockCommandSender cmdBlock = (BlockCommandSender) sender;
        if (!EventAdditions.listOfWorlds.contains(cmdBlock.getBlock().getWorld().getName())) {
            cmdBlock.sendMessage(ChatColor.RED + "This world does not have EventAdditions enabled");
            return false;
        }

        if (args.length == 1 && Objects.equals(args[0], "delete")) {
            shopItems.clear();
            cmdBlock.sendMessage("Deleted all shop items");
            return true;
        }

        if (args.length < 3) {
            cmdBlock.sendMessage("Correct usage: /shopitem id material count {data}");
            return false;
        }

        String id = args[0];
        // Check if id is unique
        for (ShopItem shopItem : shopItems) {
            if (shopItem.isItemId(args[0])) {
                cmdBlock.sendMessage(ChatColor.RED + "A shop item with that id already exists");
                return false;
            }
        }

        // Combine the remaining args into a single input string for the regex
        StringBuilder builder = new StringBuilder();
        for (int i = 3; i < args.length; i++) {
            builder.append(args[i]);
            builder.append(" ");
        }
        String input = builder.toString();

        // Regex to get an optional custom name
        String customName = null;
        Pattern customNamePattern = Pattern.compile("customName:\"(.*?)\"");
        Matcher matcher = customNamePattern.matcher(input);
        if (matcher.find()) {
            customName = matcher.group(1);
        }

        // Regex to get an items enchantments
        Map<Enchantment, Integer> enchantments = new HashMap<>();
        Pattern enchantPattern = Pattern.compile("enchants:\"([\\w]+)\":\"([\\w]+)\"");
        matcher = enchantPattern.matcher(input);
        while (matcher.find()) {
            Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(matcher.group(1).strip().toLowerCase()));
            if (enchantment == null) {
                cmdBlock.sendMessage("Invalid enchantment: " + matcher.group(1).strip().toLowerCase());
                return false;
            }
            try {
                enchantments.put(enchantment, Integer.parseInt(matcher.group(2).strip()));
            } catch (NumberFormatException e) {
                cmdBlock.sendMessage("Invalid enchantment level: " + matcher.group(2).strip());
                return false;
            }
        }

        // Regex to get optional lore to add to an item
        Pattern lorePattern = Pattern.compile("lore:\"([\\w\\s:]+)\"");
        matcher = lorePattern.matcher(input);
        List<String> lore = new ArrayList<>();
        while (matcher.find()) {
            lore.add(matcher.group(1));
        }

        // Regex to see if the item should be unbreakable
        boolean unbreakable = false;
        Pattern unbreakablePattern = Pattern.compile("unbreakable:\"(true|false)\"");
        matcher = unbreakablePattern.matcher(input);
        if (matcher.find()) {
            unbreakable = Boolean.parseBoolean(matcher.group(1));
        }

        // Check for a shop item id for when this item is upgraded
        Pattern upgradePattern = Pattern.compile("upgrade:\"([\\w]+)\"");
        matcher = upgradePattern.matcher(input);
        ShopItem upgrade = null;
        if (matcher.find()) {
            for (ShopItem shopItem : shopItems) {
                if (shopItem.isItemId(matcher.group(1))) {
                    upgrade = shopItem;
                }
            }
        }

        // Get how much an upgrade costs
        Pattern upgradeCostPattern = Pattern.compile("upgradecost:\"([\\w]+)\"");
        matcher = upgradeCostPattern.matcher(input);
        int upgradeCost = 0;
        if (matcher.find()) {
            try {
                upgradeCost = Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                cmdBlock.sendMessage("Invalid upgrade cost: " + matcher.group(1));
                return false;
            }
        }

        // Make sure the given material is valid
        Material material;
        try {
            material = Material.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            cmdBlock.sendMessage("Invalid material: " + args[1]);
            return false;
        }

        // Make sure the given amount of objects is valid
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            cmdBlock.sendMessage("Invalid item amount: " + args[2]);
            return false;
        }

        ShopItem shopItem = new ShopItem(id, customName, material, lore, enchantments, unbreakable, amount,
                upgrade, upgradeCost);
        shopItems.add(shopItem);
        cmdBlock.sendMessage("Added a new shop item");

        return true;
    }
}
