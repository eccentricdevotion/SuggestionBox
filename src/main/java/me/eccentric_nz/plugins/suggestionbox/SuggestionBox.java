package me.eccentric_nz.plugins.suggestionbox;

import java.io.File;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class SuggestionBox extends JavaPlugin implements Listener {

    protected static SuggestionBox plugin;
    SuggestionBoxDatabase service = SuggestionBoxDatabase.getInstance();
    private SuggestionBoxCommands commando;
    PluginManager pm = Bukkit.getServer().getPluginManager();

    @Override
    public void onEnable() {
        plugin = this;
        this.saveDefaultConfig();

        if (!getDataFolder().exists()) {
            if (!getDataFolder().mkdir()) {
                System.out.println(SuggestionBoxConstants.MY_PLUGIN_NAME + "¤cCould not create directory!¤r");
                System.out.println(SuggestionBoxConstants.MY_PLUGIN_NAME + "Requires you to manually make the SuggestionBox/ directory!");
            }
            getDataFolder().setWritable(true);
            getDataFolder().setExecutable(true);
        }
        try {
            String path = getDataFolder() + File.separator + "SuggestionBox.db";
            service.setConnection(path);
            service.createTables();
            if (getConfig().getBoolean("first_run")) {
                service.addDefault();
                getConfig().set("first_run", false);
            }
        } catch (Exception e) {
            System.out.println(SuggestionBoxConstants.MY_PLUGIN_NAME + "¤cConnection and Tables Error:¤r " + e);
        }

        commando = new SuggestionBoxCommands(plugin);
        getCommand("suggest").setExecutor(commando);
        getCommand("sblist").setExecutor(commando);
        getCommand("sbread").setExecutor(commando);
        getCommand("sbdelete").setExecutor(commando);
        getCommand("sbpriority").setExecutor(commando);
        getCommand("sbfile").setExecutor(commando);

        try {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
        }
        if (!getConfig().contains("first_run")) {
            getConfig().set("first_run", false);
        }
    }

    @Override
    public void onDisable() {
        this.saveConfig();
        try {
            service.connection.close();
        } catch (Exception e) {
            System.out.println(SuggestionBoxConstants.MY_PLUGIN_NAME + "¤cCould not close database connection:¤r " + e);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage("Welcome, " + event.getPlayer().getDisplayName() + "!");
    }
}
