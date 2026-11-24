/**
 * Farmer.java
 * -----------
 * A Farmer is a special type of User who sells agricultural products.
 *
 * OOP CONCEPT - INHERITANCE:
 *   The keyword "extends User" means Farmer inherits everything from User
 *   (userId, name, email, password, role and all getters/setters).
 *   We do not have to write those fields again here. Farmer only adds the
 *   extra things a farmer needs (for example, a list of products they own).
 */
public class Farmer extends User {

    // A farmer can keep track of how many products they have added.
    private int productCount;

    public Farmer() {
        // super() calls the parent class (User) constructor automatically.
        super();
        // A farmer's role is always "farmer".
        this.setRole("farmer");
    }

    public Farmer(String name, String email, String password) {
        // Call the parent constructor to set the common fields.
        super(name, email, password, "farmer");
    }

    public int getProductCount() {
        return productCount;
    }

    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }
}
