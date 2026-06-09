/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import Utilidades.Constantes;

public class Conexion {

    private static final Logger LOGGER = Logger.getLogger(Conexion.class.getName());

    private static final String URL = "jdbc:mysql://"
            + Constantes.DB_HOST + ":"
            + Constantes.DB_PORT + "/"
            + Constantes.DB_NAME
            + "?useSSL=false&serverTimezone=UTC";

    private static final String USER = Constantes.DB_USER;

    private static final String PASSWORD = Constantes.DB_PASSWORD;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "No se pudo cargar el driver de MySQL", e);
        }
    }

    private Conexion() {
    }

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static boolean probarConexion() {
        try (Connection ignored = conectar()) {
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al conectar a la base de datos", e);
            return false;
        }
    }
}