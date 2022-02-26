package net.simpvp.EventAdditions;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scoreboard.Team;

public class EventListener implements Listener {


    /* Test for players near a flag */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (CreateFlagCommand.flags.isEmpty()) return;
        Team team = e.getPlayer().getScoreboard().getPlayerTeam(e.getPlayer());

        for (FlagObject flag: CreateFlagCommand.flags) {
            if (flag.getIsCaptured()) return;
            if (flag.getTeam().equals(team)) return;
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
        Team team = e.getPlayer().getScoreboard().getPlayerTeam(e.getPlayer());
        if (team != null) {
            e.setFormat("<" + team.getColor() + e.getPlayer().getName()  + ChatColor.RESET + "> " + e.getMessage());
        }
    }
}
