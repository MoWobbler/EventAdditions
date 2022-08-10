package net.simpvp.EventAdditions;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class EventAdditions extends JavaPlugin {

    public static JavaPlugin instance;
    public static List<?> listOfWorlds;

    @Override
    public void onEnable() {
        instance = this;
        listOfWorlds = instance.getConfig().getList("worlds");

        this.getConfig().options().copyDefaults(true);
        this.saveConfig();

        getCommand("createflag").setExecutor(new CreateFlagCommand());
        getCommand("deleteflags").setExecutor(new DeleteFlagsCommand());
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        getCommand("timer").setExecutor(new TimerCommand());
        getCommand("stoptimers").setExecutor(new TimerCommand());
        getCommand("tagevent").setExecutor(new TagMinigame());
        getCommand("createobjective").setExecutor(new CreateObjectiveCommand());
        getCommand("deleteobjectives").setExecutor(new DeleteObjectivesCommand());
    }
}
