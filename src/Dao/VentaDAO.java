package Dao;

import Modelo.Venta;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VentaDAO {

    private static final Logger LOGGER = Logger.getLogger(VentaDAO.class.getName());

    public int guardarVenta(Venta v) {
        int idVenta = 0;
        String sql = "INSERT INTO ventas(fecha,cliente_id,total,descuento,total_pagar) VALUES(NOW(),?,?,?,?)";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, v.getClienteId());
            ps.setDouble(2, v.getTotal());
            ps.setDouble(3, v.getDescuento());
            ps.setDouble(4, v.getTotalPagar());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    idVenta = rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al guardar venta", e);
        }

        return idVenta;
    }

    public void guardarDetalle(int ventaId, int productoId, int cantidad, double precio, double subtotal) {
        String sql = "INSERT INTO detalle_venta (venta_id,producto_id,cantidad,precio_unitario,subtotal) VALUES(?,?,?,?,?)";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, ventaId);
            ps.setInt(2, productoId);
            ps.setInt(3, cantidad);
            ps.setDouble(4, precio);
            ps.setDouble(5, subtotal);
            ps.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al guardar detalle de venta", e);
        }
    }

    public void descontarStock(int productoId, int cantidad) {
        String sql = "UPDATE productos SET stock = stock - ? WHERE id=?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, cantidad);
            ps.setInt(2, productoId);
            ps.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al descontar stock", e);
        }
    }

    public void registrarMovimiento(int productoId, int cantidad) {
        String sql = "INSERT INTO movimientos_inventario(tipo,producto_id,cantidad,observacion) VALUES('SALIDA',?,?,?)";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, productoId);
            ps.setInt(2, cantidad);
            ps.setString(3, "Venta realizada");
            ps.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al registrar movimiento de inventario", e);
        }
    }
}
