package net.simpvp.EventAdditions.gameObjects;

import net.simpvp.EventAdditions.EventAdditions;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ShopItem {
    String id;
    Material material;
    String customName;
    List<String> lore;
    Map<Enchantment, Integer> enchants;
    boolean unbreakable;
    int amount;
    ShopItem upgradeItem;
    int upgradeCost;
    List<Player> playersUpgraded;


    public ShopItem(String id, String customName, Material material, List<String> lore,
                    Map<Enchantment, Integer> enchants, boolean unbreakable, int amount,
                    ShopItem upgradeItem, int upgradeCost) {
        this.id = id;
        this.material = material;
        this.customName = customName;
        this.lore = lore;
        this.enchants = enchants;
        this.unbreakable = unbreakable;
        this.amount = amount;
        this.upgradeItem = upgradeItem;
        this.upgradeCost = upgradeCost;
        playersUpgraded = new ArrayList<>();
    }


    // Create the shop item and give it to the player
    public void giveItem(Player player) {
        if (upgradeItem != null && playersUpgraded.contains(player)) {
            upgradeItem.giveItem(player);
            return;
        }

        ItemStack item = new ItemStack(material, amount);

        ItemMeta itemMeta = item.getItemMeta();

        if (itemMeta == null) {
            EventAdditions.instance.getLogger().info("Item meta is null");
            return;
        }
        if (customName != null) {
            itemMeta.setDisplayName(customName);
        }

        if (!lore.isEmpty()) {
            itemMeta.setLore(lore);
        }

        if (unbreakable) {
            itemMeta.setUnbreakable(true);
        }

        item.setItemMeta(itemMeta);

        // Enchant the item
        if (!enchants.isEmpty()) {
            enchants.forEach(item::addUnsafeEnchantment);
        }

        player.getInventory().addItem(item);
        playersUpgraded.add(player);
    }


    // Return a string with information about this item
    public String printItem() {
        String itemName = material.toString();
        itemName = itemName.replace('_',' ').toLowerCase();
        StringBuilder display = new StringBuilder(ChatColor.GOLD + "\nItem: " + itemName);

        if (!enchants.isEmpty()) {
            display.append("\nEnchantments:");
        }
        for (Map.Entry<Enchantment, Integer> e : enchants.entrySet()) {
            display.append("\n   ").append(e.getKey().getKey()).append(" lvl ").append(e.getValue());
        }
       return display.toString();
    }


    // Return true if the given id matches this object
    public boolean isItemId(String id) {
        return (Objects.equals(this.id, id));
    }


    public Material getMaterial() {
        return material;
    }


    // Return the current item upgrade that the player is on. Do this
    // by iterating through the linked shop item objects
    public ShopItem getCurrentUpgradeItem(Player player) {
        ShopItem temp = this;
        ShopItem currentUpgradeItem = null;

        while (temp != null) {
            if (temp.containsUpgradedPlayer(player)) {
                currentUpgradeItem = temp;
            }
            temp = temp.upgradeItem;
        }

        return currentUpgradeItem;
    }


    public ShopItem getUpgradeItem() {
        return upgradeItem;
    }


    public int getUpgradeCost() {
        return upgradeCost;
    }


    // return true if the given player has gotten this upgraded item
    public boolean containsUpgradedPlayer(Player player) {
        return (playersUpgraded.contains(player));
    }
}
