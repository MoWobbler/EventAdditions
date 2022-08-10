package net.simpvp.EventAdditions;

import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DeleteObjectivesCommand implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (!(sender instanceof BlockCommandSender)) {
            sender.sendMessage("This command can only be run in a command block");
            return true;
        }

        BlockCommandSender cmdBlock = (BlockCommandSender) sender;
        if (!EventAdditions.listOfWorlds.contains(cmdBlock.getBlock().getWorld().getName())) {
            cmdBlock.sendMessage(ChatColor.RED + "This world does not have EventAdditions enabled");
            return true;
        }

        for (ObjectiveObject objective: CreateObjectiveCommand.objectives) {
            objective.stopTimer();
            objective.removeObject();
        }
        CreateObjectiveCommand.objectives.clear();
        return true;
    }
}
