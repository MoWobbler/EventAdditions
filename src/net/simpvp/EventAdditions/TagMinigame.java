package net.simpvp.EventAdditions;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class TagMinigame implements CommandExecutor {

    static BlockCommandSender cmdBlock;
    public static ArrayList<Player> taggedPlayers = new ArrayList<>();
    public static ArrayList<Player> nearbyTaggablePlayers = new ArrayList<>();

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!cmd.getName().equalsIgnoreCase("tagevent")) {
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

        getTaggablePlayers();

        if (args.length == 1 && args[0].equalsIgnoreCase("remove")) {
            killTaggedPlayers();
            return true;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("tag")) {
            findPlayerToTag(args[1]);
            return true;
        }
        return true;
    }



    /* Find a player to tag by name or a random player to tag */
    public void findPlayerToTag(String string) {
        Player player = Bukkit.getServer().getPlayer(string);
        if (string.equalsIgnoreCase("-random")) {
            int random = new Random().nextInt(nearbyTaggablePlayers.size());
            player = nearbyTaggablePlayers.get(random);
            tagPlayer(player, player.getDisplayName() + " has been tagged!");

        } else if (player != null){
            tagPlayer(player, player.getDisplayName() + " has been tagged!");
        }
    }

    /* Mark a player as "it" */
    public static void tagPlayer(Player player, String string) {
        taggedPlayers.add(player);
        nearbyTaggablePlayers.remove(player);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10000, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 10000, 0));
        messageNearbyPlayers(string);

    }

    /* Kill all players who are "it" */
    public void killTaggedPlayers() {
        for (Player p: taggedPlayers) {
            EventAdditions.instance.getLogger().info(p.getDisplayName());
            p.removePotionEffect(PotionEffectType.SPEED);
            p.removePotionEffect(PotionEffectType.GLOWING);
            p.setHealth(0.0);
        }
        taggedPlayers.clear();
    }

    /* Remove "it" status from player */
    public static void untagPlayer(Player player) {
        taggedPlayers.remove(player);
        nearbyTaggablePlayers.add(player);
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.GLOWING);
    }

    /* Populate an array of taggable players */
    public void getTaggablePlayers() {
        nearbyTaggablePlayers.clear();
        for (Player p: Bukkit.getOnlinePlayers()) {
            if (!Objects.equals(p.getLocation().getWorld(), cmdBlock.getBlock().getWorld())) {
                continue;
            }

            if (isPlayerTagged(p)) {
                continue;
            }

            if (p.getLocation().distance(cmdBlock.getBlock().getLocation()) <= 500 && (p.getGameMode().equals(GameMode.SURVIVAL) || p.getGameMode().equals(GameMode.ADVENTURE))) {
                nearbyTaggablePlayers.add(p);
            }
        }
    }

    /* Alert all nearby players with a string and a sound */
    public static void messageNearbyPlayers(String string) {
        for (Player p: Bukkit.getOnlinePlayers()) {
            if (p.getLocation().distance(cmdBlock.getBlock().getLocation()) <= 1000) {
                p.sendMessage(ChatColor.DARK_RED + string);
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 10, 2);
            }
        }
    }

    /* Return true if a player is it */
    public static boolean isPlayerTagged(Player player) {
        return taggedPlayers.contains(player);
    }




}
