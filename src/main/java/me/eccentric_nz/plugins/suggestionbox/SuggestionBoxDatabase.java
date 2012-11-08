package me.eccentric_nz.plugins.suggestionbox;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SuggestionBoxDatabase {

    private static SuggestionBoxDatabase instance = new SuggestionBoxDatabase();
    public Connection connection = null;
    public Statement statement;
    private SuggestionBox plugin;

    public static synchronized SuggestionBoxDatabase getInstance() {
        return instance;
    }

    public void setConnection(String path) throws Exception {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);
    }

    public Connection getConnection() {
        return connection;
    }

    public void createTables() {
        try {
            statement = connection.createStatement();
            String querySuggestions = "CREATE TABLE IF NOT EXISTS suggestions (sb_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, suggestion TEXT, player TEXT, type TEXT DEFAULT 'Suggestion', priority INTEGER DEFAULT 0)";
            statement.executeUpdate(querySuggestions);
            ResultSet rsFirst = statement.executeQuery("SELECT * FROM suggestions");
            if (!rsFirst.next()) {
                String queryAddDefault = "INSERT INTO suggestions (suggestion, player, type, priority) VALUES ('Welcome to SuggestionBox!', 'eccentric_nz','Comment',3)";
                statement.executeUpdate(queryAddDefault);
            }
            rsFirst.close();
            statement.close();
        } catch (SQLException e) {
            System.err.println(SuggestionBoxConstants.MY_PLUGIN_NAME + " Create table error: " + e);
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Clone is not allowed.");
    }
}
