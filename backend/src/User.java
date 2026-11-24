/**
 * User.java
 * --------
 * BASE CLASS for all users of HarvestHub (farmers and customers).
 *
 * OOP CONCEPT - CLASS AND OBJECT:
 *   A "class" is a blueprint. An "object" is a real instance created from that blueprint.
 *   For example, "User" is a class. When a farmer named Ramesh registers, we create an
 *   object of User (or its subclass Farmer) to hold Ramesh's details in memory.
 *
 * OOP CONCEPT - ENCAPSULATION:
 *   Encapsulation means hiding data inside a class and only allowing access through
 *   public methods (getters and setters). Here all variables are "private", so they
 *   cannot be changed directly from outside the class. Other code must use setName(),
 *   getName(), etc. This protects the data from accidental changes.
 *
 * OOP CONCEPT - INHERITANCE:
 *   This class is the parent (super class). Farmer and Customer will EXTEND User,
 *   meaning they inherit all its fields and methods and add their own special features.
 */
public class User {

    // Private fields -> only this class can see them directly (encapsulation)
    private int userId;
    private String name;
    private String email;
    private String password;
    private String role;   // "farmer" or "customer"

    // Default constructor
    public User() {
    }

    // Parameterised constructor - used when we already know the user's details
    public User(String name, String email, String password, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // ---- Getters and Setters ----
    // These public methods allow other classes to read/change the private fields safely.

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
