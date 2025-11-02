package Main;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FINAL CORRECTED: TemplateDAO (MySQL). Includes methods for insert, update,
 * delete, findById, findByName, listAll, AND insertTemplateParameter.
 */
public class TemplateDAO {

    public TemplateDAO() {
    }

    public int insertTemplate(TestTemplate t) {
        String sqlTmpl = "INSERT INTO test_templates(name, category, description) VALUES (?, ?, ?);";
        String sqlParam = "INSERT INTO template_parameters(template_id, param_name, units, reference_range, critical_values) VALUES (?, ?, ?, ?, ?);";
        Connection conn = null;
        int templateId = -1;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement psTmpl = conn.prepareStatement(sqlTmpl, Statement.RETURN_GENERATED_KEYS)) {
                psTmpl.setString(1, t.getName());
                psTmpl.setString(2, t.getCategory());
                psTmpl.setString(3, t.getDescription());
                if (psTmpl.executeUpdate() > 0) {
                    try (ResultSet rs = psTmpl.getGeneratedKeys()) {
                        if (rs.next()) {
                            templateId = rs.getInt(1);
                        }
                    }
                } else {
                    throw new SQLException("Inserting template failed.");
                }
            }
            if (templateId > 0 && t.getParameters() != null && !t.getParameters().isEmpty()) {
                try (PreparedStatement psParam = conn.prepareStatement(sqlParam)) {
                    for (TestParameter p : t.getParameters()) {
                        psParam.setInt(1, templateId);
                        psParam.setString(2, p.getParameterName());
                        psParam.setString(3, p.getUnits());
                        psParam.setString(4, p.getReferenceRange());
                        psParam.setString(5, p.getCriticalValues());
                        psParam.addBatch();
                    }
                    psParam.executeBatch();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            System.err.println("Error inserting template: " + e.getMessage());
            e.printStackTrace();
            templateId = -1;
            if (conn != null) try {
                conn.rollback();
            } catch (SQLException ex) {
            }
        } finally {
            if (conn != null) try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException ex) {
            }
        }
        return templateId;
    }

    public boolean insertTemplateParameter(int templateId, String name, String units, String range, String critical) {
        String sql = "INSERT INTO template_parameters(template_id, param_name, units, reference_range, critical_values) VALUES (?, ?, ?, ?, ?);";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, templateId);
            ps.setString(2, name);
            ps.setString(3, units);
            ps.setString(4, range);
            ps.setString(5, critical);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting template parameter for ID " + templateId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateTemplate(TestTemplate t) {
        String sqlUpdateTmpl = "UPDATE test_templates SET name=?, category=?, description=? WHERE id=?;";
        String sqlDeleteParams = "DELETE FROM template_parameters WHERE template_id = ?;";
        String sqlInsertParam = "INSERT INTO template_parameters(template_id, param_name, units, reference_range, critical_values) VALUES (?, ?, ?, ?, ?);";
        Connection conn = null;
        boolean success = false;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdateTmpl)) {
                psUpdate.setString(1, t.getName());
                psUpdate.setString(2, t.getCategory());
                psUpdate.setString(3, t.getDescription());
                psUpdate.setInt(4, t.getId());
                if (psUpdate.executeUpdate() == 0) {
                    throw new SQLException("Updating template failed.");
                }
            }
            try (PreparedStatement psDelete = conn.prepareStatement(sqlDeleteParams)) {
                psDelete.setInt(1, t.getId());
                psDelete.executeUpdate();
            }
            if (t.getParameters() != null && !t.getParameters().isEmpty()) {
                try (PreparedStatement psInsert = conn.prepareStatement(sqlInsertParam)) {
                    for (TestParameter p : t.getParameters()) {
                        psInsert.setInt(1, t.getId());
                        psInsert.setString(2, p.getParameterName());
                        psInsert.setString(3, p.getUnits());
                        psInsert.setString(4, p.getReferenceRange());
                        psInsert.setString(5, p.getCriticalValues());
                        psInsert.addBatch();
                    }
                    psInsert.executeBatch();
                }
            }
            conn.commit();
            success = true;
        } catch (SQLException e) {
            System.err.println("Error updating template: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) try {
                conn.rollback();
            } catch (SQLException ex) {
            }
            success = false;
        } finally {
            if (conn != null) try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException ex) {
            }
        }
        return success;
    }

    public boolean deleteTemplate(int templateId) {
        String sql = "DELETE FROM test_templates WHERE id = ?;";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, templateId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting template: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public TestTemplate findById(int templateId) {
        TestTemplate t = null;
        String sqlTmpl = "SELECT * FROM test_templates WHERE id = ?;";
        String sqlParams = "SELECT * FROM template_parameters WHERE template_id = ? ORDER BY id;";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement psTmpl = conn.prepareStatement(sqlTmpl)) {
            psTmpl.setInt(1, templateId);
            try (ResultSet rsTmpl = psTmpl.executeQuery()) {
                if (rsTmpl.next()) {
                    t = mapRowToTemplate(rsTmpl);
                } else {
                    System.err.println("Template ID not found: " + templateId);
                    return null;
                }
            }
            List<TestParameter> params = new ArrayList<>();
            try (PreparedStatement psParams = conn.prepareStatement(sqlParams)) {
                psParams.setInt(1, templateId);
                try (ResultSet rsParams = psParams.executeQuery()) {
                    while (rsParams.next()) {
                        params.add(mapRowToTemplateParameter(rsParams));
                    }
                }
            }
            t.setParameters(params);
        } catch (SQLException e) {
            System.err.println("Error finding template by ID: " + e.getMessage());
            e.printStackTrace();
            t = null;
        }
        return t;
    }

    public List<TestTemplate> listAll() {
        List<TestTemplate> list = new ArrayList<>();
        String sql = "SELECT id, name, category, description FROM test_templates ORDER BY name;";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowToTemplate(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listing templates: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public TestTemplate findByName(String name) {
        TestTemplate t = null;
        String sqlTmpl = "SELECT * FROM test_templates WHERE name = ?;";
        String sqlParams = "SELECT * FROM template_parameters WHERE template_id = ? ORDER BY id;";
        Connection conn = null;
        int templateId = -1;
        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement psTmpl = conn.prepareStatement(sqlTmpl)) {
                psTmpl.setString(1, name);
                try (ResultSet rsTmpl = psTmpl.executeQuery()) {
                    if (rsTmpl.next()) {
                        t = mapRowToTemplate(rsTmpl);
                        templateId = t.getId();
                    } else {
                        return null;
                    }
                }
            }
            if (t != null && templateId > 0) {
                List<TestParameter> params = new ArrayList<>();
                try (PreparedStatement psParams = conn.prepareStatement(sqlParams)) {
                    psParams.setInt(1, templateId);
                    try (ResultSet rsParams = psParams.executeQuery()) {
                        while (rsParams.next()) {
                            params.add(mapRowToTemplateParameter(rsParams));
                        }
                    }
                }
                t.setParameters(params);
                System.out.println("DEBUG (findByName): Found template '" + name + "' with " + params.size() + " parameters.");
            }
        } catch (SQLException e) {
            System.err.println("Error finding template by name: " + e.getMessage());
            e.printStackTrace();
            t = null;
        } finally {
            if (conn != null) try {
                conn.close();
            } catch (SQLException ex) {
            }
        }
        return t;
    }

    private TestTemplate mapRowToTemplate(ResultSet rs) throws SQLException {
        TestTemplate t = new TestTemplate();
        t.setId(rs.getInt("id"));
        t.setName(rs.getString("name"));
        t.setCategory(rs.getString("category"));
        t.setDescription(rs.getString("description"));
        return t;
    }

    private TestParameter mapRowToTemplateParameter(ResultSet rs) throws SQLException {
        TestParameter p = new TestParameter();
        p.setId(rs.getInt("id"));
        p.setParameterName(rs.getString("param_name"));
        p.setUnits(rs.getString("units"));
        p.setReferenceRange(rs.getString("reference_range"));
        p.setCriticalValues(rs.getString("critical_values"));
        return p;
    }
}
