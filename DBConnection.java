// DBConnection.java
// MySQL-only connection manager
package Main;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static Connection conn = null;

    public static synchronized Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            String url = DBConfig.JDBC_URL;
            if (url == null || url.isEmpty()) {
                throw new SQLException("JDBC_URL is not configured in DBConfig.");
            }
            conn = DriverManager.getConnection(url, DBConfig.MYSQL_USER, DBConfig.MYSQL_PASSWORD);
        }
        return conn;
    }

    public static synchronized void close() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
