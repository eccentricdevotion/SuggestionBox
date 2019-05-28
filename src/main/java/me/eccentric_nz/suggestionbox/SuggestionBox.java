package me.eccentric_nz.suggestionbox;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;

public class SuggestionBox extends JavaPlugin implements Listener {

    static final String CHAT_PREFIX = ChatColor.GOLD + "[SuggestionBox] " + ChatColor.RESET;
    static SuggestionBox plugin;
    SuggestionBoxDatabase service = SuggestionBoxDatabase.getInstance();
    SuggestionBoxCommands commands;

    @Override
    public void onDisable() {
        saveConfig();
        try {
            service.connection.close();
        } catch (SQLException e) {
            System.err.println(CHAT_PREFIX + ChatColor.RED + "Could not close database connection: " + ChatColor.RESET + e);
        }
    }

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        try {
            String path = getDataFolder() + File.separator + "SuggestionBox.db";
            service.setConnection(path);
            service.createTables();
            if (getConfig().getBoolean("first_run")) {
                service.addDefault();
                getConfig().set("first_run", false);
            }
        } catch (Exception e) {
            System.err.println(CHAT_PREFIX + ChatColor.RED + "Connection and Tables Error: " + ChatColor.RESET + e);
        }

        commands = new SuggestionBoxCommands(this);
        getCommand("suggest").setExecutor(commands);
        getCommand("sblist").setExecutor(commands);
        getCommand("sbread").setExecutor(commands);
        getCommand("sbdelete").setExecutor(commands);
        getCommand("sbpriority").setExecutor(commands);
        getCommand("sbfile").setExecutor(commands);
        getCommand("sbclear").setExecutor(commands);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage("Welcome, " + event.getPlayer().getDisplayName() + "!");
    }
}
