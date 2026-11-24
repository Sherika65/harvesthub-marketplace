/**
 * DatabaseConnection.java
 * -----------------------
 * This class handles the connection between Java and MySQL using JDBC.
 *
 * WHAT IS JDBC?
 *   JDBC stands for Java Database Connectivity. It is an API (a set of classes)
 *   built into Java that lets Java programs talk to a database. Using JDBC we can:
 *     - Connect to MySQL
 *     - Run SQL queries (INSERT, SELECT, UPDATE, DELETE)
 *     - Read the results
 *
 * HOW JAVA CONNECTS TO MYSQL (the three steps used here):
 *   1. Load the MySQL JDBC driver:        Class.forName("com.mysql.cj.jdbc.Driver");
 *   2. Build a connection URL with the database name.
 *   3. Ask DriverManager for a Connection object using the URL, username and password.
 *
 * WHERE TO ENTER YOUR MYSQL USERNAME AND PASSWORD:
 *   Configure DB_URL, DB_USER, and DB_PASSWORD as environment variables.
 
 *
 * EXCEPTION HANDLING:
 *   Connecting to a database can fail (wrong password, MySQL not running, etc.).
 *   We wrap the connection code in try-catch so the program does not crash and instead
 *   prints a helpful message.
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String DB_URL =
            System.getenv("DB_URL");

    private static final String DB_USER =
            System.getenv("DB_USER");

    private static final String DB_PASSWORD =
            System.getenv("DB_PASSWORD");

    public static Connection getConnection() {
        try {
            if (DB_URL == null || DB_USER == null || DB_PASSWORD == null) {
                throw new IllegalStateException(
    "Database environment variables are missing."
);
            }

            Class.forName("com.mysql.cj.jdbc.Driver");

            return DriverManager.getConnection(
                    DB_URL, DB_USER, DB_PASSWORD
            );

        } catch (Exception e) {
            System.out.println("ERROR: Could not connect to MySQL.");
            e.printStackTrace();
            return null;
        }
    }
}
