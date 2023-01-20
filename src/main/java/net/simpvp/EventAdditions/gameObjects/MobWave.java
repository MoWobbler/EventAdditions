package net.simpvp.EventAdditions.gameObjects;

import net.simpvp.EventAdditions.EventAdditions;
import net.simpvp.EventAdditions.commands.EndMobWaves;
import org.bukkit.*;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MobWave {
    YamlConfiguration config;
    Random random;
    Location location;
    int x1, y1, z1, x2, y2, z2;
    int totalMobsToSpawn;
    int totalWaveSeconds;
    List<CustomMob> customMobs;
    List<Location> possibleLocations;
    boolean betterTargeting;
    BukkitTask timer;
    long activate_at;
    long startTime;

    public MobWave(Location location, int x1, int y1, int z1, int x2, int y2, int z2,
                   int totalMobsToSpawn, int totalWaveSeconds,
                   boolean betterTargeting, List<CustomMob> customMobs, BlockCommandSender cmdBlock) {
        this.startTime = System.currentTimeMillis();
        this.config = EventAdditions.mobConfig.getConfig();
        this.random = new Random();
        this.location = location;
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
        this.totalMobsToSpawn = totalMobsToSpawn;
        this.totalWaveSeconds = totalWaveSeconds;
        this.customMobs = customMobs;
        this.betterTargeting = betterTargeting;
        activate_at = System.currentTimeMillis() + totalWaveSeconds * 1000L;
        this.possibleLocations = getAllSurfaceLocations(location.getWorld(), x1, y1, z1, x2, y2, z2);

        if (this.possibleLocations.isEmpty()) {
            cmdBlock.sendMessage(ChatColor.RED + "No valid spawn locations were found");
            return;
        }
        startRepeatingSpawnTask(totalMobsToSpawn, totalWaveSeconds);
    }


    /* Function to randomly spawn a custom mob based on probability and set target if betterTargeting is true */
    private void spawnCustomMob(int totalProbability, int amountPerSpawn) {
        int r = random.nextInt(totalProbability);
        for (CustomMob customMob : customMobs) {
            int probability = customMob.getPercentage();
            if (r < probability) {
                customMob.spawn(getRandomLocation(), amountPerSpawn);
                break;
            } else {
                r -= probability;
            }
        }
    }


    /* Spawn the given amount of mobs over the given amount of seconds
    * Place a redstone when the wave ends */
    private void startRepeatingSpawnTask(int totalMobsToSpawn, int totalWaveSeconds) {

        final int[] remaining = {totalMobsToSpawn};
        double delay = (double) totalWaveSeconds / totalMobsToSpawn;
        final int[] amountPerSpawn = {(int) Math.floor(((double)totalMobsToSpawn / totalWaveSeconds) / (20.0 / totalWaveSeconds))};

        if (amountPerSpawn[0] <= 0) {
            amountPerSpawn[0] = 1;
        }

        EndMobWaves.storeMobWave(this);


        long ticks = (long) (delay * 20 * amountPerSpawn[0]); // Time between each mob spawn

        final int[] totalProbability = {0};
        for (CustomMob customMob : customMobs) {
            totalProbability[0] += customMob.getPercentage();
        }
        if (totalProbability[0] == 0) {
            totalProbability[0] = 100;
        }



        // Repeating task
        timer = Bukkit.getScheduler().runTaskTimer(
                EventAdditions.instance, () -> {
                    spawnCustomMob(totalProbability[0], amountPerSpawn[0]);
                    remaining[0] -= amountPerSpawn[0];
                    if (remaining[0] == 0) {
                        this.location.getBlock().getRelative(0, 2, 0).setType(Material.REDSTONE_BLOCK);
                        stopMobWave();
                        EndMobWaves.removeMobWave(this);
                        //EventAdditions.instance.getLogger().info("Time: " + (System.currentTimeMillis() - startTime));
                    }
                }, 0L, ticks
        );
    }



//        final int[] remaining = {totalMobsToSpawn};
//        double delay = (double) totalWaveSeconds / totalMobsToSpawn;
//        long ticks = (long) (delay * 20); // Time between each mob spawn
//
//        final int[] totalProbability = {0};
//        for (CustomMob customMob : customMobs) {
//            totalProbability[0] += customMob.getPercentage();
//        }
//
//        // Async repeating task
//        timer = Bukkit.getScheduler().runTaskTimerAsynchronously(
//                EventAdditions.instance, () -> Bukkit.getScheduler().callSyncMethod(EventAdditions.instance,
//                        (Callable<Void>) () -> {
//                            spawnCustomMob(totalProbability[0]);
//                            remaining[0]--;
//                            if (remaining[0] == 0) {
//                                this.location.getBlock().getRelative(0,2,0).setType(Material.REDSTONE_BLOCK);
//                                timer.cancel();
//                                EventAdditions.instance.getLogger().info("Start time: " + (System.currentTimeMillis() - startTime));
//                            }
//                            return null;
//                        }
//                ), 0L, ticks
//        );
    //}

    /* Get a random location from the possibleLocations list */
    private Location getRandomLocation() {
        int index = random.nextInt(possibleLocations.size());
        return possibleLocations.get(index);
    }


    /* Given a x and z coordinate, find first y level that is a block and that has two air directly above it */
    public Location getSurfaceBlock(World world, int x, int z, int minY, int maxY) {
        for (int y = maxY; y >= minY; y--) {
            if (!world.getBlockAt(x, y, z).getType().isAir() && world.getBlockAt(x, y + 1, z).getType().isAir()
                    && world.getBlockAt(x, y + 2, z).getType().isAir()) {
                return new Location(world, x + .5, y + 1, z + .5);
            }
        }
        return null;
    }


    /* Iterate through all x and z coordinates in the given selection.
    Get the surface block at each x and z coordinate, then store the locations */
    private List<Location> getAllSurfaceLocations(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
        List<Location> locations = new ArrayList<>();
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
            for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) {
                Location location = getSurfaceBlock(world, x, z, Math.min(y1, y2), Math.max(y1,y2));
                if (location == null) {
                    continue;
                }
                locations.add(location);
            }
        }
        return locations;
    }

    public void stopMobWave() {
        timer.cancel();
    }



}
