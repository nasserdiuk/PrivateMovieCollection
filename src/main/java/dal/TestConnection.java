package dal;
import dal.ConnectionManager;
import java.sql.Connection;

    public class TestConnection {
        public static void main(String[] args) {
            try (Connection conn = new ConnectionManager().getConnection()) {

                System.out.println(" CONNECTED TO DATABASE");
                System.out.println("DB name: " + conn.getCatalog());
                System.out.println("DB product: " +
                        conn.getMetaData().getDatabaseProductName());

            } catch (Exception e) {
                System.out.println("❌ CONNECTION FAILED");
                e.printStackTrace();
            }
        }
    }


