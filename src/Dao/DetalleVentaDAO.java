package Dao;

import Modelo.DetalleVenta;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DetalleVentaDAO {

    private static final Logger LOGGER = Logger.getLogger(DetalleVentaDAO.class.getName());

    public boolean guardar(DetalleVenta d) {
        String sql = "INSERT INTO detalle_venta (venta_id,producto_id,cantidad,precio_unitario,subtotal) VALUES(?,?,?,?,?)";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, d.getVentaId());
            ps.setInt(2, d.getProductoId());
            ps.setInt(3, d.getCantidad());
            ps.setDouble(4, d.getPrecioUnitario());
            ps.setDouble(5, d.getSubtotal());
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al guardar detalle de venta", e);
            return false;
        }
    }
}
