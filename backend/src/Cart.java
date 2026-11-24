/**
 * Cart.java
 * ---------
 * A simple shopping cart for a customer.
 *
 * The cart is kept very simple on purpose so it is easy to explain in a college review.
 * In this project the cart is actually managed by the frontend (JavaScript) and sent
 * to the backend only when the customer places an order. This class is used in the Java
 * backend to represent the items inside an order and to calculate totals.
 *
 * Each cart item is a CartItem object that stores the product and the quantity chosen.
 */
public class Cart {

    /**
     * CartItem - an inner class representing one line in the cart.
     * (An inner class is a class defined inside another class. It is a Java OOP feature.)
     */
    public static class CartItem {
        private int productId;
        private String productName;
        private double price;
        private int quantity;

        public CartItem(int productId, String productName, double price, int quantity) {
            this.productId = productId;
            this.productName = productName;
            this.price = price;
            this.quantity = quantity;
        }

        public int getProductId() { return productId; }
        public String getProductName() { return productName; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        // Line total = price x quantity for this item
        public double getLineTotal() {
            return price * quantity;
        }
    }
}
