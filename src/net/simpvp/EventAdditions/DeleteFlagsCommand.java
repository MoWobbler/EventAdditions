package net.simpvp.EventAdditions;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DeleteFlagsCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        for (FlagObject flag: CreateFlagCommand.flags) {
            flag.cancelTimerTask();
            flag.removeFlag();
        }
        CreateFlagCommand.flags.clear();
        return true;
    }



}
