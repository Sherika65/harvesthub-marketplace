import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    public int createOrder(
        int customerId,
        double totalAmount,
        List<Cart.CartItem> items,
        String customerName,
        String customerPhone,
        String deliveryAddress,
        String deliveryCity,
        String deliveryPincode, 
        String paymentMethod){

    String orderSql =
    "INSERT INTO orders "
  + "(customer_id, order_date, total_amount, status, customer_name, "
  + "customer_phone, delivery_address, delivery_city, delivery_pincode, "
  + "payment_method, payment_status) "
  + "VALUES (?, CURDATE(), ?, 'Placed', ?, ?, ?, ?, ?, ?, ?)";

    String itemSql =
        "INSERT INTO order_items (order_id, product_id, quantity, price) "
      + "VALUES (?, ?, ?, ?)";

    Connection conn = null;

    try {
        conn = DatabaseConnection.getConnection();

        if (conn == null) {
            return -1;
        }

        conn.setAutoCommit(false);

        int orderId;

        try (PreparedStatement ps = conn.prepareStatement(
                orderSql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, customerId);
            ps.setDouble(2, totalAmount);
            ps.setString(3, customerName);
            ps.setString(4, customerPhone);
            ps.setString(5, deliveryAddress);
            ps.setString(6, deliveryCity);
            ps.setString(7, deliveryPincode);
            ps.setString(8, paymentMethod);
            ps.setString(9, paymentMethod.equals("Cash on Delivery") ? "Pending" : "Paid (Demo)");

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    orderId = keys.getInt(1);
                } else {
                    conn.rollback();
                    return -1;
                }
            }
        }

        try (PreparedStatement ps = conn.prepareStatement(itemSql)) {

    for (Cart.CartItem item : items) {

        // Check current stock
        String stockSql =
            "SELECT quantity FROM products WHERE product_id = ?";

        try (PreparedStatement stockPs = conn.prepareStatement(stockSql)) {

            stockPs.setInt(1, item.getProductId());

            try (ResultSet rs = stockPs.executeQuery()) {

                if (!rs.next()) {
                    conn.rollback();
                    return -1;
                }

                int currentStock = rs.getInt("quantity");

                // Not enough stock
                if (currentStock < item.getQuantity()) {
                    System.out.println(
                        "Not enough stock for product ID: "
                        + item.getProductId()
                    );
                    conn.rollback();
                    return -1;
                }

                // Reduce stock
                String updateStockSql =
                    "UPDATE products "
                  + "SET quantity = quantity - ? "
                  + "WHERE product_id = ?";

                try (PreparedStatement updatePs =
                         conn.prepareStatement(updateStockSql)) {

                    updatePs.setInt(1, item.getQuantity());
                    updatePs.setInt(2, item.getProductId());
                    updatePs.executeUpdate();
                }
            }
        }

        // Save order item
        ps.setInt(1, orderId);
        ps.setInt(2, item.getProductId());
        ps.setInt(3, item.getQuantity());
        ps.setDouble(4, item.getPrice());
        ps.addBatch();
    }

    ps.executeBatch();
}
        conn.commit();
        return orderId;

    } catch (SQLException e) {

        System.out.println("ERROR creating order: " + e.getMessage());

        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                // ignore
            }
        }

        return -1;

    } finally {

        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }
}

    public List<Order> getCustomerOrders(int customerId) {

        List<Order> list = new ArrayList<>();

        String sql = "SELECT order_id, customer_id, order_date, total_amount, status "
                   + "FROM orders "
                   + "WHERE customer_id = ? "
                   + "ORDER BY order_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    list.add(mapRowToOrder(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("ERROR fetching customer orders: " + e.getMessage());
        }

        return list;
    }

    public List<Order> getFarmerOrders(int farmerId) {

        List<Order> list = new ArrayList<>();

        String sql =
    "SELECT DISTINCT o.order_id, o.customer_id, o.order_date, "
  + "o.total_amount, o.status, o.customer_name, "
  + "o.customer_phone, o.delivery_address, o.delivery_city, "
  + "o.delivery_pincode "
  + "FROM orders o "
  + "JOIN order_items oi ON o.order_id = oi.order_id "
  + "JOIN products p ON oi.product_id = p.product_id "
  + "JOIN users u ON o.customer_id = u.user_id "
  + "WHERE p.farmer_id = ? "
  + "ORDER BY o.order_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, farmerId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Order order = mapRowToOrder(rs);

order.setCustomerName(rs.getString("customer_name"));
order.setCustomerPhone(rs.getString("customer_phone"));
order.setDeliveryAddress(rs.getString("delivery_address"));
order.setDeliveryCity(rs.getString("delivery_city"));
order.setDeliveryPincode(rs.getString("delivery_pincode"));

                    // Get the products from this order
                    List<OrderItem> items = getFarmerOrderItems(
                        conn,
                        order.getOrderId(),
                        farmerId
                    );

                    order.setItems(items);

                    list.add(order);
                }
            }

        } catch (SQLException e) {
            System.out.println("ERROR fetching farmer orders: " + e.getMessage());
        }

        return list;
    }

    private List<OrderItem> getFarmerOrderItems(
            Connection conn,
            int orderId,
            int farmerId) {

        List<OrderItem> items = new ArrayList<>();

        String sql =
            "SELECT p.product_id, p.product_name AS product_name, "
          + "oi.quantity, oi.price "
          + "FROM order_items oi "
          + "JOIN products p ON oi.product_id = p.product_id "
          + "WHERE oi.order_id = ? AND p.farmer_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ps.setInt(2, farmerId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    OrderItem item = new OrderItem();

                    item.setProductId(rs.getInt("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setPrice(rs.getDouble("price"));

                    items.add(item);
                }
            }

        } catch (SQLException e) {
            System.out.println("ERROR fetching order items: " + e.getMessage());
        }

        return items;
    }

    private Order mapRowToOrder(ResultSet rs) throws SQLException {

        Order o = new Order();

        o.setOrderId(rs.getInt("order_id"));
        o.setCustomerId(rs.getInt("customer_id"));
        o.setOrderDate(rs.getString("order_date"));
        o.setTotalAmount(rs.getDouble("total_amount"));
        o.setStatus(rs.getString("status"));

        return o;
    }


public boolean addReview(
        int orderId,
        int customerId,
        int rating,
        String reviewText) {

    String sql =
        "INSERT INTO reviews "
      + "(product_id, customer_id, order_id, rating, review_text, review_date) "
      + "SELECT oi.product_id, ?, ?, ?, ?, CURDATE() "
      + "FROM order_items oi "
      + "WHERE oi.order_id = ? "
      + "LIMIT 1";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, customerId);
        ps.setInt(2, orderId);
        ps.setInt(3, rating);
        ps.setString(4, reviewText);
        ps.setInt(5, orderId);

        return ps.executeUpdate() > 0;

    } catch (SQLException e) {
        System.out.println("ERROR adding review: " + e.getMessage());
        return false;
    }
}
public List<String> getFarmerReviews(int farmerId) {

    List<String> reviews = new ArrayList<>();

    String sql =
        "SELECT p.product_name, r.rating, r.review_text, r.review_date "
      + "FROM reviews r "
      + "JOIN products p ON r.product_id = p.product_id "
      + "WHERE p.farmer_id = ? "
      + "ORDER BY r.review_id DESC";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, farmerId);

        try (ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String review =
                    rs.getString("product_name") + " | "
                  + rs.getInt("rating") + " | "
                  + rs.getString("review_text") + " | "
                  + rs.getString("review_date");

                reviews.add(review);
            }
        }

    } catch (SQLException e) {
        System.out.println("ERROR fetching farmer reviews: " + e.getMessage());
    }

    return reviews;
}
}