package net.simpvp.EventAdditions;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import java.util.Objects;

public class SnowballDamageCommand implements CommandExecutor {

    static BlockCommandSender cmdBlock;
    static Location location;

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!cmd.getName().equalsIgnoreCase("snowballdamage")) {
            return true;
        }

        if (!(sender instanceof BlockCommandSender)) {
            sender.sendMessage(ChatColor.RED + "Command can only be run from a command block");
            return true;
        }

        cmdBlock = (BlockCommandSender) sender;

        if (!EventAdditions.listOfWorlds.contains(cmdBlock.getBlock().getWorld().getName())) {
            cmdBlock.sendMessage(ChatColor.RED + "This world does not have EventAdditions enabled");
            return true;
        }

        location = cmdBlock.getBlock().getLocation();
        cmdBlock.sendMessage(ChatColor.RED + "Nearby snowballs will damage players");


        return true;

    }


    public static boolean isSnowballNear(Entity e) {


        if (location == null) {
            return false;
        }

        if (!Objects.equals(location.getWorld(), e.getWorld())) {
            return false;
        }

        return e.getLocation().distance(location) < 500;
    }


}
