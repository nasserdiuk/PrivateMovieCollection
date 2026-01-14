package dal;

import model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    private final ConnectionManager cm = new ConnectionManager();

    // Create category and return generated id
    public int create(String name) throws SQLException {

        String sql = """
        INSERT INTO dbo.Category (name)
        VALUES (?)
        """;

        try (Connection conn = cm.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);

            int rows = ps.executeUpdate();
            if (rows != 1) {
                throw new SQLException("Insert failed, rows affected: " + rows);
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }

            // Developer error – should never happen if DB is correct
            throw new SQLException("No generated key returned for Category insert.");
        }
    }



    public List<Category> getAll() throws SQLException {
        String sql = """
            SELECT id, name
            FROM dbo.Category
            ORDER BY name
            """;

        List<Category> categories = new ArrayList<>();

        try (Connection conn = cm.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                categories.add(mapCategory(rs));
            }
        }
        return categories;
    }

    public Category getById(int categoryId) throws SQLException {
        String sql = """
            SELECT id, name
            FROM dbo.Category
            WHERE id = ?
            """;

        try (Connection conn = cm.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, categoryId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapCategory(rs);
                return null;
            }
        }
    }

    public List<Category> searchByName(String namePart) throws SQLException {
        String sql = """
            SELECT id, name
            FROM dbo.Category
            WHERE name LIKE ?
            ORDER BY name
            """;

        List<Category> categories = new ArrayList<>();

        try (Connection conn = cm.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + namePart + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categories.add(mapCategory(rs));
                }
            }
        }

        return categories;
    }

    public boolean deleteById(int categoryId) throws SQLException {
        // Must delete links first (CatMovie) or the FK will block deletion.
        String deleteLinks = "DELETE FROM dbo.CatMovie WHERE CategoryId = ?";
        String deleteCategory = "DELETE FROM dbo.Category WHERE id = ?";

        try (Connection conn = cm.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(deleteLinks);
                 PreparedStatement ps2 = conn.prepareStatement(deleteCategory)) {

                ps1.setInt(1, categoryId);
                ps1.executeUpdate();

                ps2.setInt(1, categoryId);
                int rows = ps2.executeUpdate();

                conn.commit();
                return rows == 1;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private Category mapCategory(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        return new Category(id, name);
    }

}
