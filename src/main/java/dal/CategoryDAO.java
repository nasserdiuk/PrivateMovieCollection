package dal;

import java.sql.PreparedStatement;
import java.sql.Statement;

public class CategoryDAO {
    private final ConnectionManager connectionManager = new ConnectionManager();
    public void addCategory(String name) {
        try (var connection = connectionManager.getConnection()) {
            Statement statement = connection.createStatement();
            String sql = "INSERT INTO Categories (Name) VALUES (?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, name);
            preparedStatement.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
