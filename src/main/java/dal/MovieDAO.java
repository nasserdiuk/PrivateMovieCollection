
package dal;

import model.Movie;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

    public class MovieDAO {
        private final ConnectionManager cm = new ConnectionManager();

        public int create(Movie movie) throws SQLException {
            String sql = """
            INSERT INTO dbo.Movie (title, imdbRating, personalRating, filePath, lastViewed)
            VALUES (?, ?, ?, ?, ?)
            """;

            try (Connection conn = cm.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, movie.getTitle());
                ps.setBigDecimal(2, new java.math.BigDecimal(String.valueOf(movie.getImdbRating())));
                ps.setBigDecimal(3, new java.math.BigDecimal(String.valueOf(movie.getPersonalRating())));
                ps.setString(4, movie.getFileLink());

                LocalDate lv = movie.getLastView();
                if (lv == null) ps.setNull(5, Types.DATE);
                else ps.setDate(5, Date.valueOf(lv));

                ps.executeUpdate();

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

        public void updatePersonalRating(int movieId, double personalRating) throws SQLException {
            String sql = "UPDATE dbo.Movie SET personalRating = ? WHERE id = ?";

            try (Connection conn = cm.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setBigDecimal(1, new java.math.BigDecimal(String.valueOf(personalRating)));
                ps.setInt(2, movieId);
                ps.executeUpdate();
            }
        }

        public void updateLastViewToToday(int movieId) throws SQLException {
            String sql = "UPDATE dbo.Movie SET lastViewed = ? WHERE id = ?";

            try (Connection conn = cm.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setDate(1, Date.valueOf(LocalDate.now()));
                ps.setInt(2, movieId);
                ps.executeUpdate();
            }
        }

        public List<Movie> searchByTitle(String titlePart) throws SQLException {
            String sql = """
            SELECT id, title, imdbRating, personalRating, fileLink, lastViewed
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

        private Movie mapMovie(ResultSet rs) throws SQLException {
            int id = rs.getInt("id");
            String title = rs.getString("title");
            double imdb = rs.getBigDecimal("imdbRating").doubleValue();
            String fileLink = rs.getString("filePath");

            Movie m = new Movie(id, title, imdb, fileLink);

            // personalRating may be -1
            double personal = rs.getBigDecimal("personalRating").doubleValue();
            m.setPersonalRating(personal);

            Date lv = rs.getDate("lastViewed");
            m.setLastView(lv == null ? null : lv.toLocalDate());

            return m;
        }
    }



