package util;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Initializes the SQLite database schema and seeds default data.
 */
public final class DatabaseInitializer {
    private static final String[] CREATE_TABLES = {
            "CREATE TABLE IF NOT EXISTS users (" +
                    "user_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT UNIQUE NOT NULL," +
                    "password_hash TEXT NOT NULL," +
                    "full_name TEXT," +
                    "is_active INTEGER DEFAULT 1)",
            "CREATE TABLE IF NOT EXISTS customers (" +
                    "customer_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "full_name TEXT NOT NULL," +
                    "phone TEXT," +
                    "email TEXT," +
                    "id_number TEXT)",
            "CREATE TABLE IF NOT EXISTS rooms (" +
                    "room_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "room_number TEXT UNIQUE NOT NULL," +
                    "room_type TEXT NOT NULL," +
                    "price_per_night REAL NOT NULL," +
                    "status TEXT NOT NULL DEFAULT 'AVAILABLE')",
            "CREATE TABLE IF NOT EXISTS reservations (" +
                    "reservation_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "customer_id INTEGER NOT NULL," +
                    "room_id INTEGER NOT NULL," +
                    "check_in_date TEXT NOT NULL," +
                    "check_out_date TEXT NOT NULL," +
                    "status TEXT NOT NULL," +
                    "FOREIGN KEY (customer_id) REFERENCES customers(customer_id)," +
                    "FOREIGN KEY (room_id) REFERENCES rooms(room_id))",
            "CREATE TABLE IF NOT EXISTS payments (" +
                    "payment_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "reservation_id INTEGER NOT NULL," +
                    "amount REAL NOT NULL," +
                    "method TEXT NOT NULL," +
                    "payment_date TEXT NOT NULL," +
                    "FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id))"
    };

    public static void initialize() throws SQLException {
        try (Connection conn = SQLiteConnectionManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            for (String sql : CREATE_TABLES) {
                stmt.execute(sql);
            }
            runMigrations(stmt);
        }
    }

    private static void runMigrations(Statement stmt) throws SQLException {
        try {
            stmt.execute("ALTER TABLE customers ADD COLUMN username TEXT");
        } catch (SQLException ignored) { /* column may exist */ }
        try {
            stmt.execute("ALTER TABLE customers ADD COLUMN password_hash TEXT");
        } catch (SQLException ignored) { }
        try {
            stmt.execute("ALTER TABLE reservations ADD COLUMN number_of_guests INTEGER DEFAULT 1");
        } catch (SQLException ignored) { }
    }


}
