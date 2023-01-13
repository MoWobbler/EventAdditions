package net.simpvp.EventAdditions.gameObjects;

import net.simpvp.EventAdditions.EventAdditions;
import net.simpvp.EventAdditions.commands.TimerCommand;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;

public class Timer {

    Location timerLocation;
    int seconds;

    BukkitTask bukkitTask1;
    BukkitTask bukkitTask2;

    public ArrayList<Player> nearbyPlayers = new ArrayList<>();

    int x, z, y;

    boolean visual;


    public Timer(Location timerLocation, int seconds, int x, int y, int z, boolean visual) {
        this.timerLocation = timerLocation;
        this.seconds = seconds;
        this.x = x;
        this.y = y;
        this.z = z;
        this.visual = visual;
        getNearbyPlayers();

        startTimer();
    }


    /* Start asnyc timer */
    public void startTimer() {

        long activate_at = System.currentTimeMillis() + seconds * 1000L;

        bukkitTask1 = new BukkitRunnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() < activate_at) {
                    if (visual) {
                        xpBarTimer((float) (((activate_at - System.currentTimeMillis())) / seconds) / 1000);
                    }
                    return;
                }

                this.cancel();
                bukkitTask2 = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (visual) {
                            xpBarTimer(0);
                        }
                        placeRedstoneOutput();
                        removeObject();
                    }
                }.runTaskLater(EventAdditions.instance, 0);
            }
        }.runTaskTimerAsynchronously(EventAdditions.instance, 1L, 1L);
    }


    /* Place redstone block */
    public void placeRedstoneOutput() {
        timerLocation.getBlock().getRelative(x,y,z).setType(Material.REDSTONE_BLOCK);
    }


    /* Remove the timer object from the array */
    public void removeObject() {
        TimerCommand.timers.remove(this);
    }


    /* Get the location of the command block that ran the command */
    public Location getTimerLocation() {
        return timerLocation;
    }


    /* Stop the timer */
    public void stopTimer() {
        if (bukkitTask1 != null) {
            bukkitTask1.cancel();
        }
        if (bukkitTask2 != null) {
            bukkitTask2.cancel();
        }
    }


    public void getNearbyPlayers() {
        for (Player player: Bukkit.getServer().getOnlinePlayers()) {
            if (!(player.getWorld().equals(timerLocation.getWorld()))) {
                continue;
            }
            if (player.getLocation().distance(timerLocation) < 1000 && !player.isDead()) {
                nearbyPlayers.add(player);
            }
        }
    }

    public void xpBarTimer(float level) {
        for (Player player: nearbyPlayers) {
            player.setExp(level);
        }
    }

    public boolean isVisual() {
        return visual;
    }

    public void removeNearbyPlayer(Player player) {
        nearbyPlayers.remove(player);
    }

}
