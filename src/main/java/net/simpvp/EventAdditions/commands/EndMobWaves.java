package net.simpvp.EventAdditions.commands;

import net.simpvp.EventAdditions.EventAdditions;
import net.simpvp.EventAdditions.gameObjects.MobWave;
import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class EndMobWaves implements CommandExecutor {

    private static final ArrayList<MobWave> mobWaves = new ArrayList<>();

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

        if (mobWaves.size() == 0) {
            cmdBlock.sendMessage(ChatColor.RED + "There are no active mobwaves currently");
            return true;
        }

        for (MobWave mobWave: mobWaves) {
            mobWave.stopMobWave();
        }
        mobWaves.clear();
        cmdBlock.sendMessage(ChatColor.GREEN + "Stopped all mobwaves");
        return true;
    }

    public static void storeMobWave(MobWave mobWave) {
        mobWaves.add(mobWave);
    }

    public static void removeMobWave(MobWave mobWave) {
        mobWaves.remove(mobWave);
    }

}
