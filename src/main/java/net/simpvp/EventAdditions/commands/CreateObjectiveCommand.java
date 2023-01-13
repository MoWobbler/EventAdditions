package net.simpvp.EventAdditions.commands;

import net.simpvp.EventAdditions.EventAdditions;
import net.simpvp.EventAdditions.gameObjects.ObjectiveObject;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Objects;

public class CreateObjectiveCommand implements CommandExecutor {

    public static ArrayList<ObjectiveObject> objectives = new ArrayList<>();

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof BlockCommandSender)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run in a command block");
            return true;
        }

        BlockCommandSender cmdBlock = (BlockCommandSender) sender;
        if (!EventAdditions.listOfWorlds.contains(cmdBlock.getBlock().getWorld().getName())) {
            cmdBlock.sendMessage(ChatColor.RED + "This world does not have EventAdditions enabled");
            return true;
        }

        String objectiveName;
        Location location1;
        Location location2;
        Team team;
        float secondsToCapture;

        try {
            objectiveName = args[0];
            int x1 = cmdBlock.getBlock().getX() + Integer.parseInt(args[1]);
            int y1 = cmdBlock.getBlock().getY() + Integer.parseInt(args[2]);
            int z1 = cmdBlock.getBlock().getZ() + Integer.parseInt(args[3]);
            int x2 = cmdBlock.getBlock().getX() + Integer.parseInt(args[4]);
            int y2 = cmdBlock.getBlock().getY() + Integer.parseInt(args[5]);
            int z2 = cmdBlock.getBlock().getZ() + Integer.parseInt(args[6]);
            location1 = new Location(cmdBlock.getBlock().getWorld(), x1, y1, z1);
            location2 = new Location(cmdBlock.getBlock().getWorld(), x2, y2, z2);
            team = Objects.requireNonNull(EventAdditions.instance.getServer().getScoreboardManager()).getMainScoreboard().getTeam(args[7]);
            secondsToCapture = Long.parseLong(args[8]);
        } catch (Exception e) {
            cmdBlock.sendMessage("Correct usage: createobjective <Name> <~x1> <~y1> <~z1> <~x2> <~y2> <~z2> <team> <seconds>");
            return true;
        }

        for (ObjectiveObject obj: objectives) {
            if (obj.isLocationInsideRegion(location1)) {
                cmdBlock.sendMessage("Failed to create objective. An objective already exists there");
                return true;
            }
        }

        ObjectiveObject objective = new ObjectiveObject(objectiveName, cmdBlock, team, location1, location2, secondsToCapture);
        objectives.add(objective);
        return true;
    }
}
