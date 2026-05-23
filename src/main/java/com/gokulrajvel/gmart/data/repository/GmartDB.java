package com.gokulrajvel.gmart.data.repository;

import com.gokulrajvel.gmart.data.Role;
import com.gokulrajvel.gmart.data.dto.*;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class GmartDB {
    private static GmartDB instance;
    private final DataSource dataSource;

    public GmartDB(DataSource dataSource) {
        this.dataSource = dataSource;
        instance = this;
        initializeDatabase();
    }

    public static GmartDB getInstance() {
        return instance;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private void initializeDatabase() {
        String[] queries = {
            "CREATE TABLE IF NOT EXISTS users (id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(50) NOT NULL UNIQUE, password VARCHAR(255) NOT NULL, role ENUM('ADMIN', 'BILLING_STAFF', 'WAREHOUSE', 'PURCHASING_MANAGER') NOT NULL, address VARCHAR(255), phone VARCHAR(50), aadhar_no VARCHAR(50))",
            "INSERT IGNORE INTO users (username, password, role) VALUES ('admin', 'admin123', 'ADMIN')",
            "CREATE TABLE IF NOT EXISTS categories (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) NOT NULL UNIQUE)",
            "CREATE TABLE IF NOT EXISTS suppliers (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) NOT NULL, contact_info TEXT)",
            "CREATE TABLE IF NOT EXISTS products (id INT AUTO_INCREMENT PRIMARY KEY, sku_code VARCHAR(20) NOT NULL UNIQUE, name VARCHAR(100) NOT NULL, category_id INT, supplier_id INT, price DECIMAL(10, 2) NOT NULL, stock_quantity INT DEFAULT 0, FOREIGN KEY (category_id) REFERENCES categories(id), FOREIGN KEY (supplier_id) REFERENCES suppliers(id))",
            "CREATE TABLE IF NOT EXISTS transactions (id INT AUTO_INCREMENT PRIMARY KEY, product_id INT, user_id INT, transaction_type ENUM('INWARD', 'OUTWARD') NOT NULL, quantity INT NOT NULL, transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (product_id) REFERENCES products(id), FOREIGN KEY (user_id) REFERENCES users(id))",
            "CREATE TABLE IF NOT EXISTS bills (id INT AUTO_INCREMENT PRIMARY KEY, user_id INT, total_amount DECIMAL(10, 2) NOT NULL, tax_amount DECIMAL(10, 2) NOT NULL, payment_method ENUM('CASH', 'CARD', 'UPI') NOT NULL, bill_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (user_id) REFERENCES users(id))",
            "CREATE TABLE IF NOT EXISTS bill_items (id INT AUTO_INCREMENT PRIMARY KEY, bill_id INT, product_id INT, quantity INT NOT NULL, price_at_sale DECIMAL(10, 2) NOT NULL, FOREIGN KEY (bill_id) REFERENCES bills(id), FOREIGN KEY (product_id) REFERENCES products(id))"
        };

        try (Connection conn = getConnection()) {
            if (conn == null) {
                System.err.println("Could not establish database connection.");
                return;
            }
            try (Statement stmt = conn.createStatement()) {
                for (String query : queries) {
                    stmt.execute(query);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    public void addUser(User user) {
        String query = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = getConnection()) {
            if (conn == null) return;
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, user.getUsername());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getRole().name());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error adding user: " + e.getMessage());
        }
    }

    public List<User> getUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT id, username, password, role FROM users";
        try (Connection conn = getConnection()) {
            if (conn == null) return users;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    users.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        Role.valueOf(rs.getString("role"))
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching users: " + e.getMessage());
        }
        return users;
    }

    public User authenticate(String username, String password) {
        String query = "SELECT id, role FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection()) {
            if (conn == null) return null;
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return new User(rs.getInt("id"), username, password, Role.valueOf(rs.getString("role")));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
        }
        return null;
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT id, sku_code, name, category_id, supplier_id, price, stock_quantity FROM products";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int catId = rs.getInt("category_id");
                Integer categoryId = rs.wasNull() ? null : catId;
                int suppId = rs.getInt("supplier_id");
                Integer supplierId = rs.wasNull() ? null : suppId;
                products.add(new Product(
                    rs.getInt("id"),
                    rs.getString("sku_code"),
                    rs.getString("name"),
                    categoryId,
                    supplierId,
                    rs.getDouble("price"),
                    rs.getInt("stock_quantity")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all products: " + e.getMessage());
        }
        return products;
    }

    public Product getProductBySku(String sku) {
        String query = "SELECT id, sku_code, name, category_id, supplier_id, price, stock_quantity FROM products WHERE sku_code = ?";
        try (Connection conn = getConnection()) {
            if (conn == null) return null;
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, sku);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        int catId = rs.getInt("category_id");
                        Integer categoryId = rs.wasNull() ? null : catId;
                        int suppId = rs.getInt("supplier_id");
                        Integer supplierId = rs.wasNull() ? null : suppId;
                        return new Product(
                            rs.getInt("id"),
                            rs.getString("sku_code"),
                            rs.getString("name"),
                            categoryId,
                            supplierId,
                            rs.getDouble("price"),
                            rs.getInt("stock_quantity")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching product by SKU: " + e.getMessage());
        }
        return null;
    }

    public void updateProductStock(int productId, int quantityChange) {
        String query = "UPDATE products SET stock_quantity = stock_quantity + ? WHERE id = ?";
        try (Connection conn = getConnection()) {
            if (conn == null) return;
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, quantityChange);
                pstmt.setInt(2, productId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error updating product stock: " + e.getMessage());
        }
    }

    public void recordTransaction(InventoryTransaction transaction) {
        String query = "INSERT INTO transactions (product_id, user_id, transaction_type, quantity) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection()) {
            if (conn == null) return;
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, transaction.getProductId());
                pstmt.setInt(2, transaction.getUserId());
                pstmt.setString(3, transaction.getTransactionType());
                pstmt.setInt(4, transaction.getQuantity());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error recording transaction: " + e.getMessage());
        }
    }

    public void createBill(Bill bill, List<BillItem> items) {
        String billQuery = "INSERT INTO bills (user_id, total_amount, tax_amount, payment_method) VALUES (?, ?, ?, ?)";
        String itemQuery = "INSERT INTO bill_items (bill_id, product_id, product_name, quantity, price_at_sale) VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = getConnection();
            if (conn == null) return;
            conn.setAutoCommit(false);

            int billId = -1;
            try (PreparedStatement pstmt = conn.prepareStatement(billQuery, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, bill.getUserId());
                pstmt.setDouble(2, bill.getTotalAmount());
                pstmt.setDouble(3, bill.getTaxAmount());
                pstmt.setString(4, bill.getPaymentMethod());
                pstmt.executeUpdate();
                
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        billId = generatedKeys.getInt(1);
                    }
                }
            }

            if (billId != -1) {
                try (PreparedStatement pstmt = conn.prepareStatement(itemQuery)) {
                    for (BillItem item : items) {
                        pstmt.setInt(1, billId);
                        pstmt.setInt(2, item.getProductId());
                        pstmt.setString(3, item.getProductName());
                        pstmt.setInt(4, item.getQuantity());
                        pstmt.setDouble(5, item.getPriceAtSale());
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { e.addSuppressed(ex); }
            }
            System.err.println("Error creating bill: " + e.getMessage());
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) { /* ignore */ }
            }
        }
    }

    public List<InventoryTransaction> getAllTransactions() {
        List<InventoryTransaction> transactions = new ArrayList<>();
        String query = "SELECT id, product_id, user_id, transaction_type, quantity, transaction_date FROM transactions ORDER BY transaction_date DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                transactions.add(new InventoryTransaction(
                    rs.getInt("id"),
                    rs.getInt("product_id"),
                    rs.getInt("user_id"),
                    rs.getString("transaction_type"),
                    rs.getInt("quantity"),
                    rs.getTimestamp("transaction_date")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching transactions: " + e.getMessage());
        }
        return transactions;
    }

    public List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        String query = "SELECT name FROM categories";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                categories.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching categories: " + e.getMessage());
        }
        return categories;
    }

    public List<Supplier> getAllSuppliers() {
        List<Supplier> suppliers = new ArrayList<>();
        String query = "SELECT id, name, contact_info FROM suppliers";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                suppliers.add(new Supplier(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("contact_info")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching suppliers: " + e.getMessage());
        }
        return suppliers;
    }

    public void addSupplier(Supplier supplier) {
        String query = "INSERT INTO suppliers (name, contact_info) VALUES (?, ?)";
        try (Connection conn = getConnection()) {
            if (conn == null) return;
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, supplier.getName());
                pstmt.setString(2, supplier.getContactInfo());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error adding supplier: " + e.getMessage());
        }
    }

    public void addCategory(String name) {
        String query = "INSERT INTO categories (name) VALUES (?)";
        try (Connection conn = getConnection()) {
            if (conn == null) return;
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, name);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error adding category: " + e.getMessage());
        }
    }

    public void addProduct(Product product) {
        String query = "INSERT INTO products (sku_code, name, category_id, supplier_id, price, stock_quantity) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection()) {
            if (conn == null) return;
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, product.getSkuCode());
                pstmt.setString(2, product.getName());
                if (product.getCategoryId() != null) {
                    pstmt.setInt(3, product.getCategoryId());
                } else {
                    pstmt.setNull(3, Types.INTEGER);
                }
                if (product.getSupplierId() != null) {
                    pstmt.setInt(4, product.getSupplierId());
                } else {
                    pstmt.setNull(4, Types.INTEGER);
                }
                pstmt.setDouble(5, product.getPrice());
                pstmt.setInt(6, product.getStockQuantity());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error adding product: " + e.getMessage());
        }
    }
}
