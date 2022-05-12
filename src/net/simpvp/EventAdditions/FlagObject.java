package net.simpvp.EventAdditions;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;

public class FlagObject {

    private final String flagName;

    private final BlockCommandSender cmdBlock;
    private final Team flagTeam;
    private final boolean redstoneOutput;

    public final ArrayList<Block> flagBlocks = new ArrayList<>();
    public ArrayList<Player> nearbyPlayers = new ArrayList<>();

    private final Block startingBlock;
    private Block topOfFlagPoleBase;
    private Block topOfFlagPole;
    private Block anchorBlock;

    private int taskID;
    private boolean isCaptured = false;
    private boolean isFlagInitialized = false;
    private boolean isTaskActive = false;

    private int totalFlagpoleLength;
    private int captureProgress = 0;

    public FlagObject(Block startingBlock, Team flagTeam, BlockCommandSender cmdBlock, boolean redstoneOutput, String flagName) {
       this.startingBlock = startingBlock;
       this.flagTeam = flagTeam;
       this.cmdBlock = cmdBlock;
       this.redstoneOutput = redstoneOutput;
       this.flagName = flagName;
    }


    /* Validate flag position, store flagpole blocks and place flag */
    public void initializeFlag() {
        if (!populateFlagPoleBaseBlocks()) {
            cmdBlock.sendMessage("Invalid flag detected!");
            return;
        }
        if (!populateFlagPoleBlocks()) {
            cmdBlock.sendMessage("Invalid flag detected!");
            return;
        }
        if (!getPoleTop()) {
            cmdBlock.sendMessage("Invalid flag detected!");
            return;
        }
        anchorBlock = topOfFlagPole;
        placeFlag(anchorBlock);
        isFlagInitialized = true;
        cmdBlock.sendMessage("Successfully created a flag");
        EventAdditions.instance.getLogger().info("Successfully created a flag");
    }


    /* Return false if no flagpole base is found */
    private boolean populateFlagPoleBaseBlocks() {
        Block flagPoleBase = startingBlock;
        flagPoleBase = flagPoleBase.getRelative(BlockFace.UP);

        if (flagPoleBase.getType().equals(Material.AIR) || flagPoleBase.getType().equals(Material.CAVE_AIR) ||
                flagPoleBase.getType().equals(Material.VOID_AIR)) {
            return false;
        }

        while (flagPoleBase.getType().equals(flagPoleBase.getRelative(BlockFace.UP).getType())) {
            flagPoleBase = flagPoleBase.getRelative(BlockFace.UP);
        }
        topOfFlagPoleBase = flagPoleBase;
        return true;
    }


    /* Return false if no pole is found */
    private boolean populateFlagPoleBlocks() {
        Block flagPole = topOfFlagPoleBase.getRelative(BlockFace.UP);

        if (flagPole.getType().equals(Material.AIR) || flagPole.getType().equals(Material.CAVE_AIR) ||
                flagPole.getType().equals(Material.VOID_AIR)) {
            return false;
        }

        while (flagPole.getType().equals(flagPole.getRelative(BlockFace.UP).getType())) {
            flagPole = flagPole.getRelative(BlockFace.UP);
        }
        topOfFlagPole = flagPole;
        totalFlagpoleLength = topOfFlagPole.getY() - topOfFlagPoleBase.getY() - 2;
        return true;
    }


    /* Return false if no tip is found */
    private boolean getPoleTop() {
        Block flagPoleTip = topOfFlagPole.getRelative(BlockFace.UP);
        return !flagPoleTip.getType().equals(Material.AIR) && !flagPoleTip.getType().equals(Material.CAVE_AIR) &&
                !flagPoleTip.getType().equals(Material.VOID_AIR);
    }


    /* Place the six flag blocks attached to the pole */
    private void placeFlag(Block anchorBlock) {

        removeFlag();

        flagBlocks.clear();
        flagBlocks.add(anchorBlock.getRelative(1, 0,0));
        flagBlocks.add(anchorBlock.getRelative(2,0,0));
        flagBlocks.add(anchorBlock.getRelative(3,0,0));
        flagBlocks.add(anchorBlock.getRelative(1,-1,0));
        flagBlocks.add(anchorBlock.getRelative(2,-1,0));
        flagBlocks.add(anchorBlock.getRelative(3,-1,0));

        for (Block block: flagBlocks) {
            block.setType(getFlagBlockMaterial());
        }
        playFlagMoveSound();

        if (anchorBlock.getRelative(0,-2,0).equals(topOfFlagPoleBase)) {
            isCaptured = true;
            placeRedstoneOutput();
            cancelTimerTask();
        }
    }


    /* Move flag down one */
    public void moveFlagDown() {
        captureProgress += 1;
        if (flagName != null) {
            messageNearbyPlayers("The " + flagName + " is being captured!");
        }
        anchorBlock = anchorBlock.getRelative(BlockFace.DOWN);
        placeFlag(anchorBlock);
    }


    /* Keep trying to move the flag down every 5 seconds */
    public void startTimerTask() {
        isTaskActive = true;
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(EventAdditions.instance, this::moveFlagDown, 100, 100);
    }


    /* Cancel the repeating task */
    public void cancelTimerTask() {
        isTaskActive = false;
        Bukkit.getScheduler().cancelTask(taskID);
    }


    /* Compare two locations to see if a player is near the flag */
    public boolean isPlayerNear(Location location) {
        return location.distance(startingBlock.getLocation()) <= 4;
    }


    /* Set flag to air */
    public void removeFlag() {
        for (Block block: flagBlocks) {
            block.setType(Material.AIR);
        }
    }


    /* Play a sound each time a flag is being captured */
    public void playFlagMoveSound() {
        startingBlock.getWorld().playSound(startingBlock.getLocation(), Sound.BLOCK_WOOL_BREAK, 10 ,1);
    }


    /* Get what the flag should be made out of */
    public Material getFlagBlockMaterial() {

        if(flagTeam == null) {
            return Material.WHITE_WOOL;
        }
        if (flagTeam.getDisplayName().equals("blue")) {
            return Material.BLUE_WOOL;
        }
        if (flagTeam.getDisplayName().equals("red")) {
            return Material.RED_WOOL;
        }
        if (flagTeam.getDisplayName().equals("yellow")) {
            return Material.YELLOW_WOOL;
        }
        if (flagTeam.getDisplayName().equals("green")) {
            return Material.LIME_WOOL;
        }
        return Material.WHITE_WOOL;
    }


    /* Place a redstone output once the flag is captured */
    public void placeRedstoneOutput() {
        if (redstoneOutput) {
            cmdBlock.getBlock().getRelative(0, 2, 0).setType(Material.REDSTONE_BLOCK);
        }
    }


    /* Return the flag's starting block */
    public Block getStartBlock() {
        return startingBlock;
    }


    /* Return true if flag was successfully created */
    public boolean getIsFlagInitialized() {
        return isFlagInitialized;
    }


    /* Return true if flag is fully captured */
    public boolean getIsCaptured() {
        return isCaptured;
    }


    /* Return true if a repeating task is active */
    public boolean getIsTaskActive() {
        return isTaskActive;
    }


    /* Return team that this flag belongs to */
    public Team getTeam() {
        return flagTeam;
    }


    /* Message everyone who is near this flag*/
    public void messageNearbyPlayers(String message) {
        for (Player player: Bukkit.getServer().getOnlinePlayers()) {
            if (!(player.getWorld().equals(startingBlock.getWorld()))) {
                continue;
            }
            if (player.getLocation().distance(startingBlock.getLocation()) < 1000) {
                player.sendMessage(flagTeam.getColor() + message
                        + " (" + captureProgress + "/" + totalFlagpoleLength + ")");
            }
        }
    }
}
