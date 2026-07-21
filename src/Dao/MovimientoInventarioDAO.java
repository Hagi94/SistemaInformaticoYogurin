package Dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;
import util.Conexion;

/**
 * DAO de los movimientos de inventario (RF-27).
 * Permite auditar todas las entradas y salidas de productos registradas por el sistema.
 */
public class MovimientoInventarioDAO {

    private static final String[] COLUMNAS =
            {"ID", "Fecha", "Tipo", "Producto", "Cantidad", "Observacion"}; // (luiggi) cabecera de la tabla

    /** Historial completo de movimientos, del mas reciente al mas antiguo. */
    public DefaultTableModel listarHistorial() {

        String sql = "SELECT m.id, m.fecha, m.tipo, p.nombre, m.cantidad, m.observacion "
                   + "FROM movimientos_inventario m "
                   + "LEFT JOIN productos p ON p.id = m.producto_id "  // (luiggi) muestra el nombre, no el id
                   + "ORDER BY m.fecha DESC";

        return consultar(sql, null, null);
    }

    /** Historial filtrado por rango de fechas, para auditorias por periodo. */
    public DefaultTableModel listarPorRango(String fechaInicio, String fechaFin) {

        String sql = "SELECT m.id, m.fecha, m.tipo, p.nombre, m.cantidad, m.observacion "
                   + "FROM movimientos_inventario m "
                   + "LEFT JOIN productos p ON p.id = m.producto_id "
                   + "WHERE DATE(m.fecha) BETWEEN ? AND ? "            // (luiggi) parametros, no concatenacion
                   + "ORDER BY m.fecha DESC";

        return consultar(sql, fechaInicio, fechaFin);
    }

    /** Ejecuta la consulta y arma el modelo de tabla que consume la vista. */
    private DefaultTableModel consultar(String sql, String desde, String hasta) {

        DefaultTableModel modelo = new DefaultTableModel(null, COLUMNAS) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;                                   // (luiggi) historial de solo lectura
            }
        };

        try (PreparedStatement ps = Conexion.getInstancia().getConexion().prepareStatement(sql)) {

            if (desde != null && hasta != null) {
                ps.setString(1, desde);
                ps.setString(2, hasta);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    modelo.addRow(new Object[]{                 // (luiggi) agrega una fila por movimiento
                        rs.getInt("id"),
                        rs.getTimestamp("fecha"),
                        rs.getString("tipo"),
                        rs.getString("nombre"),
                        rs.getInt("cantidad"),
                        rs.getString("observacion")
                    });
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al listar movimientos: " + e.getMessage());
        }
        return modelo;
    }
}
