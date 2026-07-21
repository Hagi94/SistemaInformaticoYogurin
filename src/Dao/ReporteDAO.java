package Dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;
import util.Conexion;

/**
 * DAO de reportes (RF-19, RF-20, RF-21).
 * Todas las consultas usan PreparedStatement parametrizado, lo que ademas previene inyeccion SQL.
 */
public class ReporteDAO {

    private static final String[] COL_VENTAS =
            {"ID", "Fecha", "Cliente", "Total"};
    private static final String[] COL_PRODUCCION =
            {"ID", "Fecha", "Lote", "Sabor", "Cantidad", "Precio"};
    private static final String[] COL_STOCK =
            {"ID", "Producto", "Stock actual", "Stock minimo"};
    private static final String[] COL_INSUMOS =
            {"ID", "Insumo", "Unidad", "Stock actual", "Stock minimo"};
    private static final String[] COL_CIERRE =
            {"Fecha", "N ventas", "Total recaudado"};

    private static final String SELECT_VENTAS =
            "SELECT v.id, v.fecha, c.nombre, v.total_pagar "
          + "FROM ventas v LEFT JOIN clientes c ON v.cliente_id = c.id ";

    /** Ventas del dia actual (RF-20). */
    public DefaultTableModel ventasDia() {
        return ventasPorRango(null, null, "DATE(v.fecha)=CURDATE()");
    }

    /** Ventas de los ultimos 7 dias (RF-20). */
    public DefaultTableModel ventasSemana() {
        return ventasPorRango(null, null, "v.fecha >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)");
    }

    /** Ventas del mes en curso (RF-20). */
    public DefaultTableModel ventasMes() {
        return ventasPorRango(null, null,
                "MONTH(v.fecha)=MONTH(CURDATE()) AND YEAR(v.fecha)=YEAR(CURDATE())");
    }

    /** Ventas entre dos fechas elegidas por el usuario (RF-20). */
    public DefaultTableModel ventasPorRango(String fechaInicio, String fechaFin) {
        return ventasPorRango(fechaInicio, fechaFin, "DATE(v.fecha) BETWEEN ? AND ?");
    }

    /** Reporte de lotes producidos. */
    public DefaultTableModel reporteProduccion() {

        DefaultTableModel modelo = crearModelo(COL_PRODUCCION);
        String sql = "SELECT id,fecha,lote,sabor,cantidad,precio_venta FROM produccion ORDER BY fecha DESC";

        try (PreparedStatement ps = Conexion.getInstancia().getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                modelo.addRow(new Object[]{
                    rs.getInt("id"), rs.getString("fecha"), rs.getString("lote"),
                    rs.getString("sabor"), rs.getInt("cantidad"), rs.getDouble("precio_venta")
                });
            }

        } catch (SQLException e) {
            System.err.println("Error en reporte de produccion: " + e.getMessage());
        }
        return modelo;
    }

    /** Productos que llegaron o bajaron de su stock minimo (RF-18). */
    public DefaultTableModel stockCritico() {

        DefaultTableModel modelo = crearModelo(COL_STOCK);
        String sql = "SELECT id,nombre,stock,stock_minimo FROM productos "
                   + "WHERE estado=1 AND stock <= stock_minimo ORDER BY stock"; // (luiggi) usa el minimo real, no un numero fijo

        try (PreparedStatement ps = Conexion.getInstancia().getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                modelo.addRow(new Object[]{
                    rs.getInt("id"), rs.getString("nombre"),
                    rs.getInt("stock"), rs.getInt("stock_minimo")
                });
            }

        } catch (SQLException e) {
            System.err.println("Error en stock critico: " + e.getMessage());
        }
        return modelo;
    }

    /** Insumos que llegaron o bajaron de su stock minimo (RF-18). */
    public DefaultTableModel insumosCriticos() {

        DefaultTableModel modelo = crearModelo(COL_INSUMOS);
        String sql = "SELECT id,nombre,unidad,stock_actual,stock_minimo FROM insumos "
                   + "WHERE stock_actual <= stock_minimo ORDER BY nombre";

        try (PreparedStatement ps = Conexion.getInstancia().getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                modelo.addRow(new Object[]{
                    rs.getInt("id"), rs.getString("nombre"), rs.getString("unidad"),
                    rs.getDouble("stock_actual"), rs.getDouble("stock_minimo")
                });
            }

        } catch (SQLException e) {
            System.err.println("Error en insumos criticos: " + e.getMessage());
        }
        return modelo;
    }

    /** Resumen de caja del dia: cantidad de ventas y total recaudado (RF-21). */
    public DefaultTableModel cierreCaja() {

        DefaultTableModel modelo = crearModelo(COL_CIERRE);
        String sql = "SELECT CURDATE() AS fecha, COUNT(*) AS nventas, "
                   + "IFNULL(SUM(total_pagar),0) AS total "
                   + "FROM ventas WHERE DATE(fecha)=CURDATE()";

        try (PreparedStatement ps = Conexion.getInstancia().getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                modelo.addRow(new Object[]{
                    rs.getString("fecha"), rs.getInt("nventas"), rs.getDouble("total")
                });
            }

        } catch (SQLException e) {
            System.err.println("Error en cierre de caja: " + e.getMessage());
        }
        return modelo;
    }

    /** Guarda el cierre de caja del dia en la tabla 'cierre_caja' (RF-21). */
    public boolean guardarCierreCaja(String observacion) {

        String sql = "INSERT INTO cierre_caja(fecha,total_ventas,observacion) "
                   + "SELECT CURDATE(), IFNULL(SUM(total_pagar),0), ? "
                   + "FROM ventas WHERE DATE(fecha)=CURDATE()";

        try (PreparedStatement ps = Conexion.getInstancia().getConexion().prepareStatement(sql)) {

            ps.setString(1, observacion);
            return ps.executeUpdate() > 0;                  // (luiggi) deja el cierre archivado del dia

        } catch (SQLException e) {
            System.err.println("Error al guardar cierre de caja: " + e.getMessage());
            return false;
        }
    }

    /** Total recaudado hoy, para mostrarlo como indicador en el dashboard (RF-19). */
    public double totalRecaudadoHoy() {

        String sql = "SELECT IFNULL(SUM(total_pagar),0) AS total FROM ventas WHERE DATE(fecha)=CURDATE()";

        try (PreparedStatement ps = Conexion.getInstancia().getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getDouble("total") : 0;

        } catch (SQLException e) {
            System.err.println("Error al calcular el total del dia: " + e.getMessage());
            return 0;
        }
    }

    // ------------------------------------------------------------------
    // Metodos privados de apoyo
    // ------------------------------------------------------------------

    /** Ejecuta el reporte de ventas aplicando la condicion recibida. */
    private DefaultTableModel ventasPorRango(String desde, String hasta, String condicion) {

        DefaultTableModel modelo = crearModelo(COL_VENTAS);
        String sql = SELECT_VENTAS + "WHERE " + condicion + " ORDER BY v.fecha DESC";

        try (PreparedStatement ps = Conexion.getInstancia().getConexion().prepareStatement(sql)) {

            if (desde != null && hasta != null) {
                ps.setString(1, desde);                     // (luiggi) fechas como parametros, no concatenadas
                ps.setString(2, hasta);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    modelo.addRow(new Object[]{
                        rs.getInt("id"), rs.getString("fecha"),
                        rs.getString("nombre"), rs.getDouble("total_pagar")
                    });
                }
            }

        } catch (SQLException e) {
            System.err.println("Error en reporte de ventas: " + e.getMessage());
        }
        return modelo;
    }

    /** Crea un modelo de tabla de solo lectura con las columnas indicadas. */
    private DefaultTableModel crearModelo(String[] columnas) {
        return new DefaultTableModel(null, columnas) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;                               // (luiggi) los reportes no se editan a mano
            }
        };
    }
}
