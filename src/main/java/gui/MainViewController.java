package gui;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextInputDialog;
import model.Category;
import bll.MovieService;
import model.Movie;

import java.sql.SQLException;
import java.util.List;
import bll.MovieService;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.Optional;
import javafx.scene.control.Alert;



public class MainViewController {

    private final MovieService movieService = new MovieService();

    @FXML private TextField txtTitle;
    @FXML private TextField txtMinRating;
    @FXML
    private ListView<Category> categoryListView;
    @FXML
    private ListView<Movie> movieListView;
    @FXML
    public void initialize() {
        categoryListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

       /* //  TEST DATA: categories
        movieService.addCategory("Action");
        movieService.addCategory("Drama");
        movieService.addCategory("Comedy");

        //  TEST DATA: movies
        movieService.addMovie("The Dark Knight", 9.0, "file.mp4");
        movieService.addMovie("Shawshank", 9.3, "file2.mp4");
        movieService.addMovie("Some Comedy", 6.0, "file3.mp4");

        //  Connect categories to movies
        Category action = movieService.getCategories().get(0);
        Category drama  = movieService.getCategories().get(1);
        Category comedy = movieService.getCategories().get(2);

        Movie m1 = movieService.getMovies().get(0);
        Movie m2 = movieService.getMovies().get(1);
        Movie m3 = movieService.getMovies().get(2);

        movieService.addCategoryToMovie(m1, action);
        movieService.addCategoryToMovie(m1, drama);

        movieService.addCategoryToMovie(m2, drama);

        movieService.addCategoryToMovie(m3, comedy); */

        //  Show lists in UI
        categoryListView.getItems().setAll(movieService.getCategories());
        movieListView.getItems().setAll(movieService.getMovies());



    }

    @FXML
    public void onFilterClicked() {
        String titleText = txtTitle.getText();
        String minRatingText = txtMinRating.getText();

        Double minRating = null;
        if (minRatingText != null && !minRatingText.trim().isEmpty()) {
            try {
                minRating = Double.parseDouble(minRatingText);
            } catch (NumberFormatException e) {
                System.out.println("Min Imdb must be a number");
                return;
            }
        }
        List<Category> selectedCategories = categoryListView.getSelectionModel().getSelectedItems();
        List<Movie> filtered = movieService.filterMovies(titleText, minRating, selectedCategories);
        movieListView.getItems().setAll(filtered);
    }

    @FXML
    public void onClearClicked() {
        txtTitle.clear();
        txtMinRating.clear();
        categoryListView.getSelectionModel().clearSelection();
        movieListView.getItems().setAll(movieService.getMovies());
    }

    @FXML
    public void onDeleteMovieClicked() {
        Movie selected = movieListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            System.out.println("Select a movie first");
            return;
        }

        movieService.deleteMovie(selected);  
        movieListView.getItems().setAll(movieService.getMovies());
    }

    @FXML
    public void onAddMovieClicked() throws SQLException {
        MovieService movieService = new MovieService();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a movie file");

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "MP4 Videos (*.mp4)", "*.mp4"
                )
        );

        File file = fileChooser.showOpenDialog(movieListView.getScene().getWindow());
        if (file == null) {
            return; // user cancelled
        }

        int n = movieService.getMovies().size() + 1;

        Movie newMovie = movieService.addMovie(
                file.getName(),   // title = filename
                7.0,              // test IMDb rating
                file.getAbsolutePath()
        );

        Category selectedCategory =
                categoryListView.getSelectionModel().getSelectedItem();
        if (selectedCategory != null) {
            movieService.addCategoryToMovie(newMovie, selectedCategory);

        }

        movieListView.getItems().setAll(movieService.getMovies());
    }


    @FXML
    public void onSetRatingClicked() {
        Movie selected = movieListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            System.out.println("Select a movie first");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Personal Rating");
        dialog.setHeaderText("Set personal rating for: " + selected.getTitle());
        dialog.setContentText("Enter rating (0-10):");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;

        try {
            double rating = Double.parseDouble(result.get().trim());
            if (rating < 0 || rating > 10) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Invalid rating");
                alert.setHeaderText(null);
                alert.setContentText("Rating must be between 0 and 10.");
                alert.showAndWait();
                return;
            }

            selected.setPersonalRating(rating);

            // refresh list
            movieListView.refresh();

        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid input");
            alert.setHeaderText(null);
            alert.setContentText("Please enter a number.");
            alert.showAndWait();
        }
    }
    
    @FXML 
    public void onAddCategoryClicked() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Category");
        dialog.setHeaderText("Enter new category name:");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return; // user cancelled
        }

        String name = result.get().trim();
        if (name.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid input");
            alert.setHeaderText(null);
            alert.setContentText("Category name cannot be empty.");
            alert.showAndWait();
            return;
        }

        // prevent duplicates
        for (Category c : movieService.getCategories()) {
            if (c.getName().equalsIgnoreCase(name)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Duplicate category");
                alert.setHeaderText(null);
                alert.setContentText("A category with this name already exists.");
                alert.showAndWait();
                return;
            }
        }

        Category created = movieService.addCategory(name);

        // refresh and select the newly added category
        categoryListView.getItems().setAll(movieService.getCategories());
        categoryListView.getSelectionModel().select(created);
    }
    

    @FXML
    public void onDeleteCategoryClicked() {
        Category selected = categoryListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            System.out.println("Select a category first");
            return;
        }

        // Remove this category from all movies
        for (Movie m : movieService.getMovies()) {
            if (m.getCategories().contains(selected)) {
                movieService.removeCategoryFromMovie(m, selected);
            }
        }

        // Remove the category itself
        movieService.removeCategory(selected);

        // Update UI lists
        categoryListView.getItems().setAll(movieService.getCategories());
        movieListView.refresh();
    }
}
