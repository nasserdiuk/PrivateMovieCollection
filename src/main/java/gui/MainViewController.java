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
import java.awt.Desktop;
import java.io.IOException;
import java.time.LocalDate;



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

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a movie file");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("MP4 Videos (*.mp4)", "*.mp4", "*.MP4")
        );


        File file = fileChooser.showOpenDialog(movieListView.getScene().getWindow());
        if (file == null) return;

        // Ask for IMDb rating
        Double imdbRating;

        TextInputDialog imdbDialog = new TextInputDialog("7.0");
        imdbDialog.setTitle("IMDb Rating");
        imdbDialog.setHeaderText("Set IMDb rating");
        imdbDialog.setContentText("Enter IMDb rating (0–10):");

        Optional<String> imdbResult = imdbDialog.showAndWait();
        if (imdbResult.isEmpty()) return;

        try {
            imdbRating = Double.parseDouble(imdbResult.get().trim());
            if (imdbRating < 0 || imdbRating > 10) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid rating");
            alert.setHeaderText(null);
            alert.setContentText("IMDb rating must be between 0 and 10.");
            alert.showAndWait();
            return;
        }

        List<Category> selectedCategories =
                categoryListView.getSelectionModel().getSelectedItems();

        if (selectedCategories == null || selectedCategories.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Select category");
            alert.setHeaderText("No category selected");
            alert.setContentText(
                    "Please select one or more categories from the list\n" +
                            "BEFORE clicking 'Add Movie'."
            );
            alert.showAndWait();
            return;
        }


        Movie newMovie = movieService.addMovie(
                file.getName(),
                imdbRating,
                file.getAbsolutePath()
        );

        for (Category category : selectedCategories) {
            movieService.addCategoryToMovie(newMovie, category);
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
    @FXML
    public void onPlayClicked() {
        Movie selected = movieListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No movie selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a movie to play.");
            alert.showAndWait();
            return;
        }

        String path = selected.getFilePath();
        if (path == null || path.trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid file");
            alert.setHeaderText(null);
            alert.setContentText("The selected movie doesn't have a valid file path.");
            alert.showAndWait();
            return;
        }

        File file = new File(path);
        if (!file.exists()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("File not found");
            alert.setHeaderText(null);
            alert.setContentText("The file could not be found:\n" + path);
            alert.showAndWait();
            return;
        }

        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                throw new UnsupportedOperationException("Desktop integration is not supported on this platform.");
            }
            // update last viewed timestamp and refresh UI
            selected.setLastView(LocalDate.now());
            movieListView.refresh();
        } catch (IOException | UnsupportedOperationException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Cannot play movie");
            alert.setHeaderText("Failed to open the movie file");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}
