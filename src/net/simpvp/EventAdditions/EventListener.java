package net.simpvp.EventAdditions;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.*;

public class EventListener implements Listener {

    ArrayList<UUID> snowballIds = new ArrayList<>();

    /* Test for players near a flag */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {

        if (!EventAdditions.listOfWorlds.contains(e.getPlayer().getWorld().getName())) {
            return;
        }

        if (e.getPlayer().getGameMode().equals(GameMode.SPECTATOR) || e.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }

        if (!CreateFlagCommand.flags.isEmpty()) {
            Team team = e.getPlayer().getScoreboard().getPlayerTeam(e.getPlayer());

            for (FlagObject flag: CreateFlagCommand.flags) {
                if (flag.getIsCaptured()) continue;
                if (flag.getTeam().equals(team)) continue;
                if (flag.isPlayerNear(e.getPlayer().getLocation()) && !flag.getIsTaskActive()) {
                    flag.nearbyPlayers.add(e.getPlayer());
                    flag.startTimerTask();
                    continue;
                }

                if (!flag.isPlayerNear(e.getPlayer().getLocation())) {
                    flag.nearbyPlayers.remove(e.getPlayer());
                }

                if (!flag.isPlayerNear(e.getPlayer().getLocation()) && flag.getIsTaskActive() && flag.nearbyPlayers.isEmpty()) {
                    flag.cancelTimerTask();
                }
            }
        }

        if (!CreateObjectiveCommand.objectives.isEmpty()) {
            Team team = e.getPlayer().getScoreboard().getPlayerTeam(e.getPlayer());
            for (ObjectiveObject objective: CreateObjectiveCommand.objectives) {
                boolean isInside = objective.isLocationInsideRegion(e.getPlayer().getLocation());

                if (objective.getObjectiveTeam().equals(team) && isInside) {
                    if (objective.teammatesAtObjective.contains(e.getPlayer())) continue;
                    objective.teammatesAtObjective.add(e.getPlayer());
                }
                if (!objective.getObjectiveTeam().equals(team) && isInside) {
                    if (objective.enemiesAtObjective.contains(e.getPlayer())) continue;
                    objective.enemiesAtObjective.add(e.getPlayer());
                }

                if (!isInside) {
                    objective.teammatesAtObjective.remove(e.getPlayer());
                    objective.enemiesAtObjective.remove(e.getPlayer());
                }

                if (objective.teammatesAtObjective.isEmpty() && !objective.enemiesAtObjective.isEmpty() && !objective.getIsTaskActive() && !objective.hasBeenCaptured()) {
                    objective.startTimer();
                    continue;
                }
                if (!isInside && objective.enemiesAtObjective.isEmpty() && objective.getIsTaskActive()) {
                    objective.stopTimer();
                    objective.messageNearbyPlayers("Attackers have been forced out of the objective", ChatColor.BLUE);
                    continue;
                }

                if (!objective.teammatesAtObjective.isEmpty() && objective.getIsTaskActive()) {
                    objective.stopTimer();
                    objective.messageNearbyPlayers("The objective is being contested!", ChatColor.BLUE);
                }
            }
        }
    }

    /* If a flag block is broken delete the flag object */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {

        if (!EventAdditions.listOfWorlds.contains(e.getPlayer().getWorld().getName())) {
            return;
        }

        if (!e.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }

        for (FlagObject flag: CreateFlagCommand.flags) {
            for (Block block: flag.flagBlocks) {
                if (block.equals(e.getBlock())) {
                    flag.removeFlag();
                    flag.cancelTimerTask();
                    CreateFlagCommand.flags.remove(flag);
                    return;
                }
            }
        }
    }

    /* Set each player's name to the color of their team */
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {

        if (!EventAdditions.listOfWorlds.contains(e.getPlayer().getWorld().getName())) {
            return;
        }

        Team team = e.getPlayer().getScoreboard().getPlayerTeam(e.getPlayer());
        if (team != null) {
            e.setFormat("<" + team.getColor() + e.getPlayer().getName()  + ChatColor.RESET + "> " + e.getMessage());
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {

        if (!EventAdditions.listOfWorlds.contains(Objects.requireNonNull(e.getEntity().getPlayer()).getWorld().getName())) {
            return;
        }

        if (e.getEntity().getPlayer().getGameMode().equals(GameMode.SPECTATOR) || e.getEntity().getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }

        for (FlagObject flag: CreateFlagCommand.flags) {
            flag.nearbyPlayers.remove(e.getEntity().getPlayer());
        }

        for (ObjectiveObject obj: CreateObjectiveCommand.objectives) {
            obj.enemiesAtObjective.remove(e.getEntity().getPlayer());
            obj.teammatesAtObjective.remove(e.getEntity().getPlayer());
            if (obj.enemiesAtObjective.isEmpty() && obj.getIsTaskActive()) {
                obj.stopTimer();
                obj.messageNearbyPlayers("Attackers have been pushed out of the objective", ChatColor.BLUE);
            }
            if (!obj.enemiesAtObjective.isEmpty() && obj.teammatesAtObjective.isEmpty() && !obj.getIsTaskActive()) {
                obj.startTimer();
            }
        }

        for (Timer timer: TimerCommand.timers) {
            timer.nearbyPlayers.remove(e.getEntity().getPlayer());
        }

        Player p = e.getEntity().getPlayer();
        if (TagMinigame.isPlayerTagged(p)) {
            p.removePotionEffect(PotionEffectType.SPEED);
            p.removePotionEffect(PotionEffectType.GLOWING);
            e.setDeathMessage(p.getDisplayName() + " was killed for being it");
            TagMinigame.taggedPlayers.remove(p);
        }
    }


    @EventHandler
    public void playerHit(EntityDamageByEntityEvent e) {
        if (!EventAdditions.listOfWorlds.contains(e.getEntity().getWorld().getName())) {
            return;
        }

        if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
            Player playerWhoGotHit = (Player) e.getEntity();
            Player playerWhoHit = (Player) e.getDamager();
            if (TagMinigame.isPlayerTagged(playerWhoHit) && !TagMinigame.isPlayerTagged(playerWhoGotHit)) {
                TagMinigame.untagPlayer(playerWhoHit);
                TagMinigame.tagPlayer(playerWhoGotHit, playerWhoGotHit.getDisplayName() + " has been tagged by " + playerWhoHit.getDisplayName());
            }
        }


        if (e.getDamager() instanceof Snowball && snowballIds.contains(e.getDamager().getUniqueId())) {
            e.setDamage(5);
            playSoundEffect(e.getDamager().getLocation(), Sound.ENTITY_WITHER_SHOOT, (float) .3,2);
            snowballIds.remove(e.getDamager().getUniqueId());
        }

    }

    @EventHandler
    public void playerTeleport(PlayerTeleportEvent e) {
        if (!EventAdditions.listOfWorlds.contains(Objects.requireNonNull(e.getPlayer()).getWorld().getName())) {
            return;
        }

        if (e.getPlayer().getGameMode().equals(GameMode.SPECTATOR) || e.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }

        for (FlagObject flag: CreateFlagCommand.flags) {
            flag.nearbyPlayers.remove(e.getPlayer());
        }

        for (ObjectiveObject obj: CreateObjectiveCommand.objectives) {
            obj.enemiesAtObjective.remove(e.getPlayer());
            obj.teammatesAtObjective.remove(e.getPlayer());
            if (obj.enemiesAtObjective.isEmpty() && obj.getIsTaskActive()) {
                obj.stopTimer();
                obj.messageNearbyPlayers("Attackers have been pushed out of the objective", ChatColor.BLUE);
            }
        }

    }

    @EventHandler
    public void logOut(PlayerQuitEvent e) {
        if (!EventAdditions.listOfWorlds.contains(Objects.requireNonNull(e.getPlayer()).getWorld().getName())) {
            return;
        }

        if (e.getPlayer().getGameMode().equals(GameMode.SPECTATOR) || e.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }

        for (FlagObject flag: CreateFlagCommand.flags) {
            flag.nearbyPlayers.remove(e.getPlayer());
        }

        for (ObjectiveObject obj: CreateObjectiveCommand.objectives) {
            obj.enemiesAtObjective.remove(e.getPlayer());
            obj.teammatesAtObjective.remove(e.getPlayer());
            if (obj.enemiesAtObjective.isEmpty() && obj.getIsTaskActive()) {
                obj.stopTimer();
                obj.messageNearbyPlayers("Attackers have been pushed out of the objective", ChatColor.BLUE);
            }
            if (obj.teammatesAtObjective.isEmpty() && !obj.getIsTaskActive()) {
                obj.startTimer();
            }
        }

        for (Timer timer: TimerCommand.timers) {
            timer.nearbyPlayers.remove(e.getPlayer());
        }

        Player p = e.getPlayer();
        if (TagMinigame.isPlayerTagged(p)) {
            p.removePotionEffect(PotionEffectType.SPEED);
            p.removePotionEffect(PotionEffectType.GLOWING);
            TagMinigame.taggedPlayers.remove(p);
        }
    }




    @EventHandler
    public void projectileThrow(PlayerInteractEvent e) {
        if (!EventAdditions.listOfWorlds.contains(Objects.requireNonNull(e.getPlayer()).getWorld().getName())) {
            return;
        }

        if (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (e.getItem() == null) return;

            if (e.getClickedBlock() != null && e.getClickedBlock().getType().isInteractable()) {
                e.setCancelled(true);
                return;
            }

            if (e.getItem().getType().equals(Material.SPLASH_POTION) || e.getItem().getType().equals(Material.LINGERING_POTION)) {


                boolean throwFarther = hasCorrectItemLore(e.getItem(), "Throws Farther");
                boolean flaming = hasCorrectItemLore(e.getItem(), "Flaming");
                boolean smoking = hasCorrectItemLore(e.getItem(), "Smoking");

                if (!flaming && !throwFarther && !smoking) {
                    return;
                }

                Player player = e.getPlayer();
                new SplashPotion(player, flaming, smoking, throwFarther, e.getItem());
                e.getItem().setAmount(e.getItem().getAmount() - 1);
                e.setCancelled(true);
            }

            if (e.getItem().getType().equals(Material.SNOWBALL)) {
                if (hasCorrectItemLore(e.getItem(), "Damaging Snowball")) {
                    Location location = e.getPlayer().getLocation();
                    location.setY(location.getY() + 1.5);

                    Snowball snowball = (Snowball) e.getPlayer().getWorld().spawnEntity(location, EntityType.SNOWBALL);
                    snowball.setItem(e.getItem());
                    snowball.setVelocity((e.getPlayer().getLocation()).getDirection());
                    snowballIds.add(snowball.getUniqueId());
                    playSoundEffect(e.getPlayer().getLocation(), Sound.ENTITY_SPLASH_POTION_THROW, .7f, .1f);

                    e.getItem().setAmount(e.getItem().getAmount() - 1);
                    e.setCancelled(true);
                }
            }

            if (hasCorrectItemLore(e.getItem(), "Cooldown") && e.getPlayer().getCooldown(e.getItem().getType()) == 0) {

                Material mat = e.getItem().getType();
                if (mat.equals(Material.SNOWBALL) || mat.equals(Material.SPLASH_POTION) || mat.equals(Material.LINGERING_POTION)) {
                    e.getPlayer().getInventory().getItemInMainHand().setAmount(2);
                } else {
                    e.getPlayer().getInventory().getItemInMainHand().setAmount(1);
                }

                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(EventAdditions.instance, () -> {
                    e.getPlayer().setCooldown(e.getItem().getType(), 60);
                }, 0);
            }

        }
    }

    @EventHandler
    public void potionBreak(PotionSplashEvent e) {
        if (SplashPotion.containsFlamingUUID(e.getEntity().getUniqueId())) {
            SplashPotion.removeFlamingUUID(e.getEntity().getUniqueId());
            Block centerBlock = e.getEntity().getLocation().getBlock();
            ArrayList<Block> blocksInRadius = getBlocksInRadius(centerBlock, 1);
            for (Block block: blocksInRadius) {
                if (block.getType() != Material.AIR) continue;
                block.setType(Material.FIRE);
            }
        }

        if (SplashPotion.containsSmokingUUID(e.getEntity().getUniqueId())) {
            SplashPotion.removeSmokingUUID(e.getEntity().getUniqueId());
            Block centerBlock = e.getEntity().getLocation().getBlock();

            if (e.getEntity().getLocation().getWorld() != null) {
                Location entityLocation = e.getEntity().getLocation();
                Location loc = centerBlock.getLocation();
                ArrayList<Block> blocksInRadius = getBlocksInRadius(centerBlock, 2);

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
    }

    @EventHandler
    public void lingeringPotionBreak(LingeringPotionSplashEvent e) {
        if (SplashPotion.containsFlamingUUID(e.getEntity().getUniqueId())) {
            SplashPotion.removeFlamingUUID(e.getEntity().getUniqueId());
            Block centerBlock = e.getEntity().getLocation().getBlock();
            ArrayList<Block> blocksInRadius = getBlocksInRadius(centerBlock, 1);
            for (Block block: blocksInRadius) {
                if (block.getType() != Material.AIR) continue;
                block.setType(Material.FIRE);
            }
        }

        if (SplashPotion.containsSmokingUUID(e.getEntity().getUniqueId())) {
            SplashPotion.removeSmokingUUID(e.getEntity().getUniqueId());
            Block centerBlock = e.getEntity().getLocation().getBlock();

            if (e.getEntity().getLocation().getWorld() != null) {
                Location entityLocation = e.getEntity().getLocation();
                Location loc = centerBlock.getLocation();
                ArrayList<Block> blocksInRadius = getBlocksInRadius(centerBlock, 2);

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
    }

    public boolean hasCorrectItemLore(ItemStack item, String lore) {
        List<String> stringList = Objects.requireNonNull(item.getItemMeta()).getLore();

        if (stringList == null) return false;
        for (String element: stringList) {
            if (element.startsWith(lore)) {
                return true;
            }
        }
        return false;
    }

    public void playSoundEffect(Location location, Sound sound, float volume, float pitch) {
        Objects.requireNonNull(location.getWorld()).playSound(location, sound, volume , pitch);
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


    public void spawnSmoke(ArrayList<Block> blocksInRadius, Location loc, Location entityLocation) {
        for (Block block : blocksInRadius) {
            if (block.getType() != Material.AIR) continue;
            if (entityLocation.getWorld() == null) continue;
            Vector vel = new Vector(block.getX() - loc.getX(), block.getY() - loc.getY(), block.getZ() - loc.getZ());
            entityLocation.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, block.getLocation(), 10, vel.getX(), vel.getY(), vel.getZ(), 0.05);
        }
    }


}
