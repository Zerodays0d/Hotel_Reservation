package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class SQLiteConnectionManager {
    private static final String DB_URL = "jdbc:sqlite:hotel_reservation.db";
    private static volatile SQLiteConnectionManager instance;
    private Connection connection;

    private SQLiteConnectionManager() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found", e);
        }
    }

    public static SQLiteConnectionManager getInstance() {
        if (instance == null) {
            synchronized (SQLiteConnectionManager.class) {
                if (instance == null) {
                    instance = new SQLiteConnectionManager();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(true);
        }
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            // Log or handle
        }
    }
}
