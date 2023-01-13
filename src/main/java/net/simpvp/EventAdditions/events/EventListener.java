package net.simpvp.EventAdditions.events;

import net.simpvp.EventAdditions.EventAdditions;
import net.simpvp.EventAdditions.commands.CreateFlagCommand;
import net.simpvp.EventAdditions.commands.CreateObjectiveCommand;
import net.simpvp.EventAdditions.commands.TimerCommand;
import net.simpvp.EventAdditions.gameObjects.FlagObject;
import net.simpvp.EventAdditions.gameObjects.ModifiedItem;
import net.simpvp.EventAdditions.gameObjects.ObjectiveObject;
import net.simpvp.EventAdditions.gameObjects.TagMinigame;
import net.simpvp.EventAdditions.gameObjects.Timer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class EventListener implements Listener {

    public static HashMap<UUID, ModifiedItem> modifiedItems = new HashMap<>();


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

        for (ItemStack material: e.getEntity().getPlayer().getInventory()) {
            if (material == null) continue;
            e.getEntity().getPlayer().setCooldown(material.getType(), 0);
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

        for (net.simpvp.EventAdditions.gameObjects.Timer timer: TimerCommand.timers) {
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
        try {
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


            if (e.getDamager() instanceof Snowball && modifiedItems.containsKey(e.getDamager().getUniqueId())) {
                e.setDamage(modifiedItems.get(e.getDamager().getUniqueId()).getDamage());
                playSoundEffect(e.getDamager().getLocation(), Sound.ENTITY_WITHER_SHOOT, (float) .3, 2);
                modifiedItems.get(e.getDamager().getUniqueId()).removeFromHashMap();
            }
        } catch (Exception ignored) { }

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
    public void ItemUseEvent(PlayerInteractEvent e) {
        if (!EventAdditions.listOfWorlds.contains(Objects.requireNonNull(e.getPlayer()).getWorld().getName())) {
            return;
        }

        if (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (e.getItem() == null) return;

            if (e.getPlayer().getCooldown(e.getItem().getType()) != 0) {
                e.setCancelled(true);
                return;
            }




            if (Objects.requireNonNull(e.getItem().getItemMeta()).hasLore() && !Objects.requireNonNull(e.getItem().getItemMeta().getLore()).contains("(+NBT)")) {


                if (!Objects.requireNonNull(e.getItem().getItemMeta().getLore().get(0)).equalsIgnoreCase("event additions")) {
                    return;
                }

                if (e.getClickedBlock() != null && e.getClickedBlock().getType().isInteractable()) {
                    e.setCancelled(true);
                    return;
                }



                ModifiedItem modifiedItem = new ModifiedItem(e.getItem(), e.getPlayer());

                e.getPlayer().setCooldown(e.getPlayer().getInventory().getItem(Objects.requireNonNull(e.getHand())).getType(), modifiedItem.getCooldownSeconds() * 20);

                if (modifiedItem.isItemThrowable()) {
                    modifiedItem.summonModifiedProjectile();
                    e.setCancelled(true);
                }

                if (modifiedItem.isDepletable()) {
                    e.getPlayer().getInventory().getItem(Objects.requireNonNull(e.getHand())).setAmount(e.getItem().getAmount() - 1);
                }
            }
        }
    }


    @EventHandler void ProjectileHit(ProjectileHitEvent e) {
        if (!EventAdditions.listOfWorlds.contains(Objects.requireNonNull(e.getEntity()).getWorld().getName())) {
            return;
        }

        for (ModifiedItem item: modifiedItems.values()) {
            if (item.getUniqueId() == e.getEntity().getUniqueId()) {
                if (item.getSmokeRadius() > 0) {
                    item.startSmokeTask(e.getEntity());
                }
                if (item.getFlameRadius() > 0) {
                    item.spawnFire(e.getEntity());
                }
                item.removeFromHashMap();

            }
        }
    }


    @EventHandler void PotionDrink(PlayerItemConsumeEvent e) {
        if (!EventAdditions.listOfWorlds.contains(Objects.requireNonNull(e.getPlayer()).getWorld().getName())) {
            return;
        }
        List<String> listOfLore = ModifiedItem.hasModifyingItemLore(e.getItem(), "Depletable");
        if (listOfLore.size() <= 0) return;
        if (Objects.equals(listOfLore.get(0).strip(), "false")) {
            e.setCancelled(true);
        }
    }


    @EventHandler void CustomMobDeath(EntityDeathEvent e) {
        if (!EventAdditions.listOfWorlds.contains(Objects.requireNonNull(e.getEntity().getWorld().getName()))) {
            return;
        }
        if (e.getEntity().hasMetadata("mobWaveMob")) {
            e.getDrops().clear();
        }
    }


    public void playSoundEffect(Location location, Sound sound, float volume, float pitch) {
        Objects.requireNonNull(location.getWorld()).playSound(location, sound, volume , pitch);
    }
}
