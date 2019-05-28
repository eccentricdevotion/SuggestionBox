package me.eccentric_nz.suggestionbox;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SuggestionBoxCommands implements CommandExecutor {

    private final SuggestionBox plugin;
    SuggestionBoxDatabase service = SuggestionBoxDatabase.getInstance();

    public SuggestionBoxCommands(SuggestionBox plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // If the player typed /suggest then do the following...
        // check there is the right number of arguments
        if (cmd.getName().equalsIgnoreCase("suggest")) {
            if (args.length == 0) {
                sender.sendMessage(SuggestionBox.CHAT_PREFIX + "Help"); //suggestion, comment, question or request
                sender.sendMessage(ChatColor.GREEN + "/suggest [s|c|q|r] [Your text here]");
                sender.sendMessage("Change the options between the square brackets - [ ]");
                sender.sendMessage(ChatColor.BOLD + "s" + ChatColor.RESET + " = suggestion");
                sender.sendMessage(ChatColor.BOLD + "c" + ChatColor.RESET + " = comment");
                sender.sendMessage(ChatColor.BOLD + "q" + ChatColor.RESET + " = question");
                sender.sendMessage(ChatColor.BOLD + "r" + ChatColor.RESET + " = request");
                sender.sendMessage(ChatColor.GREEN + "/sblist");
                sender.sendMessage("List existing suggestions, comments, questions or requests");
                return true;
            }
            String theType = args[0].toLowerCase(Locale.ENGLISH);
            Type type = Type.getByArgument().get(theType);
            if (type == null) {
                sender.sendMessage(SuggestionBox.CHAT_PREFIX + "Not a valid suggestion type!");
                return false;
            }
            if (args.length < 2) {
                sender.sendMessage(SuggestionBox.CHAT_PREFIX + "Not enough command arguments!");
                return false;
            } else {
                StringBuilder sb = new StringBuilder(args[1]);
                for (int i = 2; i < args.length; i++) {
                    sb.append(" ").append(args[i]);
                }
                String endSuggest = sb.toString();
                String p;
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    p = player.getName();
                } else {
                    p = "Admin via console";
                }
                try {
                    Connection connection = service.getConnection();
                    PreparedStatement statement = connection.prepareStatement("INSERT INTO suggestions (suggestion, type, player) VALUES (?,?,?)");
                    statement.setString(1, endSuggest);
                    statement.setString(2, type.getName());
                    statement.setString(3, p);
                    statement.executeUpdate();
                    statement.close();
                } catch (SQLException e) {
                    System.err.println(SuggestionBox.CHAT_PREFIX + ChatColor.RED + "Couldn't save suggestions: " + ChatColor.RESET + e);
                }
                sender.sendMessage(SuggestionBox.CHAT_PREFIX + "Thank you for your " + type.getName() + "!");
                return true;
            }
        }
        if (cmd.getName().equalsIgnoreCase("sbread")) {
            if (args.length == 0) {
                sender.sendMessage(SuggestionBox.CHAT_PREFIX + "You must specify a SuggestionBox ID number!");
                return false;
            }
            try {
                Connection connection = service.getConnection();
                Statement statement = connection.createStatement();
                String queryRead = "SELECT * FROM suggestions WHERE sb_id = " + args[0];
                ResultSet rsRead = statement.executeQuery(queryRead);
                if (!rsRead.isBeforeFirst()) {
                    sender.sendMessage(SuggestionBox.CHAT_PREFIX + "Could not find that suggestion!");
                    return false;
                }
                sender.sendMessage(SuggestionBox.CHAT_PREFIX + "READ");
                sender.sendMessage("-- " + rsRead.getString("type") + " --");
                sender.sendMessage(rsRead.getString("suggestion"));
                sender.sendMessage("-- Who: " + rsRead.getString("player"));
                int py = rsRead.getInt("priority");
                String p;
                switch (py) {
                    case 1:
                        p = ChatColor.GREEN + "NORMAL" + ChatColor.RESET;
                        break;
                    case 2:
                        p = ChatColor.GOLD + "MEDIUM" + ChatColor.RESET;
                        break;
                    case 3:
                        p = ChatColor.RED + "HIGH" + ChatColor.RESET;
                        break;
                    default:
                        p = "NONE";
                }
                sender.sendMessage("-- Priority: " + p);
                rsRead.close();
                statement.close();
            } catch (SQLException e) {
                System.err.println(SuggestionBox.CHAT_PREFIX + ChatColor.RED + "Couldn't get suggestion to read:" + ChatColor.RESET + e);
            }
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("sbdelete")) {
            if (args.length == 0) {
                sender.sendMessage(SuggestionBox.CHAT_PREFIX + "You must specify a SuggestionBox ID number!");
                return false;
            }
            try {
                Connection connection = service.getConnection();
                Statement statement = connection.createStatement();
                String queryChkID = "SELECT sb_id FROM suggestions WHERE sb_id = " + args[0];
                ResultSet rsID = statement.executeQuery(queryChkID);
                if (!rsID.isBeforeFirst()) {
                    sender.sendMessage(SuggestionBox.CHAT_PREFIX + "Could not find that suggestion!");
                    return false;
                }
                String queryDelete = "DELETE FROM suggestions WHERE sb_id = " + args[0];
                statement.executeUpdate(queryDelete);
                rsID.close();
                statement.close();
            } catch (SQLException e) {
                System.err.println(SuggestionBox.CHAT_PREFIX + ChatColor.RED + "Couldn't delete suggestion:" + ChatColor.RESET + e);
            }
            sender.sendMessage(SuggestionBox.CHAT_PREFIX + "Successfully deleted suggestion!");
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("sblist")) {
            try {
                Connection connection = service.getConnection();
                Statement statement = connection.createStatement();
                String queryList = "SELECT * FROM suggestions ORDER BY type, priority DESC";
                ResultSet rsList = statement.executeQuery(queryList);
                if (!rsList.isBeforeFirst()) {
                    sender.sendMessage(SuggestionBox.CHAT_PREFIX + "There are no suggestions in the box yet!");
                    return false;
                }
                sender.sendMessage(SuggestionBox.CHAT_PREFIX + "LIST");
                sender.sendMessage("No. | ID | Suggestion | Player | Type | Priority");
                int i = 1;
                while (rsList.next()) {
                    int id = rsList.getInt("sb_id");
                    int str_len = rsList.getString("suggestion").length();
                    String s;
                    if (str_len > 20) {
                        s = rsList.getString("suggestion").substring(0, 20);
                    } else {
                        s = rsList.getString("suggestion").substring(0, str_len);
                    }
                    String p = rsList.getString("player");
                    String t = rsList.getString("type");
                    String y;
                    int py = rsList.getInt("priority");
                    switch (py) {
                        case 1:
                            y = ChatColor.GREEN + "NORMAL" + ChatColor.RESET;
                            break;
                        case 2:
                            y = ChatColor.GOLD + "MEDIUM" + ChatColor.RESET;
                            break;
                        case 3:
                            y = ChatColor.RED + "HIGH" + ChatColor.RESET;
                            break;
                        default:
                            y = "";
                    }
                    sender.sendMessage(i + " | " + id + " | " + s + "... | " + p + " | " + t + " | " + y);
                    sender.sendMessage("----------");
                    i++;
                }
                rsList.close();
                statement.close();
            } catch (SQLException e) {
                System.err.println(SuggestionBox.CHAT_PREFIX + ChatColor.RED + "Couldn't get suggestion list:" + ChatColor.RESET + e);
            }
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("sbpriority")) {
            if (args.length == 0) {
                sender.sendMessage(SuggestionBox.CHAT_PREFIX + "You must specify a SuggestionBox ID number!");
                return false;
            }
            if (args.length < 2) {
                sender.sendMessage(SuggestionBox.CHAT_PREFIX + "Not enough command arguments!");
                return false;
            }
            String p = args[1].toUpperCase();
            int i;
            Priority priority;
            try {
                priority = Priority.valueOf(p);
                i = priority.getId();
            } catch (IllegalArgumentException e) {
                sender.sendMessage(SuggestionBox.CHAT_PREFIX + "Does not recognise that priority!");
                return false;
            }
            try {
                Connection connection = service.getConnection();
                Statement statement = connection.createStatement();
                String queryChkID = "SELECT sb_id FROM suggestions WHERE sb_id = " + args[0];
                ResultSet rsID = statement.executeQuery(queryChkID);
                if (!rsID.isBeforeFirst()) {
                    sender.sendMessage(SuggestionBox.CHAT_PREFIX + "Could not find that suggestion!");
                    return false;
                }
                String queryPriority = "UPDATE suggestions SET priority = " + i + " WHERE sb_id = " + args[0];
                statement.executeUpdate(queryPriority);
                rsID.close();
                statement.close();
            } catch (SQLException e) {
                System.err.println(SuggestionBox.CHAT_PREFIX + ChatColor.RED + "Couldn't set priority:" + ChatColor.RESET + e);
            }
            sender.sendMessage(SuggestionBox.CHAT_PREFIX + "Successfully updated suggestion priority!");
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("sbfile")) {
            try {
                Connection connection = service.getConnection();
                Statement statement = connection.createStatement();
                String queryFile = "SELECT * FROM suggestions ORDER BY type, priority DESC";
                ResultSet rsFile = statement.executeQuery(queryFile);
                int j = 1;
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd-");
                Date now = Calendar.getInstance().getTime();
                String fileDate = df.format(now);
                String fileName = fileDate + "SuggestionBox.txt";
                File file = new File(plugin.getDataFolder(), fileName);
                try {
                    BufferedWriter bw = new BufferedWriter(new FileWriter(file, false));
                    bw.write("No. | ID | Suggestion | Player | Type | Priority");
                    bw.newLine();
                    bw.write("----------");
                    bw.newLine();
                    while (rsFile.next()) {
                        int id = rsFile.getInt("sb_id");
                        String s = rsFile.getString("suggestion");
                        String p = rsFile.getString("player");
                        String t = rsFile.getString("type");
                        String y;
                        int py = rsFile.getInt("priority");
                        switch (py) {
                            case 1:
                                y = "NORMAL";
                                break;
                            case 2:
                                y = "MEDIUM";
                                break;
                            case 3:
                                y = "HIGH";
                                break;
                            default:
                                y = "";
                        }
                        bw.write(j + " | " + id + " | " + s + " | " + p + " | " + t + " | " + y);
                        bw.newLine();
                        bw.write("----------");
                        bw.newLine();
                        j++;
                    }
                    bw.close();
                } catch (IOException io) {
                    System.err.println(SuggestionBox.CHAT_PREFIX + ChatColor.RED + "Couldn't write to file:" + ChatColor.RESET + io);
                }
                rsFile.close();
                statement.close();
            } catch (SQLException e) {
                System.err.println(SuggestionBox.CHAT_PREFIX + ChatColor.RED + "Couldn't get substitutions:" + ChatColor.RESET + e);
            }
            sender.sendMessage(SuggestionBox.CHAT_PREFIX + "Successfully wrote suggestions to file!");
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("sbclear")) {
            try {
                Connection connection = service.getConnection();
                Statement statement = connection.createStatement();
                String queryClear = "DELETE FROM suggestions";
                statement.executeUpdate(queryClear);
            } catch (SQLException e) {
                System.err.println(SuggestionBox.CHAT_PREFIX + ChatColor.RED + "Couldn't clear suggestions:" + ChatColor.RESET + e);
            }
            sender.sendMessage(SuggestionBox.CHAT_PREFIX + "All suggestions were deleted!");
            return true;
        }
        return false;
    }
}
