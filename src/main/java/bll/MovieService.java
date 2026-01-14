package bll;

import dal.MovieDAO;
import model.Category;
import model.Movie;
import dal.CategoryDAO;
import dal.CatMovieDAO;

import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import model.WarningType;
import java.time.LocalDate;

public class MovieService {

    private final List<Movie> movies = new ArrayList<>();
    private final List<Category> categories = new ArrayList<>();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final MovieDAO movieDAO = new MovieDAO();



    private final CatMovieDAO catMovieDAO = new CatMovieDAO();

    public void addCategoryToMovie(Movie movie, Category category) {

        movie.addCategory(category);


        try {
            catMovieDAO.addCategoryToMovie(movie.getId(), category.getId());
        } catch (SQLException ignored) {}

    }

    public void removeCategoryFromMovie(Movie movie, Category category) {
        movie.removeCategory(category);
        try {
            catMovieDAO.removeCategoryFromMovie(movie.getId(), category.getId());
        } catch (SQLException ignored) {

        }
    }



    public Movie addMovie(String title, double imdbRating, String fileLink) throws SQLException {

        double notRated = -1.0;
        int dbId = movieDAO.create(title, imdbRating, notRated, fileLink);

        Movie movie = new Movie(dbId, title, imdbRating, fileLink);
        // movie already starts with personalRating = -1 in the constructor

        movies.add(movie);
        return movie;
    }




    public void deleteMovie(Movie movie) {

        if (movie == null) return;

        try {
            // delete in DB (also deletes links in CatMovie inside DAO)
            movieDAO.deleteById(movie.getId());

            // delete in memory
            movies.remove(movie);

        } catch (SQLException ignored) {

        }
    }

    public Category addCategory(String name) {
        try {
            int id = categoryDAO.create(name);
            Category created = new Category(id, name);
            categories.add(created);
            return created;
        } catch (SQLException ignored) {
            return null;
        }
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public List<Movie> filterByTitle(String text) {
        List<Movie> result = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {  //shows all movies when user didnt type anything
            result.addAll(movies);
            return result;
        }
        String search = text.toLowerCase(); // if text starts from lower case show results for search

        for (Movie movie : movies) {
            if (movie.getTitle().toLowerCase().contains(search)) {
                result.add(movie);
            }
        }
        return result;
    }

    public List<Movie> filterByMinImdb(double minRating) {  // returns all movies where rating is at least the number user typed
        List<Movie> result = new ArrayList<>();

        for (Movie movie : movies) {  //  for-each loop
            if (movie.getImdbRating() >= minRating) {
                result.add(movie);
            }
        }
        return result;
    }

    public List<Movie> filterByCategory(Category category) {  // only filters movies with one category
        List<Movie> result = new ArrayList<>();
        if (category == null) {
            result.addAll(movies);
            return result;
        }

        for (Movie movie : movies) {
            if (movie.getCategories().contains(category)) {
                result.add(movie);
            }
        }
        return result;
    }


    public List<Movie> filterByCategories(List<Category> selectedCategories) {
        List<Movie> result = new ArrayList<>();

        // If user did not select any categories show all movies

        if (selectedCategories == null || selectedCategories.isEmpty()) {
            result.addAll(movies);
            return result;
        }

        for (Movie movie : movies) {
            for (Category category : selectedCategories) {
                if (movie.getCategories().contains(category)) {
                    result.add(movie);
                    break;    // stop checking this movie
                }
            }
        }
        return result;
    }

    public List<Movie> filterMovies(String titleText, Double minRating, List<Category> selectedCategories) {
        List<Movie> result = new ArrayList<>();

        String search = null;  //prepare title search
        if (titleText != null && !titleText.isEmpty())
        {
            search = titleText.toLowerCase();
        }
        boolean useCategoryFilter = selectedCategories != null && !selectedCategories.isEmpty();
        boolean useImdbFilter = minRating != null;

        for (Movie movie : movies) {
            // title filter
            if (search != null && !movie.getTitle().toLowerCase().contains(search)) {
                continue;
            }
            // imdb rating filter
            if (useImdbFilter && movie.getImdbRating() < minRating) {
                continue;
            }
            // category filter
            if (useCategoryFilter) {
                boolean matchesAtLeastOne = false;
                for (Category category : selectedCategories)
                {
                    if (movie.getCategories().contains(category)) {
                        matchesAtLeastOne = true;
                        break;
                    }
                }
                if (!matchesAtLeastOne) {
                    continue;
                }
            }
            result.add(movie);
        }
        return result;


    }

    public List<Category> getCategories() {
        return categories;
    }




    public void removeCategory(Category category) {
        if (category == null) return;

        // Remove category from all movies (DB + memory)
        for (Movie movie : movies) {
            if (movie.getCategories().contains(category)) {
                removeCategoryFromMovie(movie, category); // uses your existing method
            }
        }

        // Delete category itself from DB
        try {
            categoryDAO.deleteById(category.getId());
        } catch (SQLException ignored) {
            // fail silently
        }

        // Remove from in-memory list
        categories.remove(category);
    }




    public WarningType getWarningType(Movie movie) {
        LocalDate last = movie.getLastView();
        boolean oldOrNever = (last == null) || last.isBefore(LocalDate.now().minusYears(2));

        if (!oldOrNever) {
            return WarningType.NONE;
        }
        if (!movie.hasPersonalRating()) {
            return WarningType.OLD_NO_RATING;
        } else if (movie.getPersonalRating() < 6) {
            return WarningType.OLD_LOW_RATING;
        } else {
            return WarningType.NONE;
        }
    }
    public boolean shouldWarn(Movie movie) {
        return getWarningType(movie) != WarningType.NONE;
    }

    public void setPersonalRating(Movie movie, double rating) {
        movie.setPersonalRating(rating);

        try {
            movieDAO.updatePersonalRating(movie.getId(), rating);
        } catch (SQLException ignored) {

        }
    }

    public void markAsViewed(Movie movie) {
        LocalDate now = LocalDate.now();
        movie.setLastView(now);

        try {
            movieDAO.updateLastView(movie.getId(), now);
        } catch (SQLException ignored) {
        }
    }

    public void loadMovies() {
        try {
            movies.clear();
            movies.addAll(movieDAO.getAll());

            for (Movie m : movies) {
                m.getCategories().clear();
                m.getCategories().addAll(catMovieDAO.getCategoriesForMovie(m.getId()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadCategories() {
        try {
            categories.clear();
            categories.addAll(categoryDAO.getAll());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}


