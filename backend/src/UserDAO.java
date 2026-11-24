/**
 * UserDAO.java
 * ------------
 * DAO = Data Access Object. This class contains all database operations related to Users
 * (register and login). It uses JDBC with PreparedStatement.
 *
 * WHY PreparedStatement?
 *   A PreparedStatement sends the SQL and the data separately. This prevents SQL injection
 *   (a hacker typing SQL into a form field). It is the safe way to run SQL in Java.
 *
 * EXCEPTION HANDLING:
 *   Every database method is wrapped in try-catch. If something fails we print a clear
 *   message and return a safe value (null / false) instead of crashing.
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    /**
     * Register a new user (farmer or customer).
     * Returns true if the registration succeeded, false otherwise.
     */
    public boolean registerUser(User user) {
        // Check for duplicate email before inserting.
        if (emailExists(user.getEmail())) {
            System.out.println("Registration failed: email already exists -> " + user.getEmail());
            return false;
        }

        String sql = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)";

        // try-with-resources: the Connection and PreparedStatement are closed automatically.
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole());

            int rows = ps.executeUpdate();   // runs the INSERT
            return rows > 0;                 // true if at least one row was added

        } catch (SQLException e) {
            System.out.println("ERROR during user registration: " + e.getMessage());
            return false;
        }
    }

    /**
     * Login: find a user by email and password.
     * Returns a User object (Farmer or Customer) if found, or null if login fails.
     */
    public User login(String email, String password) {
        String sql = "SELECT user_id, name, email, password, role FROM users WHERE email = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String role = rs.getString("role");
                    User user;
                    // POLYMORPHISM: we create a Farmer or Customer object depending on the role,
                    // but return it as a User reference. This is a key OOP concept.
                    if ("farmer".equalsIgnoreCase(role)) {
                        user = new Farmer();
                    } else {
                        user = new Customer();
                    }
                    user.setUserId(rs.getInt("user_id"));
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password"));
                    user.setRole(role);
                    return user;
                }
            }

        } catch (SQLException e) {
            System.out.println("ERROR during login: " + e.getMessage());
        }
        return null;   // invalid login
    }

    /**
     * Helper: check if an email already exists (used to stop duplicate registration).
     */
    public boolean emailExists(String email) {
        String sql = "SELECT user_id FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();   // true if a row was found
            }
        } catch (SQLException e) {
            System.out.println("ERROR checking email: " + e.getMessage());
        }
        return false;
    }
}
