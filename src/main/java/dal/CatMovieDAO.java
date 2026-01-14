package dal;

import model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CatMovieDAO {
    private final ConnectionManager cm = new ConnectionManager();


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


    public List<Category> getCategoriesForMovie(int movieId) throws SQLException {
        String sql = """
            SELECT c.id, c.name
            FROM dbo.Category c
            JOIN dbo.CatMovie cm ON cm.CategoryId = c.id
            WHERE cm.MovieId = ?
            ORDER BY c.name
            """;

        List<Category> result = new ArrayList<>();

        try (Connection conn = cm.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, movieId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Category(rs.getInt("id"), rs.getString("name")));
                }
            }
        }

        return result;
    }
}
