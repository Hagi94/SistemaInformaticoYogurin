package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase de utilidad que gestiona la conexion a MySQL.
 * Implementa el patron Singleton: una unica instancia viva durante toda la sesion.
 */
public class Conexion {

    private static Conexion instancia;                          // (luiggi) unica instancia del Singleton
    private Connection conexion;                                // (luiggi) conexion viva reutilizada

    /** Constructor privado: nadie fuera de la clase puede crear otra instancia. */
    private Conexion() {
    }

    /** Devuelve siempre la misma instancia (patron Singleton). */
    public static synchronized Conexion getInstancia() {
        if (instancia == null) {                                // (luiggi) solo la crea la primera vez
            instancia = new Conexion();
        }
        return instancia;
    }

    /** Entrega la conexion activa; si esta cerrada o nula la vuelve a abrir. */
    public Connection getConexion() {
        try {
            if (conexion == null || conexion.isClosed()) {      // (luiggi) reabre si se perdio la conexion
                Class.forName("com.mysql.cj.jdbc.Driver");      // (luiggi) carga el driver JDBC de MySQL
                conexion = DriverManager.getConnection(         // (luiggi) toma los datos de ConfigBD
                        ConfigBD.getUrlJdbc(), ConfigBD.USUARIO, ConfigBD.CLAVE);
            }
        } catch (ClassNotFoundException | SQLException e) {
            throw new IllegalStateException(                    // (luiggi) falla fuerte: sin BD no hay sistema
                    "No se pudo conectar a la base de datos: " + e.getMessage(), e);
        }
        return conexion;
    }

    /** Cierra la conexion al salir del sistema. */
    public void cerrar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();                               // (luiggi) libera la conexion en MySQL
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexion: " + e.getMessage());
        } finally {
            conexion = null;                                    // (luiggi) permite reabrir despues
        }
    }
}
