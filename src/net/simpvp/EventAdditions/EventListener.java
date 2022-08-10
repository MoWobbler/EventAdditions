package net.simpvp.EventAdditions;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class EventListener implements Listener {

    ArrayList<UUID> snowballIds = new ArrayList<>();

    /* Test for players near a flag */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {

        if (!EventAdditions.listOfWorlds.contains(e.getPlayer().getWorld().getName())) {
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
                    objective.messageNearbyPlayers("Attackers have been pushed out of the objective", ChatColor.BLUE);
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

        for (FlagObject flag: CreateFlagCommand.flags) {
            flag.nearbyPlayers.remove(e.getEntity().getPlayer());
        }

        for (ObjectiveObject obj: CreateObjectiveCommand.objectives) {
            obj.enemiesAtObjective.remove(e.getEntity().getPlayer());
            obj.teammatesAtObjective.remove(e.getEntity().getPlayer());
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
            snowballIds.remove(e.getDamager().getUniqueId());
        }

    }

    @EventHandler
    public void logOut(PlayerQuitEvent e) {
        if (!EventAdditions.listOfWorlds.contains(Objects.requireNonNull(e.getPlayer()).getWorld().getName())) {
            return;
        }

        for (FlagObject flag: CreateFlagCommand.flags) {
            flag.nearbyPlayers.remove(e.getPlayer());
        }

        for (ObjectiveObject obj: CreateObjectiveCommand.objectives) {
            obj.enemiesAtObjective.remove(e.getPlayer());
            obj.teammatesAtObjective.remove(e.getPlayer());
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
        if (e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            if (e.getItem() == null) return;
            if (e.getItem().getType().equals(Material.SPLASH_POTION) || e.getItem().getType().equals(Material.LINGERING_POTION)) {


                if (!hasCorrectItemLore(e.getItem(), "Throws Farther")) return;

                Location location = e.getPlayer().getLocation();
                location.setY(location.getY() + 1.5);
                ThrownPotion thrownPotion = (ThrownPotion) e.getPlayer().getWorld().spawnEntity(location, EntityType.SPLASH_POTION);
                thrownPotion.setItem(e.getItem());
                thrownPotion.setVelocity((e.getPlayer().getLocation()).getDirection());

                e.getItem().setType(Material.AIR);

                e.setCancelled(true);

            }

            if (e.getItem().getType().equals(Material.SNOWBALL)) {
                if (!hasCorrectItemLore(e.getItem(), "Damaging Snowball")) return;
                Location location = e.getPlayer().getLocation();
                location.setY(location.getY() + 1.5);

                Snowball snowball = (Snowball) e.getPlayer().getWorld().spawnEntity(location, EntityType.SNOWBALL);
                snowball.setItem(e.getItem());
                snowball.setVelocity((e.getPlayer().getLocation()).getDirection());
                snowballIds.add(snowball.getUniqueId());

                e.getItem().setType(Material.AIR);

                e.setCancelled(true);

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
}
