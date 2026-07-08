package com.dueling.plugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import com.dueling.plugin.managers.ArenaManager;
import com.dueling.plugin.managers.MatchHandler;
import com.dueling.plugin.managers.ConfigManager;
import com.dueling.plugin.managers.GUIManager; // IMPORT THIS
import com.dueling.plugin.commands.DuelCommand;
import com.dueling.plugin.listeners.PlayerInteractionListener;
import com.dueling.plugin.listeners.PlayerDeathListener;
import com.dueling.plugin.listeners.BlockBreakListener;

public class DuelingPlugin extends JavaPlugin {

    private static DuelingPlugin instance;
    private ArenaManager arenaManager;
    private MatchHandler matchHandler;
    private ConfigManager configManager;
    private GUIManager guiManager; // ADD THIS FIELD
    private DuelCommand duelCommand;

    @Override
    public void onEnable() {
        instance = this;
        
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        arenaManager = new ArenaManager(this);
        matchHandler = new MatchHandler(this);
        
        // INITIALIZE GUIMANAGER HERE
        guiManager = new GUIManager(this); 
        
        duelCommand = new DuelCommand(this);

        getCommand("duel").setExecutor(duelCommand);
        
        Bukkit.getPluginManager().registerEvents(new PlayerInteractionListener(this, duelCommand), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BlockBreakListener(this), this);

        getLogger().info("\u00a7a[Dueling Plugin] Plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (matchHandler != null) {
            matchHandler.cancelAllMatches();
        }
    }

    public static DuelingPlugin getInstance() { return instance; }
    public ArenaManager getArenaManager() { return arenaManager; }
    public MatchHandler getMatchHandler() { return matchHandler; }
    public ConfigManager getConfigManager() { return configManager; }
    public GUIManager getGuiManager() { return guiManager; } // ADD THIS GETTER
    public DuelCommand getDuelCommand() { return duelCommand; }
}
