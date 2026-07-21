package util;

/**
 * Clase de utilidad con los parametros de conexion a la base de datos.
 * Centraliza los datos para que Conexion y Respaldo no los repitan.
 */
public class ConfigBD {

    public static final String HOST = "localhost";              // (luiggi) el sistema opera de forma local
    public static final String PUERTO = "3306";
    public static final String NOMBRE_BD = "yogurin_bustamantedb";
    public static final String USUARIO = "root";                // (luiggi) usuario por defecto de XAMPP
    public static final String CLAVE = "";                      // (luiggi) XAMPP no trae clave por defecto

    private ConfigBD() {                                        // (luiggi) clase de utilidad, no se instancia
    }

    /** Arma la URL JDBC a partir de los parametros anteriores. */
    public static String getUrlJdbc() {
        return "jdbc:mysql://" + HOST + ":" + PUERTO + "/" + NOMBRE_BD;
    }
}
