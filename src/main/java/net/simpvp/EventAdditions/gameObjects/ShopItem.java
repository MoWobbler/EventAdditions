package net.simpvp.EventAdditions.gameObjects;

import net.simpvp.EventAdditions.EventAdditions;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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


    public ShopItem(String id, String customName, Material material, List<String> lore,
                    Map<Enchantment, Integer> enchants, boolean unbreakable, int amount) {
        this.id = id;
        this.material = material;
        this.customName = customName;
        this.lore = lore;
        this.enchants = enchants;
        this.unbreakable = unbreakable;
        this.amount = amount;
    }


    // Create the shop item and give it to the player
    public void giveItem(Player player) {
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
    }


    // Return true if the given id matches this object
    public boolean isItemId(String id) {
        return (Objects.equals(this.id, id));
    }

    public Material getMaterial() {
        return material;
    }
}
