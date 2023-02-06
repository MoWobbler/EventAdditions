package net.simpvp.EventAdditions;

import net.simpvp.EventAdditions.commands.*;
import net.simpvp.EventAdditions.events.EventListener;
import net.simpvp.EventAdditions.gameObjects.TagMinigame;
import net.simpvp.EventAdditions.util.ConfigUtil;
import org.bukkit.block.Sign;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class EventAdditions extends JavaPlugin {

    public static JavaPlugin instance;
    public static List<?> listOfWorlds;
    public static ConfigUtil mobConfig;

    @Override
    public void onEnable() {
        instance = this;
        listOfWorlds = instance.getConfig().getList("worlds");

        saveDefaultConfig();
        mobConfig = new ConfigUtil(this,"mobs.yml");

        getServer().getPluginManager().registerEvents(new EventListener(), this);
        getCommand("createflag").setExecutor(new CreateFlagCommand());
        getCommand("deleteflags").setExecutor(new DeleteFlagsCommand());
        getCommand("timer").setExecutor(new TimerCommand());
        getCommand("stoptimers").setExecutor(new TimerCommand());
        getCommand("tagevent").setExecutor(new TagMinigame());
        getCommand("createobjective").setExecutor(new CreateObjectiveCommand());
        getCommand("deleteobjectives").setExecutor(new DeleteObjectivesCommand());
        getCommand("mobwave").setExecutor(new MobWaveCommand());
        getCommand("endmobwaves").setExecutor(new EndMobWaves());
        getCommand("shopitem").setExecutor(new ShopItemCommand());
        getCommand("signshop").setExecutor(new SignShopCommand());
    }
}
