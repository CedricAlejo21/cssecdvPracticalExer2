package Controller;

import Model.History;
import Model.Logs;
import Model.Product;
import Model.User;
import View.Frame;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

public class Main {
    
    public SQLite sqlite;
    
    public static void main(String[] args) throws NoSuchAlgorithmException {
        new Main().init();
    }
    
    public void init() throws NoSuchAlgorithmException{
        // Initialize a driver object
        sqlite = new SQLite();

        // Create a database
        sqlite.createNewDatabase();
        
        // Drop tables if needed (for testing purposes only)
        sqlite.dropHistoryTable();
        sqlite.dropLogsTable();
        sqlite.dropProductTable();
        sqlite.dropUserTable();
        
        // Create tables if they do not exist
        sqlite.createHistoryTable();
        sqlite.createLogsTable();
        sqlite.createProductTable();
        sqlite.createUserTable();
        
        // Add sample history
        sqlite.addHistory("admin", "Antivirus", 1, "2019-04-03 14:30:00.000");
        sqlite.addHistory("manager", "Firewall", 1, "2019-04-03 14:30:01.000");
        sqlite.addHistory("staff", "Scanner", 1, "2019-04-03 14:30:02.000");
        
        // Add sample logs
        sqlite.addLogs("NOTICE", "admin", "User creation successful", new Timestamp(new Date().getTime()).toString());
        sqlite.addLogs("NOTICE", "manager", "User creation successful", new Timestamp(new Date().getTime()).toString());
        sqlite.addLogs("NOTICE", "admin", "User creation successful", new Timestamp(new Date().getTime()).toString());
        
        // Add sample product
        sqlite.addProduct("Antivirus", 5, 500.0);
        sqlite.addProduct("Firewall", 3, 1000.0);
        sqlite.addProduct("Scanner", 10, 100.0);

        // Add sample users with strong passwords and enforce password change on first login
        String adminPassword = hashPassword("Admin@1234"); // Example of a strong password
        String managerPassword = hashPassword("Manager@1234");
        String staffPassword = hashPassword("Staff@1234");
        String client1Password = hashPassword("Client1@1234");
        String client2Password = hashPassword("Client2@1234");

        sqlite.addUser("admin", adminPassword, 5);
        sqlite.addUser("manager", managerPassword, 4);
        sqlite.addUser("staff", staffPassword, 3);
        sqlite.addUser("client1", client1Password, 2);
        sqlite.addUser("client2", client2Password, 2);

        // Initialize User Interface
        Frame frame = new Frame();
        frame.init(this);
    }

    // Helper method to hash passwords using BCrypt
    private String hashPassword(String plainTextPassword) {
        // Implement a basic hashing mechanism (e.g., BCrypt)
        // For simplicity, we'll use a placeholder hash function here
        return org.mindrot.jbcrypt.BCrypt.hashpw(plainTextPassword, org.mindrot.jbcrypt.BCrypt.gensalt());
    }
}