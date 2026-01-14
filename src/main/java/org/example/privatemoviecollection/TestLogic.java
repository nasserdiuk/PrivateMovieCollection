package org.example.privatemoviecollection;

import bll.MovieService;
import model.Category;
import model.Movie;

import java.util.ArrayList;
import java.util.List;

public class TestLogic {
    public static void main(String[] args) {

        MovieService service = new MovieService();

        Category action = service.addCategory("Action");
        Category drama = service.addCategory("Drama");



        List<Category> selected = new ArrayList<>();
        selected.add(action);

        List<Movie> filtered = service.filterMovies("dark", 8.0, selected);

        System.out.println(filtered.size()); // should be 1
        System.out.println(filtered.get(0).getTitle()); // The Dark Knight;
    }
}