package net.simpvp.EventAdditions;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class TimerCommand implements CommandExecutor {

    Location location;
    BlockCommandSender cmdBlock;

    public static ArrayList<Timer> timers = new ArrayList<>();

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("timer")) {
            if (!(sender instanceof BlockCommandSender)) {
                sender.sendMessage(ChatColor.RED + "Command can only be run from a command block");
                return true;
            }

            cmdBlock = (BlockCommandSender) sender;
            location = cmdBlock.getBlock().getLocation();

            if (args.length != 1) {
                cmdBlock.sendMessage("/timer <seconds>");
                return true;
            }

            if (timers.size() >= 5) {
                cmdBlock.sendMessage("There can only be 5 timers running at a time");
                return true;
            }

            for (Timer t: timers) {
                if (t.getTimerLocation().equals(location)) {
                    cmdBlock.sendMessage("There is already a timer running at this location");
                    return true;
                }
            }

            try {
                int seconds = Integer.parseInt(args[0]);
                if (seconds > 600) {
                    cmdBlock.sendMessage("Timer can't be longer than 600 seconds");
                    return true;
                }
                Timer timer = new Timer(cmdBlock.getBlock().getLocation(), seconds);
                timers.add(timer);
                cmdBlock.sendMessage("Starting " + seconds + " second timer");
            } catch (Exception e) {
                cmdBlock.sendMessage("/timer <seconds>");
                return true;
            }
        }
        else if (cmd.getName().equalsIgnoreCase("stoptimers")) {

            if (sender instanceof Player && !sender.isOp()) {
                sender.sendMessage(ChatColor.RED + "You must be an admin to use this command");
                return true;
            }

            if (timers.isEmpty()) {
                sender.sendMessage(ChatColor.RED + "There are currently no timers running");
                return true;
            }

            sender.sendMessage(ChatColor.GREEN + "Stopping all timers");

            for(Timer t: timers) {
                t.stopTimer();
            }
            timers.clear();
        }

        return true;
    }



}
