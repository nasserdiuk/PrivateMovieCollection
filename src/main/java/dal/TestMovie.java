package dal;

import model.Movie;

public class TestMovie {
    public static void main(String[] args) throws Exception {
        MovieDAO dao = new MovieDAO();

        Movie m = new Movie(0, "Inception", 8.8, "D:\\Movies\\Inception.mp4");
        int newId = dao.create(m);
        System.out.println("Inserted movie id = " + newId);

        dao.updateLastViewToToday(newId);
        dao.updatePersonalRating(newId, 9.5);

        System.out.println("\nAll movies:");
        dao.getAll().forEach(System.out::println);
    }
}
