package net.simpvp.EventAdditions.gameObjects;

import net.simpvp.EventAdditions.EventAdditions;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
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
    public void spawn(Location location, int amount, int healthScale) {

        int playerCount = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld() == location.getWorld()) {
                double distance = player.getLocation().distance(location);

                if (distance <= 3000 && (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE)) {
                    playerCount++;
                }
            }
        }

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

            if (playerCount == 0) continue;

            double health = e.getHealth();
            double additionalHealth = Math.log(healthScale) * playerCount;
            //System.out.println(health + additionalHealth * 2);
            Objects.requireNonNull(e.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(health + additionalHealth * 2);
            e.setHealth(health + additionalHealth * 2);
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
