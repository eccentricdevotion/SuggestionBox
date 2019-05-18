package me.eccentric_nz.plugins.suggestionbox;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
                System.err.println(SuggestionBoxConstants.MY_PLUGIN_NAME + ChatColor.RED + "Could not create directory!");
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
            System.err.println(SuggestionBoxConstants.MY_PLUGIN_NAME + ChatColor.RED + "Connection and Tables Error: " + ChatColor.RESET + e);
        }

        commando = new SuggestionBoxCommands(this);
        getCommand("suggest").setExecutor(commando);
        getCommand("sblist").setExecutor(commando);
        getCommand("sbread").setExecutor(commando);
        getCommand("sbdelete").setExecutor(commando);
        getCommand("sbpriority").setExecutor(commando);
        getCommand("sbfile").setExecutor(commando);
        getCommand("sbclear").setExecutor(commando);

            getConfig().set("first_run", false);
        }
    }

    @Override
    public void onDisable() {
        this.saveConfig();
        try {
            service.connection.close();
        } catch (SQLException e) {
            System.err.println(SuggestionBoxConstants.MY_PLUGIN_NAME + ChatColor.RED + "Could not close database connection: " + ChatColor.RESET + e);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage("Welcome, " + event.getPlayer().getDisplayName() + "!");
    }
}
