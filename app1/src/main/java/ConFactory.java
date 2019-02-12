import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConFactory {

    public static Connection getConnectionPostgres() throws SQLException {
        return DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/rmi_concurrence_id",
                "postgres","postgres");
    }

}
