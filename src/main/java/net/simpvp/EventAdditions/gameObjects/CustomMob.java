package net.simpvp.EventAdditions.gameObjects;

import net.simpvp.EventAdditions.EventAdditions;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Comparator;
import java.util.Objects;

public class CustomMob {
    YamlConfiguration config;
    String mobId;
    EntityType mobType;
    String name;
    Boolean isNameVisible;
    Boolean betterTargeting;
    int percentage;
    Material helmet;
    Material chestplate;
    Material leggings;
    Material boots;
    Material mainWeapon;
    Material secondaryWeapon;


    public CustomMob(String mobId, EntityType mobType, String name, Boolean isNameVisible, Boolean betterTargeting,int percentage, Material helmet,
                     Material chestplate, Material leggings, Material boots,
                     Material mainWeapon, Material secondaryWeapon) {
        config = EventAdditions.mobConfig.getConfig();
        this.mobId = mobId;
        this.mobType = mobType;
        this.name = name;
        this.isNameVisible = isNameVisible;
        this.betterTargeting = betterTargeting;
        this.percentage = percentage;
        this.helmet = helmet;
        this.chestplate = chestplate;
        this.leggings = leggings;
        this.boots = boots;
        this.mainWeapon = mainWeapon;
        this.secondaryWeapon = secondaryWeapon;
    }


    /* Spawn the mob at the given location */
    public void spawn(Location location, int amount) {
        for (int i = 0; i < amount; i++) {
            LivingEntity e = (LivingEntity) Objects.requireNonNull(location.getWorld()).
                    spawnEntity(location, mobType);
            e.setCanPickupItems(false);
            e.setRemoveWhenFarAway(false);
            e.setMetadata("mobWaveMob", new FixedMetadataValue(EventAdditions.instance, true));
            e.setCustomName(name);
            e.setCustomNameVisible(isNameVisible);
            if (e.getEquipment() == null) {
                return;
            }
            e.getEquipment().setHelmet(new ItemStack(helmet));
            e.getEquipment().setChestplate(new ItemStack(chestplate));
            e.getEquipment().setLeggings(new ItemStack(leggings));
            e.getEquipment().setBoots(new ItemStack(boots));
            e.getEquipment().setItemInMainHand(new ItemStack(mainWeapon));
            e.getEquipment().setItemInOffHand(new ItemStack(secondaryWeapon));
            if (betterTargeting) {
                if (e instanceof Creature) {
                    ((Creature) e).setTarget(getNearestPlayer(e));
                }
            }
        }
    }

    /*  */
    private Player getNearestPlayer(Entity e) {
        return (e.getWorld().getPlayers().stream()
                .filter(player -> player.getGameMode().name().equals("SURVIVAL")
                        || player.getGameMode().name().equals("ADVENTURE"))
                .min(Comparator.comparingDouble(p -> p.getLocation().distanceSquared(e.getLocation())))
                .orElse(null));
    }

    public int getPercentage() {
        return this.percentage;
    }
}
