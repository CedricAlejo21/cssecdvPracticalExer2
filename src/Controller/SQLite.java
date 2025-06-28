package Controller;

import Model.History;
import Model.Logs;
import Model.Product;
import Model.User;
import java.sql.*;
import java.util.ArrayList;

public class SQLite {

    public int DEBUG_MODE = 0;
    String driverURL = "jdbc:sqlite:" + "database.db";

    public void createNewDatabase() {
        try (Connection conn = DriverManager.getConnection(driverURL)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("Database database.db created.");
            }
        } catch (Exception ex) {
            logError(ex);
        }
    }

    public void createHistoryTable() {
        String sql = "CREATE TABLE IF NOT EXISTS history (" +
                     " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                     " username TEXT NOT NULL," +
                     " name TEXT NOT NULL," +
                     " stock INTEGER DEFAULT 0," +
                     " timestamp TEXT NOT NULL" +
                     ");";
        executeUpdate(sql, "Table history in database.db created.");
    }

    public void createLogsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS logs (" +
                     " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                     " event TEXT NOT NULL," +
                     " username TEXT NOT NULL," +
                     " desc TEXT NOT NULL," +
                     " timestamp TEXT NOT NULL" +
                     ");";
        executeUpdate(sql, "Table logs in database.db created.");
    }

    public void createProductTable() {
        String sql = "CREATE TABLE IF NOT EXISTS product (" +
                     " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                     " name TEXT NOT NULL UNIQUE," +
                     " stock INTEGER DEFAULT 0," +
                     " price REAL DEFAULT 0.00" +
                     ");";
        executeUpdate(sql, "Table product in database.db created.");
    }

    public void createUserTable() {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                     " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                     " username TEXT NOT NULL UNIQUE," +
                     " password TEXT NOT NULL," +
                     " role INTEGER DEFAULT 2," +
                     " locked INTEGER DEFAULT 0" +
                     ");";
        executeUpdate(sql, "Table users in database.db created.");
    }

    public void dropHistoryTable() {
        String sql = "DROP TABLE IF EXISTS history;";
        executeUpdate(sql, "Table history in database.db dropped.");
    }

    public void dropLogsTable() {
        String sql = "DROP TABLE IF EXISTS logs;";
        executeUpdate(sql, "Table logs in database.db dropped.");
    }

    public void dropProductTable() {
        String sql = "DROP TABLE IF EXISTS product;";
        executeUpdate(sql, "Table product in database.db dropped.");
    }

    public void dropUserTable() {
        String sql = "DROP TABLE IF EXISTS users;";
        executeUpdate(sql, "Table users in database.db dropped.");
    }

    public void addHistory(String username, String name, int stock, String timestamp) {
        String sql = "INSERT INTO history(username, name, stock, timestamp) VALUES(?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(driverURL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, name);
            pstmt.setInt(3, stock);
            pstmt.setString(4, timestamp);
            pstmt.executeUpdate();
        } catch (Exception ex) {
            logError(ex);
        }
    }

    public void addLogs(String event, String username, String desc, String timestamp) {
        String sql = "INSERT INTO logs(event, username, desc, timestamp) VALUES(?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(driverURL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, event);
            pstmt.setString(2, username);
            pstmt.setString(3, desc);
            pstmt.setString(4, timestamp);
            pstmt.executeUpdate();
        } catch (Exception ex) {
            logError(ex);
        }
    }

    public void addProduct(String name, int stock, double price) {
        String sql = "INSERT INTO product(name, stock, price) VALUES(?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(driverURL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, stock);
            pstmt.setDouble(3, price);
            pstmt.executeUpdate();
        } catch (Exception ex) {
            logError(ex);
        }
    }

    public void addUser(String username, String password) {
        addUser(username, password, 2); // Default role is 2 (client)
    }

    public void addUser(String username, String password, int role) {
        String hashedPassword = hashPassword(password);
        String sql = "INSERT INTO users(username, password, role) VALUES(?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(driverURL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setInt(3, role);
            pstmt.executeUpdate();
        } catch (Exception ex) {
            logError(ex);
        }
    }

    public ArrayList<History> getHistory() {
        String sql = "SELECT id, username, name, stock, timestamp FROM history";
        return executeQueryForHistory(sql);
    }

    public ArrayList<Logs> getLogs() {
        String sql = "SELECT id, event, username, desc, timestamp FROM logs";
        return executeQueryForLogs(sql);
    }

    public ArrayList<Product> getProduct() {
        String sql = "SELECT id, name, stock, price FROM product";
        return executeQueryForProducts(sql);
    }

    public ArrayList<User> getUsers() {
        String sql = "SELECT id, username, role, locked FROM users"; // Exclude password field
        ArrayList<User> users = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(driverURL);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                users.add(new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getInt("role"),
                    rs.getInt("locked")
                ));
            }
        } catch (Exception ex) {
            logError(ex);
        }
        return users;
    }

    public Product getProduct(String name) {
        String sql = "SELECT name, stock, price FROM product WHERE name = ?";
        Product product = null;
        try (Connection conn = DriverManager.getConnection(driverURL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                product = new Product(
                    rs.getString("name"),
                    rs.getInt("stock"),
                    rs.getFloat("price")
                );
            }
        } catch (Exception ex) {
            logError(ex);
        }
        return product;
    }

    public void removeUser(String username) {
        String sql = "DELETE FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(driverURL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
            System.out.println("User " + username + " has been deleted.");
        } catch (Exception ex) {
            logError(ex);
        }
    }

    private void executeUpdate(String sql, String successMessage) {
        try (Connection conn = DriverManager.getConnection(driverURL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println(successMessage);
        } catch (Exception ex) {
            logError(ex);
        }
    }

    private ArrayList<History> executeQueryForHistory(String sql) {
        ArrayList<History> histories = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(driverURL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                histories.add(new History(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("name"),
                    rs.getInt("stock"),
                    rs.getString("timestamp")
                ));
            }
        } catch (Exception ex) {
            logError(ex);
        }
        return histories;
    }

    private ArrayList<Logs> executeQueryForLogs(String sql) {
        ArrayList<Logs> logs = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(driverURL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                logs.add(new Logs(
                    rs.getInt("id"),
                    rs.getString("event"),
                    rs.getString("username"),
                    rs.getString("desc"),
                    rs.getString("timestamp")
                ));
            }
        } catch (Exception ex) {
            logError(ex);
        }
        return logs;
    }

    private ArrayList<Product> executeQueryForProducts(String sql) {
        ArrayList<Product> products = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(driverURL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("stock"),
                    rs.getFloat("price")
                ));
            }
        } catch (Exception ex) {
            logError(ex);
        }
        return products;
    }

    private String hashPassword(String password) {
        return org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt());
    }

    private void logError(Exception ex) {
        if (DEBUG_MODE == 1) {
            ex.printStackTrace();
        } else {
            System.err.println("An error occurred. Please check the logs.");
        }
    }
}