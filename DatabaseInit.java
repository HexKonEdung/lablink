// DatabaseInit.java
// MySQL-only initialization; idempotent, safe to call multiple times.
package Main;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseInit {

    private static volatile boolean initialized = false;

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        try {
            // Load driver (already loaded in Sign_In but safe here)
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignore) {
        }

        String url = DBConfig.JDBC_URL;
        if (url == null || !url.startsWith("jdbc:mysql:")) {
            throw new RuntimeException("This build expects a MySQL JDBC URL in DBConfig.JDBC_URL");
        }

        try {
            JdbcUrlParts parts = parseJdbcUrl(url);
            createDatabaseIfNotExists(parts);

            try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement()) {

                // Accounts
                s.execute("CREATE TABLE IF NOT EXISTS accounts ("
                        + "account_id INT AUTO_INCREMENT PRIMARY KEY,"
                        + "full_name TEXT NOT NULL,"
                        + "username TEXT NOT NULL UNIQUE,"
                        + "password_hash TEXT,"
                        + "password TEXT,"
                        + "email TEXT,"
                        + "contact_number TEXT,"
                        + "sex TEXT,"
                        + "role TEXT NOT NULL,"
                        + "profile_picture TEXT,"
                        + "date_created TEXT,"
                        + "last_login TEXT"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

                // Patients
                s.execute("CREATE TABLE IF NOT EXISTS patients ("
                        + "patient_id INT AUTO_INCREMENT PRIMARY KEY,"
                        + "name TEXT NOT NULL,"
                        + "sex TEXT,"
                        + "date_of_birth TEXT,"
                        + "contact_number TEXT,"
                        + "email TEXT,"
                        + "address TEXT,"
                        + "blood_type TEXT,"
                        + "allergies TEXT,"
                        + "existing_conditions TEXT,"
                        + "emergency_contact TEXT,"
                        + "date_registered TEXT,"
                        + "registered_by TEXT,"
                        + "profile_picture TEXT"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

                // Tests
                s.execute("CREATE TABLE IF NOT EXISTS tests ("
                        + "test_id INT AUTO_INCREMENT PRIMARY KEY,"
                        + "patient_id INT NOT NULL,"
                        + "test_name TEXT,"
                        + "category TEXT,"
                        + "sample_type TEXT,"
                        + "date_conducted TEXT,"
                        + "technician TEXT,"
                        + "status TEXT,"
                        + "remarks TEXT,"
                        + "verification_status TEXT,"
                        + "priority_level TEXT,"
                        + "date_verified TEXT,"
                        + "FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

                // Parameters
                s.execute("CREATE TABLE IF NOT EXISTS parameters ("
                        + "parameter_id INT AUTO_INCREMENT PRIMARY KEY,"
                        + "test_id INT NOT NULL,"
                        + "parameter_name TEXT,"
                        + "result_value TEXT,"
                        + "normal_range TEXT,"
                        + "units TEXT,"
                        + "interpretation TEXT,"
                        + "remarks TEXT,"
                        + "FOREIGN KEY (test_id) REFERENCES tests(test_id) ON DELETE CASCADE"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

                // Reports
                s.execute("CREATE TABLE IF NOT EXISTS reports ("
                        + "report_id INT AUTO_INCREMENT PRIMARY KEY,"
                        + "test_id INT,"
                        + "generated_by TEXT,"
                        + "generated_date TEXT,"
                        + "file_format TEXT,"
                        + "printed_by TEXT,"
                        + "FOREIGN KEY (test_id) REFERENCES tests(test_id) ON DELETE SET NULL"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

                // Activity log
                s.execute("CREATE TABLE IF NOT EXISTS activity_log ("
                        + "log_id INT AUTO_INCREMENT PRIMARY KEY,"
                        + "user TEXT,"
                        + "action TEXT,"
                        + "target_table TEXT,"
                        + "target_id INT,"
                        + "description TEXT,"
                        + "timestamp TEXT"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

                // Ensure older DBs have columns (idempotent)
                ensureColumnExists(c, "accounts", "password_hash", "TEXT");
                ensureColumnExists(c, "accounts", "password", "TEXT");
                ensureColumnExists(c, "accounts", "last_login", "TEXT");

                ensureColumnExists(c, "patients", "date_of_birth", "TEXT");
                ensureColumnExists(c, "patients", "contact_number", "TEXT");
                ensureColumnExists(c, "patients", "email", "TEXT");
                ensureColumnExists(c, "patients", "address", "TEXT");
                ensureColumnExists(c, "patients", "blood_type", "TEXT");
                ensureColumnExists(c, "patients", "allergies", "TEXT");
                ensureColumnExists(c, "patients", "existing_conditions", "TEXT");
                ensureColumnExists(c, "patients", "emergency_contact", "TEXT");
                ensureColumnExists(c, "patients", "date_registered", "TEXT");
                ensureColumnExists(c, "patients", "registered_by", "TEXT");
                ensureColumnExists(c, "patients", "profile_picture", "TEXT");

                ensureColumnExists(c, "tests", "date_conducted", "TEXT");
                ensureColumnExists(c, "tests", "technician", "TEXT");
                ensureColumnExists(c, "tests", "status", "TEXT");
                ensureColumnExists(c, "tests", "remarks", "TEXT");
                ensureColumnExists(c, "tests", "verification_status", "TEXT");
                ensureColumnExists(c, "tests", "priority_level", "TEXT");
                ensureColumnExists(c, "tests", "date_verified", "TEXT");

                ensureColumnExists(c, "activity_log", "description", "TEXT");

                // Seed admin if none
                try (ResultSet rs = s.getConnection().createStatement().executeQuery("SELECT COUNT(*) AS c FROM accounts")) {
                    int count = rs.next() ? rs.getInt("c") : 0;
                    if (count == 0) {
                        String adminPassHash = PasswordUtil.hashPassword(DBConfig.DEFAULT_ADMIN_PASSWORD);
                        String now = LocalDateTime.now().toString();
                        String ins = "INSERT INTO accounts (full_name, username, password_hash, email, role, created_at) VALUES (?,?,?,?,?,?)";
                        try (PreparedStatement ps = c.prepareStatement(ins)) {
                            ps.setString(1, "Administrator");
                            ps.setString(2, DBConfig.DEFAULT_ADMIN_USERNAME);
                            ps.setString(3, adminPassHash);
                            ps.setString(4, "admin@example.com");
                            ps.setString(5, "Admin");
                            ps.setString(6, now);
                            ps.executeUpdate();
                        }
                    }
                }

                System.out.println("Database initialized on MySQL: " + parts.database);
                initialized = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Database initialization failed: " + e.getMessage(), e);
        }
    }

    private static void ensureColumnExists(Connection c, String table, String column, String definition) {
        try {
            DatabaseMetaData meta = c.getMetaData();
            String catalog = c.getCatalog();
            try (ResultSet cols = meta.getColumns(catalog, null, table, column)) {
                if (!cols.next()) {
                    try (Statement s = c.createStatement()) {
                        s.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
                        System.out.println("Added missing column " + column + " to " + table + " (migration helper).");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static JdbcUrlParts parseJdbcUrl(String url) {
        Pattern p = Pattern.compile("^jdbc:mysql://([^/]+)(?:/([^?]+))?(?:\\?(.*))?$", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(url);
        if (!m.matches()) {
            throw new IllegalArgumentException("Unsupported JDBC URL format: " + url);
        }
        String hostPort = m.group(1);
        String database = m.group(2) != null ? m.group(2) : "";
        String params = m.group(3) != null ? m.group(3) : "";
        return new JdbcUrlParts(hostPort, database, params);
    }

    private static void createDatabaseIfNotExists(JdbcUrlParts parts) throws SQLException {
        if (parts.database == null || parts.database.isEmpty()) {
            return;
        }
        String serverUrl = "jdbc:mysql://" + parts.hostPort + "/?zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=UTC";
        try (Connection conn = DriverManager.getConnection(serverUrl, DBConfig.MYSQL_USER, DBConfig.MYSQL_PASSWORD); Statement stmt = conn.createStatement()) {
            String dbName = parts.database;
            String createDbSql = "CREATE DATABASE IF NOT EXISTS `" + escapeIdentifier(dbName) + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
            stmt.execute(createDbSql);
            System.out.println("Ensured database exists: " + dbName);
        }
    }

    private static String escapeIdentifier(String id) {
        return id.replace("`", "``");
    }

    private static class JdbcUrlParts {

        String hostPort, database, params;

        JdbcUrlParts(String hostPort, String database, String params) {
            this.hostPort = hostPort;
            this.database = database;
            this.params = params;
        }
    }
}
