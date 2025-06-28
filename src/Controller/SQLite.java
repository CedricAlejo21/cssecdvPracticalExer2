package Controller;

import Model.History;
import Model.Logs;
import Model.Product;
import Model.User;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class SQLite {

    public int DEBUG_MODE = 0;
    String driverURL = "jdbc:sqlite:" + "database.db";

    // Create database if it does not exist
    public void createNewDatabase() {
        try (Connection conn = DriverManager.getConnection(driverURL)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("Database database.db created.");
            }
        } catch (Exception ex) {
            System.out.print(ex);
        }
    }

    // Create history table
    public void createHistoryTable() {
        String sql = "CREATE TABLE IF NOT EXISTS history ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " username TEXT NOT NULL,"
                + " name TEXT NOT NULL,"
                + " stock INTEGER DEFAULT 0,"
                + " timestamp TEXT NOT NULL"
                + ");";
        executeUpdate(sql, "Table history in database.db created.");
    }

    // Create logs table
    public void createLogsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS logs ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " event TEXT NOT NULL,"
                + " username TEXT NOT NULL,"
                + " desc TEXT NOT NULL,"
                + " timestamp TEXT NOT NULL"
                + ");";
        executeUpdate(sql, "Table logs in database.db created.");
    }

    // Create product table
    public void createProductTable() {
        String sql = "CREATE TABLE IF NOT EXISTS product ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " name TEXT NOT NULL UNIQUE,"
                + " stock INTEGER DEFAULT 0,"
                + " price REAL DEFAULT 0.00"
                + ");";
        executeUpdate(sql, "Table product in database.db created.");
    }

    // Create users table
    public void createUserTable() {
        String sql = "CREATE TABLE IF NOT EXISTS users ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " username TEXT NOT NULL UNIQUE,"
                + " password TEXT NOT NULL,"
                + " role INTEGER DEFAULT 2,"
                + " locked INTEGER DEFAULT 0,"
                + " failed_attempts INTEGER DEFAULT 0"
                + ");";
        executeUpdate(sql, "Table users in database.db created.");
    }

    // Drop history table
    public void dropHistoryTable() {
        String sql = "DROP TABLE IF EXISTS history;";
        executeUpdate(sql, "Table history in database.db dropped.");
    }

    // Drop logs table
    public void dropLogsTable() {
        String sql = "DROP TABLE IF EXISTS logs;";
        executeUpdate(sql, "Table logs in database.db dropped.");
    }

    // Drop product table
    public void dropProductTable() {
        String sql = "DROP TABLE IF EXISTS product;";
        executeUpdate(sql, "Table product in database.db dropped.");
    }

    // Drop users table
    public void dropUserTable() {
        String sql = "DROP TABLE IF EXISTS users;";
        executeUpdate(sql, "Table users in database.db dropped.");
    }

    // Add history entry using PreparedStatement
    public void addHistory(String username, String name, int stock, String timestamp) {
        String sql = "INSERT INTO history(username,name,stock,timestamp) VALUES(?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(driverURL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, name);
            pstmt.setInt(3, stock);
            pstmt.setString(4, timestamp);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            logError(ex);
        }
    }

    // Add logs entry using PreparedStatement
    public void addLogs(String event, String username, String desc, String timestamp) {
        String sql = "INSERT INTO logs(event,username,desc,timestamp) VALUES(?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(driverURL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, event);
            pstmt.setString(2, username);
            pstmt.setString(3, desc);
            pstmt.setString(4, timestamp);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            logError(ex);
        }
    }

    // Add product entry using PreparedStatement
    public void addProduct(String name, int stock, double price) {
        String sql = "INSERT INTO product(name,stock,price) VALUES(?,?,?)";
        try (Connection conn = DriverManager.getConnection(driverURL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, stock);
            pstmt.setDouble(3, price);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            logError(ex);
        }
    }

    // Add user entry with hashed password using PreparedStatement
    public void addUser(String username, String password, int role) throws NoSuchAlgorithmException {
        String hashedPassword = hashPassword(password); // Hash the password before storing
        String sql = "INSERT INTO users(username,password,role) VALUES(?,?,?)";
        try (Connection conn = DriverManager.getConnection(driverURL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setInt(3, role);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            logError(ex);
        }
    }

    // Remove user by username
    public void removeUser(String username) {
        String sql = "DELETE FROM users WHERE username=?";
        try (Connection conn = DriverManager.getConnection(driverURL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
            System.out.println("User " + username + " has been deleted.");
        } catch (SQLException ex) {
            logError(ex);
        }
    }

    // Get history entries
    public ArrayList<History> getHistory() {
        String sql = "SELECT id, username, name, stock, timestamp FROM history";
        return executeQueryForHistory(sql);
    }

    // Get logs entries
    public ArrayList<Logs> getLogs() {
        String sql = "SELECT id, event, username, desc, timestamp FROM logs";
        return executeQueryForLogs(sql);
    }

    // Get product entries
    public ArrayList<Product> getProduct() {
        String sql = "SELECT id, name, stock, price FROM product";
        return executeQueryForProducts(sql);
    }

    // Get users excluding passwords
    public ArrayList<User> getUsers() {
        String sql = "SELECT id, username, role, locked FROM users"; // Exclude password field
        ArrayList<User> users = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(driverURL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(new User(rs.getInt("id"),
                        rs.getString("username"),
                        "",
                        rs.getInt("role"),
                        rs.getInt("locked")));
            }
        } catch (SQLException ex) {
            logError(ex);
        }
        return users;
    }

    // Helper method to hash passwords using SHA-256
    private String hashPassword(String plainTextPassword) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(plainTextPassword.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    // Helper method to log errors
    private void logError(Exception ex) {
        if (DEBUG_MODE == 1) {
            ex.printStackTrace();
        }
    }

    // Helper method to execute SQL updates
    private void executeUpdate(String sql, String successMessage) {
        try (Connection conn = DriverManager.getConnection(driverURL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println(successMessage);
        } catch (SQLException ex) {
            logError(ex);
        }
    }

    // Helper method to execute SQL queries for history
    private ArrayList<History> executeQueryForHistory(String sql) {
        ArrayList<History> histories = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(driverURL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                histories.add(new History(rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("name"),
                        rs.getInt("stock"),
                        rs.getString("timestamp")));
            }
        } catch (SQLException ex) {
            logError(ex);
        }
        return histories;
    }

    // Helper method to execute SQL queries for logs
    private ArrayList<Logs> executeQueryForLogs(String sql) {
        ArrayList<Logs> logs = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(driverURL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                logs.add(new Logs(rs.getInt("id"),
                        rs.getString("event"),
                        rs.getString("username"),
                        rs.getString("desc"),
                        rs.getString("timestamp")));
            }
        } catch (SQLException ex) {
            logError(ex);
        }
        return logs;
    }

    // Helper method to execute SQL queries for products
    private ArrayList<Product> executeQueryForProducts(String sql) {
        ArrayList<Product> products = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(driverURL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(new Product(rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("stock"),
                        rs.getFloat("price")));
            }
        } catch (SQLException ex) {
            logError(ex);
        }
        return products;
    }

    public User getUserByUsername(String username) {
        String sql = "SELECT id, username, password, role, locked FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(driverURL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username); // Safely set the username parameter to prevent SQL injection
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"), // Password should be hashed
                        rs.getInt("role"),
                        rs.getInt("locked")
                );
            }
        } catch (SQLException ex) {
            System.out.println("Error retrieving user by username: " + ex.getMessage());
        }
        return null; // Return null if no user is found
    }
    
    public void resetFailedLoginAttempts(String username) {
        String sql = "UPDATE users SET failed_attempts = 0 WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(driverURL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("Error resetting failed login attempts: " + ex.getMessage());
        }
    }

    public int incrementFailedLoginAttempts(String username) {
        String incrementSql = "UPDATE users SET failed_attempts = failed_attempts + 1 WHERE username = ?";
        String selectSql = "SELECT failed_attempts FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(driverURL)) {
            // Increment failed attempts
            try (PreparedStatement incrementStmt = conn.prepareStatement(incrementSql)) {
                incrementStmt.setString(1, username);
                incrementStmt.executeUpdate();
            }
            // Retrieve updated failed attempts
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setString(1, username);
                ResultSet rs = selectStmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("failed_attempts");
                }
            }
        } catch (SQLException ex) {
            System.out.println("Error updating failed login attempts: " + ex.getMessage());
        }
        return 0;
    }

    public void lockAccount(String username) {
        String sql = "UPDATE users SET locked = 1 WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(driverURL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("Error locking account: " + ex.getMessage());
        }
    }
    
    public void logAuthenticationEvent(String username, String event) {
    String sql = "INSERT INTO logs(event, username, desc, timestamp) VALUES(?, ?, ?, ?)";
    try (Connection conn = DriverManager.getConnection(driverURL);
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, event);
        pstmt.setString(2, username);
        pstmt.setString(3, event.equals("SUCCESSFUL_LOGIN") ? "User logged in successfully" : "Failed login attempt");
        pstmt.setString(4, new java.sql.Timestamp(new java.util.Date().getTime()).toString());
        pstmt.executeUpdate();
    } catch (SQLException ex) {
        System.out.println("Error logging authentication event: " + ex.getMessage());
    }
    }

    public Product getProduct(String name){
        String sql = "SELECT name, stock, price FROM product WHERE name='" + name + "';";
        Product product = null;
        try (Connection conn = DriverManager.getConnection(driverURL);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)){
            product = new Product(rs.getString("name"),
                                   rs.getInt("stock"),
                                   rs.getFloat("price"));
        } catch (Exception ex) {
            System.out.print(ex);
        }
        return product;
    }
    
}