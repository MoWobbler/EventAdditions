package net.simpvp.EventAdditions;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;

import java.util.Objects;

public class EventListener implements Listener {


    /* Test for players near a flag */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {

        if (!EventAdditions.listOfWorlds.contains(e.getPlayer().getWorld().getName())) {
            return;
        }

        if (CreateFlagCommand.flags.isEmpty()) return;
        Team team = e.getPlayer().getScoreboard().getPlayerTeam(e.getPlayer());

        for (FlagObject flag: CreateFlagCommand.flags) {
            if (flag.getIsCaptured()) continue;
            if (flag.getTeam().equals(team)) continue;
            if (flag.isPlayerNear(e.getPlayer().getLocation()) && !flag.getIsTaskActive()) {
                flag.nearbyPlayers.add(e.getPlayer());
                flag.startTimerTask();
                return;
            }

            if (!flag.isPlayerNear(e.getPlayer().getLocation())) {
                flag.nearbyPlayers.remove(e.getPlayer());
            }

            if (!flag.isPlayerNear(e.getPlayer().getLocation()) && flag.getIsTaskActive() && flag.nearbyPlayers.isEmpty()) {
                flag.cancelTimerTask();
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


        if (e.getDamager() instanceof Snowball && SnowballDamageCommand.isSnowballNear(e.getEntity())) {
            e.setDamage(5);
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
}
