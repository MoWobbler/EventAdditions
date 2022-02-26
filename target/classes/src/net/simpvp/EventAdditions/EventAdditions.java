package net.simpvp.EventAdditions;

import org.bukkit.plugin.java.JavaPlugin;

public class EventAdditions extends JavaPlugin {

    public static JavaPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        getCommand("createflag").setExecutor(new CreateFlagCommand());
        getCommand("deleteflags").setExecutor(new DeleteFlagsCommand());
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        getCommand("timer").setExecutor(new TimerCommand());
        getCommand("stoptimers").setExecutor(new TimerCommand());
    }

}
