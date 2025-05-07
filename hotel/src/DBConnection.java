import java.sql.*;

public class DBConnection {
    static final String DB_URL = "jdbc:mysql://localhost:3306/hotel_db";
    static final String USER = "root";      // Change if your username is different
    static final String PASS = "root";       // Change if your password is different

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
