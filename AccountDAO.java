// AccountDAO.java
// Authentication with legacy migration support and improved save/insert behavior.
package Main;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.security.MessageDigest;

public class AccountDAO {

    public static Account authenticate(String username, String password) {
        String sql = "SELECT * FROM accounts WHERE username = ?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Account a = mapRow(rs);

                    String stored = a.passwordHash;
                    // If no passwordHash, try legacy 'password'
                    if (stored == null || stored.isEmpty()) {
                        stored = safeGetString(rs, "password");
                    }

                    // PBKDF2 format
                    if (stored != null && stored.contains(":")) {
                        if (PasswordUtil.verifyPassword(password, stored)) {
                            updateLastLogin(c, a.accountId);
                            return a;
                        } else {
                            return null;
                        }
                    }

                    // plaintext or legacy MD5
                    if (stored != null && !stored.contains(":")) {
                        if (stored.equals(password)) {
                            String newHash = PasswordUtil.hashPassword(password);
                            ensurePasswordHashColumnExists(c);
                            updatePasswordHash(c, a.accountId, newHash);
                            a.passwordHash = newHash;
                            updateLastLogin(c, a.accountId);
                            return a;
                        }
                        if (md5(password).equalsIgnoreCase(stored)) {
                            String newHash = PasswordUtil.hashPassword(password);
                            ensurePasswordHashColumnExists(c);
                            updatePasswordHash(c, a.accountId, newHash);
                            a.passwordHash = newHash;
                            updateLastLogin(c, a.accountId);
                            return a;
                        }
                        return null;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void updateLastLogin(Connection c, int accountId) {
        try (PreparedStatement ups = c.prepareStatement("UPDATE accounts SET last_login = ? WHERE account_id = ?")) {
            ups.setString(1, java.time.LocalDateTime.now().toString());
            ups.setInt(2, accountId);
            ups.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Warning: could not update last_login: " + e.getMessage());
        }
    }

    private static void updatePasswordHash(Connection c, int accountId, String newHash) {
        try (PreparedStatement ps = c.prepareStatement("UPDATE accounts SET password_hash = ? WHERE account_id = ?")) {
            ps.setString(1, newHash);
            ps.setInt(2, accountId);
            ps.executeUpdate();
            ActivityLogDAO.log("system", "MigratedPassword", "accounts", accountId, "Migrated legacy password to PBKDF2.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void ensurePasswordHashColumnExists(Connection c) {
        try {
            DatabaseMetaData meta = c.getMetaData();
            // Use catalog from connection (may be null depending on driver)
            String catalog = c.getCatalog();
            try (ResultSet cols = meta.getColumns(catalog, null, "accounts", "password_hash")) {
                if (!cols.next()) {
                    try (Statement s = c.createStatement()) {
                        s.execute("ALTER TABLE accounts ADD COLUMN password_hash TEXT");
                        System.out.println("Added missing column password_hash to accounts.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String md5(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] b = md.digest(s.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte x : b) {
                sb.append(String.format("%02x", x & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    public static Account findById(int id) {
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT * FROM accounts WHERE account_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Account> listAll(String filterRole, String search) {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE 1=1 ";
        if (filterRole != null && !filterRole.isEmpty()) {
            sql += " AND role = ?";
        }
        if (search != null && !search.isEmpty()) {
            sql += " AND (full_name LIKE ? OR username LIKE ?)";
        }
        sql += " ORDER BY account_id DESC";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            int idx = 1;
            if (filterRole != null && !filterRole.isEmpty()) {
                ps.setString(idx++, filterRole);
            }
            if (search != null && !search.isEmpty()) {
                ps.setString(idx++, "%" + search + "%");
                ps.setString(idx++, "%" + search + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Save or update account. When inserting, generated key is retrieved and
     * set on the account object so callers get the assigned account_id.
     *
     * @param a account to save (mutable)
     * @param plainPassword plaintext password to set (null to leave unchanged
     * for updates)
     * @return true if saved
     */
    public static boolean save(Account a, String plainPassword) {
        try (Connection c = DBConnection.getConnection()) {
            if (a.accountId > 0) {
                String sql = "UPDATE accounts SET full_name=?, username=?, email=?, contact_number=?, sex=?, role=?, profile_picture=?"
                        + (plainPassword != null && !plainPassword.isEmpty() ? ", password_hash=? " : "")
                        + " WHERE account_id=?";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    int i = 1;
                    ps.setString(i++, a.fullName);
                    ps.setString(i++, a.username);
                    ps.setString(i++, a.email);
                    ps.setString(i++, a.contactNumber);
                    ps.setString(i++, a.sex);
                    ps.setString(i++, a.role);
                    ps.setString(i++, a.profilePicture);
                    if (plainPassword != null && !plainPassword.isEmpty()) {
                        ps.setString(i++, PasswordUtil.hashPassword(plainPassword));
                    }
                    ps.setInt(i++, a.accountId);
                    ps.executeUpdate();
                }
            } else {
                // Insert and capture generated key
                String sql = "INSERT INTO accounts (full_name, username, password_hash, email, contact_number, sex, role, profile_picture, created_at) VALUES (?,?,?,?,?,?,?,?,?)";
                try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, a.fullName);
                    ps.setString(2, a.username);
                    ps.setString(3, PasswordUtil.hashPassword(plainPassword != null ? plainPassword : "password"));
                    ps.setString(4, a.email);
                    ps.setString(5, a.contactNumber);
                    ps.setString(6, a.sex);
                    ps.setString(7, a.role);
                    ps.setString(8, a.profilePicture);
                    ps.setString(9, java.time.LocalDateTime.now().toString());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            a.accountId = rs.getInt(1); // set generated id back to object
                        }
                    }
                }
            }
            ActivityLogDAO.log(Session.getCurrentUser() != null ? Session.getCurrentUser().username : "system", a.accountId > 0 ? "Updated" : "Added", "accounts", a.accountId, "Account saved/updated.");
            return true;
        } catch (SQLException e) {
            // Detect unique username violation and give clearer message in logs
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("duplicate") || msg.contains("unique") || msg.contains("constraint")) {
                System.err.println("Account save failed: username may already exist.");
            }
            e.printStackTrace();
            return false;
        }
    }

    public static boolean delete(int id) {
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM accounts WHERE account_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            ActivityLogDAO.log(Session.getCurrentUser() != null ? Session.getCurrentUser().username : "system", "Deleted", "accounts", id, "Account deleted.");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static Account mapRow(ResultSet rs) {
        try {
            Account a = new Account();
            a.accountId = safeGetInt(rs, "account_id");
            a.fullName = safeGetString(rs, "full_name");
            a.username = safeGetString(rs, "username");
            a.passwordHash = safeGetString(rs, "password_hash");
            if (a.passwordHash == null || a.passwordHash.isEmpty()) {
                a.passwordHash = safeGetString(rs, "password");
            }
            a.email = safeGetString(rs, "email");
            a.contactNumber = safeGetString(rs, "contact_number");
            a.sex = safeGetString(rs, "sex");
            a.role = safeGetString(rs, "role");
            a.profilePicture = safeGetString(rs, "profile_picture");
            a.dateCreated = safeGetString(rs, "created_at");
            a.lastLogin = safeGetString(rs, "last_login");
            return a;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String safeGetString(ResultSet rs, String col) throws SQLException {
        try {
            return rs.getString(col);
        } catch (SQLException e) {
            return null;
        }
    }

    private static int safeGetInt(ResultSet rs, String col) throws SQLException {
        try {
            return rs.getInt(col);
        } catch (SQLException e) {
            return 0;
        }
    }

    /**
     * Helper to check whether a username already exists.
     *
     * @param username username to check
     * @return true if taken
     */
    public static boolean isUsernameTaken(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) AS c FROM accounts WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("c") > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
