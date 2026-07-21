package Dao;

import Modelo.ItemVenta;
import Modelo.Venta;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import util.Conexion;

/**
 * DAO de la entidad Venta.
 * El registro y la anulacion de ventas se ejecutan dentro de una transaccion JDBC:
 * o se guardan todos los pasos, o no se guarda ninguno (rollback).
 */
public class VentaDAO {

    private static final String SQL_VENTA =
            "INSERT INTO ventas(fecha,cliente_id,total,descuento,total_pagar) VALUES(NOW(),?,?,?,?)";

    private static final String SQL_DETALLE =
            "INSERT INTO detalle_venta(venta_id,producto_id,cantidad,precio_unitario,subtotal) VALUES(?,?,?,?,?)";

    private static final String SQL_DETALLE_LOTE =
            "INSERT INTO detalle_venta_lote(venta_id,produccion_id,lote,cantidad,precio_unitario,subtotal) "
            + "VALUES(?,?,?,?,?,?)";

    // Descuenta stock solo si alcanza: la condicion stock>=? evita dejar stock negativo
    private static final String SQL_DESCONTAR_PRODUCTO =
            "UPDATE productos SET stock = stock - ? WHERE id=? AND stock >= ?";

    private static final String SQL_DESCONTAR_LOTE =
            "UPDATE produccion SET cantidad = cantidad - ? WHERE id=? AND cantidad >= ?";

    private static final String SQL_MOVIMIENTO =
            "INSERT INTO movimientos_inventario(tipo,producto_id,cantidad,observacion) VALUES(?,?,?,?)";

    /**
     * Registra la cabecera de la venta y todo su detalle dentro de una unica transaccion (RF-13 al RF-15).
     *
     * @return el id de la venta generada, o 0 si la operacion fue revertida.
     */
    public int registrarVenta(Venta v, List<ItemVenta> items) {

        if (items == null || items.isEmpty()) {
            System.err.println("La venta no tiene items");
            return 0;                                        // (luiggi) no registra ventas vacias
        }

        Connection con = Conexion.getInstancia().getConexion();
        int idVenta = 0;

        try {
            con.setAutoCommit(false);                        // (luiggi) inicia la transaccion

            idVenta = insertarCabecera(con, v);              // (luiggi) guarda la venta y obtiene su id

            for (ItemVenta item : items) {
                if (item.esProducto()) {
                    guardarLineaProducto(con, idVenta, item); // (luiggi) detalle + descuento + movimiento
                } else {
                    guardarLineaLote(con, idVenta, item);     // (luiggi) detalle y descuento del lote
                }
            }

            con.commit();                                    // (luiggi) confirma todos los pasos juntos
            return idVenta;

        } catch (SQLException e) {
            revertir(con);                                   // (luiggi) deshace todo si algo fallo
            System.err.println("Venta revertida: " + e.getMessage());
            return 0;

        } finally {
            restaurarAutoCommit(con);                        // (luiggi) devuelve la conexion a su modo normal
        }
    }

    /**
     * Anula una venta registrada por error y devuelve el stock descontado (RF-25).
     * Tambien se ejecuta como transaccion completa.
     */
    public boolean anularVenta(int idVenta) {

        Connection con = Conexion.getInstancia().getConexion();

        try {
            con.setAutoCommit(false);                        // (luiggi) inicia la transaccion de anulacion

            devolverStockProductos(con, idVenta);            // (luiggi) reingresa lo vendido al inventario
            devolverStockLotes(con, idVenta);

            ejecutar(con, "DELETE FROM detalle_venta WHERE venta_id=?", idVenta);
            ejecutar(con, "DELETE FROM detalle_venta_lote WHERE venta_id=?", idVenta);
            ejecutar(con, "DELETE FROM ventas WHERE id=?", idVenta);

            con.commit();                                    // (luiggi) confirma la anulacion completa
            return true;

        } catch (SQLException e) {
            revertir(con);
            System.err.println("Anulacion revertida: " + e.getMessage());
            return false;

        } finally {
            restaurarAutoCommit(con);
        }
    }

    /**
     * Cabecera de una venta para imprimir el comprobante (RF-16).
     *
     * @return {numero, fecha, cliente, dni, total, descuento, totalPagar} o null si no existe.
     */
    public String[] cabeceraComprobante(int idVenta) {

        String sql = "SELECT v.id, v.fecha, IFNULL(c.nombre,'Cliente varios') AS cliente, "
                   + "IFNULL(c.dni,'-') AS dni, v.total, v.descuento, v.total_pagar "
                   + "FROM ventas v LEFT JOIN clientes c ON c.id = v.cliente_id "
                   + "WHERE v.id = ?";

        try (PreparedStatement ps = Conexion.getInstancia().getConexion().prepareStatement(sql)) {

            ps.setInt(1, idVenta);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;                             // (luiggi) la venta no existe o fue anulada
                }
                return new String[]{
                    String.valueOf(rs.getInt("id")),
                    String.valueOf(rs.getTimestamp("fecha")),
                    rs.getString("cliente"),
                    rs.getString("dni"),
                    String.format("%.2f", rs.getDouble("total")),
                    String.format("%.2f", rs.getDouble("descuento")),
                    String.format("%.2f", rs.getDouble("total_pagar"))
                };
            }

        } catch (SQLException e) {
            System.err.println("Error al leer la venta: " + e.getMessage());
            return null;
        }
    }

    /**
     * Detalle de una venta, uniendo las lineas por producto y las lineas por lote (RF-16).
     */
    public javax.swing.table.DefaultTableModel detalleComprobante(int idVenta) {

        String[] columnas = {"Descripcion", "Cantidad", "P. Unitario", "Subtotal"};

        javax.swing.table.DefaultTableModel modelo =
                new javax.swing.table.DefaultTableModel(null, columnas) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        // UNION ALL: el comprobante muestra juntas las dos formas de venta
        String sql = "SELECT p.nombre AS descripcion, d.cantidad, d.precio_unitario, d.subtotal "
                   + "FROM detalle_venta d INNER JOIN productos p ON p.id = d.producto_id "
                   + "WHERE d.venta_id = ? "
                   + "UNION ALL "
                   + "SELECT CONCAT(pr.sabor,' (Lote ',dl.lote,')'), dl.cantidad, "
                   + "dl.precio_unitario, dl.subtotal "
                   + "FROM detalle_venta_lote dl INNER JOIN produccion pr ON pr.id = dl.produccion_id "
                   + "WHERE dl.venta_id = ?";

        try (PreparedStatement ps = Conexion.getInstancia().getConexion().prepareStatement(sql)) {

            ps.setInt(1, idVenta);
            ps.setInt(2, idVenta);                           // (luiggi) el mismo id para las dos mitades

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    modelo.addRow(new Object[]{
                        rs.getString("descripcion"),
                        rs.getInt("cantidad"),
                        String.format("%.2f", rs.getDouble("precio_unitario")),
                        String.format("%.2f", rs.getDouble("subtotal"))
                    });
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al leer el detalle: " + e.getMessage());
        }
        return modelo;
    }

    // ------------------------------------------------------------------
    // Metodos privados de apoyo
    // ------------------------------------------------------------------

    /** Inserta la cabecera en 'ventas' y devuelve el id autogenerado. */
    private int insertarCabecera(Connection con, Venta v) throws SQLException {

        try (PreparedStatement ps = con.prepareStatement(SQL_VENTA, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, v.getClienteId());
            ps.setDouble(2, v.getTotal());
            ps.setDouble(3, v.getDescuento());
            ps.setDouble(4, v.getTotalPagar());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("MySQL no devolvio el id de la venta");
                }
                return rs.getInt(1);                         // (luiggi) id que usaran las lineas de detalle
            }
        }
    }

    /** Guarda una linea de producto: detalle, descuento de stock y movimiento de inventario. */
    private void guardarLineaProducto(Connection con, int idVenta, ItemVenta item) throws SQLException {

        try (PreparedStatement ps = con.prepareStatement(SQL_DETALLE)) {
            ps.setInt(1, idVenta);
            ps.setInt(2, item.getIdItem());
            ps.setInt(3, item.getCantidad());
            ps.setDouble(4, item.getPrecioUnitario());
            ps.setDouble(5, item.getSubtotal());
            ps.executeUpdate();
        }

        try (PreparedStatement ps = con.prepareStatement(SQL_DESCONTAR_PRODUCTO)) {
            ps.setInt(1, item.getCantidad());
            ps.setInt(2, item.getIdItem());
            ps.setInt(3, item.getCantidad());

            if (ps.executeUpdate() == 0) {                   // (luiggi) 0 filas = no habia stock suficiente
                throw new SQLException("Stock insuficiente de " + item.getDescripcion());
            }
        }

        try (PreparedStatement ps = con.prepareStatement(SQL_MOVIMIENTO)) {
            ps.setString(1, "SALIDA");
            ps.setInt(2, item.getIdItem());
            ps.setInt(3, item.getCantidad());
            ps.setString(4, "Venta N " + idVenta);           // (luiggi) deja rastro para auditoria (RF-27)
            ps.executeUpdate();
        }
    }

    /** Guarda una linea vendida por lote de produccion. */
    private void guardarLineaLote(Connection con, int idVenta, ItemVenta item) throws SQLException {

        try (PreparedStatement ps = con.prepareStatement(SQL_DETALLE_LOTE)) {
            ps.setInt(1, idVenta);
            ps.setInt(2, item.getIdItem());
            ps.setString(3, item.getLote());
            ps.setInt(4, item.getCantidad());
            ps.setDouble(5, item.getPrecioUnitario());
            ps.setDouble(6, item.getSubtotal());
            ps.executeUpdate();
        }

        try (PreparedStatement ps = con.prepareStatement(SQL_DESCONTAR_LOTE)) {
            ps.setInt(1, item.getCantidad());
            ps.setInt(2, item.getIdItem());
            ps.setInt(3, item.getCantidad());

            if (ps.executeUpdate() == 0) {                   // (luiggi) el lote no tiene unidades suficientes
                throw new SQLException("Unidades insuficientes en el lote " + item.getLote());
            }
        }
    }

    /** Devuelve al catalogo el stock de los productos de una venta anulada. */
    private void devolverStockProductos(Connection con, int idVenta) throws SQLException {

        String sql = "UPDATE productos p "
                   + "JOIN detalle_venta d ON d.producto_id = p.id "
                   + "SET p.stock = p.stock + d.cantidad "
                   + "WHERE d.venta_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            ps.executeUpdate();                              // (luiggi) reingresa las unidades al inventario
        }

        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO movimientos_inventario(tipo,producto_id,cantidad,observacion) "
              + "SELECT 'ENTRADA', producto_id, cantidad, ? FROM detalle_venta WHERE venta_id=?")) {
            ps.setString(1, "Anulacion de venta N " + idVenta); // (luiggi) registra la anulacion en el historial
            ps.setInt(2, idVenta);
            ps.executeUpdate();
        }
    }

    /** Devuelve a produccion las unidades de los lotes de una venta anulada. */
    private void devolverStockLotes(Connection con, int idVenta) throws SQLException {

        String sql = "UPDATE produccion pr "
                   + "JOIN detalle_venta_lote dl ON dl.produccion_id = pr.id "
                   + "SET pr.cantidad = pr.cantidad + dl.cantidad "
                   + "WHERE dl.venta_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            ps.executeUpdate();
        }
    }

    /** Ejecuta una sentencia simple que recibe un unico id como parametro. */
    private void ejecutar(Connection con, String sql, int id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Deshace la transaccion cuando ocurre un error. */
    private void revertir(Connection con) {
        try {
            con.rollback();                                  // (luiggi) la BD queda como antes de empezar
        } catch (SQLException ex) {
            System.err.println("Error en el rollback: " + ex.getMessage());
        }
    }

    /** Devuelve la conexion al modo autocommit para las demas consultas. */
    private void restaurarAutoCommit(Connection con) {
        try {
            con.setAutoCommit(true);
        } catch (SQLException ex) {
            System.err.println("Error al restaurar autocommit: " + ex.getMessage());
        }
    }
}
