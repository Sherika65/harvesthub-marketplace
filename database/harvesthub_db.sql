-- =====================================================================
-- HarvestHub - Farmer to Customer Marketplace
-- MySQL Database Script
-- Database: harvesthub_db
-- =====================================================================

-- STEP 1: Create the database
CREATE DATABASE IF NOT EXISTS harvesthub_db;

-- STEP 2: Use the database
USE harvesthub_db;

-- =====================================================================
-- STEP 3: Create Tables
-- =====================================================================

-- Drop tables if they already exist (so the script can be re-run)
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS users;

-- ---------------------------------------------------------------------
-- Table: users
-- Stores both farmers and customers. The 'role' column tells them apart.
-- ---------------------------------------------------------------------
CREATE TABLE users (
    user_id   INT AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(100) NOT NULL,
    email     VARCHAR(100) NOT NULL UNIQUE,
    password  VARCHAR(100) NOT NULL,
    role      VARCHAR(20)  NOT NULL
);

-- ---------------------------------------------------------------------
-- Table: products
-- Products added by farmers. farmer_id links back to users.user_id.
-- ---------------------------------------------------------------------
CREATE TABLE products (
    product_id   INT AUTO_INCREMENT PRIMARY KEY,
    farmer_id    INT NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    category     VARCHAR(50)  NOT NULL,
    price        DECIMAL(10,2) NOT NULL,
    quantity     INT NOT NULL,
    description  TEXT,
    FOREIGN KEY (farmer_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- Table: orders
-- An order placed by a customer. customer_id links to users.user_id.
-- ---------------------------------------------------------------------
CREATE TABLE orders (
    order_id     INT AUTO_INCREMENT PRIMARY KEY,
    customer_id  INT NOT NULL,
    order_date   DATE NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'Placed',
    FOREIGN KEY (customer_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- Table: order_items
-- Individual line items inside an order.
-- ---------------------------------------------------------------------
CREATE TABLE order_items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id      INT NOT NULL,
    product_id    INT NOT NULL,
    quantity      INT NOT NULL,
    price         DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id)   REFERENCES orders(order_id)   ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);

-- =====================================================================
-- STEP 4: Insert Sample Data
-- =====================================================================

-- Sample users: 2 farmers and 2 customers
-- NOTE: Passwords are stored as plain text for this educational project.
-- In a real project these would be hashed (see README - Future Enhancements).
INSERT INTO users (name, email, password, role) VALUES
('Ramesh Patel',  'ramesh@harvesthub.com', 'farmer123', 'farmer'),
('Sunita Devi',   'sunita@harvesthub.com', 'farmer123', 'farmer'),
('Amit Kumar',    'amit@harvesthub.com',   'customer123', 'customer'),
('Priya Sharma',  'priya@harvesthub.com',  'customer123', 'customer');

-- Sample products (10 products across two farmers)
INSERT INTO products (farmer_id, product_name, category, price, quantity, description) VALUES
(1, 'Tomato',  'Vegetable', 40.00, 100, 'Fresh red tomatoes grown organically without pesticides.'),
(1, 'Potato',  'Vegetable', 30.00, 200, 'Farm-picked potatoes, great for everyday cooking.'),
(1, 'Rice',    'Grain',     60.00, 150, 'Premium quality basmati rice directly from the farm.'),
(1, 'Mango',   'Fruit',     120.00, 80, 'Sweet and juicy Alphonso mangoes, seasonal special.'),
(1, 'Carrot',  'Vegetable', 35.00, 120, 'Crunchy farm-fresh carrots rich in vitamins.'),
(2, 'Onion',   'Vegetable', 25.00, 300, 'Fresh onions harvested this week.'),
(2, 'Banana',  'Fruit',     50.00, 100, 'Ripe bananas, naturally grown without chemicals.'),
(2, 'Spinach', 'Vegetable', 20.00, 60,  'Green leafy spinach, rich in iron and nutrients.'),
(2, 'Wheat',   'Grain',     45.00, 250, 'Whole wheat grains, freshly milled quality.'),
(2, 'Chilli',  'Vegetable', 80.00, 70,  'Spicy green chillies, farm fresh and aromatic.');

-- Sample order placed by customer Amit Kumar (user_id = 3)
INSERT INTO orders (customer_id, order_date, total_amount, status) VALUES
(3, '2026-09-20', 165.00, 'Placed');

-- Order items for the above order (order_id = 1)
-- 2 kg Tomato @ 40 = 80, 2 kg Potato @ 30 = 60, 1 kg Onion @ 25 = 25, Total = 165
INSERT INTO order_items (order_id, product_id, quantity, price) VALUES
(1, 1, 2, 40.00),  -- 2 x Tomato
(1, 2, 2, 30.00),  -- 2 x Potato
(1, 6, 1, 25.00);  -- 1 x Onion

-- =====================================================================
-- STEP 5: SELECT Examples (used by the Java backend)
-- =====================================================================

-- Select all products (for the customer marketplace)
-- SELECT * FROM products;

-- Select products added by a particular farmer (farmer_id = 1)
-- SELECT * FROM products WHERE farmer_id = 1;

-- Search products by name or category
-- SELECT * FROM products WHERE product_name LIKE '%to%';
-- SELECT * FROM products WHERE category = 'Vegetable';

-- Select orders placed by a customer (customer_id = 3)
-- SELECT * FROM orders WHERE customer_id = 3;

-- Select orders received by a farmer (join products + orders)
-- SELECT o.order_id, o.order_date, p.product_name, oi.quantity, oi.price
-- FROM orders o
-- JOIN order_items oi ON o.order_id = oi.order_id
-- JOIN products p ON oi.product_id = p.product_id
-- WHERE p.farmer_id = 1;

-- =====================================================================
-- STEP 6: UPDATE Examples
-- =====================================================================

-- Update a product price
-- UPDATE products SET price = 45.00 WHERE product_id = 1;

-- Update product quantity
-- UPDATE products SET quantity = 150 WHERE product_id = 3;

-- Update order status
-- UPDATE orders SET status = 'Shipped' WHERE order_id = 1;

-- =====================================================================
-- STEP 7: DELETE Examples
-- =====================================================================

-- Delete a product
-- DELETE FROM products WHERE product_id = 10;

-- Delete an order (this also deletes its order_items because of ON DELETE CASCADE)
-- DELETE FROM orders WHERE order_id = 1;

-- =====================================================================
-- End of script
-- =====================================================================
