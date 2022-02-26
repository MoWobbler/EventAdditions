package net.simpvp.EventAdditions;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Objects;

public class CreateFlagCommand implements CommandExecutor {

    public static ArrayList<FlagObject> flags = new ArrayList<>();

    Location startingLocation;
    Location finalLocation = new Location(null, 0, 0, 0);

    int finalX, finalY, finalZ;
    String flagName;
    boolean redstoneOutput = false;
    Team team = null;

    boolean isXDefined;
    boolean isYDefined;
    boolean isZDefined;
    boolean isTeamDefined;
    boolean isOutputDefined;
    boolean isNameDefined;

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){

        if (!(sender instanceof BlockCommandSender)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run in a command block");
            return true;
        }

        isXDefined = false;
        isYDefined = false;
        isZDefined = false;
        isTeamDefined = false;
        isOutputDefined = false;
        isNameDefined = false;

        BlockCommandSender cmdBlock = (BlockCommandSender) sender;
        startingLocation = cmdBlock.getBlock().getLocation();

        for (String arg : args) {
            if (arg.toLowerCase().startsWith("x=")) {
                if (isXDefined) {
                    cmdBlock.sendMessage("X coord has been defined more than once");
                    return true;
                }
                finalX = validateCoord(arg, cmdBlock, startingLocation);
                if (finalX == 0) {
                    cmdBlock.sendMessage("Invalid x coordinate");
                    return true;
                }
                isXDefined = true;
            }
            else if (arg.toLowerCase().startsWith("y=")) {
                if (isYDefined) {
                    cmdBlock.sendMessage("Y coord has been defined more than once");
                    return true;
                }
                finalY = validateCoord(arg, cmdBlock, startingLocation);
                if (finalY == 0) {
                    cmdBlock.sendMessage("Invalid y coordinate");
                    return true;
                }
                isYDefined = true;
            }
            else if (arg.toLowerCase().startsWith("z=")) {
                if (isZDefined) {
                    cmdBlock.sendMessage("Z coord has been defined more than once");
                    return true;
                }
                finalZ = validateCoord(arg, cmdBlock, startingLocation);
                if (finalZ == 0) {
                    cmdBlock.sendMessage("Invalid z coordinate");
                    return true;
                }
                isZDefined = true;
            }
            else if (arg.toLowerCase().startsWith("output=")) {
                if (isOutputDefined) {
                    cmdBlock.sendMessage("Output has been defined more than once");
                    return true;
                }
                redstoneOutput = arg.substring(7).equalsIgnoreCase("true");
                isOutputDefined = true;
            }
            else if (arg.toLowerCase().startsWith("team=")) {
                if (isTeamDefined) {
                    cmdBlock.sendMessage("Team has been defined more than once");
                    return true;
                }
                String sTeam = arg.toLowerCase().substring(5);
                team = Objects.requireNonNull(EventAdditions.instance.getServer().getScoreboardManager()).getMainScoreboard().getTeam(sTeam);
                if (team == null) {
                    String message = "No such scoreboard team '" + sTeam + "'. Type /team list"
                            + "\nfor a list of scoreboard teams.";
                    cmdBlock.sendMessage(message);
                    return true;
                }
                isTeamDefined = true;
            }
            else if (arg.toLowerCase().startsWith("name=")) {
                flagName = arg.substring(5);
                if (flagName.length() >= 20) {
                    cmdBlock.sendMessage("Flag name cannot be longer than 20 characters");
                    return true;
                }
                if (isNameDefined) {
                    cmdBlock.sendMessage("Flag name has been defined more than once");
                    return true;
                }
                isNameDefined = true;
            }
        }


        finalLocation.setWorld(cmdBlock.getBlock().getWorld());
        finalLocation.setX(finalX);
        finalLocation.setY(finalY);
        finalLocation.setZ(finalZ);

        FlagObject flag = new FlagObject(finalLocation.getBlock(), team, cmdBlock, redstoneOutput, flagName);

        for (FlagObject f: flags) {
            if (f.getStartBlock().equals(finalLocation.getBlock())) {
                cmdBlock.sendMessage("Failed to place flag. A flag already exists");
                return true;
            }
        }
        flag.initializeFlag();
        if (!flag.getIsFlagInitialized()) {
            cmdBlock.sendMessage("Failed to place flag. Invalid flagpole detected");
            flags.remove(flag);
            return true;
        }
        flags.add(flag);
        return true;
    }


    /* Check if a given coordinate is valid */
    public int validateCoord(String arg, BlockCommandSender sender, Location startLoc) {
        boolean isRelative = false;
        String coordPlain = arg.substring(0,1).toLowerCase();
        arg = arg.substring(2);
        int coord;
        if (arg.startsWith("~")) {
            isRelative = true;
            arg = arg.substring(1);
        }

        try {
            coord = Integer.parseInt(arg);
        } catch (Exception e) {
            String msg = "Invalid coordinate '" + arg + "': " + e;
            sender.sendMessage(msg);
            return 0;
        }

        if (isRelative) {
            switch (coordPlain) {
                case "x":
                    coord = startLoc.getBlockX() + coord;
                    break;
                case "y":
                    coord = startLoc.getBlockY() + coord;
                    break;
                case "z":
                    coord = startLoc.getBlockZ() + coord;
                    break;
            }
        }
        return coord;
    }
}
