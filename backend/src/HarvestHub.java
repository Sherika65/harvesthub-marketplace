/**
 * HarvestHub.java  (MAIN CLASS - the backend server)
 * -------------------------------------------------
 * This is the entry point of the Java backend.
 *
 * HOW DOES THE HTML FRONTEND TALK TO THE JAVA BACKEND?
 *   We use a tiny HTTP server built into Java itself (com.sun.net.httpserver.HttpServer).
 *   No Spring Boot, no Tomcat, no extra software needed - just the JDK.
 *   - The Java server listens on http://localhost:8080
 *   - It serves the HTML/CSS/JS files from the frontend/ folder.
 *   - It also exposes small JSON API endpoints (e.g. /api/login) that the
 *     frontend JavaScript calls using fetch().
 *   - The Java code then uses JDBC to read/write the MySQL database.
 *
 * HOW TO RUN (see README for full steps):
 *   1. Make sure MySQL is running and harvesthub_db is created.
 *   2. Put the MySQL JDBC driver jar in the classpath.
 *   3. Compile:  javac -cp ".;mysql-connector-j-8.x.x.jar" *.java   (Windows)
 *               javac -cp ".:mysql-connector-j-8.x.x.jar" *.java   (Mac/Linux)
 *   4. Run:      java -cp ".;mysql-connector-j-8.x.x.jar" HarvestHub  (Windows)
 *               java -cp ".:mysql-connector-j-8.x.x.jar" HarvestHub  (Mac/Linux)
 *   5. Open http://localhost:8080 in your browser.
 */
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class HarvestHub {

    private static final int PORT = 8080;
    // Path to the frontend folder. Adjust if you run from a different directory.
    private static final String FRONTEND_DIR = "frontend";

    // Our DAO objects (the classes that talk to MySQL).
    private static final UserDAO userDAO = new UserDAO();
    private static final ProductDAO productDAO = new ProductDAO();
    private static final OrderDAO orderDAO = new OrderDAO();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // API endpoints
        server.createContext("/api/register", new RegisterHandler());
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/products", new ProductsHandler());
        server.createContext("/api/ai-description", new AIDescriptionHandler());
        server.createContext("/api/ai-image", new AIImageHandler());
        server.createContext("/api/product", new ProductHandler());
        server.createContext("/api/farmer/products", new FarmerProductsHandler());
        server.createContext("/api/order", new OrderHandler());
        server.createContext("/api/review", new ReviewHandler());
        server.createContext("/api/reviews/farmer", new FarmerReviewsHandler());
        server.createContext("/api/orders/customer", new CustomerOrdersHandler());
        server.createContext("/api/orders/farmer", new FarmerOrdersHandler());

        // Serve static frontend files (HTML, CSS, JS) from the frontend/ folder.
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("==================================================");
        System.out.println("  HarvestHub server started!");
        System.out.println("  Open http://localhost:" + PORT + " in your browser.");
        System.out.println("  Press Ctrl+C to stop the server.");
        System.out.println("==================================================");
    }

    // =================================================================
    //  Helper: CORS headers so the browser allows the frontend to call us
    // =================================================================
    private static void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    // =================================================================
    //  Helper: read the request body (JSON string sent by the frontend)
    // =================================================================
    private static String readBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    // =================================================================
    //  Helper: send a JSON response back to the browser
    // =================================================================
    private static void sendJson(HttpExchange exchange, int code, String json) throws IOException {
        setCorsHeaders(exchange);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // =================================================================
    //  Helper: very small JSON builder so we don't need any external library.
    //  (Escapes quotes and basic characters.)
    // =================================================================
    private static String jsonStr(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"")
                       .replace("\n", " ").replace("\r", "") + "\"";
    }

    // =================================================================
    //  REGISTER handler  ->  POST /api/register
    //  Body JSON: { "name":"...", "email":"...", "password":"...", "role":"..." }
    // =================================================================
    static class RegisterHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 200, "{}");
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            String body = readBody(exchange);
            Map<String, String> data = parseSimpleJson(body);

            String name = data.get("name");
            String email = data.get("email");
            String password = data.get("password");
            String role = data.get("role");

            // ---- VALIDATION ----
            if (name == null || name.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                role == null || role.trim().isEmpty()) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"All fields are required.\"}");
                return;
            }
            if (!email.contains("@") || !email.contains(".")) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"Invalid email format.\"}");
                return;
            }
            if (!"farmer".equalsIgnoreCase(role) && !"customer".equalsIgnoreCase(role)) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"Role must be farmer or customer.\"}");
                return;
            }

            User user;
            // Farmer and Customer constructors already set the role internally.
            if ("farmer".equalsIgnoreCase(role)) {
                user = new Farmer(name.trim(), email.trim(), password);
            } else {
                user = new Customer(name.trim(), email.trim(), password);
            }

            boolean ok = userDAO.registerUser(user);
            if (ok) {
                sendJson(exchange, 200, "{\"success\":true,\"message\":\"Registration successful. Please login.\"}");
            } else {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"Email already registered. Please use a different email or login.\"}");
            }
        }
    }

    // =================================================================
    //  LOGIN handler  ->  POST /api/login
    //  Body JSON: { "email":"...", "password":"..." }
    //  Returns the user id, name and role so the frontend knows where to go.
    // =================================================================
    static class LoginHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 200, "{}");
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            String body = readBody(exchange);
            Map<String, String> data = parseSimpleJson(body);

            String email = data.get("email");
            String password = data.get("password");

            if (email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"Email and password are required.\"}");
                return;
            }

            User user = userDAO.login(email.trim(), password);
            if (user != null) {
                String json = "{\"success\":true,\"userId\":" + user.getUserId()
                        + ",\"name\":" + jsonStr(user.getName())
                        + ",\"role\":" + jsonStr(user.getRole()) + "}";
                sendJson(exchange, 200, json);
            } else {
                sendJson(exchange, 401, "{\"success\":false,\"message\":\"Invalid email or password.\"}");
            }
        }
    }

    // =================================================================
    //  PRODUCTS handler  ->  GET /api/products?search=keyword
    //  Returns all products (or filtered by search keyword) as JSON.
    // =================================================================
    static class ReviewHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 200, "{}");
            return;
        }

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        String body = readBody(exchange);
        Map<String, String> data = parseSimpleJson(body);

        try {
            int orderId = Integer.parseInt(data.get("orderId"));
            int customerId = Integer.parseInt(data.get("customerId"));
            int rating = Integer.parseInt(data.get("rating"));
            String reviewText = data.get("reviewText");

            if (rating < 1 || rating > 5) {
                sendJson(exchange, 400,
                    "{\"success\":false,\"message\":\"Rating must be between 1 and 5.\"}");
                return;
            }

            if (reviewText == null || reviewText.trim().isEmpty()) {
                sendJson(exchange, 400,
                    "{\"success\":false,\"message\":\"Please write a review.\"}");
                return;
            }

            boolean success = orderDAO.addReview(
                orderId,
                customerId,
                rating,
                reviewText.trim()
            );

            if (success) {
                sendJson(exchange, 200,
                    "{\"success\":true,\"message\":\"Review submitted successfully!\"}");
            } else {
                sendJson(exchange, 400,
                    "{\"success\":false,\"message\":\"Could not submit review.\"}");
            }

        } catch (Exception e) {
            sendJson(exchange, 400,
                "{\"success\":false,\"message\":\"Invalid review data.\"}");
        }
    }
}
static class FarmerReviewsHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 200, "{}");
            return;
        }

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        try {
            String query = exchange.getRequestURI().getQuery();

            if (query == null || !query.contains("farmerId=")) {
                sendJson(exchange, 400,
                    "{\"success\":false,\"message\":\"Farmer ID is required.\"}");
                return;
            }

            int farmerId = Integer.parseInt(
                query.split("farmerId=")[1].split("&")[0]
            );

            List<String> reviews = orderDAO.getFarmerReviews(farmerId);

            StringBuilder sb = new StringBuilder();
            sb.append("[");

            for (int i = 0; i < reviews.size(); i++) {
                if (i > 0) {
                    sb.append(",");
                }

                String[] parts = reviews.get(i).split(" \\| ", 4);

                sb.append("{")
                  .append("\"productName\":").append(jsonStr(parts[0]))
                  .append(",\"rating\":").append(parts[1])
                  .append(",\"reviewText\":").append(jsonStr(parts[2]))
                  .append(",\"reviewDate\":").append(jsonStr(parts[3]))
                  .append("}");
            }

            sb.append("]");

            sendJson(exchange, 200, sb.toString());

        } catch (Exception e) {
            sendJson(exchange, 400,
                "{\"success\":false,\"message\":\"Could not load reviews.\"}");
        }
    }
}

    static class ProductsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 200, "{}");
                return;
            }
            String query = exchange.getRequestURI().getQuery();
            String search = null;
            if (query != null) {
                for (String pair : query.split("&")) {
                    String[] kv = pair.split("=", 2);
                    if (kv.length == 2 && kv[0].equals("search")) {
                        search = java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                    }
                }
            }

            List<Product> products;
            if (search != null && !search.trim().isEmpty()) {
                products = productDAO.searchProducts(search.trim());
            } else {
                products = productDAO.getAllProducts();
            }
            sendJson(exchange, 200, productsToJson(products));
        }
    }

    // =================================================================
    //  SINGLE PRODUCT handler  ->  GET /api/product?id=123
    // =================================================================
    static class ProductHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 200, "{}");
                return;
            }
            int id = getIntParam(exchange, "id");
            if (id <= 0) {
                sendJson(exchange, 400, "{\"error\":\"Missing product id\"}");
                return;
            }
            Product p = productDAO.getProductById(id);
            if (p != null) {
                sendJson(exchange, 200, productToJson(p));
            } else {
                sendJson(exchange, 404, "{\"error\":\"Product not found\"}");
            }
        }
    }

    // =================================================================
    //  FARMER PRODUCTS handler
    //    GET  /api/farmer/products?farmerId=1   -> list products
    //    POST /api/farmer/products              -> add product
    //    PUT  /api/farmer/products              -> update product
    //    DELETE /api/farmer/products?id=1&farmerId=1 -> delete product
    // =================================================================
    static class FarmerProductsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 200, "{}");
                return;
            }
            String method = exchange.getRequestMethod();

            if ("GET".equalsIgnoreCase(method)) {
                int farmerId = getIntParam(exchange, "farmerId");
                if (farmerId <= 0) {
                    sendJson(exchange, 400, "{\"error\":\"Missing farmerId\"}");
                    return;
                }
                List<Product> products = productDAO.getProductsByFarmer(farmerId);
                sendJson(exchange, 200, productsToJson(products));
                return;
            }

            if ("POST".equalsIgnoreCase(method)) {
                // Add a product
                Map<String, String> data = parseSimpleJson(readBody(exchange));
                try {
                    Product p = new Product();
                    p.setFarmerId(Integer.parseInt(data.get("farmerId")));
                    p.setProductName(data.get("productName"));
                    p.setCategory(data.get("category"));
                    p.setPrice(Double.parseDouble(data.get("price")));
                    p.setQuantity(Integer.parseInt(data.get("quantity")));
                    p.setDescription(data.get("description"));
                    p.setImageUrl(data.get("imageUrl"));

                    // ---- VALIDATION ----
                    if (p.getProductName() == null || p.getProductName().trim().isEmpty()) {
                        sendJson(exchange, 400, "{\"success\":false,\"message\":\"Product name is required.\"}");
                        return;
                    }
                    if (p.getPrice() <= 0) {
                        sendJson(exchange, 400, "{\"success\":false,\"message\":\"Price must be greater than zero.\"}");
                        return;
                    }
                    if (p.getQuantity() <= 0) {
                        sendJson(exchange, 400, "{\"success\":false,\"message\":\"Quantity must be greater than zero.\"}");
                        return;
                    }

                    boolean ok = productDAO.addProduct(p);
                    sendJson(exchange, ok ? 200 : 400,
                            ok ? "{\"success\":true,\"message\":\"Product added successfully.\"}"
                               : "{\"success\":false,\"message\":\"Could not add product.\"}");
                } catch (NumberFormatException e) {
                    sendJson(exchange, 400, "{\"success\":false,\"message\":\"Price and quantity must be valid numbers.\"}");
                }
                return;
            }

            if ("PUT".equalsIgnoreCase(method)) {
                // Update a product
                Map<String, String> data = parseSimpleJson(readBody(exchange));
                try {
                    Product p = new Product();
                    p.setProductId(Integer.parseInt(data.get("productId")));
                    p.setFarmerId(Integer.parseInt(data.get("farmerId")));
                    p.setProductName(data.get("productName"));
                    p.setCategory(data.get("category"));
                    p.setPrice(Double.parseDouble(data.get("price")));
                    p.setQuantity(Integer.parseInt(data.get("quantity")));
                    p.setDescription(data.get("description"));
                    p.setImageUrl(data.get("imageUrl"));

                    if (p.getProductName() == null || p.getProductName().trim().isEmpty()) {
                        sendJson(exchange, 400, "{\"success\":false,\"message\":\"Product name is required.\"}");
                        return;
                    }
                    if (p.getPrice() <= 0) {
                        sendJson(exchange, 400, "{\"success\":false,\"message\":\"Price must be greater than zero.\"}");
                        return;
                    }
                    if (p.getQuantity() <= 0) {
                        sendJson(exchange, 400, "{\"success\":false,\"message\":\"Quantity must be greater than zero.\"}");
                        return;
                    }

                    boolean ok = productDAO.updateProduct(p);
                    sendJson(exchange, ok ? 200 : 400,
                            ok ? "{\"success\":true,\"message\":\"Product updated successfully.\"}"
                               : "{\"success\":false,\"message\":\"Could not update product.\"}");
                } catch (NumberFormatException e) {
                    sendJson(exchange, 400, "{\"success\":false,\"message\":\"Price and quantity must be valid numbers.\"}");
                }
                return;
            }

            if ("DELETE".equalsIgnoreCase(method)) {
                int productId = getIntParam(exchange, "id");
                int farmerId = getIntParam(exchange, "farmerId");
                if (productId <= 0 || farmerId <= 0) {
                    sendJson(exchange, 400, "{\"success\":false,\"message\":\"Missing id or farmerId.\"}");
                    return;
                }
                boolean ok = productDAO.deleteProduct(productId, farmerId);
                sendJson(exchange, ok ? 200 : 400,
                        ok ? "{\"success\":true,\"message\":\"Product deleted successfully.\"}"
                           : "{\"success\":false,\"message\":\"Could not delete product.\"}");
                return;
            }

            sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
        }
    }

    // =================================================================
    //  ORDER handler  ->  POST /api/order
    //  Body JSON: { "customerId":1, "items":[ {"productId":1,"quantity":2,"price":40}, ... ] }
    // =================================================================
    static class OrderHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 200, "{}");
            return;
        }

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        String body = readBody(exchange);
        Map<String, String> data = parseSimpleJson(body);

        try {
            int customerId = Integer.parseInt(data.get("customerId"));

            String customerName = data.get("customerName");
            String paymentMethod = data.get("paymentMethod");
            String customerPhone = data.get("customerPhone");
            String deliveryAddress = data.get("deliveryAddress");
            String deliveryCity = data.get("deliveryCity");
            String deliveryPincode = data.get("deliveryPincode");

            if (customerName == null || customerName.trim().isEmpty()
                    || customerPhone == null || customerPhone.trim().isEmpty()
                    || deliveryAddress == null || deliveryAddress.trim().isEmpty()
                    || deliveryCity == null || deliveryCity.trim().isEmpty()
                    || deliveryPincode == null || deliveryPincode.trim().isEmpty()) {

                sendJson(exchange, 400,
                    "{\"success\":false,\"message\":\"Please fill all delivery details.\"}");
                return;
            }

            List<Cart.CartItem> items = parseItems(data.get("items"));

            if (items.isEmpty()) {
                sendJson(exchange, 400,
                    "{\"success\":false,\"message\":\"Cart is empty. Add products before placing an order.\"}");
                return;
            }

            double total = 0;

            for (Cart.CartItem item : items) {
                total += item.getPrice() * item.getQuantity();
            }

            
                int orderId = orderDAO.createOrder(
    customerId,
    total,
    items,
    customerName,
    customerPhone,
    deliveryAddress,
    deliveryCity,
    deliveryPincode,
    paymentMethod
);
            if (orderId > 0) {
                sendJson(exchange, 200,
                    "{\"success\":true,\"orderId\":" + orderId +
                    ",\"message\":\"Order placed successfully!\"}");
            } else {
                sendJson(exchange, 500,
                    "{\"success\":false,\"message\":\"Could not place order. Please try again.\"}");
            }

        } catch (Exception e) {

            sendJson(exchange, 400,
                "{\"success\":false,\"message\":\"Invalid order data: "
                + e.getMessage() + "\"}");
        }
    }
}

    // =================================================================
    //  CUSTOMER ORDERS handler  ->  GET /api/orders/customer?customerId=1
    // =================================================================
    static class CustomerOrdersHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 200, "{}");
                return;
            }
            int customerId = getIntParam(exchange, "customerId");
            if (customerId <= 0) {
                sendJson(exchange, 400, "{\"error\":\"Missing customerId\"}");
                return;
            }
            List<Order> orders = orderDAO.getCustomerOrders(customerId);
            sendJson(exchange, 200, ordersToJson(orders));
        }
    }
    // =================================================================
// AI DESCRIPTION handler
// POST /api/ai-description
// =================================================================
static class AIDescriptionHandler implements HttpHandler {

    public void handle(HttpExchange exchange) throws IOException {

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 200, "{}");
            return;
        }

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405,
                    "{\"error\":\"Method not allowed\"}");
            return;
        }

        try {

            String body = readBody(exchange);

            Map<String, String> data =
                    parseSimpleJson(body);

            String productName = data.get("productName");
            String category = data.get("category");

            if (productName == null ||
                productName.trim().isEmpty()) {

                sendJson(exchange, 400,
                        "{\"success\":false,\"message\":\"Product name is required.\"}");
                return;
            }

            if (category == null ||
                category.trim().isEmpty()) {

                category = "Agricultural Product";
            }

            String description =
                    GeminiAI.generateDescription(
                            productName.trim(),
                            category.trim()
                    );

            sendJson(exchange, 200,
                    "{\"success\":true,\"description\":"
                    + jsonStr(description)
                    + "}");

        } catch (Exception e) {

            e.printStackTrace();

            sendJson(exchange, 500,
                    "{\"success\":false,\"message\":\"AI description generation failed.\"}");
        }
    }
}
// =================================================================
// AI IMAGE handler
// POST /api/ai-image
// =================================================================
static class AIImageHandler implements HttpHandler {

    public void handle(HttpExchange exchange) throws IOException {

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 200, "{}");
            return;
        }

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405,
                    "{\"error\":\"Method not allowed\"}");
            return;
        }

        try {

            String body = readBody(exchange);

            Map<String, String> data =
                    parseSimpleJson(body);

            String productName = data.get("productName");
            String category = data.get("category");

            if (productName == null ||
                productName.trim().isEmpty()) {

                sendJson(exchange, 400,
                        "{\"success\":false,\"message\":\"Product name is required.\"}");
                return;
            }

            if (category == null ||
                category.trim().isEmpty()) {

                category = "Agricultural Product";
            }

            String imageUrl = "";

            if (imageUrl == null) {

                sendJson(exchange, 500,
                        "{\"success\":false,\"message\":\"Gemini image generation failed.\"}");
                return;
            }

            sendJson(exchange, 200,
                    "{\"success\":true,\"imageUrl\":"
                    + jsonStr(imageUrl)
                    + "}");

        } catch (Exception e) {

            e.printStackTrace();

            sendJson(exchange, 500,
                    "{\"success\":false,\"message\":\"AI image generation failed.\"}");
        }
    }
}
    // =================================================================
    //  FARMER ORDERS handler  ->  GET /api/orders/farmer?farmerId=1
    // =================================================================
    static class FarmerOrdersHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 200, "{}");
                return;
            }
            int farmerId = getIntParam(exchange, "farmerId");
            if (farmerId <= 0) {
                sendJson(exchange, 400, "{\"error\":\"Missing farmerId\"}");
                return;
            }
            List<Order> orders = orderDAO.getFarmerOrders(farmerId);
            sendJson(exchange, 200, ordersToJson(orders));
        }
    }

    // =================================================================
    //  STATIC FILE HANDLER
    //  Serves HTML/CSS/JS files from the frontend/ folder so the browser
    //  can open the site at http://localhost:8080
    // =================================================================
    static class StaticFileHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";

            // Prevent path traversal attacks.
            if (path.contains("..")) {
                sendJson(exchange, 403, "{\"error\":\"Forbidden\"}");
                return;
            }

            File file = new File(FRONTEND_DIR, path);
            if (!file.exists() || file.isDirectory()) {
                String notFound = "<h1>404 - Page not found</h1><p><a href=\"/\">Go home</a></p>";
                byte[] bytes = notFound.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(404, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
                return;
            }

            String contentType = guessContentType(path);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            byte[] fileBytes = readFile(file);
            exchange.sendResponseHeaders(200, fileBytes.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(fileBytes); }
        }
    }

    // =================================================================
    //  Helper: guess the Content-Type from the file extension
    // =================================================================
    private static String guessContentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=UTF-8";
        if (path.endsWith(".css"))   return "text/css; charset=UTF-8";
        if (path.endsWith(".js"))    return "application/javascript; charset=UTF-8";
        if (path.endsWith(".png"))   return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".svg"))   return "image/svg+xml";
        return "application/octet-stream";
    }

    private static byte[] readFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return fis.readAllBytes();
        }
    }

    // =================================================================
    //  Helper: get an integer query parameter (?name=value)
    // =================================================================
    private static int getIntParam(HttpExchange exchange, String name) {
        String query = exchange.getRequestURI().getQuery();
        if (query == null) return -1;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && kv[0].equals(name)) {
                try { return Integer.parseInt(java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8)); }
                catch (NumberFormatException e) { return -1; }
            }
        }
        return -1;
    }

    // =================================================================
    //  Helper: a very small JSON parser for flat objects like
    //  { "name":"Ramesh", "email":"r@x.com", "price":"40.0" }
    //  This is intentionally simple - it is enough for this project.
    // =================================================================
    private static Map<String, String> parseSimpleJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.trim().isEmpty()) return map;
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}")) json = json.substring(0, json.length() - 1);

        int i = 0;
        while (i < json.length()) {
            // find the key
            int keyStart = json.indexOf('"', i);
            if (keyStart < 0) break;
            int keyEnd = json.indexOf('"', keyStart + 1);
            String key = json.substring(keyStart + 1, keyEnd);
            int colon = json.indexOf(':', keyEnd + 1);
            if (colon < 0) break;

            // value can be a string or a number/boolean
            int v = colon + 1;
            while (v < json.length() && (json.charAt(v) == ' ' || json.charAt(v) == '\t')) v++;
            String value;
            if (v < json.length() && json.charAt(v) == '"') {
                // String value - find the closing quote.
                int valueEnd = json.indexOf('"', v + 1);
                value = json.substring(v + 1, valueEnd);
                i = valueEnd + 1;
            } else if (v < json.length() && json.charAt(v) == '[') {
                // Array value - find the matching ] by counting brackets.
                int depth = 0;
                int end = v;
                for (int j = v; j < json.length(); j++) {
                    if (json.charAt(j) == '[') depth++;
                    else if (json.charAt(j) == ']') {
                        depth--;
                        if (depth == 0) { end = j; break; }
                    }
                }
                value = json.substring(v, end + 1);
                i = end + 1;
            } else {
                // Number or boolean value - read until the next comma.
                int comma = json.indexOf(',', v);
                if (comma < 0) comma = json.length();
                value = json.substring(v, comma).trim();
                i = comma;
            }
            map.put(key, value);
            // skip the comma
            int nextComma = json.indexOf(',', i);
            if (nextComma < 0) break;
            i = nextComma + 1;
        }
        return map;
    }

    // =================================================================
    //  Helper: parse the "items" array from the order JSON.
    //  Format: [ {"productId":1,"quantity":2,"price":40}, ... ]
    // =================================================================
    private static List<Cart.CartItem> parseItems(String arrayJson) {
        List<Cart.CartItem> items = new ArrayList<>();
        if (arrayJson == null || arrayJson.trim().isEmpty() || arrayJson.equals("null")) return items;
        String s = arrayJson.trim();
        if (s.startsWith("[")) s = s.substring(1);
        if (s.endsWith("]")) s = s.substring(0, s.length() - 1);

        // Split on "}," to get each object
        String[] parts = s.split("\\},");
        for (String part : parts) {
            if (!part.trim().endsWith("}")) part = part.trim() + "}";
            Map<String, String> m = parseSimpleJson(part.trim());
            try {
                int productId = Integer.parseInt(m.get("productId"));
                int quantity = Integer.parseInt(m.get("quantity"));
                double price = Double.parseDouble(m.get("price"));
                String productName = m.get("productName");
                if (productName == null) productName = "";
                items.add(new Cart.CartItem(productId, productName, price, quantity));
            } catch (NumberFormatException e) {
                // skip bad item
            }
        }
        return items;
    }

    // =================================================================
    //  Helper: convert a list of products to a JSON array string
    // =================================================================
    private static String productsToJson(List<Product> products) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < products.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(productToJson(products.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    private static String productToJson(Product p) {
    return "{" +
        "\"productId\":" + p.getProductId() +
        ",\"farmerId\":" + p.getFarmerId() +
        ",\"farmerName\":" + jsonStr(p.getFarmerName()) +
        ",\"productName\":" + jsonStr(p.getProductName()) +
        ",\"category\":" + jsonStr(p.getCategory()) +
        ",\"price\":" + p.getPrice() +
        ",\"quantity\":" + p.getQuantity() +
        ",\"description\":" + jsonStr(p.getDescription()) +
        ",\"imageUrl\":" + jsonStr(p.getImageUrl()) +
        "}";
}

    private static String ordersToJson(List<Order> orders) {
    StringBuilder sb = new StringBuilder("[");

    for (int i = 0; i < orders.size(); i++) {

        Order o = orders.get(i);

        if (i > 0) {
            sb.append(",");
        }

        sb.append("{")
  .append("\"orderId\":").append(o.getOrderId())
  .append(",\"customerId\":").append(o.getCustomerId())
  .append(",\"customerName\":").append(jsonStr(o.getCustomerName()))
  .append(",\"customerPhone\":").append(jsonStr(o.getCustomerPhone()))
  .append(",\"deliveryAddress\":").append(jsonStr(o.getDeliveryAddress()))
  .append(",\"deliveryCity\":").append(jsonStr(o.getDeliveryCity()))
  .append(",\"deliveryPincode\":").append(jsonStr(o.getDeliveryPincode()))
  .append(",\"orderDate\":").append(jsonStr(o.getOrderDate()))
  .append(",\"totalAmount\":").append(o.getTotalAmount())
  .append(",\"status\":").append(jsonStr(o.getStatus()));

        // Add ordered products
        sb.append(",\"items\":[");

        List<OrderItem> items = o.getItems();

        for (int j = 0; j < items.size(); j++) {

            OrderItem item = items.get(j);

            if (j > 0) {
                sb.append(",");
            }

            sb.append("{")
              .append("\"productId\":").append(item.getProductId())
              .append(",\"productName\":").append(jsonStr(item.getProductName()))
              .append(",\"quantity\":").append(item.getQuantity())
              .append(",\"price\":").append(item.getPrice())
              .append("}");
        }

        sb.append("]");

        sb.append("}");
    }

        sb.append("]");

    return sb.toString();
}

}
