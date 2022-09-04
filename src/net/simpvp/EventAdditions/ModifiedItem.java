package net.simpvp.EventAdditions;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ModifiedItem {

    Player player;
    ItemStack item;
    int throwsFartherScalar = 0;
    int smokeRadius = 0;
    int flameRadius = 0;
    int damage = 0;
    int cooldownSeconds = 0;
    boolean depletable = true;
    ArrayList<Integer> potionIds = new ArrayList<>();
    UUID projectileID;



    public ModifiedItem(ItemStack item, Player player) {
        this.item = item;
        this.player = player;
        initializeModifierStrength();
    }

    public void initializeModifierStrength() {
        if (!Objects.requireNonNull(item.getItemMeta()).hasLore()) {
            return;
        }

        try {

            List<String> modifierValue = hasModifyingItemLore(item, "ThrowsFarther");
            if (modifierValue.size() > 0 && Integer.parseInt(modifierValue.get(0).strip()) > 0) {
                throwsFartherScalar = Integer.parseInt(modifierValue.get(0).strip());
            }

            modifierValue = hasModifyingItemLore(item, "Smoking");
            if (modifierValue.size() > 0 && Integer.parseInt(modifierValue.get(0).strip()) > 0) {
                smokeRadius = Integer.parseInt(modifierValue.get(0).strip());
            }

            modifierValue = hasModifyingItemLore(item, "Flaming");
            if (modifierValue.size() > 0 && Integer.parseInt(modifierValue.get(0).strip()) > 0) {
                flameRadius = Integer.parseInt(modifierValue.get(0).strip());
            }

            modifierValue = hasModifyingItemLore(item, "Damaging");
            if (modifierValue.size() > 0 && Integer.parseInt(modifierValue.get(0).strip()) > 0) {
                damage = Integer.parseInt(modifierValue.get(0).strip());
            }

            modifierValue = hasModifyingItemLore(item, "Cooldown");
            if (modifierValue.size() > 0 && Integer.parseInt(modifierValue.get(0).strip()) > 0) {
                cooldownSeconds = Integer.parseInt(modifierValue.get(0).strip());
            }

            modifierValue = hasModifyingItemLore(item, "Depletable");
            if (modifierValue.size() > 0 && Objects.equals(modifierValue.get(0).strip(), "false")) {
                depletable = false;
            }


            modifierValue = hasModifyingItemLore(item, "Potion");
            if (modifierValue.size() > 0) {
                for (String s : modifierValue) {


                    String[] separatePotionEffects = s.split(" ");


                    player.addPotionEffect(new PotionEffect(Objects.requireNonNull(PotionEffectType.getByName(separatePotionEffects[0])), Integer.parseInt(separatePotionEffects[1]) * 20, Integer.parseInt(separatePotionEffects[2]) - 1));


                }
            }
        } catch (Exception e) {
            EventAdditions.instance.getLogger().info(e.getMessage());
        }


    }



    public List<String> hasModifyingItemLore(ItemStack item, String lore) {
        List<String> stringList = Objects.requireNonNull(item.getItemMeta()).getLore();

        List<String> matches = new ArrayList<>();

        assert stringList != null;
        for (String element: stringList) {

            if (element.startsWith(lore)) {

                String substring = element.substring(element.lastIndexOf(':') + 1);
                matches.add(substring);
            }
        }
        return matches;
    }

    public boolean isItemThrowable() {
        Material item = this.item.getType();
        return item == Material.SPLASH_POTION || item == Material.LINGERING_POTION || item == Material.EGG ||
                item == Material.SNOWBALL || item == Material.ENDER_PEARL;
    }

    public ArrayList<Block> getBlocksInRadius(Block start, int radius){
        ArrayList<Block> blocks = new ArrayList<>();
        for(double x = start.getLocation().getX() - radius; x <= start.getLocation().getX() + radius; x++){
            for(double y = start.getLocation().getY() - radius; y <= start.getLocation().getY() + radius; y++){
                for(double z = start.getLocation().getZ() - radius; z <= start.getLocation().getZ() + radius; z++){
                    Location loc = new Location(start.getWorld(), x, y, z);
                    blocks.add(loc.getBlock());
                }
            }
        }
        return blocks;
    }

    public void startSmokeTask(Entity entity) {
        Block centerBlock = entity.getLocation().getBlock();

        if (entity.getLocation().getWorld() != null) {
            Location entityLocation = entity.getLocation();
            Location loc = centerBlock.getLocation();
            ArrayList<Block> blocksInRadius = getBlocksInRadius(centerBlock, smokeRadius);

            new BukkitRunnable() {
                private int i = 0;
                @Override
                public void run() {
                    if(i >= 5) {
                        cancel();
                    }
                    ++i;
                    spawnSmoke(blocksInRadius, loc, entityLocation);
                    playSoundEffect(loc, Sound.BLOCK_FIRE_EXTINGUISH, .8F, .1F);

                }
            }.runTaskTimer(EventAdditions.instance, 0, 40L);
        }
    }


    public void spawnSmoke(ArrayList<Block> blocksInRadius, Location loc, Location entityLocation) {
        for (Block block : blocksInRadius) {
            if (block.getType() != Material.AIR) continue;
            if (entityLocation.getWorld() == null) continue;
            Vector vel = new Vector(block.getX() - loc.getX(), block.getY() - loc.getY(), block.getZ() - loc.getZ());
            entityLocation.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, block.getLocation(), 10, vel.getX(), vel.getY(), vel.getZ(), 0.05);
        }
    }

    public void spawnFire(Entity entity) {
        Block centerBlock = entity.getLocation().getBlock();
        ArrayList<Block> blocksInRadius = getBlocksInRadius(centerBlock, flameRadius);
        for (Block block: blocksInRadius) {
            if (block.getType() != Material.AIR) continue;
            block.setType(Material.FIRE);
        }
    }

    public void summonModifiedSnowball(EntityType entityType) {
        Location location = player.getLocation();
        location.setY(location.getY() + 1.5);
        ThrowableProjectile projectile = (ThrowableProjectile) player.getWorld().spawnEntity(location, entityType);
        projectile.setItem(item);
        projectile.setVelocity((player.getLocation()).getDirection());
        projectileID = projectile.getUniqueId();
        EventListener.modifiedItems.put(projectile.getUniqueId(),this);
    }

    public void summonModifiedPotion() {
        Location location = player.getLocation();
        location.setY(location.getY() + 1.5);
        ThrownPotion thrownPotion = (ThrownPotion) player.getWorld().spawnEntity(location, EntityType.SPLASH_POTION);
        thrownPotion.setItem(item);
        if (throwsFartherScalar <= 0) {
            thrownPotion.setVelocity((player.getLocation()).getDirection().multiply(.5));
        } else {
            thrownPotion.setVelocity((player.getLocation()).getDirection().multiply(throwsFartherScalar));
        }

        projectileID = thrownPotion.getUniqueId();

        Objects.requireNonNull(location.getWorld()).playSound(location, Sound.ENTITY_SPLASH_POTION_THROW, 1, .1f);
        EventListener.modifiedItems.put(thrownPotion.getUniqueId(), this);
    }


    public void summonModifiedProjectile() {

        if (item.getType() == Material.SNOWBALL) {
            summonModifiedSnowball(EntityType.SNOWBALL);
        }

        if (item.getType() == Material.EGG) {
            summonModifiedSnowball(EntityType.EGG);
        }

        if (item.getType() == Material.SPLASH_POTION) {
            summonModifiedPotion();
        }

        if (item.getType() == Material.LINGERING_POTION) {
            summonModifiedPotion();
        }

    }




    public void playSoundEffect(Location location, Sound sound, float volume, float pitch) {
        Objects.requireNonNull(location.getWorld()).playSound(location, sound, volume , pitch);
    }

    public UUID getUniqueId() {
        return projectileID;
    }

    public int getDamage() {
        return damage;
    }

    public boolean isDepletable() {
        return depletable;
    }

    public void removeFromHashMap() {

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(EventAdditions.instance, () -> {
            EventListener.modifiedItems.remove(projectileID);
        }, 0);

    }





}
