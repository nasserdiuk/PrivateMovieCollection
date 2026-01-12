package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Movie {

    private int id;
    private String title;
    private double imdbRating;
    private double personalRating;
    private String fileLink;
    private LocalDate lastView;

    private List<Category> categories = new ArrayList<>();

    public Movie(int id, String title, double imdbRating, String fileLink) {
        this.id = id;
        this.title = title;
        this.imdbRating = imdbRating;
        this.fileLink = fileLink;

        this.personalRating = -1; // movie has not been rated yet
        this.lastView = null;
    }


    public boolean hasPersonalRating() {
        return personalRating >= 0;
    }
    public double getPersonalRating() {
        return personalRating;
    }

    public int getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }

    public double getImdbRating() {
        return imdbRating;
    }

    public String getFileLink() {
        return fileLink;
    }
    public LocalDate getLastView() {
        return lastView;
    }
    public List<Category> getCategories() {
        return categories;
    }
    public void setImdbRating(double imdbRating) {
        this.imdbRating = imdbRating;
    }
    public void setPersonalRating(double personalRating) {
        this.personalRating = personalRating;
    }
    public void setFileLink(String fileLink) {
        this.fileLink = fileLink;
    }
    public void setLastView(LocalDate lastView) {
        this.lastView = lastView;
    }
    public void addCategory(Category category) {
        categories.add(category);
    }
    public void removeCategory(Category category) {
        categories.remove(category);
    }

    @Override
    public String toString() {
        if (personalRating > 0) {
            return title + "  |  Personal Rating: " + personalRating;
        } else {
            return title;
        }
    }


}


