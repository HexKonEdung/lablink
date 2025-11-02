package Main;

// TestRecordDAO.java
// Defensive mapping, ensures missing columns do not crash.
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TestRecordDAO {

    public static List<TestRecord> listAll(String statusFilter, String search) {
        List<TestRecord> list = new ArrayList<>();
        String sql = "SELECT t.* FROM tests t JOIN patients p ON t.patient_id = p.patient_id WHERE 1=1";
        if (statusFilter != null && !statusFilter.isEmpty()) {
            sql += " AND t.status = ?";
        }
        if (search != null && !search.isEmpty()) {
            sql += " AND (t.test_id LIKE ? OR p.name LIKE ? OR t.category LIKE ?)";
        }
        sql += " ORDER BY t.test_id DESC";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            if (statusFilter != null && !statusFilter.isEmpty()) {
                ps.setString(i++, statusFilter);
            }
            if (search != null && !search.isEmpty()) {
                ps.setString(i++, "%" + search + "%");
                ps.setString(i++, "%" + search + "%");
                ps.setString(i++, "%" + search + "%");
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

    public static TestRecord findById(int id) {
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT * FROM tests WHERE test_id = ?")) {
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

    public static int save(TestRecord t) {
        String sql;
        if (t.testId > 0) {
            sql = "UPDATE tests SET patient_id=?, test_name=?, category=?, sample_type=?, date_conducted=?, technician=?, status=?, remarks=?, verification_status=?, priority_level=?, date_verified=? WHERE test_id=?";
        } else {
            sql = "INSERT INTO tests (patient_id, test_name, category, sample_type, date_conducted, technician, status, remarks, verification_status, priority_level, date_verified) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        }
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, t.patientId);
            ps.setString(2, t.testName);
            ps.setString(3, t.category);
            ps.setString(4, t.sampleType);
            ps.setString(5, t.dateConducted);
            ps.setString(6, t.technician);
            ps.setString(7, t.status);
            ps.setString(8, t.remarks);
            ps.setString(9, t.verificationStatus);
            ps.setString(10, t.priorityLevel);
            ps.setString(11, t.dateVerified);
            if (t.testId > 0) {
                ps.setInt(12, t.testId);
            }
            ps.executeUpdate();
            if (t.testId == 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        t.testId = rs.getInt(1);
                    }
                }
            }
            return t.testId;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static boolean delete(int id) {
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM tests WHERE test_id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static TestRecord mapRow(ResultSet rs) {
        TestRecord t = new TestRecord();
        t.testId = safeGetInt(rs, "test_id");
        t.patientId = safeGetInt(rs, "patient_id");
        t.testName = safeGetString(rs, "test_name");
        t.category = safeGetString(rs, "category");
        t.sampleType = safeGetString(rs, "sample_type");
        t.dateConducted = safeGetString(rs, "date_conducted");
        t.technician = safeGetString(rs, "technician");
        t.status = safeGetString(rs, "status");
        t.remarks = safeGetString(rs, "remarks");
        t.verificationStatus = safeGetString(rs, "verification_status");
        t.priorityLevel = safeGetString(rs, "priority_level");
        t.dateVerified = safeGetString(rs, "date_verified");
        return t;
    }

    private static String safeGetString(ResultSet rs, String col) {
        try {
            return rs.getString(col);
        } catch (SQLException ex) {
            return null;
        }
    }

    private static int safeGetInt(ResultSet rs, String col) {
        try {
            return rs.getInt(col);
        } catch (SQLException ex) {
            return 0;
        }
    }

    /**
     * Return parameters for a test record
     */
    public static List<TestParameter> getParameters(int testId) {
        List<TestParameter> out = new ArrayList<>();
        String sql = "SELECT * FROM parameters WHERE test_id = ? ORDER BY parameter_id";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, testId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TestParameter p = new TestParameter();
                    p.parameterId = safeGetInt(rs, "parameter_id");
                    p.testId = safeGetInt(rs, "test_id");
                    p.parameterName = safeGetString(rs, "parameter_name");
                    p.resultValue = safeGetString(rs, "result_value");
                    p.normalRange = safeGetString(rs, "normal_range");
                    p.units = safeGetString(rs, "units");
                    p.interpretation = safeGetString(rs, "interpretation");
                    p.remarks = safeGetString(rs, "remarks");
                    out.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }
}
