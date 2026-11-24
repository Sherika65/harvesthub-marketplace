/**
 * Customer.java
 * ------------
 * A Customer is a special type of User who buys agricultural products.
 *
 * OOP CONCEPT - INHERITANCE:
 *   "extends User" means Customer inherits all common user fields and methods.
 *   Customer adds its own special feature: a shopping cart.
 */
public class Customer extends User {

    // Each customer has their own cart (encapsulated as a private field).
    private Cart cart;

    public Customer() {
        super();
        this.setRole("customer");
        // When a customer object is created, give them an empty cart.
        this.cart = new Cart();
    }

    public Customer(String name, String email, String password) {
        super(name, email, password, "customer");
        this.cart = new Cart();
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }
}
