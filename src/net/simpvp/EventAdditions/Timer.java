package net.simpvp.EventAdditions;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Timer {

    Location timerLocation;
    int seconds;

    BukkitTask bukkitTask1;
    BukkitTask bukkitTask2;

    int x, z, y;


    public Timer(Location timerLocation, int seconds, int x, int y, int z) {
        this.timerLocation = timerLocation;
        this.seconds = seconds;
        this.x = x;
        this.y = y;
        this.z = z;
        startTimer();
    }


    /* Start asnyc timer */
    public void startTimer() {

        long activate_at = System.currentTimeMillis() + seconds * 1000L;

        bukkitTask1 = new BukkitRunnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() < activate_at) {
                    return;
                }

                this.cancel();
                bukkitTask2 = new BukkitRunnable() {
                    @Override
                    public void run() {
                        placeRedstoneOutput();
                        removeObject();
                    }
                }.runTaskLater(EventAdditions.instance, 0);
            }
        }.runTaskTimerAsynchronously(EventAdditions.instance, 20L, 20L);
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
}
