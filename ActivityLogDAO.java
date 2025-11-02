// ActivityLogDAO.java
// Robust logging, single implementation.
package Main;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDAO {

    public static void log(String user, String action, String targetTable, Integer targetId, String description) {
        try (Connection c = DBConnection.getConnection()) {
            boolean hasDescription = hasColumn(c, "activity_log", "description");
            String sql = hasDescription
                    ? "INSERT INTO activity_log (account_id, user, action, target_table, target_id, timestamp) VALUES (?,?,?,?,?,?,?)"
                    : "INSERT INTO activity_log (account_id, user, action, target_table, target_id, timestamp) VALUES (?,?,?,?,?,?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                int idx = 1;
                ps.setString(idx++, user);
                ps.setString(idx++, action);
                ps.setString(idx++, targetTable);
                if (targetId != null) {
                    ps.setInt(idx++, targetId);
                } else {
                    ps.setNull(idx++, Types.INTEGER);
                }
                if (hasDescription) {
                    ps.setString(idx++, description);
                }
                ps.setString(idx++, java.time.LocalDateTime.now().toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            // fallback minimal log
            try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("INSERT INTO activity_log (user, action, timestamp) VALUES (?,?,?)")) {
                ps.setString(1, user);
                ps.setString(2, action);
                ps.setString(3, java.time.LocalDateTime.now().toString());
                ps.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static boolean hasColumn(Connection c, String tableName, String columnName) {
        try {
            DatabaseMetaData meta = c.getMetaData();
            try (ResultSet cols = meta.getColumns(c.getCatalog(), null, tableName, columnName)) {
                return cols.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public static List<ActivityLogEntry> listAll(String search) {
        List<ActivityLogEntry> list = new ArrayList<>();
        String sql = "SELECT * FROM activity_log WHERE 1=1 ";
        if (search != null && !search.isEmpty()) {
            sql += " AND (user LIKE ? OR action LIKE ? OR COALESCE(description,'') LIKE ?)";
        }
        sql += " ORDER BY log_id DESC";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (search != null && !search.isEmpty()) {
                ps.setString(1, "%" + search + "%");
                ps.setString(2, "%" + search + "%");
                ps.setString(3, "%" + search + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ActivityLogEntry e = new ActivityLogEntry();
                    e.logId = safeGetInt(rs, "log_id");
                    e.accountId = safeGetInt(rs, "account_id");
                    e.user = safeGetString(rs, "user");
                    e.action = safeGetString(rs, "action");
                    e.targetTable = safeGetString(rs, "target_table");
                    e.targetId = safeGetInt(rs, "target_id");
                    e.description = safeGetString(rs, "description");
                    e.timestamp = safeGetString(rs, "timestamp");
                    list.add(e);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    private static String safeGetString(ResultSet rs, String col) {
        try {
            return rs.getString(col);
        } catch (SQLException ex) {
            return "";
        }
    }

    private static int safeGetInt(ResultSet rs, String col) {
        try {
            return rs.getInt(col);
        } catch (SQLException ex) {
            return 0;
        }
    }
}

class ActivityLogEntry {

    public int logId;
    public int accountId;
    public String user;
    public String action;
    public String targetTable;
    public int targetId;
    public String description;
    public String timestamp;
}
