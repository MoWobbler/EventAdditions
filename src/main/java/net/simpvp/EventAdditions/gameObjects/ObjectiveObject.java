package net.simpvp.EventAdditions.gameObjects;

import net.simpvp.EventAdditions.EventAdditions;
import net.simpvp.EventAdditions.commands.CreateObjectiveCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;

public class ObjectiveObject {

    BukkitTask bukkitTask1;
    BukkitTask bukkitTask2;
    private Boolean isTaskActive = false;


    private final String objectiveName;
    private final BlockCommandSender cmdBlock;
    private final Team objectiveTeam;
    private final Location location1;
    private final Location location2;
    private boolean hasBeenCaptured = false;
    double secondsToCapture;
    double activate_at = 0;
    BossBar bossBar;
    float startSeconds;



    public ArrayList<Player> teammatesAtObjective = new ArrayList<>();
    public ArrayList<Player> enemiesAtObjective = new ArrayList<>();

    public ObjectiveObject(String objectiveName, BlockCommandSender cmdBlock, Team objectiveTeam, Location location1, Location location2, float secondsToCapture) {
        this.objectiveName = addSpacesToName(objectiveName);
        this.cmdBlock = cmdBlock;
        this.objectiveTeam = objectiveTeam;
        this.location1 = location1;
        this.location2 = location2;
        this.secondsToCapture = secondsToCapture;
        this.startSeconds = secondsToCapture;
        bossBar = Bukkit.createBossBar(this.objectiveName, BarColor.YELLOW, BarStyle.SEGMENTED_10);
        addToBossBar();
    }




    public boolean isLocationInsideRegion(Location location) {
        if (location.getWorld() != location1.getWorld() || location.getWorld() != location2.getWorld()) return false;

        if((location.getBlockX() >= location1.getBlockX() && location.getBlockX() <= location2.getBlockX()) || (location.getBlockX() <= location1.getBlockX() && location.getBlockX() >= location2.getBlockX())) {
            if ((location.getBlockZ() >= location1.getBlockZ() && location.getBlockZ() <= location2.getBlockZ()) || (location.getBlockZ() <= location1.getBlockZ() && location.getBlockZ() >= location2.getBlockZ())) {
                return (location.getBlockY() >= location1.getBlockY() && location.getBlockY() <= location2.getBlockY()) || (location.getBlockY() <= location1.getBlockY() && location.getBlockY() >= location2.getBlockY());
            }
        }
        return false;
    }

    public Team getObjectiveTeam() {
        return objectiveTeam;
    }


    public void startTimer() {


        activate_at = System.currentTimeMillis() + this.secondsToCapture * 1000L;
        isTaskActive = true;

        messageNearbyPlayers("An enemy has entered " + objectiveName, ChatColor.RED);

        bukkitTask1 = new BukkitRunnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() < activate_at) {

                    bossBar.setProgress((float) (((activate_at - System.currentTimeMillis())) / startSeconds) / 1000);
                    return;
                }

                this.cancel();
                bukkitTask2 = new BukkitRunnable() {
                    @Override
                    public void run() {
                        isTaskActive = false;
                        placeRedstoneOutput();
                        hasBeenCaptured = true;

                        removeObject();
                    }
                }.runTaskLater(EventAdditions.instance, 0);
            }
        }.runTaskTimerAsynchronously(EventAdditions.instance, 1L, 1L);
    }

    public void stopTimer() {
        isTaskActive = false;
        secondsToCapture = (activate_at - System.currentTimeMillis()) / 1000L;


        if (bukkitTask1 != null) {
            bukkitTask1.cancel();
        }
        if (bukkitTask2 != null) {
            bukkitTask2.cancel();
        }
    }

    /* Return true if a repeating task is active */
    public boolean getIsTaskActive() {
        return isTaskActive;
    }


    /* Place a redstone output once the flag is captured */
    public void placeRedstoneOutput() {
        cmdBlock.getBlock().getRelative(0, 2, 0).setType(Material.REDSTONE_BLOCK);
    }

    /* Message everyone who is near this flag*/
    public void messageNearbyPlayers(String message, ChatColor color) {
        for (Player player: Bukkit.getServer().getOnlinePlayers()) {
            if (!(player.getWorld().equals(cmdBlock.getBlock().getWorld()))) {
                continue;
            }
            if (player.getLocation().distance(cmdBlock.getBlock().getLocation()) < 1000) {
                player.sendMessage(color + message);
            }
        }
    }

    public void addToBossBar() {
        bossBar.setVisible(true);
        for (Player player: Bukkit.getServer().getOnlinePlayers()) {
            if (!(player.getWorld().equals(cmdBlock.getBlock().getWorld()))) {
                continue;
            }
            if (player.getLocation().distance(cmdBlock.getBlock().getLocation()) < 1000) {
                bossBar.addPlayer(player);
            }
        }
    }

    public boolean hasBeenCaptured() {
        return hasBeenCaptured;
    }

    /* Remove the object from the array */
    public void removeObject() {
        bossBar.removeAll();
        bossBar.setVisible(false);
        CreateObjectiveCommand.objectives.remove(this);
    }

    public String addSpacesToName(String string) {
        return string.replaceAll("_", " ");
    }
}
