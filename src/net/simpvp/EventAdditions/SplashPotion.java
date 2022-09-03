package net.simpvp.EventAdditions;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class SplashPotion {

    static ArrayList<UUID> flamingPotionIds = new ArrayList<>();
    static ArrayList<UUID> smokingPotionIds = new ArrayList<>();

    Player player;
    boolean isFlaming;
    boolean isSmoking;
    boolean throwFarther;
    ItemStack baseItem;

    SplashPotion(Player player, boolean isFlaming, boolean isSmoking, boolean throwFarther, ItemStack baseItem) {
        this.player = player;
        this.isFlaming = isFlaming;
        this.isSmoking = isSmoking;
        this.throwFarther = throwFarther;
        this.baseItem = baseItem;
        spawnPotion();
    }

    public void spawnPotion() {
        float scalar;
        if (throwFarther) {
            scalar = 1;
        } else {
            scalar = .5f;
        }

        Location location = player.getLocation();
        location.setY(location.getY() + 1.5);
        ThrownPotion thrownPotion = (ThrownPotion) player.getWorld().spawnEntity(location, EntityType.SPLASH_POTION);
        thrownPotion.setItem(baseItem);
        thrownPotion.setVelocity((player.getLocation()).getDirection().multiply(scalar));
        Objects.requireNonNull(location.getWorld()).playSound(location, Sound.ENTITY_SPLASH_POTION_THROW, 1, .1f);

        if (isFlaming) {
            flamingPotionIds.add(thrownPotion.getUniqueId());
        }

        if (isSmoking) {
            smokingPotionIds.add(thrownPotion.getUniqueId());
        }
    }

    public static boolean containsFlamingUUID(UUID uuid) {
        return flamingPotionIds.contains(uuid);
    }

    public static void removeFlamingUUID(UUID uuid) {
        flamingPotionIds.remove(uuid);
    }

    public static boolean containsSmokingUUID(UUID uuid) {
        return smokingPotionIds.contains(uuid);
    }

    public static void removeSmokingUUID(UUID uuid) {
        smokingPotionIds.remove(uuid);
    }






}
