/**
 * ProductDAO.java
 * ---------------
 * All database operations related to Products.
 * Uses JDBC PreparedStatement for every query.
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    /**
     * Add a new product for a farmer.
     */
    public boolean addProduct(Product product) {

        String sql = "INSERT INTO products "
                   + "(farmer_id, product_name, category, price, quantity, description, image_url) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, product.getFarmerId());
            ps.setString(2, product.getProductName());
            ps.setString(3, product.getCategory());
            ps.setDouble(4, product.getPrice());
            ps.setInt(5, product.getQuantity());
            ps.setString(6, product.getDescription());
            ps.setString(7, product.getImageUrl());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("ERROR adding product: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all products for customer marketplace.
     */
    public List<Product> getAllProducts() {

        List<Product> list = new ArrayList<>();

        String sql = "SELECT p.product_id, p.farmer_id, p.product_name, p.category, "
                   + "p.price, p.quantity, p.description, p.image_url, "
                   + "u.name AS farmer_name "
                   + "FROM products p "
                   + "JOIN users u ON p.farmer_id = u.user_id "
                   + "ORDER BY p.product_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToProduct(rs));
            }

        } catch (SQLException e) {
            System.out.println("ERROR fetching products: " + e.getMessage());
        }

        return list;
    }

    /**
     * Get all products added by a particular farmer.
     */
    public List<Product> getProductsByFarmer(int farmerId) {

        List<Product> list = new ArrayList<>();

        String sql = "SELECT p.product_id, p.farmer_id, p.product_name, p.category, "
                   + "p.price, p.quantity, p.description, p.image_url, "
                   + "u.name AS farmer_name "
                   + "FROM products p "
                   + "JOIN users u ON p.farmer_id = u.user_id "
                   + "WHERE p.farmer_id = ? "
                   + "ORDER BY p.product_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, farmerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToProduct(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("ERROR fetching farmer products: " + e.getMessage());
        }

        return list;
    }

    /**
     * Get a single product by its id.
     */
    public Product getProductById(int productId) {

        String sql = "SELECT p.product_id, p.farmer_id, p.product_name, p.category, "
                   + "p.price, p.quantity, p.description, p.image_url, "
                   + "u.name AS farmer_name "
                   + "FROM products p "
                   + "JOIN users u ON p.farmer_id = u.user_id "
                   + "WHERE p.product_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToProduct(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("ERROR fetching product: " + e.getMessage());
        }

        return null;
    }

    /**
     * Search products by name or category.
     */
    public List<Product> searchProducts(String keyword) {

        List<Product> list = new ArrayList<>();

        String sql = "SELECT p.product_id, p.farmer_id, p.product_name, p.category, "
                   + "p.price, p.quantity, p.description, p.image_url, "
                   + "u.name AS farmer_name "
                   + "FROM products p "
                   + "JOIN users u ON p.farmer_id = u.user_id "
                   + "WHERE p.product_name LIKE ? OR p.category LIKE ? "
                   + "ORDER BY p.product_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String like = "%" + keyword + "%";

            ps.setString(1, like);
            ps.setString(2, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToProduct(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("ERROR searching products: " + e.getMessage());
        }

        return list;
    }

    /**
     * Update an existing product.
     */
    public boolean updateProduct(Product product) {

        String sql = "UPDATE products SET "
                   + "product_name = ?, "
                   + "category = ?, "
                   + "price = ?, "
                   + "quantity = ?, "
                   + "description = ?, "
                   + "image_url = ? "
                   + "WHERE product_id = ? AND farmer_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, product.getProductName());
            ps.setString(2, product.getCategory());
            ps.setDouble(3, product.getPrice());
            ps.setInt(4, product.getQuantity());
            ps.setString(5, product.getDescription());
            ps.setString(6, product.getImageUrl());
            ps.setInt(7, product.getProductId());
            ps.setInt(8, product.getFarmerId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("ERROR updating product: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete a product.
     */
    public boolean deleteProduct(int productId, int farmerId) {

        String sql = "DELETE FROM products WHERE product_id = ? AND farmer_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            ps.setInt(2, farmerId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("ERROR deleting product: " + e.getMessage());
            return false;
        }
    }

    /**
     * Convert one ResultSet row into a Product object.
     */
    private Product mapRowToProduct(ResultSet rs) throws SQLException {

        Product p = new Product();

        p.setProductId(rs.getInt("product_id"));
        p.setFarmerId(rs.getInt("farmer_id"));
        p.setFarmerName(rs.getString("farmer_name"));
        p.setProductName(rs.getString("product_name"));
        p.setCategory(rs.getString("category"));
        p.setPrice(rs.getDouble("price"));
        p.setQuantity(rs.getInt("quantity"));
        p.setDescription(rs.getString("description"));
        p.setImageUrl(rs.getString("image_url"));

        return p;
    }
}