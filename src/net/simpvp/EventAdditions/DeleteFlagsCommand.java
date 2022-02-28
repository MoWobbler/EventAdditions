package net.simpvp.EventAdditions;

import org.bukkit.ChatColor;
import org.bukkit.command.*;

public class DeleteFlagsCommand implements CommandExecutor {

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


        for (FlagObject flag: CreateFlagCommand.flags) {
            flag.cancelTimerTask();
            flag.removeFlag();
        }
        CreateFlagCommand.flags.clear();
        return true;
    }



}
