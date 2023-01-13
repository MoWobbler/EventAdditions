package net.simpvp.EventAdditions.commands;

import net.simpvp.EventAdditions.EventAdditions;
import net.simpvp.EventAdditions.gameObjects.CustomMob;
import net.simpvp.EventAdditions.gameObjects.MobWave;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MobWaveCommand implements CommandExecutor {

    List<CustomMob> customMobs = new ArrayList<>();
    YamlConfiguration config;

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        config = EventAdditions.mobConfig.getConfig();
        customMobs.clear();

        if (!(sender instanceof BlockCommandSender)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run in a command block");
            return true;
        }

        BlockCommandSender cmdBlock = (BlockCommandSender) sender;
        if (!EventAdditions.listOfWorlds.contains(cmdBlock.getBlock().getWorld().getName())) {
            cmdBlock.sendMessage(ChatColor.RED + "This world does not have EventAdditions enabled");
            return true;
        }

        if (args.length != 10) {
            cmdBlock.sendMessage(ChatColor.RED + "Usage: /mobwave x1 y1 z1 x2 y2 z2 mobsToSpawn seconds targeting zombiedata");
            return true;
        }

        int x1, y1, z1, x2, y2, z2;
        int totalMobsToSpawn;
        int totalWaveSeconds;
        boolean betterTargeting;
        Location location = cmdBlock.getBlock().getLocation();

        try {
            x1 = coordinateHandler(args[0], location.getBlockX());
            y1 = coordinateHandler(args[1], location.getBlockY());
            z1 = coordinateHandler(args[2], location.getBlockZ());
            x2 = coordinateHandler(args[3], location.getBlockX());
            y2 = coordinateHandler(args[4], location.getBlockY());
            z2 = coordinateHandler(args[5], location.getBlockZ());
            totalMobsToSpawn = Integer.parseInt(args[6]);
            totalWaveSeconds = Integer.parseInt(args[7]);
            betterTargeting = Boolean.parseBoolean(args[8]);
        } catch (Exception e) {
            cmdBlock.sendMessage("Correct usage: mobwave x1 y1 z1 x2 y2 z2 amountOfMobs seconds targeting zombiedata");
            return false;
        }

        if (!parseMobData(args[9], betterTargeting, cmdBlock)) {
            return true;
        }

        List<CustomMob> clonedList = new ArrayList<>(customMobs); // Weird workaround to pass the list by value
        new MobWave(location, x1, y1, z1, x2, y2, z2, totalMobsToSpawn, totalWaveSeconds, betterTargeting, clonedList);

        return true;
    }


    /* Return a coordinate integer. Get the relative coordinate if ~ is provided */
    public int coordinateHandler(String coord, int startingCoord) {
        if (coord.startsWith("~")) {
            String value = coord.substring(1);
            if (value.isEmpty()) {
                return startingCoord;
            } else {
                return startingCoord + Integer.parseInt(value);
            }
        } else {
            return Integer.parseInt(coord);
        }
    }

    /* Populate the customMobs list by parsing the mobData string. Make sure all the mobs are valid first
    * Example regex matches:
    * zombie
    * 50%zombie,70%leatherzombie
    * zombie,50%ironzombie{armor=diamond}
    * zombie,50%ironzombie{armor=iron,weapons=ironaxe} */
    private boolean parseMobData(String mobData, Boolean betterTargeting,BlockCommandSender cmdBlock) {
        Pattern p = Pattern.compile("(\\d+)%?([\\w]+)?(?:\\{(?:armor=([\\w]+),?)?(?:weapons=([\\w]+),?)?\\})?,?");
        // Example regex matches:
        // zombie
        // 50%zombie,70%leatherzombie
        // zombie,50%ironzombie{armor=diamond}
        // zombie,50%ironzombie{armor=iron,weapons=ironaxe}
        Matcher m = p.matcher(mobData);
        while (m.find()) {
            int percent = 100;
            String percentString = m.group(1);
            if (NumberUtils.isDigits(percentString)) {
                percent = Integer.parseInt(percentString);
            }
            String mobID = m.group(2);
            EntityType mobType = isValidMob(mobID);
            if (mobType == null) {
                cmdBlock.sendMessage("Invalid mob detected: " + mobID);
                return false;
            }

            String armor = m.group(3);
            String weapons = m.group(4);
            if (armor == null) armor = "none";
            if (weapons == null) weapons = "none";
            String name = (String) config.get("mobs." + mobID + ".name");
            Boolean isNameVisible = (Boolean) config.get("mobs." + mobID + ".isNameVisible");
            if (isNameVisible == null) {
                isNameVisible = false;
            }
            CustomMob customMob = new CustomMob(mobID, mobType, name, isNameVisible, betterTargeting, percent,
                    validateMaterial(config.get("armorSets." + armor + ".helmet")),
                    validateMaterial(config.get("armorSets." + armor + ".chestplate")),
                    validateMaterial(config.get("armorSets." + armor + ".leggings")),
                    validateMaterial(config.get("armorSets." + armor + ".boots")),
                    validateMaterial(config.get("weaponSets." + weapons + ".mainhand")),
                    validateMaterial(config.get("weaponSets." + weapons + ".offhand")));
            customMobs.add(customMob);
        }
        return true;
    }


    /* See if there is a valid mob stored for the given mobID in the mobs.yml file */
    private EntityType isValidMob(String mobID) {
        Object mob = EventAdditions.mobConfig.getConfig().get("mobs." + mobID + ".mob");
        if (mob == null) {
            return null;
        }
        try {
            return EntityType.valueOf(mob.toString().toUpperCase());
        } catch (IllegalArgumentException NullPointerException) {
            return null;
        }
    }

    /* Given a possible material from the mobs.yml, validate that it is a material
    * Return air if it is not a valid material */
    private Material validateMaterial(Object material) {
        try {
            if (material == null) {
                return Material.AIR;
            }
            return Material.valueOf(material.toString().toUpperCase());
        } catch(Exception e) {
            return Material.AIR;
        }
    }
}
