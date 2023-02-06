package net.simpvp.EventAdditions.gameObjects;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ShopSign {

    private final List<ShopItem> items;
    private final int cost;
    private final Location location;


    public ShopSign(Location location, List<ShopItem> items, int cost) {
        this.items = items;
        this.cost = cost;
        this.location = location;
    }

    public void transaction(Player player) {

        if (player.getLevel() < cost) {
            player.sendMessage(
                    ChatColor.GOLD + "<" +
                    ChatColor.DARK_RED + "!!!" +
                    ChatColor.GOLD + ">" +
                    ChatColor.RED + " Hey! Come back when you have " +
                    cost + " levels if you want this item!");
            player.playSound(location, Sound.ENTITY_WANDERING_TRADER_NO, .5f, 1.25f);
            return;
        }

        for (ShopItem shopItem : items) {
            shopItem.giveItem(player);
        }

        player.setLevel(player.getLevel() - cost);
        player.sendMessage(ChatColor.GREEN + "Thanks! Here you go.");
        player.playSound(location, Sound.ENTITY_VILLAGER_CELEBRATE, .5f, 1.25f);
    }


    public boolean isShopLocation(Location location) {
        return (this.location.equals(location));
    }
}
