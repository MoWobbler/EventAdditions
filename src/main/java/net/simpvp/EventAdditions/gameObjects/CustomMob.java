package net.simpvp.EventAdditions.gameObjects;

import net.simpvp.EventAdditions.EventAdditions;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Objects;

public class CustomMob {
    YamlConfiguration config;
    String mobId;
    EntityType mobType;
    String name;
    Boolean isNameVisible;
    int percentage;
    Material helmet;
    Material chestplate;
    Material leggings;
    Material boots;
    Material mainWeapon;
    Material secondaryWeapon;


    public CustomMob(String mobId, EntityType mobType, String name, Boolean isNameVisible, int percentage, Material helmet,
                     Material chestplate, Material leggings, Material boots,
                     Material mainWeapon, Material secondaryWeapon) {
        config = EventAdditions.mobConfig.getConfig();
        this.mobId = mobId;
        this.mobType = mobType;
        this.name = name;
        this.isNameVisible = isNameVisible;
        this.percentage = percentage;
        this.helmet = helmet;
        this.chestplate = chestplate;
        this.leggings = leggings;
        this.boots = boots;
        this.mainWeapon = mainWeapon;
        this.secondaryWeapon = secondaryWeapon;
    }


    /* Spawn the mob at the given location */
    public LivingEntity spawn(Location location) {
        LivingEntity e = (LivingEntity) Objects.requireNonNull(location.getWorld()).
                spawnEntity(location, mobType);
        e.setCanPickupItems(false);
        e.setRemoveWhenFarAway(false);
        e.setMetadata("mobWaveMob", new FixedMetadataValue(EventAdditions.instance, true));
        e.setCustomName(name);
        e.setCustomNameVisible(isNameVisible);
        if (e.getEquipment() == null) {
            return null;
        }
        e.getEquipment().setHelmet(new ItemStack(helmet));
        e.getEquipment().setChestplate(new ItemStack(chestplate));
        e.getEquipment().setLeggings(new ItemStack(leggings));
        e.getEquipment().setBoots(new ItemStack(boots));
        e.getEquipment().setItemInMainHand(new ItemStack(mainWeapon));
        e.getEquipment().setItemInOffHand(new ItemStack(secondaryWeapon));
        return e;
    }

    public int getPercentage() {
        return this.percentage;
    }
}
