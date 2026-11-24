# HarvestHub - Farmer to Customer Marketplace

A simple online marketplace that connects farmers directly with customers, built using **Java + JDBC + MySQL** for the backend and **HTML + CSS + JavaScript** for the frontend. This is a **Java Explore + MySQL college project**.

---

## 1. Project Title

**HarvestHub - Farmer to Customer Marketplace**

---

## 2. Abstract

HarvestHub is a web-based application that connects farmers directly with customers. Farmers can register, add their agricultural products, manage them, and view orders received from customers. Customers can browse products, search for them, add them to a cart, and place orders. The main objective is to reduce unnecessary middlemen and allow customers to buy fresh agricultural products directly from farmers at fair prices.

---

## 3. Objectives

- Connect farmers and customers directly without middlemen.
- Allow farmers to manage (add, update, delete) their own products.
- Allow customers to browse, search, and order products.
- Demonstrate Java OOP concepts (classes, objects, encapsulation, inheritance).
- Demonstrate JDBC database connectivity with MySQL.
- Demonstrate exception handling and event handling.
- Keep the project simple and beginner-friendly for a college review.

---

## 4. Introduction

Agriculture is the backbone of many economies, yet farmers often do not get a fair price for their produce because of multiple middlemen. HarvestHub is a simple web application that lets farmers sell their products directly to customers online. Customers can browse fresh products, add them to a cart, and place orders. The project is built using Java (backend), MySQL (database), and HTML/CSS/JavaScript (frontend).

---

## 5. Problem Statement

In the traditional supply chain, farmers sell their products to agents and wholesalers at low prices. Customers then buy these products at much higher prices because of the middleman markup. There is no direct platform for farmers and customers to interact. HarvestHub solves this by providing a simple online marketplace.

---

## 6. Existing System

- Farmers sell products to local agents or wholesale markets.
- Multiple middlemen add markup at each step.
- Customers pay higher prices than what farmers receive.
- No transparency in pricing.
- No direct communication between farmer and customer.

---

## 7. Proposed System

- An online marketplace where farmers can directly list their products.
- Customers can browse, search, and order products online.
- Farmers get fair prices; customers get fresh products at reasonable rates.
- Simple registration and login for both farmers and customers.
- Role-based access (farmer sees product management, customer sees shopping).

---

## 8. Features

### Farmer Features
1. Register and login
2. Farmer dashboard with product and order counts
3. Add agricultural products (name, category, price, quantity, description)
4. View own products
5. Update products
6. Delete products
7. View orders received from customers

### Customer Features
1. Register and login
2. Browse all products on the marketplace
3. Search products by name or category
4. View product details
5. Add products to cart
6. Increase/decrease quantity in cart
7. Remove items from cart
8. Place orders
9. View order history

---

## 9. Technologies Used

| Layer        | Technology                          |
|-------------|-------------------------------------|
| Frontend    | HTML5, CSS3, Vanilla JavaScript    |
| Backend     | Java (JDK 8+), JDBC                |
| Database    | MySQL 8.0                          |
| API Layer   | Java built-in HTTP server (`com.sun.net.httpserver`) |
| JDBC Driver | MySQL Connector/J 8.x              |

---

## 10. System Requirements

### 11. Software Requirements

- Operating System: Windows 10/11 (or Mac/Linux)
- Java Development Kit (JDK) 8 or higher
- MySQL 8.0 (or XAMPP / MySQL Workbench)
- VS Code (recommended) or any text editor
- A web browser (Chrome, Firefox, Edge)
- MySQL Connector/J (JDBC driver `.jar` file)

### 12. Hardware Requirements

- Processor: Intel Core i3 or equivalent
- RAM: 4 GB minimum (8 GB recommended)
- Storage: 500 MB free space
- Internet connection (only for downloading dependencies)

---

## 13. System Architecture

```
+-------------------+        JSON (HTTP)        +-------------------+        JDBC        +-------------------+
|   FRONTEND         |  <----------------------> |   BACKEND          |  <--------------> |   DATABASE        |
|   HTML/CSS/JS      |    fetch() / API calls    |   Java + HTTP      |   SQL queries     |   MySQL 8.0       |
|   (Browser)        |                          |   Server (port 8080)|                   |   harvesthub_db   |
+-------------------+                          +-------------------+                   +-------------------+
```

- The **frontend** (HTML/CSS/JS) runs in the browser and sends HTTP requests to the Java backend.
- The **backend** (Java) receives the requests, uses JDBC to talk to MySQL, and sends JSON responses back.
- The **database** (MySQL) stores all data (users, products, orders).

---

## 14. Modules

### Farmer Module
- Registration, Login, Dashboard
- Add Product, View Products, Update Product, Delete Product
- View Orders Received

### Customer Module
- Registration, Login, Dashboard (products page)
- Browse Products, Search Products, View Product Details
- Add to Cart, View Cart, Place Order
- View Order History

**Note:** There is no admin module in this project.

---

## 15. Database Design

### Table: users
| Column   | Type         | Description                        |
|----------|--------------|------------------------------------|
| user_id  | INT (PK, AI) | Unique user ID                     |
| name     | VARCHAR(100) | Full name                          |
| email    | VARCHAR(100) | Unique email                      |
| password | VARCHAR(100) | Password (plain text - see note)    |
| role     | VARCHAR(20)  | "farmer" or "customer"             |

### Table: products
| Column       | Type          | Description                        |
|--------------|---------------|------------------------------------|
| product_id   | INT (PK, AI)  | Unique product ID                  |
| farmer_id    | INT (FK)      | References users(user_id)          |
| product_name | VARCHAR(100)  | Name of the product                |
| category     | VARCHAR(50)   | Vegetable / Fruit / Grain          |
| price        | DECIMAL(10,2) | Price per kg                       |
| quantity     | INT           | Available quantity in kg           |
| description  | TEXT          | Product description               |

### Table: orders
| Column       | Type          | Description                        |
|--------------|---------------|------------------------------------|
| order_id     | INT (PK, AI)  | Unique order ID                    |
| customer_id  | INT (FK)      | References users(user_id)          |
| order_date   | DATE          | Date the order was placed          |
| total_amount | DECIMAL(10,2) | Total cost of the order            |
| status       | VARCHAR(20)   | Order status (Placed/Shipped/etc.) |

### Table: order_items
| Column        | Type          | Description                        |
|---------------|---------------|------------------------------------|
| order_item_id | INT (PK, AI)  | Unique item ID                     |
| order_id     | INT (FK)      | References orders(order_id)        |
| product_id   | INT (FK)      | References products(product_id)    |
| quantity      | INT           | Quantity ordered                   |
| price         | DECIMAL(10,2) | Price at the time of order         |

**Foreign Keys:**
- `products.farmer_id` → `users.user_id`
- `orders.customer_id` → `users.user_id`
- `order_items.order_id` → `orders.order_id`
- `order_items.product_id` → `products.product_id`

---

## 16. Java OOP Concepts Used

### Classes and Objects
- `User`, `Farmer`, `Customer`, `Product`, `Order`, `Cart`, `DatabaseConnection` are all **classes** (blueprints).
- When a farmer logs in, we create a `Farmer` **object** that holds their name, email, etc. in memory.

### Encapsulation
- All fields in `User`, `Product`, `Order` etc. are declared `private`.
- They are accessed only through public getters and setters (e.g. `getName()`, `setName()`).
- This protects the data from being changed directly from outside the class.

### Inheritance
- `User` is the parent (super) class.
- `Farmer extends User` and `Customer extends User` — they inherit all fields and methods of `User` and add their own special features (Farmer has product count, Customer has a cart).

### Polymorphism
- In `UserDAO.login()`, we create either a `Farmer` or `Customer` object depending on the role, but return it as a `User` reference. This is polymorphism — one interface, multiple forms.

### Exception Handling
- Every database method uses `try-catch` blocks to handle `SQLException` and `ClassNotFoundException`.
- If the database connection fails, the program prints a helpful message instead of crashing.

---

## 17. JDBC Explanation

**JDBC (Java Database Connectivity)** is Java's built-in API for connecting to databases.

The connection flow in `DatabaseConnection.java`:
1. **Load the driver:** `Class.forName("com.mysql.cj.jdbc.Driver");`
2. **Create a connection:** `DriverManager.getConnection(url, username, password);`
3. **Run SQL:** Use `PreparedStatement` with `?` placeholders for safe queries.
4. **Read results:** Use `ResultSet` to read rows returned by `SELECT`.
5. **Close:** Use try-with-resources so connections close automatically.

**Why PreparedStatement?** It separates SQL from data, preventing SQL injection attacks. For example:
```java
PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE email = ? AND password = ?");
ps.setString(1, email);
ps.setString(2, password);
```

---

## 18. Exception Handling

Exception handling is used in:
- `DatabaseConnection.java` — catches `ClassNotFoundException` (driver missing) and `SQLException` (connection failed).
- `UserDAO.java` — catches `SQLException` during register and login.
- `ProductDAO.java` — catches `SQLException` during all product operations.
- `OrderDAO.java` — catches `SQLException` during order creation, uses rollback on failure.
- `HarvestHub.java` — catches `NumberFormatException` for invalid price/quantity input and `IOException` for server issues.

Custom validation messages are shown to the user (e.g. "Price must be greater than zero").

---

## 19. Inheritance

```
         User (parent class)
        /    \
   Farmer    Customer
```
- `Farmer extends User` — inherits userId, name, email, password, role + adds productCount.
- `Customer extends User` — inherits all + adds a Cart object.

This avoids repeating the same fields in both classes and demonstrates inheritance clearly.

---

## 20. Encapsulation

Example from `User.java`:
```java
private String name;           // hidden from outside

public String getName() {      // controlled access
    return name;
}

public void setName(String name) {
    this.name = name;
}
```
The same pattern is used in `Product`, `Order`, `Cart`, and all model classes.

---

## 21. Event Handling

Event handling happens in the **frontend JavaScript** (`script.js`). The following events are handled:

| Event                        | Where it is used                              |
|------------------------------|-----------------------------------------------|
| Form submit (login)          | `loginForm.addEventListener('submit', ...)`  |
| Form submit (registration)   | `registerForm.addEventListener('submit', ...)`|
| Form submit (add product)    | `productForm.addEventListener('submit', ...)` |
| Form submit (edit product)   | `editForm.addEventListener('submit', ...)`   |
| Button click (search)        | `searchBtn.addEventListener('click', ...)`    |
| Button click (add to cart)   | `onclick="addToCart(...)"`                    |
| Button click (update/delete) | `onclick="editProduct(...)"` / `deleteProduct(...)` |
| Button click (place order)   | `onclick="placeOrder()"`                      |
| Button click (qty +/-)        | `onclick="changeQty(...)"`                    |
| Enter key in search box      | `searchInput.addEventListener('keypress', ...)`|

---

## 22. How to Create the MySQL Database

### Option A: Using MySQL Command Line
1. Open MySQL Command Line (or MySQL Workbench).
2. Run the SQL script:
   ```
   mysql -u root -p < database/harvesthub_db.sql
   ```
   Or open MySQL Workbench, go to File > Open SQL Script, select `database/harvesthub_db.sql`, and click Execute.

### Option B: Using XAMPP / phpMyAdmin
1. Start Apache and MySQL from XAMPP Control Panel.
2. Open `http://localhost/phpmyadmin` in your browser.
3. Click "Import" and choose `database/harvesthub_db.sql`.
4. Click "Go" to run the script.

This creates the `harvesthub_db` database with all tables and sample data.

---

## 23. How to Configure JDBC

### MySQL Database Configuration

The project uses environment variables to connect to MySQL. Database credentials are not stored directly in the source code.

Open `backend/src/DatabaseConnection.java`. The application reads these environment variables:

- `DB_URL` – MySQL database URL
- `DB_USER` – Your MySQL username
- `DB_PASSWORD` – Your MySQL password

**Windows PowerShell setup:**

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/harvesthub_db"
$env:DB_USER="root"
$env:DB_PASSWORD="your_mysql_password"
```

Replace `your_mysql_password` with your own local MySQL password.

Set these variables in the terminal before running the backend.

**Important:** Never upload your database password or other private credentials to GitHub.