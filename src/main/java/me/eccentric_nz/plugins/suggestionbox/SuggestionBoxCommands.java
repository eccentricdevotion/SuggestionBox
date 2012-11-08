package me.eccentric_nz.plugins.suggestionbox;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SuggestionBoxCommands implements CommandExecutor {

    private SuggestionBox plugin;
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
                sender.sendMessage(SuggestionBoxConstants.MY_PLUGIN_NAME + "Help"); //suggestion, comment, question or request
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
            String theType = args[0];
            boolean tOK = false;
            int tkey = 0;
            for (String type : SuggestionBoxConstants.SBTYPE) {
                if (theType.equalsIgnoreCase(type.substring(0, 1))) {
                    tOK = true;
                    break;
                }
                tkey++;
            }
            if (tOK == false) {
                sender.sendMessage(SuggestionBoxConstants.MY_PLUGIN_NAME + "Not a valid suggestion type!");
                return false;
            }
            if (args.length < 2) {
                sender.sendMessage(SuggestionBoxConstants.MY_PLUGIN_NAME + "Not enough command arguments!");
                return false;
            }
            if (args.length > 1) {
                String suggest = "";
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
                String t = SuggestionBoxConstants.SBTYPE[tkey];
                try {
                    Connection connection = service.getConnection();
                    PreparedStatement statement = connection.prepareStatement("INSERT INTO suggestions (suggestion, type, player) VALUES (?,?,?)");
                    statement.setString(1, endSuggest);
                    statement.setString(2, t);
                    statement.setString(3, p);
                    statement.executeUpdate();
                    statement.close();
                } catch (SQLException e) {
                    System.out.println(SuggestionBoxConstants.MY_PLUGIN_NAME + "¤cCouldn't save suggestions:¤r " + e);
                }
                sender.sendMessage(SuggestionBoxConstants.MY_PLUGIN_NAME + "Thank you for your "+t.toLowerCase()+"!");
                return true;
            }
        }
        if (cmd.getName().equalsIgnoreCase("sbread")) {
            if (args.length == 0) {
                sender.sendMessage(SuggestionBoxConstants.MY_PLUGIN_NAME + "You must specify a SuggestionBox ID number!");
                return false;
            }
            try {
                Connection connection = service.getConnection();
                Statement statement = connection.createStatement();
                String queryRead = "SELECT * FROM suggestions WHERE sb_id = " + args[0];
                ResultSet rsRead = statement.executeQuery(queryRead);
                if (!rsRead.isBeforeFirst()) {
                    sender.sendMessage(SuggestionBoxConstants.MY_PLUGIN_NAME + "Could not find that suggestion!");
                    return false;
                }
                sender.sendMessage(SuggestionBoxConstants.MY_PLUGIN_NAME + "READ");
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
                System.out.println(SuggestionBoxConstants.MY_PLUGIN_NAME + "¤cCouldn't get suggestion to read:¤r " + e);
            }
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("sbdelete")) {
            if (args.length == 0) {
                sender.sendMessage(SuggestionBoxConstants.MY_PLUGIN_NAME + "You must specify a SuggestionBox ID number!");
                return false;
            }
            try {
                Connection connection = service.getConnection();
                Statement statement = connection.createStatement();
                String queryChkID = "SELECT sb_id FROM suggestions WHERE sb_id = " + args[0];
                ResultSet rsID = statement.executeQuery(queryChkID);
                if (!rsID.isBeforeFirst()) {
                    sender.sendMessage(SuggestionBoxConstants.MY_PLUGIN_NAME + "Could not find that suggestion!");
                    return false;
                }
                String queryDelete = "DELETE FROM suggestions WHERE sb_id = " + args[0];
                statement.executeUpdate(queryDelete);
                rsID.close();
                statement.close();
            } catch (SQLException e) {
                System.out.println(SuggestionBoxConstants.MY_PLUGIN_NAME + "¤cCouldn't delete suggestion:¤r " + e);
            }
            sender.sendMessage(SuggestionBoxConstants.MY_PLUGIN_NAME + "Successfully deleted suggestion!");
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("sblist")) {
            try {
                Connection connection = service.getConnection();
                Statement statement = connection.createStatement();
                String queryList = "SELECT * FROM suggestions ORDER BY type, priority DESC";
                ResultSet rsList = statement.executeQuery(queryList);
                if (!rsList.isBeforeFirst()) {
                    sender.sendMessage(SuggestionBoxConstants.MY_PLUGIN_NAME + "There are no suggestions in the box yet!");
                    return false;
                }
                sender.sendMessage(SuggestionBoxConstants.MY_PLUGIN_NAME + "LIST");
                sender.sendMessage("ID | Suggestion | Player | Type | Priority");
                while (rsList.next()) {
                    int id = rsList.getInt("sb_id");
                    String s = rsList.getString("suggestion").substring(0, 20);
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
                    sender.sendMessage(id + " | " + s + "... | " + p + " | " + t + " | " + y);
                    sender.sendMessage("----------");
                }
                rsList.close();
                statement.close();
            } catch (SQLException e) {
                System.out.println(SuggestionBoxConstants.MY_PLUGIN_NAME + "¤cCouldn't get suggestion list:¤r " + e);
            }
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("sbpriority")) {
            if (args.length == 0) {
                sender.sendMessage(SuggestionBoxConstants.MY_PLUGIN_NAME + "You must specify a SuggestionBox ID number!");
                return false;
            }
            if (args.length < 2) {
                sender.sendMessage(SuggestionBoxConstants.MY_PLUGIN_NAME + "Not enough command arguments!");
                return false;
            }
            String p = args[1].toUpperCase();
            boolean pOK = false;
            int i = 0;
            for (String priority : SuggestionBoxConstants.PRIORITY) {
                if (p.equals(priority)) {
                    pOK = true;
                    break;
                }
                i++;
            }
            if (pOK == false) {
                sender.sendMessage(SuggestionBoxConstants.MY_PLUGIN_NAME + "Does not recognise that priority!");
                return false;
            }
            int pnum = i;
            try {
                Connection connection = service.getConnection();
                Statement statement = connection.createStatement();
                String queryChkID = "SELECT sb_id FROM suggestions WHERE sb_id = " + args[0];
                ResultSet rsID = statement.executeQuery(queryChkID);
                if (!rsID.isBeforeFirst()) {
                    sender.sendMessage(SuggestionBoxConstants.MY_PLUGIN_NAME + "Could not find that suggestion!");
                    return false;
                }
                String queryPriority = "UPDATE suggestions SET priority = " + pnum + " WHERE sb_id = " + args[0];
                statement.executeUpdate(queryPriority);
                rsID.close();
                statement.close();
            } catch (SQLException e) {
                System.out.println(SuggestionBoxConstants.MY_PLUGIN_NAME + "¤cCouldn't set priority:¤r " + e);
            }
            sender.sendMessage(SuggestionBoxConstants.MY_PLUGIN_NAME + "Successfully updated suggestion priority!");
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
                    System.out.println(SuggestionBoxConstants.MY_PLUGIN_NAME + "¤cCouldn't write to file:¤r " + io);
                }
                rsFile.close();
                statement.close();
            } catch (SQLException e) {
                System.out.println(SuggestionBoxConstants.MY_PLUGIN_NAME + "¤cCouldn't get substitutions:¤r " + e);
            }
            sender.sendMessage(SuggestionBoxConstants.MY_PLUGIN_NAME + "Successfully wrote suggestions to file!");
            return true;
        }
        return false;
    }
}
