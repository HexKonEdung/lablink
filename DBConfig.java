// DBConfig.java
// MySQL-only configuration (change JDBC_URL, MYSQL_USER, MYSQL_PASSWORD as needed)
package Main;
public class DBConfig {

    // Use your MySQL connection string:
    public static final String JDBC_URL = "jdbc:mysql://localhost:3306/backend?zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=UTC";

    // MySQL credentials
    public static final String MYSQL_USER = "root";
    public static final String MYSQL_PASSWORD = ""; // set if you have password

    // Seed admin credentials if accounts table is empty
    public static final String DEFAULT_ADMIN_USERNAME = "admin";
    public static final String DEFAULT_ADMIN_PASSWORD = "admin"; // seeded password (PBKDF2 hashed on insert)
}
