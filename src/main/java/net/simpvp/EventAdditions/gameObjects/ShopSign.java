package net.simpvp.EventAdditions.gameObjects;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public class ShopSign {
    private final List<ShopItem> items;
    private final int cost;
    private final Location location;
    String successMessage;
    String failureMessage;


    public ShopSign(Location location, List<ShopItem> items, int cost, String successMessage, String failureMessage) {
        this.items = items;
        this.cost = cost;
        this.location = location;
        this.successMessage = successMessage;
        this.failureMessage = failureMessage;
    }


    // Attempt to sell the shop items to the player
    public void transaction(Player player) {
        int calculatedCost = 0;

        for (ShopItem shopItem : items) {
            ShopItem s = shopItem.getCurrentUpgradeItem(player);
            if (s != null) {
                calculatedCost += s.getUpgradeCost();
            }
        }

        if (calculatedCost == 0) {
            calculatedCost = cost;
        }

        if (player.getLevel() < calculatedCost) {
            player.sendMessage(
                    ChatColor.RED + failureMessage.replace("<cost>", String.valueOf(calculatedCost)));
            player.playSound(location, Sound.ENTITY_WANDERING_TRADER_NO, .5f, 1.25f);
            if (location.getWorld() == null) return;
            location.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, player.getLocation().add(0,1.5,0),
                    4, .5, .5, .5);
            return;
        }

        for (ShopItem shopItem : items) {
            shopItem.giveItem(player);
        }

        player.setLevel(player.getLevel() - calculatedCost);
        player.sendMessage(ChatColor.GREEN + successMessage);
        if (location.getWorld() == null) return;
        location.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0,1.5,0),
                13, .5, .5, .5);
        player.playSound(location, Sound.ENTITY_VILLAGER_CELEBRATE, .5f, 1.25f);
    }

    // Print what items are for sale and some of their properties
    public void printNextPurchase(Player player) {

        int calculatedCost = 0;
        for (ShopItem shopItem : items) {
            ShopItem s = shopItem.getCurrentUpgradeItem(player);
            if (s != null) {
                calculatedCost += s.getUpgradeCost();
            }
        }

        if (calculatedCost == 0) {
            calculatedCost = cost;
        }

        ChatColor color;

        if (player.getLevel() < calculatedCost) {
            color = ChatColor.DARK_RED;
        } else {
            color = ChatColor.GREEN;
        }

        StringBuilder message = new StringBuilder(color + "\nPrice: " + calculatedCost + " levels");
        for (ShopItem shopItem : items) {
            ShopItem s = shopItem.getCurrentUpgradeItem(player);
            if (s != null && s.getUpgradeItem() != null) {
                message.append(s.getUpgradeItem().printItem());
            } else message.append(Objects.requireNonNullElse(s, shopItem).printItem());
        }

        player.sendMessage(message.toString());
    }


    // Return true if this shop is at the given location
    public boolean isShopLocation(Location location) {
        return (this.location.equals(location));
    }
}
