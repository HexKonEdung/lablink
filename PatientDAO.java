package Main;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Patient DAO with static convenience methods used by the UI.
 *
 * Important change: mapRow no longer calls AccountDAO.findById() while the
 * incoming ResultSet is still open. Calling another query on the same
 * Connection could close the current ResultSet with some JDBC drivers (MySQL
 * default behavior). Instead we only extract the raw registered_by value (int
 * id or legacy string) into the Patient object. The UI or callers should
 * resolve the account display name later (after the ResultSet has been closed)
 * if they want the full name.
 */
public class PatientDAO {

    public static List<Patient> listAll(String filterSex, String search) {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT * FROM patients WHERE 1=1";
        if (filterSex != null && !filterSex.isEmpty()) {
            sql += " AND sex = ?";
        }
        if (search != null && !search.isEmpty()) {
            sql += " AND (name LIKE ? OR contact_number LIKE ? OR email LIKE ?)";
        }
        sql += " ORDER BY patient_id DESC";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            int idx = 1;
            if (filterSex != null && !filterSex.isEmpty()) {
                ps.setString(idx++, filterSex);
            }
            if (search != null && !search.isEmpty()) {
                String q = "%" + search + "%";
                ps.setString(idx++, q);
                ps.setString(idx++, q);
                ps.setString(idx++, q);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // mapRow reads values from the current ResultSet row but DOES NOT execute
                    // any further queries while the ResultSet is open (to avoid closing it).
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Patient findById(int id) {
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT * FROM patients WHERE patient_id = ?")) {
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

    public static boolean save(Patient p) {
        try (Connection c = DBConnection.getConnection()) {
            if (p.patientId > 0) {
                String sql = "UPDATE patients SET name=?, sex=?, date_of_birth=?, contact_number=?, email=?, address=?, blood_type=?, allergies=?, existing_conditions=?, emergency_contact=?, date_registered=?, registered_by=?, profile_picture=? WHERE patient_id=?";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    int i = 1;
                    ps.setString(i++, p.name);
                    ps.setString(i++, p.sex);
                    ps.setString(i++, p.dateOfBirth);
                    ps.setString(i++, p.contactNumber);
                    ps.setString(i++, p.email);
                    ps.setString(i++, p.address);
                    ps.setString(i++, p.bloodType);
                    ps.setString(i++, p.allergies);
                    ps.setString(i++, p.existingConditions);
                    ps.setString(i++, p.emergencyContact);
                    ps.setString(i++, p.dateRegistered);
                    // registered_by stored as INT account_id (0 if unknown)
                    ps.setInt(i++, p.registeredById);
                    ps.setString(i++, p.profilePicture);
                    ps.setInt(i++, p.patientId);
                    ps.executeUpdate();
                }
            } else {
                String sql = "INSERT INTO patients (name, sex, date_of_birth, contact_number, email, address, blood_type, allergies, existing_conditions, emergency_contact, date_registered, registered_by, profile_picture) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
                try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    int i = 1;
                    ps.setString(i++, p.name);
                    ps.setString(i++, p.sex);
                    ps.setString(i++, p.dateOfBirth);
                    ps.setString(i++, p.contactNumber);
                    ps.setString(i++, p.email);
                    ps.setString(i++, p.address);
                    ps.setString(i++, p.bloodType);
                    ps.setString(i++, p.allergies);
                    ps.setString(i++, p.existingConditions);
                    ps.setString(i++, p.emergencyContact);
                    ps.setString(i++, p.dateRegistered);
                    ps.setInt(i++, p.registeredById); // store account_id
                    ps.setString(i++, p.profilePicture);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            p.patientId = rs.getInt(1);
                        }
                    }
                }
            }
            ActivityLogDAO.log(Session.getCurrentUser() != null ? Session.getCurrentUser().username : "system", p.patientId > 0 ? "UpdatedPatient" : "AddedPatient", "patients", p.patientId, "Patient saved/updated.");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean delete(int id) {
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM patients WHERE patient_id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            ActivityLogDAO.log(Session.getCurrentUser() != null ? Session.getCurrentUser().username : "system", "DeletedPatient", "patients", id, "Patient deleted.");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Map a ResultSet row to Patient.
     *
     * IMPORTANT: do NOT run additional queries (via AccountDAO or other DAOs)
     * here, because some JDBC drivers (or a shared Connection implementation)
     * may close the active ResultSet when another Statement executes on the
     * same Connection. Instead, capture the raw registered_by value (ID or
     * legacy string). UI code should resolve the account name after the
     * ResultSet has been closed.
     */
    private static Patient mapRow(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.patientId = safeGetInt(rs, "patient_id");
        p.name = safeGetString(rs, "name");
        p.sex = safeGetString(rs, "sex");
        p.dateOfBirth = safeGetString(rs, "date_of_birth");
        p.contactNumber = safeGetString(rs, "contact_number");
        p.email = safeGetString(rs, "email");
        p.address = safeGetString(rs, "address");
        p.bloodType = safeGetString(rs, "blood_type");
        p.allergies = safeGetString(rs, "allergies");
        p.existingConditions = safeGetString(rs, "existing_conditions");
        p.emergencyContact = safeGetString(rs, "emergency_contact");
        p.dateRegistered = safeGetString(rs, "date_registered");

        // Read registered_by flexibly but DO NOT resolve the account via a DB call here.
        p.registeredById = 0;
        p.registeredBy = null;

        Object regObj = null;
        try {
            regObj = rs.getObject("registered_by");
        } catch (SQLException ignore) {
            regObj = null;
        }

        if (regObj != null) {
            if (regObj instanceof Number) {
                p.registeredById = ((Number) regObj).intValue();
                // Do NOT call AccountDAO.findById here. Store the id and let UI resolve later.
                p.registeredBy = String.valueOf(p.registeredById);
            } else {
                String legacy = String.valueOf(regObj);
                if (!legacy.isEmpty()) {
                    p.registeredBy = legacy;
                }
            }
        } else {
            // fallback: try reading as string
            String legacy = rs.getString("registered_by");
            if (legacy != null && !legacy.isEmpty()) {
                p.registeredBy = legacy;
            }
        }

        p.profilePicture = safeGetString(rs, "profile_picture");
        return p;
    }

    private static String safeGetString(ResultSet rs, String col) throws SQLException {
        String v = rs.getString(col);
        return rs.wasNull() ? null : v;
    }

    private static int safeGetInt(ResultSet rs, String col) throws SQLException {
        int v = rs.getInt(col);
        return rs.wasNull() ? 0 : v;
    }
}
