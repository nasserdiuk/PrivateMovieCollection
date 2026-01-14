package dal;

import model.Movie;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MovieDAO {

    private final ConnectionManager cm = new ConnectionManager();


    public int create(String title,
                      double imdbRating,
                      Double personalRating,
                      String filePath) throws SQLException {

        String sql = """
        INSERT INTO dbo.Movie (title, imdbRating, personalRating, filePath)
        VALUES (?, ?, ?, ?)
        """;

        try (Connection conn = cm.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, title);
            ps.setDouble(2, imdbRating);

            if (personalRating == null) {
                ps.setNull(3, Types.DECIMAL);
            } else {
                ps.setBigDecimal(3, BigDecimal.valueOf(personalRating));
            }

            ps.setString(4, filePath);

            int rows = ps.executeUpdate();
            if (rows != 1) {
                throw new SQLException("Insert failed, rows affected: " + rows);
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            throw new SQLException("No generated key returned for Movie insert.");
        }
    }


    public List<Movie> getAll() throws SQLException {
        String sql = """
            SELECT id, title, imdbRating, personalRating, filePath, lastViewed
            FROM dbo.Movie
            ORDER BY id DESC
            """;

        List<Movie> movies = new ArrayList<>();

        try (Connection conn = cm.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                movies.add(mapMovie(rs));
            }
        }
        return movies;
    }

    public Movie getById(int movieId) throws SQLException {
        String sql = """
            SELECT id, title, imdbRating, personalRating, filePath, lastViewed
            FROM dbo.Movie
            WHERE id = ?
            """;

        try (Connection conn = cm.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, movieId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapMovie(rs) : null;
            }
        }
    }

    public List<Movie> searchByTitle(String titlePart) throws SQLException {
        String sql = """
            SELECT id, title, imdbRating, personalRating, filePath, lastViewed
            FROM dbo.Movie
            WHERE title LIKE ?
            ORDER BY title
            """;

        List<Movie> movies = new ArrayList<>();

        try (Connection conn = cm.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + titlePart + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) movies.add(mapMovie(rs));
            }
        }
        return movies;
    }

    public void updatePersonalRating(int movieId, Double personalRating) throws SQLException {
        String sql = "UPDATE dbo.Movie SET personalRating = ? WHERE id = ?";

        try (Connection conn = cm.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (personalRating == null) {
                ps.setNull(1, Types.DECIMAL);
            } else {
                ps.setBigDecimal(1, BigDecimal.valueOf(personalRating));
            }
            ps.setInt(2, movieId);

            ps.executeUpdate();
        }
    }

    public void updateLastView(int movieId, LocalDate lastView) throws SQLException {
        String sql = "UPDATE dbo.Movie SET lastViewed = ? WHERE id = ?";

        try (Connection conn = cm.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (lastView == null) {
                ps.setNull(1, Types.TIMESTAMP);
            } else {
                ps.setTimestamp(1, Timestamp.valueOf(lastView.atStartOfDay()));
            }
            ps.setInt(2, movieId);

            ps.executeUpdate();
        }
    }

    public boolean deleteById(int movieId) throws SQLException {
        String deleteLinks = "DELETE FROM dbo.CatMovie WHERE MovieId = ?";
        String deleteMovie = "DELETE FROM dbo.Movie WHERE id = ?";

        try (Connection conn = cm.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(deleteLinks);
                 PreparedStatement ps2 = conn.prepareStatement(deleteMovie)) {

                ps1.setInt(1, movieId);
                ps1.executeUpdate();

                ps2.setInt(1, movieId);
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

    private Movie mapMovie(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String title = rs.getString("title");

        BigDecimal imdbBD = rs.getBigDecimal("imdbRating");
        double imdb = (imdbBD == null) ? 0.0 : imdbBD.doubleValue();

        String filePath = rs.getString("filePath");

        Movie m = new Movie(id, title, imdb, filePath);


        BigDecimal personalBD = rs.getBigDecimal("personalRating");
        if (personalBD != null) {
            double pr = personalBD.doubleValue();
            if (pr >= 0) {
                m.setPersonalRating(pr);
            } else {
                m.setPersonalRating(-1); // keep "not rated yet"
            }
        } else {
            m.setPersonalRating(-1);
        }

        // lastViewed can be null
        Timestamp ts = rs.getTimestamp("lastViewed");
        m.setLastView(ts != null ? ts.toLocalDateTime().toLocalDate() : null);

        return m;
    }

}
