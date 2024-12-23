import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/UASPBO"; // Ganti dengan nama database Anda
    private static final String USER = "postgres"; // Ganti dengan username database Anda
    private static final String PASSWORD = "admin"; // Ganti dengan password database Anda

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}   
