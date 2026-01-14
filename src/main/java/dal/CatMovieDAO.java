package dal;

import java.sql.*;

public class CatMovieDAO {
    private final ConnectionManager cm = new ConnectionManager();

    // Create link Movie <-> Category
    public void addCategoryToMovie(int movieId, int categoryId) throws SQLException {
        String sql = """
            INSERT INTO dbo.CatMovie (MovieId, CategoryId)
            VALUES (?, ?)
            """;

        try (Connection conn = cm.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, movieId);
            ps.setInt(2, categoryId);
            ps.executeUpdate();
        }
    }

    public void removeCategoryFromMovie(int movieId, int categoryId) throws SQLException {
        String sql = """
            DELETE FROM dbo.CatMovie
            WHERE MovieId = ? AND CategoryId = ?
            """;

        try (Connection conn = cm.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, movieId);
            ps.setInt(2, categoryId);
            ps.executeUpdate();
        }
    }

    public void deleteLinksForMovie(int movieId) throws SQLException {
        String sql = "DELETE FROM dbo.CatMovie WHERE MovieId = ?";

        try (Connection conn = cm.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, movieId);
            ps.executeUpdate();
        }
    }
}
