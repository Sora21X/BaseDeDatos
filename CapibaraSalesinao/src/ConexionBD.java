import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    private static final String URL = "jdbc:sqlserver://localhost:1433;instanceName=SQLEXPRESS;databaseName=WEBREPORTS;encrypt=false";
    private static final String USER = "WRAdmin";
    private static final String PASS = "72055985";

    public static Connection conectar() {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Conexión exitosa a SQL Server");
            return conn;
        } catch (SQLException e) {
            System.out.println(" Error de conexión: " + e.getMessage());
            return null;
        }
    }
}
